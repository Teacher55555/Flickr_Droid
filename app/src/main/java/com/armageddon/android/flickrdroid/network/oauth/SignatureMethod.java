package com.armageddon.android.flickrdroid.network.oauth;

public interface SignatureMethod {

    /**
     * Returns the signature
     *
     * @param baseString url-encoded string to sign
     * @param apiSecret url-encoded api secret for your app
     * @param tokenSecret url-encoded token secret (could be empty string)
     *
     * @return signature value
     */
    String getSignature(String baseString, String apiSecret, String tokenSecret);

    SignatureMethodType getSignatureMethodType();
}
