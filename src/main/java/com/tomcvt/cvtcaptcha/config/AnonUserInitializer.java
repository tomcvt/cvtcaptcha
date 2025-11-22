package com.tomcvt.cvtcaptcha.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import com.tomcvt.cvtcaptcha.model.User;
import com.tomcvt.cvtcaptcha.repository.UserRepository;

@Component
public class AnonUserInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UserRepository userRepository;
    private User anonUser;
    public AnonUserInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAnonUser() {
        return anonUser;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
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
