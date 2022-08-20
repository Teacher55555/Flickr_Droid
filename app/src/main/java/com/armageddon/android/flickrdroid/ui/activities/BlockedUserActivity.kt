package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.ui.fragments.list.UserBlockListFragment

class BlockedUserActivity : SingleFragmentActivity(), UserBlockListFragment.CallBacks {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BlockedUserActivity::class.java)
        }
    }

    override fun getFragment(): Fragment {
       return UserBlockListFragment.newInstance()
    }

}