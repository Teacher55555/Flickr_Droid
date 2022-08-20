package com.armageddon.android.flickrdroid.model

import com.armageddon.android.flickrdroid.common.LogoIcon

data class PersonContact (
    val nsid: String,
    val username: String,
    val iconserver: String,
    val iconfarm: String,
) : LogoIcon {

    fun getUrl(size: String = LogoIcon.Icon.ICON_NORMAL_100PX.prefix) : String {
       return getIconUrl(iconfarm, iconserver, nsid, size)
    }
}