package com.armageddon.android.flickrdroid.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.armageddon.android.flickrdroid.model.Person


@Database(entities = [Person::class], version = 1, exportSchema = false)

abstract class BlockedPersonBase: RoomDatabase() {
    abstract fun blockedPersonDao(): BlockedPersonDao
}