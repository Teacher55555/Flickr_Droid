package com.armageddon.android.flickrdroid.api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;


/**
 * Contains information (connection status, total pages, current page in request and etc.)
 * and data List<E> (Gallery items, comments, friends and etc) after received response from Flickr.com
 */

public class RequestResponse<E> implements Serializable {
    public static final boolean CONNECTION_OK = true;
    public static final boolean CONNECTION_FAIL = false;
    public static final String RESPONSE_DATA_OK = "ok";
    public static final String RESPONSE_DATA_FAIL = "data_fail";
    public static final String RESPONSE_BAD_SEARCH = "bad_search";
    public static final String RESPONSE_NO_PHOTOS = "no_photo";
    public static final String RESPONSE_USER_HAS_NO_PHOTOS = "user_no_photos";
    public static final String RESPONSE_USER_HAS_NO_FOLLOWINGS = "user_no_followings";
    public static final String RESPONSE_GROUP_HAS_NO_PHOTOS = "group_no_photos";
    public static final String RESPONSE_USER_HAS_NO_CONTACTS = "user_no_contacts";
    public static final String RESPONSE_USER_HAS_NO_GROUPS = "no_groups";
    public static final String RESPONSE_USER_HAS_NO_GALLERIES = "no_galleries";
    public static final String RESPONSE_USER_HAS_NO_ALBUMS = "no_albums";
    public static final String RESPONSE_GROUP_HAS_PRIVATE_PHOTO = "group_has_private_photos";
    public static final String RESPONSE_NO_COMMENTS = "no_comments";

    public RequestResponse () {
    }

    @SerializedName("page")
    @Expose()
    private int page;

    @SerializedName("pages")
    @Expose()
    private int pages;

    @SerializedName("perpage")
    @Expose()
    private int perpage;

    @SerializedName("total")
    @Expose()
    private int total;

    private String mQueryType;
    private String mQuery;
    private String mCategory;

    private List<E> items;

    private String responseDataStat;

    public void setResponseDataStat(String responseDataStat) {
        this.responseDataStat = responseDataStat;
    }

    private boolean connection = CONNECTION_OK;

    public String getResponseDataStat() {
        return responseDataStat;
    }

    public boolean getConnectionStat() {
        return connection;
    }

    public void setConnectionStat(boolean connection) {
        this.connection = connection;
    }

    public void setItems(List<E> items) {
        this.items = items;
    }

    public List<E> getItems() {
        return items;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public int getPerpage() {
        return perpage;
    }

    public int getTotal() {
        return total;
    }

    public String getQueryType() {
        return mQueryType;
    }

    public void setQueryType(String queryType) {
        mQueryType = queryType;
    }

    public String getQuery() {
        return mQuery;
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }
}
