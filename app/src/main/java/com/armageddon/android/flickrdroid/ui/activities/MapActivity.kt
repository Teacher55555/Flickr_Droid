package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.ui.fragments.detail.MapFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PhotoFullFragment


class MapActivity : SingleFragmentActivity (), MapFragment.CallBacks {

    override fun getFragment(): Fragment {
        return MapFragment.newInstance()
    }

    companion object {
        fun newIntent(context: Context) : Intent {
            return Intent(context, MapActivity::class.java)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is MapFragment) {
            fragment.clearCategoryFilter()
        }
    }

    override fun onItemClick(query: Query) {
        val newFragment = PhotoFullFragment.newInstance(query)
        bindingMain.activityProgressBar.visibility = View.GONE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack("oldFragment")
            .commit()
    }

}