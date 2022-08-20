package com.armageddon.android.flickrdroid.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.armageddon.android.flickrdroid.common.HistoryType
import com.armageddon.android.flickrdroid.model.HistoryElement
import java.util.*

@Dao
interface HistoryDao {

    @Query("SELECT * FROM historyElement WHERE historyType =(:historyType)")
    fun getHistory(historyType: HistoryType): LiveData<List<HistoryElement>>

    @Query("DELETE FROM historyElement")
    fun clear()

    @Query("DELETE FROM historyElement WHERE id =(:id)")
    fun remove(id: String)

    @Query("DELETE FROM historyElement WHERE historyType =(:historyType)")
    fun clearType(historyType: HistoryType)

//    @Query("DELETE FROM historyElement WHERE text =(:text)")
//    fun remove(text: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(historyElement: HistoryElement)


}