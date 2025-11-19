package com.tomcvt.cvtcaptcha.network;

public class RequestCounter {
    private int count;
    private long windowStartMillis;

    public RequestCounter(long windowStartMillis) {
        this.count = 1;
        this.windowStartMillis = windowStartMillis;
    }

    public int getCount() {
        return count;
    }
    public void increment() {
        this.count++;
    }
    public long getWindowStartMillis() {
        return windowStartMillis;
    }

    public void reset(long newWindowStartMillis) {
        this.count = 1;
        this.windowStartMillis = newWindowStartMillis;
    }
}
