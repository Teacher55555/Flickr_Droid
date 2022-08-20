package com.armageddon.android.flickrdroid.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.armageddon.android.flickrdroid.common.AppPreferences

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppPreferences.setCategoryActivePosition(this,-1)

        /** Show intro for first launch */
        val intent = when (AppPreferences.getUserIconServer(this).isNullOrBlank()) {
            true -> {
                when (AppPreferences.isIntroShown(this)) {
                    true -> {
                        TopPhotoActivity.newIntent(this)
                    }
                    false -> {
                        AppPreferences.setIntroShown(this, true)
                        IntroActivity.newIntent(this, true)
                    }
                }
            }
            false -> {
                FollowingsActivity.newIntent(this, false)
            }
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        }
        startActivity(intent)
        finish()

    }
}