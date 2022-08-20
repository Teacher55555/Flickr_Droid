package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoGroupException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.user_has_no_groups_error
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_group_off_24
    }

    override fun showRefreshButton(): Boolean {
        return false
    }
}