package com.tomcvt.cvtcaptcha.logging;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterAttachable;
import jakarta.annotation.PostConstruct;

@Service
public class LoggingFilterRegistry {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoggingFilterRegistry.class);
    private final Map<String, StringBlockFilter> filters = new HashMap<>();

    public void registerFilters() {
        LoggerContext context = (LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
        Logger rootLogger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        filters.clear();
        var iterator = rootLogger.iteratorForAppenders();
        while (iterator.hasNext()) {
            Appender<?> appender = iterator.next();
            if (appender instanceof FilterAttachable<?>) {
                for (var filter : ((FilterAttachable<?>) appender).getCopyOfAttachedFiltersList()) {
                    if (filter instanceof StringBlockFilter sbf) {
                        filters.put(appender.getName() + ":" + sbf.hashCode(), sbf);
                        log.info("Registered StringBlockFilter for appender: {}", appender.getName());
                    }
                }
            }
        }
    }

    public void addBlockedSubstringToAllFilters(String str) {
        for (var filter : filters.values()) {
            filter.addBlockedSubstring(str);
        }
    }

    public void addBlockedSubstring(String filterKey, String str) {
        var filter = filters.get(filterKey);
        if (filter == null) {
            throw new IllegalArgumentException("No filter found for key: " + filterKey);
        }
        if (filter != null) {
            filter.addBlockedSubstring(str);
        }
    }

    public void removeBlockedSubstringFromAllFilters(String str) {
        for (var filter : filters.values()) {
            filter.removeBlockedSubstring(str);
        }
    }

    public void removeBlockedSubstring(String filterKey, String str) {
        var filter = filters.get(filterKey);
        if (filter == null) {
            throw new IllegalArgumentException("No filter found for key: " + filterKey);
        }
        if (filter != null) {
            filter.removeBlockedSubstring(str);
        }
    }

    public Map<String, StringBlockFilter> getFilters() {
        return filters;
    }
}
