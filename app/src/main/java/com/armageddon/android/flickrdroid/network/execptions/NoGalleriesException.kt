package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoGalleriesException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.user_has_no_galleries_error
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_image_no_album_and_galleries
    }

    override fun showRefreshButton(): Boolean {
        return false
    }

}