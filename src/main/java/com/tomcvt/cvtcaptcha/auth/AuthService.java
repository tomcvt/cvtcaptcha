package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.exceptions.IllegalUsageException;
import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.network.BanRegistry;
import com.tomcvt.cvtcaptcha.repository.UserRepository;

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


    public AuthService(JwtService jwtService, SecureUserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder, 
                       LoginTracker loginTracker, BanRegistry banRegistry) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.loginTracker = loginTracker;
        this.banRegistry = banRegistry;
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
        }
        final var userDetails = userDetailsService.loadUserByUsername(username);
        final var jwtToken = jwtService.generateToken(userDetails);
        return new JwtResponse(jwtToken);
    }

    public User registerActivatedUser(String username, String rawPassword, String email, String role) {
        var existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        var newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

}
