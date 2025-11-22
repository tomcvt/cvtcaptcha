package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.UserRepository;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final SecureUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthService(JwtService jwtService, SecureUserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtResponse authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final var userDetails = userDetailsService.loadUserByUsername(username);
        final var jwtToken = jwtService.generateToken(userDetails);
        return new JwtResponse(jwtToken);
    }

    public User registerActivatedUser(String username, String rawPassword, String email, String role) {
        var newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setRole(role);
        return userRepository.save(newUser);
    }

}
