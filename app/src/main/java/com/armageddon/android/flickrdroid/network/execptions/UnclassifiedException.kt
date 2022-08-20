package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class UnclassifiedException (val text: Int) : FlickrException() {
    override fun getTextMessage(): Int {
        return text
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_no_connection
    }

    override fun showRefreshButton(): Boolean {
        return true
    }

}