package com.tomcvt.cvtcaptcha.auth;

public class LoginCounter {
    int count;
    long windowStartMillis;

    public LoginCounter() {
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
