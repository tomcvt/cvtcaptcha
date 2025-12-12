package com.tomcvt.cvtcaptcha.logging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class StringBlockFilter extends Filter<ILoggingEvent> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StringBlockFilter.class);
    private final CopyOnWriteArrayList<String> blockedSubstringsList = new CopyOnWriteArrayList<>();

    public StringBlockFilter() {
        log.info("ClassLoader for stringblockfilter new: " + this.getClass().getClassLoader());
        System.out.println("ClassLoader for stringblockfilter new: " + this.getClass().getClassLoader());
    }


    public void setBlockedSubstringsList(String blockedSubstrings) {
        blockedSubstringsList.clear();
        String[] substrings = blockedSubstrings.split(",");
        for (String substring : substrings) {
            blockedSubstringsList.add(substring.trim());
        }
    }

    public void addBlockedSubstring(String substring) {
        blockedSubstringsList.add(substring);
    }
    public List<String> getBlockedSubstringsList() {
        return blockedSubstringsList;
    }

    public void removeBlockedSubstring(String substring) {
        blockedSubstringsList.remove(substring);
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        for (String blockedSubstring : blockedSubstringsList) {
            if (message.contains(blockedSubstring)) {
                return FilterReply.DENY;
            }
        }
        return FilterReply.NEUTRAL;
    }

    public String toString() {
        return "StringBlockFilter:" + super.toString() + "{blockedSubstrings=" + blockedSubstringsList + "}";
    }
    
}
