package com.armageddon.android.flickrdroid.network.execptions

import com.armageddon.android.flickrdroid.R

class ConnectionException : FlickrException() {
    override fun getTextMessage(): Int {
        return R.string.no_internet_connection_error
    }

    override fun getDrawable(): Int {
       return R.drawable.ic_baseline_no_connection
    }

    override fun showRefreshButton(): Boolean {
        return true
    }

}