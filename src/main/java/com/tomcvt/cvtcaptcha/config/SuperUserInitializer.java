package com.tomcvt.cvtcaptcha.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.repository.UserRepository;

import io.jsonwebtoken.security.Password;

@Service
public class SuperUserInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String superUsername;
    private final String superPassword;

    public SuperUserInitializer(UserRepository userRepository, 
        PasswordEncoder passwordEncoder,
        @Value("${com.tomcvt.superuser.username}") String superUsername,
        @Value("${com.tomcvt.superuser.password}") String superPassword) {
        this.userRepository = userRepository;
        this.superUsername = superUsername;
        this.superPassword = superPassword;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        var superUserOpt = userRepository.findByUsername(superUsername);
        if (superUserOpt.isEmpty()) {
            var superUser = new com.tomcvt.cvtcaptcha.model.User();
            superUser.setUsername(superUsername);
            superUser.setPassword(passwordEncoder.encode(superPassword));
            superUser.setEmail("");
            superUser.setRole("SUPERUSER");
            superUser.setEnabled(true);
            userRepository.save(superUser);
        }
    }
}
