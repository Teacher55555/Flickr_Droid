package com.armageddon.android.flickrdroid.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.ActivitySingleFragmentBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.controllers.TopPhotoFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PhotoFullFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class SingleFragmentActivity : AppCompatActivity(),
    PhotoListFragment.CallBacks,
    PhotoFullFragment.CallBacks,
    PhotoCommentListFragment.CallBacks
{
    lateinit var bindingMain : ActivitySingleFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)

        bindingMain = ActivitySingleFragmentBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, getFragment())
                .commit()
        }
    }

    abstract fun getFragment() : Fragment

    override fun onGalleryItemClick(query: Query) {
        val newFragment = PhotoFullFragment.newInstance(query)
        bindingMain.activityProgressBar.visibility = View.GONE
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, newFragment)
            .addToBackStack("oldFragment")
            .commit()
    }

    override fun onRefresh(visibility: Int) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        bindingMain.apply { when(fragment is PhotoFullFragment) {
            true -> fragmentProgressBar.visibility = visibility
            else -> activityProgressBar.visibility = visibility
        } }
    }


    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is FollowingsListFragment ||
                currentFragment is CameraRollFragment ||
                currentFragment is PrivateContactListFragment ||
                currentFragment is TopPhotoFragment) {
            if (doubleBackToExitPressedOnce) {
                finishAffinity()
            } else {
                this.doubleBackToExitPressedOnce = true
                Snackbar.make(bindingMain.root, R.string.back_to_exit, Snackbar.LENGTH_SHORT)
                    .setTextColor(resources.getColor(R.color.colorWhite, null))
                    .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                    .show()
                lifecycleScope.launch {
                    delay(2000)
                    doubleBackToExitPressedOnce = false
                }
            }
        } else {
            bindingMain.fragmentProgressBar.visibility = View.INVISIBLE
            super.onBackPressed()
        }
    }

    override fun onError(query: Query) {
        if (query.type == QueryTypes.MAP) {
            startActivity(MapActivity.newIntent(this).apply { finish() })
        }
    }

    protected fun getCurrentFragment() : Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)
    }

    override fun onUserIconClick(person: Person) {
       startActivity(PersonActivity.newIntent(this, person))
    }

    override fun onCommentClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

    override fun onCommentIconClick(photoId: String) {
        startActivity(PhotoCommentsActivity.newIntent(this, photoId))
    }

    override fun onTagClick(tag: String, openKeyboard: Boolean) {
        startActivity(SearchActivity.newIntent(this, tag, openKeyboard))
    }

    override fun reportAbuse() {

    }

}