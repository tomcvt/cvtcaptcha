package com.tomcvt.cvtcaptcha.config;

import java.util.HashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "firewall")
public class FirewallProperties {
    private Set<String> illegalRequestStrings = new HashSet<>();

    public Set<String> getIllegalRequestStrings() {
        return illegalRequestStrings;
    }

    public void setIllegalRequestStrings(Set<String> illegalRequestStrings) {
        this.illegalRequestStrings = illegalRequestStrings;
    }
}
