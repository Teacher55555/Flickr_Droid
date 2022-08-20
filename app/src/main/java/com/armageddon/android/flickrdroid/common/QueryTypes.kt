package com.armageddon.android.flickrdroid.common

import com.armageddon.android.flickrdroid.R

enum class QueryTypes (
    var query1: String = "",
    var query2: String = "",
    var query3: String = "",
    var query4: String = "",
    var query5: String = "",
    var query6: String = "",
) {
    INTERESTING,
    USER_FOLLOWINGS_PHOTOS,
    ADD_FAVORITE_PHOTO,
    BLANK,
    RECENT,
    SEARCH,
    CATEGORY,
    GROUP,
    USER_GROUP,
    USER_GROUP_CAN_ADD_PHOTOS,
    ALBUM,
    GALLERY,
    PERSON_CONTACT_LIST,
    PERSON,
    PERSON_SEARCH,
    PERSON_WITH_ID,
    MAP,
    HISTORY,
    PUBLIC_PHOTOS,
    FAVORITES_PHOTOS,
    CAMERA_ROLL,
    ALBUM_PHOTOS,
    GALLERY_PHOTOS,
    GROUP_PHOTOS;

    fun setQuery(queryType: QueryTypes = INTERESTING) {
        this.query1 = queryType.query1
        this.query2 = queryType.query2
        this.query3 = queryType.query3
    }

    fun clear() {
        values().forEach {
          it.query1 = ""
          it.query2 = ""
          it.query3 = ""
        }
    }

    enum class Sort (
        val sort: String,
        val title: Int
    ) {
        RELEVANCE ("relevance", R.string.sort_relevance),
        INTERESTINGNESS_DESC ("interestingness-desc", R.string.sort_interestingness_desc),
        INTERESTINGNESS_ASC ("interestingness-asc", R.string.sort_interestingness_asc),
        DATE_POSTED_DESC("date-posted-desc", R.string.sort_date_posted_desc),
        DATE_POSTED_ASC("date-posted-asc", R.string.sort_date_posted_asc),
        DATE_TAKEN_DESC("date-taken-desc", R.string.sort_date_taken_desc),
        DATE_TAKEN_ASC("date-taken-asc", R.string.sort_date_taken_asc)
    }

}