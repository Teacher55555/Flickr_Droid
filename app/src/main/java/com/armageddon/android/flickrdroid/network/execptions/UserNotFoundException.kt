package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R


class UserNotFoundException: FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.search_user_error
    }

    override fun getDrawable(): Int {
        return R.drawable.icon_smile_bad
    }

    override fun showRefreshButton(): Boolean {
        return false
    }
}