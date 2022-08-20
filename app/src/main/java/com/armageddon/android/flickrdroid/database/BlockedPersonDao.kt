package com.armageddon.android.flickrdroid.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.armageddon.android.flickrdroid.common.HistoryType
import com.armageddon.android.flickrdroid.model.HistoryElement
import com.armageddon.android.flickrdroid.model.Person

@Dao
interface BlockedPersonDao {
    @Query("SELECT * FROM person")
    fun getAll(): LiveData<List<Person>>

    @Query("DELETE FROM person")
    fun clear()

    @Query("DELETE FROM person WHERE id =(:id)")
    fun remove(id: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(person: Person)
}