package com.armageddon.android.flickrdroid.common

import java.io.Serializable

class Query (
    val type: QueryTypes = QueryTypes.BLANK,
    val perPage: Int = 100,
    val id: String = "",
    val text: String = "",
    val tags: String = "",
    val sort: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val date: String = "",
    val photoPrivacy: PhotoPrivacy = PhotoPrivacy.PUBLIC,
    var oauthToken: String = "",
    var oauthTokenSecret: String = "",
) : Serializable