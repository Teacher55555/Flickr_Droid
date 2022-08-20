package com.armageddon.android.flickrdroid.common

import com.armageddon.android.flickrdroid.R

enum class GroupPrivacy (
    val title: Int,
    val privacy: Int,
    val textTitle: Int
) {
    PRIVATE(R.string.group_list_private, 1, R.string.group_text_private),
    PRIVACY_INVITE(R.string.group_list_invite,2, R.string.group_text_invite),
    PRIVACY_PUBLIC(R.string.group_list_public,3, R.string.group_text_public),
    PRIVACY_NOT_INITIALIZED(0,5,0);

    companion object {
       fun getTextPrivacy (privacy: Int) : Int {
           return values().first {
               it.privacy == privacy
           }.textTitle
       }
    }
}