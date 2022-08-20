package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class NoAlbumsException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.user_has_no_albums_error
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_image_no_album_and_galleries
    }

    override fun showRefreshButton(): Boolean {
        return false
    }

}