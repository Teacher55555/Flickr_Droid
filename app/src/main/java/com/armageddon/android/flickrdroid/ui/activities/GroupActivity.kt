package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.ui.fragments.detail.GroupFragment

private const val GROUP_BUNDLE = "group_bundle"
private const val QUERY_TYPE_BUNDLE = "query_type_bundle"


class GroupActivity : SingleFragmentActivity() {
    private lateinit var mGroup: Group
    private lateinit var mQuery: Query

    companion object {
        fun newIntent(context: Context, group: Group, query: Query) : Intent {
            return Intent(context, GroupActivity::class.java).apply {
                putExtra(GROUP_BUNDLE, group)
                putExtra(QUERY_TYPE_BUNDLE, query)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mGroup = intent.getSerializableExtra(GROUP_BUNDLE) as Group
        mQuery = intent.getSerializableExtra(QUERY_TYPE_BUNDLE) as Query
        super.onCreate(savedInstanceState)
    }

    override fun getFragment(): Fragment {
        return GroupFragment.newInstance(mGroup, mQuery)
    }
}