package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoBlockedUsersException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.empty_block_list_message
    }

    override fun getDrawable(): Int {
        return R.drawable.icon_smile_good
    }

    override fun showRefreshButton(): Boolean {
        return false
    }
}
