package com.tomcvt.cvtcaptcha.controller.api;

import org.springframework.web.bind.annotation.RestController;

import com.tomcvt.cvtcaptcha.logging.LoggingFilterRegistry;
import com.tomcvt.cvtcaptcha.logging.StringBlockFilter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/api/logging")
@PreAuthorize("hasAnyRole('ADMIN','SUPERUSER')")
public class LoggingApiController {
    private final LoggingFilterRegistry loggingFilterRegistry;

    public LoggingApiController(LoggingFilterRegistry loggingFilterRegistry) {
        this.loggingFilterRegistry = loggingFilterRegistry;
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String,List<String>>> getLoggingFilters() {
        Map<String,StringBlockFilter> map = loggingFilterRegistry.getFilters();
        var result = map.entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getBlockedSubstringsList()
                )
            );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/filters/add")
    public ResponseEntity<?> addBlockedSubstringToFilter(
            @RequestParam String filterKey,
            @RequestParam String substring) {
        loggingFilterRegistry.addBlockedSubstring(filterKey, substring);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/filters/add-all")
    public ResponseEntity<?> addBlockedSubstringToAllFilters(
            @RequestParam String substring) {
        loggingFilterRegistry.addBlockedSubstringToAllFilters(substring);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/filters/remove-all")
    public ResponseEntity<?> removeBlockedSubstringFromAllFilters(
            @RequestBody String substring) {
        loggingFilterRegistry.removeBlockedSubstringFromAllFilters(substring);
        return ResponseEntity.ok().build();
    }
    
    
}
