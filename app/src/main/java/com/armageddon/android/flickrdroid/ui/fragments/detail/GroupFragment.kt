package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.FragmentGroupBinding
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator

private const val GROUP_BUNDLE = "group_bundle"
private const val QUERY_TYPE_BUNDLE = "query_type"
private const val OFFSET_OLD_MIN = 0f
private const val OFFSET_OLD_MAX = -150f
private const val OFFSET_NEW_MIN = 1f
private const val OFFSET_NEW_MAX = 0f
private const val OFFSET_OLD_RANGE = OFFSET_OLD_MAX - OFFSET_OLD_MIN
private const val OFFSET_NEW_RANGE = OFFSET_NEW_MAX - OFFSET_NEW_MIN
private const val FLICKR_GROUPS_LINK = "https://www.flickr.com/groups/"


class GroupFragment : MenuFragment(), Converter {
    private var _binding: FragmentGroupBinding? = null
    private val binding get() = _binding!!
    private val offScreenPageLimit = 2
    private lateinit var query: Query
    private lateinit var mGroup: Group

    companion object {
        fun newInstance(
            group: Group,
            query: Query
        ): GroupFragment {
            val args = Bundle()
            val fragment = GroupFragment()
            args.putSerializable(GROUP_BUNDLE, group)
            args.putSerializable(QUERY_TYPE_BUNDLE, query)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = (arguments?.getSerializable(QUERY_TYPE_BUNDLE) as Query).apply {
            oauthToken = AppPreferences.getOauthToken(requireContext()) ?: ""
            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext()) ?: ""
        }
        mGroup = arguments?.getSerializable(GROUP_BUNDLE) as Group
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGroupBinding.inflate(layoutInflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewPager.adapter = GroupAdapter()
            viewPager.offscreenPageLimit = offScreenPageLimit
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> getString(R.string.photos)
                    1 -> getString(R.string.group_description)
                    else -> ""
                }
            }.attach()

            val errorDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.logo)
            Glide.with(requireActivity())
                .load(mGroup.getGroupIcon(LogoIcon.Icon.ICON_HUGE_300PX.prefix))
                .error(errorDrawable)
                .centerCrop()
                .into(itemIcon)
            itemIcon.setOnClickListener { openLinkInWebBrowser() }
            groupTitle.text = mGroup.name
            groupPhotos.text = mGroup.photosCount
            groupMembers.text = mGroup.members
            setTitleTextColors()

        }
    }

    private inner class GroupAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PhotoListFragment.newInstance(query)
                1 -> DescriptionFragment.newInstance(query, mGroup)
                else -> Fragment()
            }
        }
    }

    private fun setTitleTextColors() {
        val colorStateList = binding.tabLayout.tabTextColors

        binding.appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                if (state == State.EXPANDED) {
                    binding.tabLayout.tabTextColors = ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.tab_user_item_expandet_selector
                    )
                } else if (state == State.COLLAPSED) {
                    binding.tabLayout.tabTextColors = colorStateList
                }
            }

        })
        binding.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset ->
                val offsetNewValue = ((verticalOffset - OFFSET_OLD_MIN)
                        * OFFSET_NEW_RANGE / OFFSET_OLD_RANGE) + OFFSET_NEW_MIN
                binding.apply {
                    groupTitle.alpha = offsetNewValue
                    groupPhotos.alpha = offsetNewValue
                    groupMembers.alpha = offsetNewValue
                }
            })
    }


    internal abstract class AppBarStateChangeListener : OnOffsetChangedListener {
        internal enum class State {
            EXPANDED, COLLAPSED, IDLE
        }

        private var mCurrentState = State.IDLE
        override fun onOffsetChanged(appBarLayout: AppBarLayout, i: Int) {
            mCurrentState = if (i == 0) {
                if (mCurrentState != State.EXPANDED) {
                    onStateChanged(appBarLayout, State.EXPANDED)
                }
                State.EXPANDED
            } else if (Math.abs(i) >= appBarLayout.totalScrollRange) {
                if (mCurrentState != State.COLLAPSED) {
                    onStateChanged(appBarLayout, State.COLLAPSED)
                }
                State.COLLAPSED
            } else {
                if (mCurrentState != State.IDLE) {
                    onStateChanged(appBarLayout, State.IDLE)
                }
                State.IDLE
            }
        }

        abstract fun onStateChanged(appBarLayout: AppBarLayout?, state: State?)
    }

    private fun openLinkInWebBrowser() {
        // Example
        // https://www.flickr.com/photos/kappo-moriyoshi/albums/72157710173102036
        // https://www.flickr.com/groups/2343438@N24/

        val url = "$FLICKR_GROUPS_LINK${mGroup.nsid}"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(url), "text/html")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Snackbar.make(binding.root, R.string.internet_connection_error, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorWhite, null))
                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                .show()
//            Toast.makeText(requireContext(), R.string.internet_connection_error, Toast.LENGTH_SHORT)
//                .show()
        }
    }
}