package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoCommentListFragment

private const val PHOTO_ID_BUNDLE = "photo_id_bundle"
private const val PHOTOBUNDLE = "photo_bundle"

class PhotoCommentsActivity : SingleActivity(), PhotoCommentListFragment.CallBacks {
    private lateinit var mPhotoId: String

    companion object {
        fun newIntent(context: Context, photoId: String) : Intent {
            val intent = Intent(context, PhotoCommentsActivity::class.java)
            intent.putExtra(PHOTO_ID_BUNDLE, photoId)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        mPhotoId = intent.getStringExtra(PHOTO_ID_BUNDLE) ?: ""
        super.onCreate(savedInstanceState)
    }

    override fun getFragment(): Fragment {
       return PhotoCommentListFragment.
       newInstance(mPhotoId)
    }

    override fun onCommentClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

    override fun reportAbuse() {
        startActivity(BlockedUserActivity.newIntent(this))
    }
}