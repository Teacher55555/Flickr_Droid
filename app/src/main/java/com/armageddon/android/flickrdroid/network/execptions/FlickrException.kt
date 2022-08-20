package com.armageddon.android.flickrdroid.network.execptions

abstract class FlickrException : Throwable() {
    abstract fun getTextMessage() : Int
    abstract fun getDrawable() : Int
    abstract fun showRefreshButton() : Boolean
}