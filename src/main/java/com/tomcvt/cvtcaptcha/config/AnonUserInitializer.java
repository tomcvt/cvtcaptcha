package com.tomcvt.cvtcaptcha.config;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.UserRepository;

@Component
public class AnonUserInitializer {
    private final UserRepository userRepository;
    private User anonUser;
    public AnonUserInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAnonUser() {
        return anonUser;
    }

    @Transactional
    public void init() {
        var anonUserOpt = userRepository.findByUsername("anonymous");
        if (anonUserOpt.isPresent()) {
            anonUser = anonUserOpt.get();
        } else {
            var newAnonUser = new User();
            newAnonUser.setUsername("anonymous");
            newAnonUser.setPassword("");
            newAnonUser.setEmail("");
            newAnonUser.setRole("ANON");
            newAnonUser.setEnabled(true);
            anonUser = userRepository.save(newAnonUser);
        }
    }
}
