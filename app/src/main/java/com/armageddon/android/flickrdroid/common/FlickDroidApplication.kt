package com.armageddon.android.flickrdroid.common

import android.app.Application
import com.armageddon.android.flickrdroid.repository.BlockedPersonRepository
import com.armageddon.android.flickrdroid.repository.HistoryRepository
import com.bumptech.glide.Glide

class FlickDroidApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        HistoryRepository.initialize(this)
        BlockedPersonRepository.initialize(this)
    }
}