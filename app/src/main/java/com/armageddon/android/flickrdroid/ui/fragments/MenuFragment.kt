package com.armageddon.android.flickrdroid.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.AppPreferences.getIsProUser
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserIconFarm
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserIconServer
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserIconUrl
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserId
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserName
import com.armageddon.android.flickrdroid.common.AppPreferences.getUserRealName
import com.armageddon.android.flickrdroid.common.AppPreferences.setIsProUser
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthToken
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthTokenSecret
import com.armageddon.android.flickrdroid.common.AppPreferences.setOauthVerifier
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconFarm
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconServer
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserIconUrl
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserId
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserName
import com.armageddon.android.flickrdroid.common.AppPreferences.setUserRealName
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.MenuSideScreenBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.activities.*
import com.armageddon.android.flickrdroid.ui.fragments.controllers.TopPhotoFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.GroupFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonPhotoFolderFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.*
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide

private const val GOOGLE_PLAY_PAGE =
    "https://play.google.com/store/apps/details?id=com.armageddon.android.flickrdroid"

abstract class MenuFragment : Fragment() {
    protected lateinit var mDrawerLayout: DrawerLayout
    private val viewModel: GalleryItemViewModel by activityViewModels()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.app_bar_menu_button -> mDrawerLayout.openDrawer(GravityCompat.END, true)
            R.id.app_bar_select_button -> {
                activity
                    ?.supportFragmentManager
                    ?.findFragmentById(R.id.fragment_container).let {
                        (it as CameraRollFragment).onItemMenuSelectClick()
                    }
            }
            R.id.app_bar_add_group_album -> {
                activity
                    ?.supportFragmentManager
                    ?.findFragmentById(R.id.fragment_container).let {
                        (it as CameraRollFragment).onAddGroupAlbum()
                    }
            }
        }
        return true
    }

    override fun onStop() {
        super.onStop()
        if (::mDrawerLayout.isInitialized)
        mDrawerLayout.closeDrawer(GravityCompat.END, false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        when(activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container)) {
            is CameraRollFragment -> super.onCreateOptionsMenu(menu, inflater)
            is PersonPhotoFolderFragment -> inflater.inflate(R.menu.common_white_menu, menu)
            is GroupFragment -> inflater.inflate(R.menu.common_white_menu, menu)
            is PhotoCommentListFragment -> inflater.inflate(R.menu.comments_menu, menu)
            else -> inflater.inflate(R.menu.common_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    abstract fun getToolbar() : Toolbar
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(getToolbar())
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }


    fun initSlideMenu (drawerLayout: DrawerLayout, slideMenuView: View, menuShow: Boolean = false) {
        mDrawerLayout = drawerLayout
        if (menuShow) { mDrawerLayout.openDrawer(GravityCompat.END, true) }
        val menuBinding = MenuSideScreenBinding.bind(slideMenuView)
        menuBinding.apply {
            val userIconServer = getUserIconServer(requireContext())
            if (!userIconServer.isNullOrBlank()) {
                val person = Person(
                    id = getUserId(requireContext())!!,
                    proUser = getIsProUser(requireContext()),
                    iconServer = userIconServer,
                    iconFarm = getUserIconFarm(requireContext())!!,
                    userName = getUserName(requireContext())!!,
                    realName = getUserRealName(requireContext())!!
                )
                val userPageListener = View.OnClickListener {
                    startActivity(PersonActivity.newIntent(requireContext(), person))
                }
                Glide.with(requireContext())
                    .load(getUserIconUrl(requireContext()))
                    .error(R.drawable.logo)
                    .into(personIcon)

                iconClose.visibility = View.VISIBLE
                iconClose.setOnClickListener {
                    setUserIconUrl(requireContext(), null)
                    setUserName(requireContext(), null)
                    setUserRealName(requireContext(), null)
                    setUserIconFarm(requireContext(), null)
                    setUserIconServer(requireContext(), null)
                    setIsProUser(requireContext(), false)
                    setUserId(requireContext(), null)
                    setOauthToken(requireContext(), null)
                    setOauthTokenSecret(requireContext(), null)
                    setOauthVerifier(requireContext(), null)
                    personRealName.text = getString(R.string.sing_in)
                    personIcon.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(), R.drawable.icon_person_filled
                        )
                    )
                    iconClose.visibility = View.INVISIBLE
                    startActivity(TopPhotoActivity.newIntent(requireContext()))
                    activity?.finish()
                }
                personRealName.text = person.getSimpleName()
                personIcon.setOnClickListener(userPageListener)
                personRealName.setOnClickListener(userPageListener)

                menuFollowingsButton.setOnClickListener {
                    val currentFragment =
                        activity?.
                        supportFragmentManager?.
                        findFragmentById(R.id.fragment_container)
                    if (currentFragment is FollowingsListFragment) {
                        mDrawerLayout.closeDrawer(GravityCompat.END, true)
                    } else {
                        startActivity(FollowingsActivity.newIntent(requireContext(), false))
                    }
                }
                menuCameraRollButton.setOnClickListener {
                    val currentFragment =
                        activity?.
                        supportFragmentManager?.
                        findFragmentById(R.id.fragment_container)
                    if (currentFragment is CameraRollFragment) {
                        mDrawerLayout.closeDrawer(GravityCompat.END, true)
                    } else {
                        startActivity(CameraRollActivity.newIntent(requireContext()))
                    }
                }
                menuContactsButton.setOnClickListener {
                    val currentFragment =
                        activity?.
                        supportFragmentManager?.
                        findFragmentById(R.id.fragment_container)
                    if (currentFragment is UserContactListFragment) {
                        mDrawerLayout.closeDrawer(GravityCompat.END, true)
                    } else {
                        val query = Query(
                            type = QueryTypes.PERSON_CONTACT_LIST,
                            id = person.id
                        )
                        startActivity(PersonContactListActivity.newIntent(requireContext(), query, false))
                    }
                }

            } else {
                val oauthListener = View.OnClickListener {
                    startActivity(LogInActivity.newIntent(requireContext()))
                }
                personIcon.setOnClickListener(oauthListener)
                personRealName.setOnClickListener(oauthListener)
                menuFollowingsButton.visibility = View.GONE
                menuCameraRollButton.visibility = View.GONE
                menuContactsButton.visibility = View.GONE
            }

            menuSearchButton.setOnClickListener {
                val currentFragment =
                    activity?.
                    supportFragmentManager?.
                    findFragmentById(R.id.fragment_container)
                if (currentFragment is TopPhotoFragment) {
                    mDrawerLayout.closeDrawer(GravityCompat.END, true)
                } else {
                    startActivity(TopPhotoActivity.newIntent(requireContext()))
                }
            }
            menuInfoButton.setOnClickListener {
                startActivity(IntroActivity.newIntent(requireContext(), false))
            }
            menuRateButton.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_PAGE)))
            }
            menuSettingsButton.setOnClickListener {
                startActivity(SettingsActivity.newIntent(requireContext()))
            }
        }
    }
}
