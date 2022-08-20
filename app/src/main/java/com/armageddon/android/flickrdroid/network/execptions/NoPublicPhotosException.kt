package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoPublicPhotosException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.user_has_no_photos_error
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_no_photography_24
    }

    override fun showRefreshButton(): Boolean {
        return false
    }

}