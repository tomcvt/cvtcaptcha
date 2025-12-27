package com.tomcvt.cvtcaptcha.network;

public class SimpleRequestCounter {
    private int count;
    private long windowStartMillis;

    public SimpleRequestCounter() {
        this.count = 1;
        this.windowStartMillis = System.currentTimeMillis();
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

    public void resetCounter() {
        this.count = 1;
        this.windowStartMillis = System.currentTimeMillis();
    }
}
