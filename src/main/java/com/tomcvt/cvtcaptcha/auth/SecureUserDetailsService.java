package com.tomcvt.cvtcaptcha.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.UserRepository;

@Service
public class SecureUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public SecureUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new SecureUserDetails(true, user, ""); // IP can be set later
    }
}
