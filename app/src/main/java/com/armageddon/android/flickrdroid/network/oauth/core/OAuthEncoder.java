package com.armageddon.android.flickrdroid.network.oauth.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

class OAuthEncoder {

    private static final String CHARSET = "UTF-8";
    private static final Map<String, String> ENCODING_RULES;

    static {
        final Map<String, String> rules = new HashMap<>();
        rules.put("*", "%2A");
        rules.put("+", "%20");
        rules.put("%7E", "~");
        ENCODING_RULES = Collections.unmodifiableMap(rules);
    }

    public static String encode(String plain) {
        Objects.requireNonNull(plain, "Cannot encode null object");
        String encoded;
        try {
            encoded = URLEncoder.encode(plain, CHARSET);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Charset not found while encoding string: " + CHARSET, uee);
        }
        for (Map.Entry<String, String> rule : ENCODING_RULES.entrySet()) {
            encoded = applyRule(encoded, rule.getKey(), rule.getValue());
        }
        return encoded;
    }

    private static String applyRule(String encoded, String toReplace, String replacement) {
        return encoded.replaceAll(Pattern.quote(toReplace), replacement);
    }

    private OAuthEncoder() {

    }
}
