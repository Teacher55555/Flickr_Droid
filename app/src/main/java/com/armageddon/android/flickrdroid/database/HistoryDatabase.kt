package com.armageddon.android.flickrdroid.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.armageddon.android.flickrdroid.model.HistoryElement


@Database(entities = [HistoryElement::class], version = 1, exportSchema = false)

abstract class HistoryDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao
}