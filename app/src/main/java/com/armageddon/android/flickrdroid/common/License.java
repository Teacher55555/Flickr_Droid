package com.armageddon.android.flickrdroid.common;

/**
 * License enum from Flickr.com
 */

public enum License {
    lic0 ("All Rights Reserved"),
    lic1 ("Attribution-NonCommercial-ShareAlike License"),
    lic2 ("Attribution-NonCommercial License"),
    lic3 ("Attribution-NonCommercial-NoDerivs License"),
    lic4 ("Attribution License"),
    lic5 ("Attribution-ShareAlike License"),
    lic6 ("Attribution-NoDerivs License"),
    lic7 ("No known copyright restrictions"),
    lic8 ("United States Government Work"),
    lic9 ("Public Domain Dedication (CC0)"),
    lic10 ("Public Domain Mark");

    private final String license;

    public String getLicense() {
        return license;
    }

    License (String license) {
        this.license = license;
    }
}
