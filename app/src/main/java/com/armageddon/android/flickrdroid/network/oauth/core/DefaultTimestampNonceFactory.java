package com.armageddon.android.flickrdroid.network.oauth.core;


import com.armageddon.android.flickrdroid.network.oauth.TimestampNonceFactory;

import java.util.Random;

public class DefaultTimestampNonceFactory implements TimestampNonceFactory {

    private Timer timer;

    public DefaultTimestampNonceFactory() {
        timer = new Timer();
    }

    @Override
    public TimestampNonce getTimestampNonce() {

        long timeInSeconds = timer.getMilis() / 1000;

        String timestamp = String.valueOf(timeInSeconds);
        String nonce = String.valueOf(timeInSeconds + timer.getRandomInteger());

        return new TimestampNonce(timestamp, nonce);
    }

    /**
     * Inner class that uses {@link System} for generating the timestamps.
     */
    private static class Timer {

        private final Random rand = new Random();

        long getMilis() {
            return System.currentTimeMillis();
        }

        int getRandomInteger() {
            return rand.nextInt();
        }
    }
}