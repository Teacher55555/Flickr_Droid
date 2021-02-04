package com.armageddon.android.flickrdroid.common;

import android.content.Context;

import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.ui.activities.PhotoFullActivity;

import androidx.preference.PreferenceManager;

/**
 * Keeps setting data and user_id.
 */

public class QueryPreferences {
    private static final String IS_INTRO_SHOWN = "is_shown";
    private static final String SLIDE_SHOW_INTERVAL = "show_interval";
    private static final String GALLERY_VIEW_COLOMUNS  = "gal_view_col";
    private static final String PICTURE_SHOW_QUALITY  = "show_quality";
    private static final String PICTURE_DOWNLOAD_QUALITY  = "download_quality";
    private static final String APP_THEME  = "app_theme";
    private static final String OAUTH_TOKEN = "oauth_token";
    private static final String OAUTH_TOKEN_SECRET = "oauth_token_secret";
    private static final String OAUTH_VERIFIER = "oauth_verifier";
    private static final String USER_ID = "user_id";
    private static final String USER_NAME = "user_name";
    private static final String USER_ICON_URL = "user_icon_url";


    public static void setIntroShown (Context context, boolean isShown) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(IS_INTRO_SHOWN, isShown)
                .apply();
    }

    public static boolean isIntroShown (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(IS_INTRO_SHOWN, false);
    }


    public static void setOauthToken (Context context, String oauthToken) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(OAUTH_TOKEN, oauthToken)
                .apply();
    }

    public static String getOauthToken (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OAUTH_TOKEN, null);
    }

    public static void setOauthTokenSecret (Context context, String oauthTokenSecret) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(OAUTH_TOKEN_SECRET, oauthTokenSecret)
                .apply();
    }

    public static String getOauthTokenSecret (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OAUTH_TOKEN_SECRET, null);
    }

    public static void setOauthVerifier (Context context, String oauthVerifier) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(OAUTH_VERIFIER, oauthVerifier)
                .apply();
    }

    public static String getOauthVerifier (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(OAUTH_VERIFIER, null);
    }



    public static void setUserName (Context context, String userName) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_NAME, userName)
                .apply();
    }

    public static String getUserName (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_NAME, null);
    }



    public static void setUserId (Context context, String userId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_ID, userId)
                .apply();
    }

    public static String getUserId (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_ID, null);
    }




    public static void setUserIconUrl (Context context, String userIconUrl) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(USER_ICON_URL, userIconUrl)
                .apply();
    }

    public static String getUserIconUrl (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(USER_ICON_URL, null);
    }



    public static void setGalleryViewColumns (Context context, int columns) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(GALLERY_VIEW_COLOMUNS, columns)
                .apply();
    }

    public static int getGalleryViewColumns (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(GALLERY_VIEW_COLOMUNS, 3);
    }


    public static void setSlideShowInterval (Context context, int interval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(SLIDE_SHOW_INTERVAL, interval)
                .apply();
    }

    public static int getSlideShowInterval (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(SLIDE_SHOW_INTERVAL, PhotoFullActivity.SLIDESHOW_INTERVAL_3_SEC);
    }


    public static void setPhotoViewQuality (Context context, int size) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PICTURE_SHOW_QUALITY, size)
                .apply();
    }

    public static int getPhotoViewQuality (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PICTURE_SHOW_QUALITY, GalleryItem.PHOTO_SIZE_2048);
    }


    public static void setPhotoDownloadQuality (Context context, int size) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PICTURE_DOWNLOAD_QUALITY, size)
                .apply();
    }

    public static int getPhotoDownloadQuality (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PICTURE_DOWNLOAD_QUALITY, GalleryItem.PHOTO_SIZE_MAX);
    }


    public static void setAppTheme (Context context, int theme) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(APP_THEME, theme)
                .apply();
    }

    public static int getAppTheme (Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(APP_THEME, ActivityUtils.THEME_DEFAULT);
    }



}
