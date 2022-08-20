package com.armageddon.android.flickrdroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.armageddon.android.flickrdroid.common.HistoryType
import com.armageddon.android.flickrdroid.model.HistoryElement
import com.armageddon.android.flickrdroid.repository.HistoryRepository

class HistoryViewModel : ViewModel() {
    private val historyRepository = HistoryRepository.get()

    val photoHistoryLiveData = historyRepository.getHistory(HistoryType.PHOTO)
    val groupHistoryLiveData = historyRepository.getHistory(HistoryType.GROUP)
    val personHistoryLiveData = historyRepository.getHistory(HistoryType.PERSON)

    fun addHistoryElement(historyElement: HistoryElement) {
        historyRepository.addHistoryElement (historyElement)
    }

    fun removeHistoryElement(historyElement: HistoryElement) {
        historyRepository.removeHistoryElement (historyElement.id)
    }

    fun clearHistoryType(historyType: HistoryType) {
        historyRepository.clearHistoryType (historyType)
    }

    fun clearBase() {
        historyRepository.clearHistory()
    }

}