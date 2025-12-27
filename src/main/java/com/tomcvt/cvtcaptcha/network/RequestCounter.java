package com.tomcvt.cvtcaptcha.network;

public class RequestCounter {
    private int count;
    private int banCount;
    private long windowStartMillis;
    private long banWindowStartMillis;

    public RequestCounter() {
        this.count = 1;
        this.windowStartMillis = System.currentTimeMillis();
        this.banWindowStartMillis = System.currentTimeMillis();
    }

    public int getCount() {
        return count;
    }
    public int getBanCount() {
        return banCount;
    }
    public void increment() {
        this.count++;
        this.banCount++;
    }
    public long getWindowStartMillis() {
        return windowStartMillis;
    }
    public long getBanWindowStartMillis() {
        return banWindowStartMillis;
    }

    public void reset() {
        this.count = 1;
        this.windowStartMillis = System.currentTimeMillis();
        this.banCount = 1;
        this.banWindowStartMillis = System.currentTimeMillis();
    }
    public void resetBanCounter() {
        this.banCount = 1;
        this.banWindowStartMillis = System.currentTimeMillis();
    }
    public void resetCounter() {
        this.count = 1;
        this.windowStartMillis = System.currentTimeMillis();
    }
}
