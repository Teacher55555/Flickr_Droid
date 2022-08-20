package com.armageddon.android.flickrdroid.ui.fragments.list

import android.os.Bundle
import com.armageddon.android.flickrdroid.common.Query

private const val QUERY_TYPE = "query_type"

class PublicContactListFragment: UserContactListFragment() {

    companion object {
        fun newInstance(query: Query)  = PublicContactListFragment().apply {
            val args = Bundle().apply {
                putSerializable(QUERY_TYPE, query)
            }
            arguments = args
        }
    }

    override fun showBackArrow(): Boolean {
        return true
    }
}