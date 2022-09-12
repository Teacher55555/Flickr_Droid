package com.armageddon.android.flickrdroid.ui.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.ActivityPrivateBinding
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.ui.fragments.controllers.PersonFragment
import com.armageddon.android.flickrdroid.ui.fragments.controllers.PrivatePersonFragment
import com.armageddon.android.flickrdroid.ui.fragments.controllers.TopPhotoFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonSearchFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PhotoFullFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.*

private const val PERSON = "person"
private const val OLD_FRAGMENT = "old_fragment"

class PrivatePersonActivity :
    AppCompatActivity(), PrivatePersonFragment.CallBacks, TopPhotoFragment.CallBacks,
    GroupsListFragment.CallBacks, AlbumsListFragment.CallBacks, GalleryListFragment.CallBacks,
    PersonSearchFragment.CallBacks, PersonContactListFragment.CallBacks, PhotoListFragment.CallBacks,
    PhotoFullFragment.CallBacks {
    private lateinit var mPerson: Person
    private lateinit var mHomeFragment: Fragment
    private lateinit var mFavoritesFragment: Fragment
    private lateinit var mSearchFragment: Fragment
    private lateinit var mContactsFragment: Fragment
    private lateinit var mPersonFragment: PersonFragment
    private lateinit var mCurrentFragment: Fragment
    private lateinit var binding: ActivityPrivateBinding
    private var mIndex = 0

    override fun onBackPressed() {
        binding.fragmentProgressBar.visibility = View.INVISIBLE
        super.onBackPressed()
    }

    companion object {
        fun newIntent(context: Context, person: Person): Intent {
            return Intent(context, PrivatePersonActivity::class.java).apply {
                putExtra(PERSON, person)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mPerson = intent.getSerializableExtra(PERSON) as Person
        ActivityUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, PrivatePersonFragment.newInstance(mPerson, ))
                .commit()
        }
    }

    override fun onSearchIconClick() {
        startActivity(
            SearchActivity.newIntent(this)
                .apply {
                    addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                })

    }

    override fun onMapIconClick() {
        startActivity(MapActivity.newIntent(this).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        })
    }

    override fun onHomeClick() {
        mIndex = 0
                val query = Query(
                    type = QueryTypes.CATEGORY,
                    text = "Sunset",
                    sort = QueryTypes.Sort.INTERESTINGNESS_DESC.sort
                )
                val fragment = PhotoListFragment.newInstance(query)
        mCurrentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inner_frame_container, fragment)
            .commit()
    }

    override fun onFavoritesClick() {
        mIndex = 1
        val query = Query(
            id = "188864276@N07",
            type = QueryTypes.FAVORITES_PHOTOS
        )
        val fragment = PhotoListFragment.newInstance(query)
        mCurrentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inner_frame_container, fragment)
            .commit()
    }

    override fun onSearchClick() {
        mIndex = 2
               val fragment =  TopPhotoFragment.newInstance()
        mCurrentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inner_frame_container, fragment)
            .commit()
    }


    override fun onContactsClick() {
        mIndex = 3
            val query = Query(
                type = QueryTypes.PERSON_CONTACT_LIST,
                id = "188864276@N07"
            )
            val fragment = PersonContactListFragment.newInstance(query)
        mCurrentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inner_frame_container, fragment)
            .commit()
    }

    override fun onPersonClick() {
        mIndex = 4
            val person = Person(
                id = "188864276@N07",
                userName = "Igor Gridin",
                realName = "Igor Gridin",
                iconFarm = "66",
                iconServer = "65535"
            )
            val fragment = PersonFragment.newInstance(person)
        mCurrentFragment = fragment
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.inner_frame_container, fragment)
            .commit()
    }

    override fun onGroupClick(query: Query, group: Group) {

    }

    override fun onGalleryItemClick(query: Query) {
        val newFragment = PhotoFullFragment.newInstance(query)
        binding.activityProgressBar.visibility = View.GONE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack("old_Fragment_s")
            .commit()
    }

    override fun onRefresh(visibility: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        binding.apply { when(fragment is PhotoFullFragment) {
            true -> fragmentProgressBar.visibility = visibility
            else -> activityProgressBar.visibility = visibility
        } }

    }

    override fun onError(query: Query) {
        if (query.type == QueryTypes.MAP) {
            startActivity(MapActivity.newIntent(this).apply { finish() })
        }
    }

    override fun onUserIconClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

    override fun onCommentIconClick(photoId: String) {
    }

    override fun onTagClick(tag: String, openKeyboard: Boolean) {
        startActivity(SearchActivity.newIntent(this, tag, openKeyboard))
    }

    override fun onItemClick(query: Query, personPhotoFolder: PersonPhotoFolder) {

    }

    override fun onPersonClick(query: Query, person: Person) {

    }

    override fun onPersonContactListClick(query: Query) {

    }

    override fun onPersonClick(person: Person) {

    }

    private fun ImageView.setDrawable(drawableId: Int) {
        val drawable = ContextCompat.getDrawable(this@PrivatePersonActivity, drawableId)
        this.setImageDrawable(drawable)
    }

    fun <T: View> T.startAnimAndGone (animation: Animation)  {
        animation.also {
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        startAnimation(animation)
    }

}