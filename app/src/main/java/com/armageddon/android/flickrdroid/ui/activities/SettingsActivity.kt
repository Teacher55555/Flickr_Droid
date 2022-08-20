package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.BuildConfig
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.AppPreferences
import com.armageddon.android.flickrdroid.databinding.ActivityAboutBinding
import com.armageddon.android.flickrdroid.ui.fragments.LogoFragment
import com.armageddon.android.flickrdroid.ui.fragments.SettingsFragment

private const val ABOUT_FRAGMENT = "about_fragment"

class SettingsActivity : SingleActivity(), SettingsFragment.CallBacks, LogoFragment.CallBacks {
    private var mColumnsChanged = false
    private var mInflateAboutFragment = false

    companion object {
        fun newIntent(context: Context, inflateAboutFragment: Boolean = false) : Intent {
            return Intent(context, SettingsActivity::class.java)
                .putExtra(ABOUT_FRAGMENT, inflateAboutFragment)
        }
    }

    override fun getFragment(): Fragment {
       return when (mInflateAboutFragment) {
           true -> LogoFragment.newInstance(true)
           false -> SettingsFragment.newInstance()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mInflateAboutFragment = intent.getBooleanExtra(ABOUT_FRAGMENT, false)
        super.onCreate(savedInstanceState)
        if (mInflateAboutFragment) {
            val bindingAbout = ActivityAboutBinding.inflate(layoutInflater)
            setContentView(bindingAbout.root)
            setSupportActionBar(bindingAbout.toolbar)
            val version = BuildConfig.VERSION_NAME

            bindingAbout.apply {
                versionView.text = getString(R.string.app_version, version)
                listOfChanges.setOnClickListener { showListOfChangesDialog() }
            }
        }


    }


    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is LogoFragment) {
            setContentView(R.layout.activity_single_fragment)
        }

        when (ActivityUtils.isThemeChanged || mColumnsChanged) {
            true -> {
                if (AppPreferences.getOauthToken(this).isNullOrBlank())
                    startActivity(TopPhotoActivity.newIntent(this))
                else
                    startActivity(TopPhotoActivity.newIntent(this))
                ActivityUtils.isThemeChanged = false
            }
            false -> {
                super.onBackPressed()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    override fun onThemeChanged() {
        val intent = newIntent(this)
        startActivity(intent)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onColumnsChanged() {
        mColumnsChanged = true
    }

    override fun onAboutClick() {
        val intent = newIntent(this, true)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    override fun onLogoAnimationEnd() {

    }

    private fun showListOfChangesDialog () {
        val builder = AlertDialog.Builder(this, R.style.DialogAllCapsFalseStyle).apply {
            setMessage(getString(R.string.text_of_changes))
            setPositiveButton(R.string.ok) { _, _ -> }
        }
        val alert = builder.create()
        alert.show()
    }
}
