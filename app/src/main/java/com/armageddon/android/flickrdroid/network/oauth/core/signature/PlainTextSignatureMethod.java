package com.armageddon.android.flickrdroid.network.oauth.core.signature;


import com.armageddon.android.flickrdroid.network.oauth.SignatureMethod;
import com.armageddon.android.flickrdroid.network.oauth.SignatureMethodType;

public class PlainTextSignatureMethod implements SignatureMethod {

    @Override
    public String getSignature(String baseString, String apiSecret, String tokenSecret) {

        try {
            return apiSecret + '&' + tokenSecret;

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public SignatureMethodType getSignatureMethodType() {
        return SignatureMethodType.PLAINTEXT;
    }
}