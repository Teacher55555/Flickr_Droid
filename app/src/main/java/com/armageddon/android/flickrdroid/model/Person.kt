package com.armageddon.android.flickrdroid.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.armageddon.android.flickrdroid.common.LogoIcon
import java.io.Serializable

@Entity
data class Person (
    @PrimaryKey val id: String,
    val proUser: Boolean = false,
    val iconServer: String = "",
    val iconFarm: String = "",
    val userName: String = "",
    val realName: String = "",
    val location: String = "",
    val description: String = "",
    val firstDateTaken: String = "",
    val photosCount: String = "",
    val contacts: String = "",
) : LogoIcon, Serializable {

    fun getIconUrl (size: String = LogoIcon.Icon.ICON_BIG_150PX.prefix) : String {
       return getIconUrl(iconFarm, iconServer, id, size)
    }

    fun getSimpleName() : String = realName.ifBlank { userName }


    @Transient val isProVisibility = when (proUser) {
        true -> 0x00000000
        false -> 0x00000004
    }

}