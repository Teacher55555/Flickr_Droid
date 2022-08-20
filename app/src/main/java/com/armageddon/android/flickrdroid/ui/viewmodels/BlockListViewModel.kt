package com.armageddon.android.flickrdroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.repository.BlockedPersonRepository

class BlockListViewModel : ViewModel() {
    private val blockListRepository = BlockedPersonRepository.get()
    val blockListLiveData = blockListRepository.getBlockedPersons()

    fun blockPerson(person: Person) {
        blockListRepository.blockPerson (person)
    }

    fun unblockPerson(id: String) {
        blockListRepository.unblockPerson (id)
    }
}