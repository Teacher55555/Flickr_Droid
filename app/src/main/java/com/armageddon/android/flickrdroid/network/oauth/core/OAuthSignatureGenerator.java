package com.armageddon.android.flickrdroid.network.oauth.core;

import com.armageddon.android.flickrdroid.network.oauth.HttpMethod;
import com.armageddon.android.flickrdroid.network.oauth.HttpParameter;
import com.armageddon.android.flickrdroid.network.oauth.OAuthConfig;
import com.armageddon.android.flickrdroid.network.oauth.OAuthField;
import com.armageddon.android.flickrdroid.network.oauth.OAuthSignature;
import com.armageddon.android.flickrdroid.network.oauth.SignatureMethod;
import com.armageddon.android.flickrdroid.network.oauth.TimestampNonceFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OAuthSignatureGenerator {

    private static final String OAUTH_VERSION = "1.0";

    private final OAuthConfig oAuthConfig;
    private final Map<OAuthField, String> constantOAuthParams;

    private final OAuthBaseStringGenerator baseStringGenerator = new OAuthBaseStringGenerator();

    public OAuthSignatureGenerator(OAuthConfig oAuthConfig) {

        Objects.requireNonNull(oAuthConfig, "OAuthConfig cannot be null");

        this.oAuthConfig = oAuthConfig;
        this.constantOAuthParams = getConstantOauthParameters(oAuthConfig);
    }

    public OAuthSignature getSignature(HttpMethod httpMethod, String requestUrl, Optional<Set<HttpParameter>> queryParams, Optional<Set<HttpParameter>> bodyParams) {

        Set<HttpParameter> actualQueryParams = queryParams.orElse(Collections.emptySet());
        Set<HttpParameter> actualBodyParams = bodyParams.orElse(Collections.emptySet());

        Map<OAuthField, String> oauthParameters = getCompleteOauthParameters(httpMethod, requestUrl, actualQueryParams, actualBodyParams);

        return new MapOAuthSignature(oauthParameters);
    }

    public Map<OAuthField, String> getConstantOauthParameters(OAuthConfig oAuthConfig) {

        SignatureMethod signatureMethod = oAuthConfig.getSignatureMethod();
        String signatureMethodType = signatureMethod.getSignatureMethodType().name().replace("_", "-");

        Map<OAuthField, String> oauthParameters = new HashMap<>();

        oauthParameters.put(OAuthField.VERSION, OAuthEncoder.encode(OAUTH_VERSION));
        oauthParameters.put(OAuthField.SIGNATURE_METHOD, OAuthEncoder.encode(signatureMethodType));
        oauthParameters.put(OAuthField.CONSUMER_KEY, OAuthEncoder.encode(oAuthConfig.getConsumerKey()));

        oAuthConfig.getCallback().ifPresent(value -> oauthParameters.put(OAuthField.CALLBACK, OAuthEncoder.encode(value)));
        oAuthConfig.getTokenKey().ifPresent(value -> oauthParameters.put(OAuthField.TOKEN, OAuthEncoder.encode(value)));
        oAuthConfig.getVerifier().ifPresent(value -> oauthParameters.put(OAuthField.VERIFIER, OAuthEncoder.encode(value)));

        return oauthParameters;
    }

    public Map<OAuthField, String> getCompleteOauthParameters(HttpMethod httpMethod, String requestUrl, Set<HttpParameter> queryParams, Set<HttpParameter> bodyParams) {

        Map<OAuthField, String> oauthParameters = new HashMap<>(constantOAuthParams);

        TimestampNonceFactory.TimestampNonce timestampNonce = oAuthConfig.getTimestampNonceFactory().getTimestampNonce();
        Objects.requireNonNull(timestampNonce, "TimestampNonce cannot be null");

        String timestamp = timestampNonce.getTimestampInSeconds();
        Objects.requireNonNull(timestamp, "Timestamp cannot be null");

        String nonce = timestampNonce.getNonce();
        Objects.requireNonNull(nonce, "Nonce cannot be null");

        oauthParameters.put(OAuthField.TIMESTAMP, OAuthEncoder.encode(timestamp));
        oauthParameters.put(OAuthField.NONCE, OAuthEncoder.encode(nonce));

        SignatureMethod signatureMethod = oAuthConfig.getSignatureMethod();
        String consumerSecret = oAuthConfig.getConsumerSecret();
        String tokenSecret = oAuthConfig.getTokenSecret().orElse("");

        List<HttpParameter> sortedParams = baseStringGenerator.getSortedParams(oauthParameters, queryParams, bodyParams);
        String urlEncodedParams = baseStringGenerator.getUrlEncodedParams(sortedParams);
        String sanitizedUrl = baseStringGenerator.getSanitizedUrl(requestUrl);

        String baseString = baseStringGenerator.getBaseString(httpMethod, sanitizedUrl, urlEncodedParams);
        String signature = signatureMethod.getSignature(baseString, OAuthEncoder.encode(consumerSecret), OAuthEncoder.encode(tokenSecret));

        oauthParameters.put(OAuthField.SIGNATURE, signature);
        oAuthConfig.getScope().ifPresent(value -> oauthParameters.put(OAuthField.SCOPE, value));
        oAuthConfig.getRealm().ifPresent(value -> oauthParameters.put(OAuthField.REALM, value));

        return oauthParameters;
    }
}
