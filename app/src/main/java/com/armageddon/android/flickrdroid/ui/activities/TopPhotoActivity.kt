package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.AppPreferences.isWelcomeMessage3Shown
import com.armageddon.android.flickrdroid.common.AppPreferences.setWelcomeMessage3IsShown
import com.armageddon.android.flickrdroid.ui.fragments.controllers.TopPhotoFragment

private const val OFFSCREEN_PAGE_LIMIT = 2
private const val SEARCH_TRIGGER = "search_trigger"
private const val TITLE = "title"
private const val ACTIVITY_MODE = "activity_mode"
private const val OPEN_MENU_FLAG = "menu_panel"
private const val CATEGORY_FLAG = "category_flag"


class TopPhotoActivity : SingleFragmentActivity(), TopPhotoFragment.CallBacks {
    var openMenuPanel = "false"

    companion object {
        fun newIntent (context: Context) : Intent {
            return Intent(context, TopPhotoActivity::class.java).apply {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isWelcomeMessage3Shown(this)) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.intro_version_2_1))
                .setPositiveButton("Ok") { dialog: DialogInterface, id: Int -> dialog.cancel() }
            val alert = builder.create()
            alert.show()
            setWelcomeMessage3IsShown(this)
        }
    }

    override fun getFragment(): Fragment {
        return TopPhotoFragment.newInstance()
    }


    override fun onSearchIconClick() {
        startActivity(SearchActivity.newIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        })

    }

    override fun onMapIconClick() {
        startActivity(MapActivity.newIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        })
    }



}