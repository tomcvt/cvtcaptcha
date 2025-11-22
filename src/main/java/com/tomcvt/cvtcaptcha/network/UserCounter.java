package com.tomcvt.cvtcaptcha.network;

public class UserCounter {
    private int hourCount;
    private int dayCount;
    private long hourWindowStartMillis;
    private long dayWindowStartMillis;

    public UserCounter(long currentTimeMillis) {
        this.hourCount = 1;
        this.dayCount = 1;
        this.hourWindowStartMillis = currentTimeMillis;
        this.dayWindowStartMillis = currentTimeMillis;
    }

    public int getHourCount() {
        return hourCount;
    }
    public void incrementHourCount() {
        this.hourCount++;
    }
    public long getHourWindowStartMillis() {
        return hourWindowStartMillis;
    }
    public void resetHourCount(long newHourWindowStartMillis) {
        this.hourCount = 1;
        this.hourWindowStartMillis = newHourWindowStartMillis;
    }
    public int getDayCount() {
        return dayCount;
    }
    public void incrementDayCount() {
        this.dayCount++;
    }
    public long getDayWindowStartMillis() {
        return dayWindowStartMillis;
    }
    public void resetDayCount(long newDayWindowStartMillis) {
        this.dayCount = 1;
        this.dayWindowStartMillis = newDayWindowStartMillis;
    }
    
}
