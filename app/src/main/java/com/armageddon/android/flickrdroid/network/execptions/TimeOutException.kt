package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class TimeOutException  : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.time_out_error
    }

    override fun getDrawable(): Int {
        return R.drawable.ic_baseline_no_connection
    }

    override fun showRefreshButton(): Boolean {
        return true
    }

}