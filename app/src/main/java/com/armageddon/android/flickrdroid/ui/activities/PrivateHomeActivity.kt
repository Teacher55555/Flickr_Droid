package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment

class PrivateHomeActivity : PrivateUserActivity() {

    companion object {
        fun newIntent (context: Context) : Intent {
            return Intent(context, PrivateHomeActivity::class.java).apply {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain.title.text = getString(R.string.personal_page_followings)
    }

    override fun getFragment(): Fragment {
        val query = Query(
            type = QueryTypes.CATEGORY,
            text = "Sunset",
            sort = QueryTypes.Sort.INTERESTINGNESS_DESC.sort
        )
        return PhotoListFragment.newInstance(query)
    }

    override fun onTagClick(tag: String, openKeyboard: Boolean) {
        startActivity(SearchActivity.newIntent(this, tag, openKeyboard))
    }

}