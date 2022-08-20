package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.ui.fragments.list.FollowingsListFragment

private const val SHOW_MENU = "show_menu"

class FollowingsActivity : SingleFragmentActivity(), FollowingsListFragment.CallBacks {
    private var mMenuShow = false

    override fun onCreate(savedInstanceState: Bundle?) {
        mMenuShow = intent.getBooleanExtra(SHOW_MENU, false)
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newIntent(context: Context, menuShow: Boolean = false) : Intent {
            return Intent(context, FollowingsActivity::class.java).apply {
                putExtra(SHOW_MENU, menuShow)
            }
        }
    }

    override fun getFragment(): Fragment {
        return FollowingsListFragment.newInstance( mMenuShow)
    }

}