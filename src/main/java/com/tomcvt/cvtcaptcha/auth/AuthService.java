package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JwtService jwtService;
    private final SecureUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthService(JwtService jwtService, SecureUserDetailsService userDetailsService,
                       AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    public JwtResponse authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        final var userDetails = userDetailsService.loadUserByUsername(username);
        final var jwtToken = jwtService.generateToken(userDetails);
        return new JwtResponse(jwtToken);
    }
}
