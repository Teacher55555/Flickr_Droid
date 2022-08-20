package com.armageddon.android.flickrdroid.common

enum class PhotoPrivacy(val state: Int) {
    PUBLIC(0),
    PRIVATE(1),
    FRIENDS(2),
    FAMILY(3),
    FRIENDS_AND_FAMILY(4)
}