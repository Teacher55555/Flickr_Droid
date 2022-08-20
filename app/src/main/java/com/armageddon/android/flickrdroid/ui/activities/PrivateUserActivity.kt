package com.armageddon.android.flickrdroid.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.ActivityPrivateBinding
import com.armageddon.android.flickrdroid.databinding.MenuSideScreenBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.detail.PhotoFullFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

abstract class PrivateUserActivity : AppCompatActivity(), PhotoListFragment.CallBacks,
    PhotoFullFragment.CallBacks {

    lateinit var bindingMain : ActivityPrivateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ActivityUtils.onActivityCreateSetTheme(this)
        super.onCreate(savedInstanceState)
        bindingMain = ActivityPrivateBinding.inflate(layoutInflater)
        setContentView(bindingMain.root)
        bindingMain.toolbar.title = ""
        initSlideMenu(bindingMain.navView.getChildAt(1))
        setSupportActionBar(bindingMain.toolbar)

        val person = Person(
            id = "188864276@N07",
            proUser = false,
            iconServer = "65535",
            iconFarm = "66",
            userName = "badbadovich",
            realName = "Andy Lane"
        )

        Glide.with(this)
            .load(person.getIconUrl())
            .transform(CenterCrop(), RoundedCorners(50))
            .into(bindingMain.logoImage)

        val isFragmentContainerEmpty = savedInstanceState == null
        if (isFragmentContainerEmpty) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, getFragment())
                .commit()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.common_menu, menu)
        return super.onCreateOptionsMenu(menu)
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

    override fun onBackPressed() {
        bindingMain.fragmentProgressBar.visibility = View.INVISIBLE
        super.onBackPressed()
    }

    override fun onError(query: Query) {
        when (query.type) {
            QueryTypes.MAP -> startActivity(MapActivity.newIntent(this).apply { finish() })
        }
    }

    override fun onUserIconClick(person: Person) {
        startActivity(PersonActivity.newIntent(this, person))
    }

    override fun onCommentIconClick(photoId: String) {
//        startActivity(PhotoCommentsActivity.newIntent(this, photoId))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.app_bar_menu_button -> bindingMain.drawerLayout.openDrawer(GravityCompat.END, true)
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        bindingMain.drawerLayout.closeDrawer(GravityCompat.END, false)
    }


    private fun initSlideMenu (slideMenuView: View) {
        MenuSideScreenBinding.bind(slideMenuView).apply {
            personIcon.setOnClickListener {
                val person = Person(
                    id = "188864276@N07",
                    proUser = false,
                    iconServer = "65535",
                    iconFarm = "66",
                    userName = "badbadovich",
                    realName = "Andy Lane"
                )
                startActivity(PersonActivity.newIntent(this@PrivateUserActivity, person))
            }

            menuFollowingsButton.setOnClickListener {
                startActivity(PrivateHomeActivity.newIntent(this@PrivateUserActivity))
            }

            menuCameraRollButton.setOnClickListener {
            }

            menuSearchButton.setOnClickListener {
                startActivity(TopPhotoActivity.newIntent(this@PrivateUserActivity))
            }
            menuInfoButton.setOnClickListener {
                startActivity(IntroActivity.newIntent(this@PrivateUserActivity, false))
            }
            menuRateButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sdf")))
            }
            menuSettingsButton.setOnClickListener {
                startActivity(SettingsActivity.newIntent(this@PrivateUserActivity))
            }
        }

    }

}