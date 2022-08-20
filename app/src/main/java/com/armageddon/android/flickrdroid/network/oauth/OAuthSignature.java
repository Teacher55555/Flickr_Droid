package com.armageddon.android.flickrdroid.network.oauth;

import java.util.Optional;

public interface OAuthSignature {

    String getAsHeader();

    String getAsQueryString();


    String getSignature();

    String getConsumerKey();

    String getTimestamp();

    String getNonce();

    String getSignatureMethod();

    String getVersion();


    Optional<String> getScope();

    Optional<String> getCallback();

    Optional<String> getToken();

    Optional<String> getVerifier();

    Optional<String> getRealm();
}
