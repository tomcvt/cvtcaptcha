package com.tomcvt.cvtcaptcha.network;

public class RequestCounter {
    private int count;
    private long windowStartMillis;
    private long lastRequestMillis;

    public RequestCounter(long windowStartMillis) {
        this.count = 1;
        this.windowStartMillis = windowStartMillis;
        this.lastRequestMillis = windowStartMillis;
    }

    public int getCount() {
        return count;
    }
    public void increment() {
        this.count++;
        this.lastRequestMillis = System.currentTimeMillis();
    }
    public long getWindowStartMillis() {
        return windowStartMillis;
    }
    public long getLastRequestMillis() {
        return lastRequestMillis;
    }

    public void reset(long newWindowStartMillis) {
        this.count = 1;
        this.windowStartMillis = newWindowStartMillis;
        this.lastRequestMillis = newWindowStartMillis;
    }
}
