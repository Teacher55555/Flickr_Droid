package com.armageddon.android.flickrdroid.common

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.armageddon.android.flickrdroid.ui.fragments.detail.SLIDESHOW_INTERVAL_3_SEC


private const val IS_INTRO_SHOWN = "is_shown"
private const val SLIDE_SHOW_INTERVAL = "show_interval_new"
private const val GALLERY_VIEW_COLUMNS = "gal_view_col"
private const val PICTURE_SHOW_QUALITY = "show_quality"
private const val PICTURE_DOWNLOAD_QUALITY = "download_quality_new"
private const val APP_THEME = "app_theme"
private const val OAUTH_TOKEN = "oauth_token"
private const val OAUTH_TOKEN_SECRET = "oauth_token_secret"
private const val OAUTH_VERIFIER = "oauth_verifier"
private const val USER_ID = "user_id"
private const val USER_NAME = "user_name"
private const val USER_REAL_NAME = "user_real_name"
private const val IS_PRO_USER = "is_pro_user"
private const val USER_ICON_SERVER = "user_icon_server"
private const val USER_ICON_FARM = "user_icon_farm"
private const val USER_ICON_URL = "user_icon_url"
private const val WELCOME_MESSAGE = "message"
private const val WELCOME_MESSAGE_2 = "message_2"
private const val WELCOME_MESSAGE_3 = "message_3"
private const val RECENT_GALLERY = "recent_gallery"
private const val CATEGORY_FILTER_POSITION = "category_filter_position"
private const val CALENDAR_DATE = "calendar_date"
private const val PHOTO_FILTER = "photo_filter"
private const val HISTORY_FLAG = "history_flag"
private const val CACHE_FLAG = "cache_flag"
private const val THEME_BEFORE = "theme_before"


object AppPreferences {

    fun setPhotoFilter(context: Context, position: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {putInt(PHOTO_FILTER, position)}

    }

    fun getPhotoFilter(context: Context) : Int {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(PHOTO_FILTER, 0)
    }

    fun setCategoryActivePosition(context: Context, position: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {putInt(CATEGORY_FILTER_POSITION, position)}

    }

    fun getCategoryActivePosition(context: Context) : Int {
       return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(CATEGORY_FILTER_POSITION, -1)
    }

    fun setRecentGalleryShow(context: Context, setShow: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit {putBoolean(RECENT_GALLERY, setShow)}
    }

    fun isRecentGalleryShow(context: Context) : Boolean {
       return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(RECENT_GALLERY, false)
    }

    fun setWelcomeMessageIsShown(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(WELCOME_MESSAGE, true) }
    }

    fun isWelcomeMessageShown(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(WELCOME_MESSAGE, false)
    }

    fun setWelcomeMessage3IsShown(context: Context) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(WELCOME_MESSAGE_3, true) }
    }

    fun isWelcomeMessage3Shown(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(WELCOME_MESSAGE_3, false)
    }

    fun setIntroShown(context: Context, isShown: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(IS_INTRO_SHOWN, isShown) }
    }

    fun isIntroShown(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(IS_INTRO_SHOWN, false)
    }


    fun setOauthToken(context: Context, oauthToken: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(OAUTH_TOKEN, oauthToken) }
    }

    fun getOauthToken(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(OAUTH_TOKEN, null)
    }

    fun setOauthTokenSecret(context: Context, oauthTokenSecret: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(OAUTH_TOKEN_SECRET, oauthTokenSecret) }
    }

    fun getOauthTokenSecret(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(OAUTH_TOKEN_SECRET, null)
    }

    fun setOauthVerifier(context: Context, oauthVerifier: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(OAUTH_VERIFIER, oauthVerifier) }
    }

    fun getOauthVerifier(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(OAUTH_VERIFIER, null)
    }

    fun setUserName(context: Context, userName: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_NAME, userName) }
    }

    fun getUserName(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_NAME, null)
    }

    fun setUserRealName(context: Context, userName: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_REAL_NAME, userName) }
    }

    fun getUserRealName(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_REAL_NAME, null)
    }

    fun setIsProUser(context: Context, isPro: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(IS_PRO_USER, isPro) }
    }

    fun getIsProUser(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(IS_PRO_USER, false)
    }

    fun setUserId(context: Context, userId: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_ID, userId) }
    }

    fun getUserId(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_ID, null)
    }

    fun setUserIconServer(context: Context, userId: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_ICON_SERVER, userId) }
    }

    fun getUserIconServer(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_ICON_SERVER, null)
    }

    fun setUserIconFarm(context: Context, userId: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_ICON_FARM, userId) }
    }

    fun getUserIconFarm(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_ICON_FARM, null)
    }


    fun setUserIconUrl(context: Context, userIconUrl: String?) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(USER_ICON_URL, userIconUrl) }
    }

    fun getUserIconUrl(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(USER_ICON_URL, null)
    }


    fun setGalleryViewColumns(context: Context, columns: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putInt(GALLERY_VIEW_COLUMNS, columns) }
    }

    fun getGalleryViewColumns(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(GALLERY_VIEW_COLUMNS, 3)
    }


    fun setSlideShowInterval(context: Context, interval: Long) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putLong(SLIDE_SHOW_INTERVAL, interval) }
    }

    fun getSlideShowInterval(context: Context): Long {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getLong(SLIDE_SHOW_INTERVAL, SLIDESHOW_INTERVAL_3_SEC)
    }

    fun setPhotoDownloadQuality(context: Context, size: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putString(PICTURE_DOWNLOAD_QUALITY, size) }
    }

    fun getPhotoDownloadQuality(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(PICTURE_DOWNLOAD_QUALITY, LogoIcon.Photo.MAX.prefix)!!
    }

    fun setAppTheme(context: Context, theme: Int) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putInt(APP_THEME, theme) }
    }

    fun getAppTheme(context: Context): Int {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(APP_THEME, ActivityUtils.THEME_DEFAULT)
    }


    fun setHistorySearch(context: Context, isOn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(HISTORY_FLAG, isOn) }
    }

    fun getHistorySearch(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(HISTORY_FLAG, true)
    }

    fun setClearCache(context: Context, isOn: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit { putBoolean(CACHE_FLAG, isOn) }
    }

    fun getClearCache(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(CACHE_FLAG, false)
    }

}