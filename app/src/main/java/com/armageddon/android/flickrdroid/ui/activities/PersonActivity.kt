package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.ui.fragments.controllers.PersonFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonInfoFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonSearchFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.AlbumsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GalleryListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GroupsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PersonContactListFragment

private const val PERSON = "person"
private const val FRAGMENT_TAG = "person_info_fragment.xml"

class PersonActivity
    : SingleFragmentActivity(), AlbumsListFragment.CallBacks, GroupsListFragment.CallBacks,
    GalleryListFragment.CallBacks, PersonSearchFragment.CallBacks, PersonContactListFragment.CallBacks,
    PersonInfoFragment.CallBacks {
    private lateinit var mPerson: Person

    companion object {
        fun newIntent(context: Context, person: Person) : Intent {
            return Intent(context, PersonActivity::class.java).apply {
                putExtra(PERSON, person)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mPerson = intent.getSerializableExtra(PERSON) as Person
        super.onCreate(savedInstanceState)

    }

    override fun getFragment(): Fragment {
        return PersonFragment.newInstance(mPerson)
    }

    override fun onGroupClick(query: Query, group: Group) {
        startActivity(GroupActivity.newIntent(this, group, query))
    }

    override fun onItemClick(query: Query, personFotoHolder: PersonPhotoFolder) {
        startActivity(PersonPhotoFolderActivity.newIntent(this, personFotoHolder, query))
    }

    override fun onPersonClick(query: Query, person: Person) {}
    override fun onPersonContactListClick(query: Query) {
        startActivity(PersonContactListActivity.newIntent(this, query))
    }

    override fun onPersonClick(person: Person) {
       startActivity(newIntent(this, person))
    }

}