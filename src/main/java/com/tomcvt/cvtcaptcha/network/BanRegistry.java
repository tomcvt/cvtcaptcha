package com.tomcvt.cvtcaptcha.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BanRegistry {
    private final Map<String, Long> bannedIPs = new ConcurrentHashMap<>();
    private final long banDurationMillis;

    public BanRegistry(@Value("{com.tomcvt.request.anon-rate-limit.ban-duration-ms}") long banDurationMillis) {
        this.banDurationMillis = banDurationMillis;
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
}
