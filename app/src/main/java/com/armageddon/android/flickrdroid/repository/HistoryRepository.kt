package com.armageddon.android.flickrdroid.repository

import android.content.Context
import androidx.room.Room
import com.armageddon.android.flickrdroid.common.HistoryType
import com.armageddon.android.flickrdroid.database.HistoryDatabase
import com.armageddon.android.flickrdroid.model.HistoryElement
import java.util.concurrent.Executors

private const val DATABASE_NAME = "history-database"

class HistoryRepository(context: Context) {

    companion object {
        private var INSTANCE: HistoryRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = HistoryRepository(context)
            }
        }

        fun get(): HistoryRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }

    private val database : HistoryDatabase = Room.databaseBuilder(
        context,
        HistoryDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val historyDao = database.historyDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getHistory(historyType: HistoryType) = historyDao.getHistory(historyType)

    fun addHistoryElement(historyElement: HistoryElement) {
        executor.execute {
            historyDao.insert(historyElement)
        }
    }

    fun removeHistoryElement(id: String) {
        executor.execute {
            historyDao.remove(id)
        }
    }

    fun clearHistoryType(historyType: HistoryType) {
        executor.execute {
            historyDao.clearType(historyType)
        }
    }

    fun clearHistory () {
        executor.execute {
            historyDao.clear()
        }
    }






}