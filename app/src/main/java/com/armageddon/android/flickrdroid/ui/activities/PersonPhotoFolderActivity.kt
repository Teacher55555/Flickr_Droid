package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonPhotoFolderFragment

private const val PERSON_COMPONENT_BUNDLE = "person_component_bundle"
private const val QUERY_TYPE_BUNDLE = "query_type_bundle"

class PersonPhotoFolderActivity: SingleFragmentActivity() {
    private lateinit var mPersonPhotoFolder: PersonPhotoFolder
    private lateinit var mQuery: Query

    companion object {
        fun newIntent(context: Context, personPhotoFolder: PersonPhotoFolder, query: Query) : Intent {
            return Intent(context, PersonPhotoFolderActivity::class.java).apply {
                putExtra(PERSON_COMPONENT_BUNDLE, personPhotoFolder)
                putExtra(QUERY_TYPE_BUNDLE, query)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mPersonPhotoFolder = intent.getSerializableExtra(PERSON_COMPONENT_BUNDLE) as PersonPhotoFolder
        mQuery = intent.getSerializableExtra(QUERY_TYPE_BUNDLE) as Query
        super.onCreate(savedInstanceState)
    }

    override fun getFragment(): Fragment {
        return PersonPhotoFolderFragment.newInstance(mPersonPhotoFolder, mQuery)
    }
}