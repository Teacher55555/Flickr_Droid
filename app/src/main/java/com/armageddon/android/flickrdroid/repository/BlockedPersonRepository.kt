package com.armageddon.android.flickrdroid.repository

import android.content.Context
import androidx.room.Room
import com.armageddon.android.flickrdroid.database.BlockedPersonBase
import com.armageddon.android.flickrdroid.model.Person
import java.util.concurrent.Executors

private const val DATABASE_NAME = "blockedPersons-database"

class BlockedPersonRepository(context: Context) {

    companion object {
        private var INSTANCE: BlockedPersonRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = BlockedPersonRepository(context)
            }
        }

        fun get(): BlockedPersonRepository {
            return INSTANCE ?: throw IllegalStateException("BlockedPersonRepository must be initialized")
        }
    }

    private val database : BlockedPersonBase = Room.databaseBuilder(
        context,
        BlockedPersonBase::class.java,
        DATABASE_NAME
    ).build()

    private val blockedPersonDao = database.blockedPersonDao()
    private val executor = Executors.newSingleThreadExecutor()

    fun getBlockedPersons() = blockedPersonDao.getAll()

    fun blockPerson(person: Person) {
        executor.execute {
            blockedPersonDao.insert(person)
        }
    }

    fun unblockPerson(id: String) {
        executor.execute {
            blockedPersonDao.remove(id)
        }
    }
}