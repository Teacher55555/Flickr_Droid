package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoCommentsException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.no_comments
    }

    override fun getDrawable(): Int {
        return R.drawable.icon_no_comments
    }

    override fun showRefreshButton(): Boolean {
        return false
    }
}