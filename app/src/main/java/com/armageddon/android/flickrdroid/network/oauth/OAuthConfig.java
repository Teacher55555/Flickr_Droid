package com.armageddon.android.flickrdroid.network.oauth;

import java.util.Optional;

public interface OAuthConfig {

    TimestampNonceFactory getTimestampNonceFactory();

    SignatureMethod getSignatureMethod();


    String getConsumerKey();

    String getConsumerSecret();


    Optional<String> getTokenKey();

    Optional<String> getTokenSecret();


    Optional<String> getCallback();

    Optional<String> getVerifier();

    Optional<String> getScope();

    Optional<String> getRealm();


    default OAuthSignatureBuilder buildSignature(HttpMethod method, String url) {
        return new OAuthSignatureBuilder(this, method, url);
    }
}