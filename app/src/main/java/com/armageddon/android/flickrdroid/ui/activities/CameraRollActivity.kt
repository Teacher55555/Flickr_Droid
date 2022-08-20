package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.ui.fragments.list.AlbumsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.CameraRollFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GroupsListFragment


class CameraRollActivity : SingleFragmentActivity(), CameraRollFragment.CallBacks, AlbumsListFragment.CallBacks, GroupsListFragment.CallBacks {

    companion object {
        fun newIntent(context: Context) : Intent {
            return Intent(context, CameraRollActivity::class.java)
        }
    }

    override fun getFragment(): Fragment {
        return CameraRollFragment.newInstance()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment is CameraRollFragment && fragment.mSelectMode) {
            fragment.onMenuDesectedMode()
        } else{
            super.onBackPressed()
        }

    }

    override fun onItemClick(query: Query, personPhotoFolder: PersonPhotoFolder) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        (fragment as CameraRollFragment).addPhotosToAlbum(albumID = query.id)
    }

    override fun onGroupClick(query: Query, group: Group) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        (fragment as CameraRollFragment).addPhotosToGroup(groupID = query.id)
    }
}