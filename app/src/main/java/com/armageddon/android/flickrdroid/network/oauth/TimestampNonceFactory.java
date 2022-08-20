package com.armageddon.android.flickrdroid.network.oauth;

import java.util.Objects;

public interface TimestampNonceFactory {

    TimestampNonce getTimestampNonce();

    public static class TimestampNonce {

        private final String timestamp;
        private final String nonce;

        public TimestampNonce(String timestamp, String nonce) {

            Objects.requireNonNull(timestamp, "Timestamp cannot be null");
            Objects.requireNonNull(nonce, "Nonce cannot be null");

            this.timestamp = timestamp;
            this.nonce = nonce;
        }

        /**
         * Retrieves a GMT timestamp (in seconds)
         */
        public String getTimestampInSeconds() {
            return timestamp;
        }

        /**
         * Returns a nonce (unique value for each request)
         */
        public String getNonce() {
            return nonce;
        }
    }
}