package com.armageddon.android.flickrdroid.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.armageddon.android.flickrdroid.common.HistoryType

@Entity
data class HistoryElement (
    val text: String,
    val historyType: HistoryType,
    @PrimaryKey val id: String = text + historyType
)