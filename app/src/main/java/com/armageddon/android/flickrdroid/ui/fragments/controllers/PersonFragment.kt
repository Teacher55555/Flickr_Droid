package com.armageddon.android.flickrdroid.ui.fragments.controllers

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.AppPreferences
import com.armageddon.android.flickrdroid.common.LogoIcon
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.FragmentPersonBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonInfoFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.AlbumsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GalleryListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.GroupsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

private const val PERSON_BUNDLE = "person_bundle"
private const val FLICKR_PEOPLE_LINK = "https://www.flickr.com/people/"
private const val FLICKR_PHOTOS_LINK = "https://www.flickr.com/photos/"
private const val FLICKR_FRIENDS_LINK = "https://www.flickr.com/photos/friends"
private const val FLICKR_CAMERA_ROLL_LINK = "https://www.flickr.com/cameraroll"

class PersonFragment : MenuFragment() {
    private var _binding : FragmentPersonBinding? = null
    private val binding get() = _binding!!
    private val photoViewModel : GalleryItemViewModel by activityViewModels()
    private val commonViewModel : CommonViewModel by activityViewModels()
    private var userId = ""
    private val offScreenLoggedUserPageLimit = 5
    private val offScreenUnLoggedUserPageLimit = 6
    private lateinit var mPerson: Person

    companion object {
        fun newInstance(peron : Person): PersonFragment {
            val args = Bundle()
            val fragment = PersonFragment()
            args.putSerializable(PERSON_BUNDLE, peron)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = AppPreferences.getUserId(requireContext()) ?: ""
        mPerson = arguments?.getSerializable(PERSON_BUNDLE) as Person
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonBinding.inflate(layoutInflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            itemIcon.setOnClickListener {
                val url: String = when (viewPager.currentItem) {
                        0 -> "$FLICKR_PHOTOS_LINK${mPerson.id}"
                        1 -> "$FLICKR_PHOTOS_LINK${mPerson.id}/favorites"
                        2 -> "$FLICKR_PHOTOS_LINK${mPerson.id}/albums"
                        3 -> "$FLICKR_PHOTOS_LINK${mPerson.id}/galleries"
                        4 -> "$FLICKR_PEOPLE_LINK${mPerson.id}/groups"
                        5 -> "${FLICKR_PEOPLE_LINK}${mPerson.id}"
                        else -> ""
                    }
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.parse(url), "text/html")
                startActivity(intent)
            }

            Glide.with(this@PersonFragment)
                .load(mPerson.getIconUrl(LogoIcon.Icon.ICON_HUGE_300PX.prefix))
                .centerCrop()
                .error(R.drawable.no_logo_account)
                .into(itemIcon)
            itemTitle.text = mPerson.getSimpleName()
            viewPager.adapter = when (userId.isBlank()) {
                true -> PersonUnLoggedAdapter()
                false -> PersonUnLoggedAdapter()
            }
            viewPager.adapter = PersonUnLoggedAdapter()
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.user_public)
                    1 -> getString(R.string.user_favorites)
                    2 -> getString(R.string.user_albums)
                    3 -> getString(R.string.user_galleries)
                    4 -> getString(R.string.user_groups)
                    5 -> getString(R.string.user_about)
                    else -> ""
                }
            }.attach()
            viewPager.offscreenPageLimit = 6
            setTabLayoutMediator(tabLayout, viewPager)
        }
    }

    private inner class PersonUnLoggedAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = offScreenUnLoggedUserPageLimit
        override fun createFragment(position: Int): Fragment {
           return when (position) {
               0 -> {
                   val queryType = QueryTypes.PUBLIC_PHOTOS.apply { query1 = mPerson.id }
                   val query = Query(
                       type = QueryTypes.PUBLIC_PHOTOS,
                       id = mPerson.id
                   )
                   PhotoListFragment.newInstance(query)
               }
               1 -> {
                   val queryType = QueryTypes.FAVORITES_PHOTOS.apply { query1 = mPerson.id }
                   val query = Query(
                       type = QueryTypes.FAVORITES_PHOTOS,
                       id = mPerson.id
                   )
                   PhotoListFragment.newInstance(query)
               }
               2 -> {
                   val queryType = QueryTypes.ALBUM.apply { query1 = mPerson.id }
                   val query = Query(
                       type = QueryTypes.ALBUM,
                       id = mPerson.id
                   )
                   AlbumsListFragment.newInstance(query)
               }
               3 -> {
                   val queryType = QueryTypes.GALLERY.apply { query1 = mPerson.id }
                   val query = Query(
                       type = QueryTypes.GALLERY,
                       id = mPerson.id
                   )
                   GalleryListFragment.newInstance(query)
               }
               4 -> {
                   val query = Query(
                       type = QueryTypes.USER_GROUP,
                       id = mPerson.id
                   )
                   GroupsListFragment.newInstance(query)
               }
               5 -> {
                   val queryType = QueryTypes.PERSON.apply { query1 = mPerson.id }
                   val query = Query(
                       type = QueryTypes.PERSON,
                       id = mPerson.id
                   )
                   PersonInfoFragment.newInstance(mPerson.id)
               }
               else -> Fragment()
            }
        }
    }

    private fun setTabLayoutMediator(tabLayout: TabLayout, viewPager: ViewPager2) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.user_public)
                    1 -> getString(R.string.user_favorites)
                    2 -> getString(R.string.user_albums)
                    3 -> getString(R.string.user_galleries)
                    4 -> getString(R.string.user_groups)
                    5 -> getString(R.string.user_about)
                    else -> ""
                }
            }.attach()
    }

}
