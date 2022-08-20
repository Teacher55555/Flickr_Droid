package com.armageddon.android.flickrdroid.network.responses


const val RESPONSE_NO_INTERNET_CONNECTION = "no_internet"
const val RESPONSE_DATA_OK = "ok"
const val RESPONSE_DATA_FAIL = "fail"
const val RESPONSE_TIMEOUT = "timeout"
const val RESPONSE_BAD_SEARCH = "bad_search"
const val RESPONSE_NO_PHOTOS = "no_photo"
const val RESPONSE_NO_INTERESTING_PHOTOS = "no_interesting_photo"
const val RESPONSE_USER_HAS_NO_PHOTOS = "user_no_photos"
const val RESPONSE_USER_HAS_NO_FOLLOWINGS = "user_no_followings"
const val RESPONSE_GROUP_HAS_NO_PHOTOS = "group_no_photos"
const val RESPONSE_USER_HAS_NO_CONTACTS = "user_no_contacts"
const val RESPONSE_USER_HAS_NO_GROUPS = "no_groups"
const val RESPONSE_USER_HAS_NO_GALLERIES = "no_galleries"
const val RESPONSE_USER_HAS_NO_ALBUMS = "no_albums"
const val RESPONSE_GROUP_HAS_PRIVATE_PHOTO = "group_has_private_photos"
const val RESPONSE_NO_COMMENTS = "no_comments"

class FlickrResponse<T> (
    var data: T? = null,
    var stat: String = "",
    var page: Int = 0,
    var pages: Int = 0,
    var perpage: Int = 0,
    var total: Int = 0,
    var dataArray : List<T> = emptyList(),
    var message: String = ""
)


