package com.armageddon.android.flickrdroid.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.databinding.ActivitySingleFragmentBinding

abstract class SingleActivity: AppCompatActivity() {
    lateinit var bindingMain : ActivitySingleFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityUtils.onActivityCreateSetTheme(this)

        super.onCreate(savedInstanceState)

        bindingMain = ActivitySingleFragmentBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, getFragment())
                .commit()
        }
    }

    abstract fun getFragment() : Fragment
}