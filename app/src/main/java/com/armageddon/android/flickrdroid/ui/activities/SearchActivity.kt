package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.model.HistoryElement
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.controllers.SearchFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonSearch2Fragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonSearchFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GroupsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.HistoryListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PersonContactListFragment

private const val FRAGMENT_TAG = "search_activity_tag"
private const val SEARCH_TAG = "search_tag"
private const val KEYBOARD_KEY = "keyboard_key"


class SearchActivity : SingleFragmentActivity(), GroupsListFragment.CallBacks,
    PersonSearchFragment.CallBacks, HistoryListFragment.CallBacks,
    PersonContactListFragment.CallBacks, PersonSearch2Fragment.CallBacks {
private var mSearchTag = ""
private var mKeyboardKey = true

    override fun getFragment(): Fragment {
        return SearchFragment.newInstance(mSearchTag, mKeyboardKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mSearchTag = intent.getStringExtra(SEARCH_TAG) ?: ""
        mKeyboardKey = intent.getBooleanExtra(KEYBOARD_KEY, true)
        super.onCreate(savedInstanceState)
    }
//
    companion object {
        fun newIntent(context: Context, tag: String = "", openKeyboard: Boolean = true) : Intent {
            return Intent(context, SearchActivity::class.java).apply {
                putExtra(SEARCH_TAG, tag)
                putExtra(KEYBOARD_KEY, openKeyboard)
            }
        }
    }


    override fun onHistoryItemClick(historyElement: HistoryElement) {
               (getCurrentFragment() as SearchFragment).startSearch(historyElement.text, historyElement.historyType.ordinal)
       }

    override fun onGroupClick(query: Query, group: Group) {
        startActivity(GroupActivity.newIntent(this, group, query))
    }

    override fun onPersonClick(query: Query, person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

    override fun onPersonContactListClick(query: Query) {
        startActivity(PersonContactListActivity.newIntent(this, query))
    }


    override fun onPersonClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

}

