package com.armageddon.android.flickrdroid.model

import com.armageddon.android.flickrdroid.common.LogoIcon
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Group (
    val nsid: String,
    val name: String,
    val eighteenplus: String,
    val iconserver: String,
    val iconfarm: String,
    val members: String,
    @SerializedName("pool_count") val photosCount: String,
    @SerializedName("topic_count") val topicsCount: String?,
    val privacy: String?
) : LogoIcon, Serializable {


    @Transient var groupInfoResponse: FlickrResponse<GroupInfo>? = null

    fun getPhotoUrl(size: String) : String {
        return getPhotoUrl(iconfarm, iconserver, nsid, size)
    }

    fun getGroupIcon(size: String = LogoIcon.Icon.ICON_BIG_150PX.prefix): String? {
        return when (iconserver.toInt() == 0) {
            true -> null
            else -> getIconUrl(iconfarm, iconserver, nsid, size)
        }
    }

}