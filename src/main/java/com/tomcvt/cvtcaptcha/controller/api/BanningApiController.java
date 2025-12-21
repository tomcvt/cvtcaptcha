package com.tomcvt.cvtcaptcha.controller.api;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.network.BanRegistry;

@RestController
@RequestMapping("/api/banning")
public class BanningApiController {
    private final BanRegistry banRegistry;

    public BanningApiController(BanRegistry banRegistry) {
        this.banRegistry = banRegistry;
    }

    @GetMapping("/banned-ips")
    public ResponseEntity<Map<String,String>> getBannedIPs() {
        Map<String, Long> bannedIPsWithExpiry = banRegistry.getBannedIPs();
        Map<String, String> bannedIPsFormatted = bannedIPsWithExpiry.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> Instant.ofEpochMilli(e.getValue()).toString()
            ));
        return ResponseEntity.ok(bannedIPsFormatted);
    }
    //TODO add frontend page to manage banned IPs
    @PostMapping("/ban-ip")
    public ResponseEntity<String> banIP(@RequestParam String ipAddress, @RequestParam long durationMinutes) {
        banRegistry.banIP(ipAddress, durationMinutes);
        return ResponseEntity.ok("IP " + ipAddress + " has been banned for " + durationMinutes + " minutes.");
    }
}
