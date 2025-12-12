package com.tomcvt.cvtcaptcha.network;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.config.FirewallProperties;

import jakarta.annotation.PostConstruct;

@Service
public class BanRegistry {
    private final Map<String, Long> bannedIPs = new ConcurrentHashMap<>();
    private final long banDurationMillis;
    private final FirewallProperties firewallProperties;
    private final Set<String> illegalRequestStrings = new HashSet<>();
    @PostConstruct
    public void init() {
        illegalRequestStrings.addAll(firewallProperties.getIllegalRequestStrings());
    }

    public BanRegistry(@Value("${com.tomcvt.request.anon-rate-limit.ban-duration-ms}") long banDurationMillis,
                       FirewallProperties firewallProperties) {
        this.banDurationMillis = banDurationMillis;
        this.firewallProperties = firewallProperties;
    }

    public boolean isRequestIllegal(String requestContent) {
        for (String illegalStr : illegalRequestStrings) {
            if (requestContent.contains(illegalStr)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIPBanned(String ipAddress) {
        Long banExpiryTime = bannedIPs.get(ipAddress);
        if (banExpiryTime == null) {
            return false;
        }
        if (System.currentTimeMillis() > banExpiryTime) {
            bannedIPs.remove(ipAddress);
            return false;
        }
        return true;
    }

    public void banIP(String ipAddress) {
        long banExpiryTime = System.currentTimeMillis() + banDurationMillis;
        bannedIPs.put(ipAddress, banExpiryTime);
    }

    public void banIP(String ipAddress, long banDurationMinutes) {
        long banExpiryTime = System.currentTimeMillis() + banDurationMinutes * 60 * 1000;
        bannedIPs.put(ipAddress, banExpiryTime);
    }
}
