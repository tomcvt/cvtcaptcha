package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.dtos.PassPayload;
import com.tomcvt.cvtcaptcha.exceptions.IllegalUsageException;
import com.tomcvt.cvtcaptcha.model.ActivationToken;
import com.tomcvt.cvtcaptcha.model.PassRecoveryToken;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.network.BanRegistry;
import com.tomcvt.cvtcaptcha.repository.ActivationTokenRepository;
import com.tomcvt.cvtcaptcha.repository.PassRecoveryTokenRepository;
import com.tomcvt.cvtcaptcha.repository.UserRepository;
import com.tomcvt.cvtcaptcha.service.EmailService;

@Service
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);
    private final JwtService jwtService;
    private final SecureUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginTracker loginTracker;
    private final BanRegistry banRegistry;
    private final EmailService emailService;
    private final PassRecoveryTokenRepository passRecoveryTokenRepository;
    private final ActivationTokenRepository activationTokenRepository;


    public AuthService(JwtService jwtService, SecureUserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       LoginTracker loginTracker, BanRegistry banRegistry, EmailService emailService, 
                       PassRecoveryTokenRepository passRecoveryTokenRepository, ActivationTokenRepository activationTokenRepository) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginTracker = loginTracker;
        this.banRegistry = banRegistry;
        this.emailService = emailService;
        this.passRecoveryTokenRepository = passRecoveryTokenRepository;
        this.activationTokenRepository = activationTokenRepository;
    }

    public JwtResponse authenticate(String username, String password, String clientIp) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (AuthenticationException e) {
            boolean allowed = loginTracker.recordLoginAttemptForIp(username, clientIp);
            log.warn("Authentication failed for user: {} with IP: {}", username, clientIp);
            if (!allowed) {
                banRegistry.banIP(clientIp, 15); // ban for 15 minutes
                throw new IllegalUsageException("Too many failed login attempts. Please try again after 15 minutes. or from another IP.");
            }
            throw new IllegalUsageException("Invalid username or password");
        }
        final var userDetails = userDetailsService.loadUserByUsername(username);
        final var jwtToken = jwtService.generateToken(userDetails);
        return new JwtResponse(jwtToken);
    }

    public User registerActivatedUser(String username, String rawPassword, String email, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        var newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

    @Transactional
    public User registerUserWithEmail(String username, String rawPassword, String email, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        var existingUser = userRepository.findByEmail(email).orElse(null);
        if (existingUser != null && !existingUser.isEnabled()) {
            resendActivationToken(existingUser);
            return existingUser;
        }
        validateUsername(username);
        validatePassword(rawPassword);

        var newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setEmail(email);
        newUser.setEnabled(false); // User needs to activate via email
        newUser.setRole(role);
        newUser = userRepository.save(newUser);
        var activationToken = new ActivationToken(newUser);
        activationToken = activationTokenRepository.save(activationToken);
        try {
            emailService.sendActivationEmail(newUser.getEmail(), activationToken.getToken());
        } catch (Exception e) {
            log.error("Failed to send activation email to " + email, e);
            userRepository.delete(newUser);
            activationTokenRepository.delete(activationToken);
            throw new RuntimeException("Failed to send activation email");
        }
        return newUser;
    }
    //TODO refactor to clean up code duplication
    private void resendActivationToken(User user) {
        activationTokenRepository.deleteByUser(user);
        ActivationToken activationToken = new ActivationToken(user);
        activationToken = activationTokenRepository.save(activationToken);
        try {
            emailService.sendActivationEmail(user.getEmail(), activationToken.getToken());
        } catch (Exception e) {
            log.error("Failed to resend activation email to " + user.getEmail(), e);
            activationTokenRepository.delete(activationToken);
            throw new RuntimeException("Failed to resend activation email");
        }
    }

    private void validateUsername(String username) {
        String pattern = "^[a-zA-Z0-9_]{3,20}$";
        if (!username.matches(pattern)) {
            throw new IllegalArgumentException("Username must be 3-20 characters long and can only contain letters, digits, and underscores.");
        }
    }

    private void validatePassword(String password) {
        String pattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
        if (!password.matches(pattern)) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and include " +
                    "at least one uppercase letter, one lowercase letter, one digit, and one special character (@$!%*?&).");
        }
    }
    @Transactional
    public void changePassword(User user, PassPayload passPayload) {
        String oldRawPassword = passPayload.oldPassword();
        String newRawPassword = passPayload.newPassword();
        if(!passwordEncoder.matches(oldRawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        changePassword(user, newRawPassword);
    }
    @Transactional
    public void changePassword(Long userId, PassPayload passPayload) {
        String oldRawPassword = passPayload.oldPassword();
        String newRawPassword = passPayload.newPassword();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(!passwordEncoder.matches(oldRawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }
        changePassword(user, newRawPassword);
    }

    @Transactional
    public void changePassword(User user, String newRawPassword) {
        if (user.getRole().equals("SUPERUSER")) {
            throw new IllegalArgumentException("Cannot change password for SUPERUSER via this method");
        }
        validatePassword(newRawPassword);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    @Transactional
    public void initiatePasswordRecovery(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User with email not found"));
        if (user.getRole().equals("SUPERUSER")) {
            return; //Only way db admin can reset superuser password
        }
        passRecoveryTokenRepository.deleteByUser(user);
        PassRecoveryToken token = new PassRecoveryToken(user);
        token = passRecoveryTokenRepository.save(token);
        try {
            emailService.sendRecoveryEmail(user.getEmail(), token.getToken());
        } catch (Exception e) {
            log.error("Failed to send recovery email to " + email, e);
            passRecoveryTokenRepository.delete(token);
            throw new RuntimeException("Failed to send recovery email");
        }
    }

    @Transactional
    public void resetPasswordWithToken(String tokenStr, String newPassword, String confirmPassword) {
        PassRecoveryToken token = passRecoveryTokenRepository.findByToken(tokenStr)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));
        if (token.isExpired()) {
            passRecoveryTokenRepository.delete(token);
            throw new IllegalArgumentException("Password reset token has expired");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        validatePassword(newPassword);
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passRecoveryTokenRepository.delete(token);
    }

    @Transactional
    public boolean activateAccount(String token) {
        ActivationToken activationToken = activationTokenRepository.findByToken(token)
            .orElse(null);
        if (activationToken == null) {
            return false;
        }
        if (activationToken.isExpired()) {
            activationTokenRepository.delete(activationToken);
            return false;
        }
        User user = activationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        activationTokenRepository.delete(activationToken);
        return true;
    }

    //TODO scheduled cleanup of expired tokens

}
