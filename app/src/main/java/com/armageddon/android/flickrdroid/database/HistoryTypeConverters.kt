package com.armageddon.android.flickrdroid.database

import androidx.room.TypeConverter
import java.util.*

class HistoryTypeConverters {
    @TypeConverter
    fun fromUUID (uuid: UUID?) : String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID (stringUUID: String?) : UUID? {
        return UUID.fromString(stringUUID)
    }


}