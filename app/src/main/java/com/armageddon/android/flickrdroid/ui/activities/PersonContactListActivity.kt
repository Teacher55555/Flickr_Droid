package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.list.*

private const val QUERY_TYPE = "query_type_persons_list"
private const val BACK_ARROW = "show_back_arrow"

class PersonContactListActivity
    : SingleFragmentActivity(), PersonContactListFragment.CallBacks, UserContactListFragment.CallBacks {
    private lateinit var mQuery: Query
    private var mShowBackArrow = true

    companion object {
        fun newIntent(context: Context, query: Query, showBackArrow: Boolean = true) : Intent {
            return Intent(context, PersonContactListActivity::class.java).apply {
                putExtra(QUERY_TYPE, query)
                putExtra(BACK_ARROW, showBackArrow)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mQuery = intent.getSerializableExtra(QUERY_TYPE) as Query
        mShowBackArrow = intent.getBooleanExtra(BACK_ARROW, true)
        super.onCreate(savedInstanceState)
    }

    override fun getFragment(): Fragment {
        return when (mShowBackArrow) {
            true -> PublicContactListFragment.newInstance(mQuery)
            false -> PrivateContactListFragment.newInstance(mQuery)
        }
    }

    override fun onPersonClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }


}