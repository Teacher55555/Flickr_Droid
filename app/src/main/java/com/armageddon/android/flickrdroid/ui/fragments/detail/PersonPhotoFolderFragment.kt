package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.FragmentPersonPhotoFolderBinding
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

private const val PERSON_COMPONENT_BUNDLE = "component_bundle"
private const val QUERY_TYPE_BUNDLE = "query_type"
private const val OFFSET_OLD_MIN = 0f
private const val OFFSET_OLD_MAX = -150f
private const val OFFSET_NEW_MIN = 1f
private const val OFFSET_NEW_MAX = 0f
private const val OFFSET_OLD_RANGE = OFFSET_OLD_MAX - OFFSET_OLD_MIN
private const val OFFSET_NEW_RANGE = OFFSET_NEW_MAX - OFFSET_NEW_MIN
private const val FLICKR_LINK = "https://www.flickr.com/photos/"
private const val GALLERIES_PREFIX = "/galleries/"
private const val ALBUMS_PREFIX = "/albums/"
private const val FLICKR_GROUPS_LINK = "https://www.flickr.com/groups/"
private const val TEXT_FRAGMENT_BUNDLE = "text_fragment"




class PersonPhotoFolderFragment : MenuFragment(), Converter {
    private var _binding: FragmentPersonPhotoFolderBinding? = null
    private val binding get() = _binding!!
    private val photoViewModel: GalleryItemViewModel by activityViewModels()
    private val commonViewModel: CommonViewModel by activityViewModels()
    private val offScreenPageLimit = 2
    private lateinit var query: Query
    private lateinit var mPersonPhotoFolder: PersonPhotoFolder

    companion object {
        fun newInstance(
            personPhotoFolder: PersonPhotoFolder,
            query: Query
        ): PersonPhotoFolderFragment {
            val args = Bundle()
            val fragment = PersonPhotoFolderFragment()
            args.putSerializable(PERSON_COMPONENT_BUNDLE, personPhotoFolder)
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
        mPersonPhotoFolder = arguments?.getSerializable(PERSON_COMPONENT_BUNDLE) as PersonPhotoFolder
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonPhotoFolderBinding.inflate(layoutInflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewPager.adapter = PersonComponentAdapter()
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
                .load(mPersonPhotoFolder.coverUrl())
                .error(errorDrawable)
                .centerCrop()
                .into(itemIcon)
            itemIcon.setOnClickListener { openLinkInWebBrowser() }
            componentTitle.text = mPersonPhotoFolder.title()
            countPhotos.text = mPersonPhotoFolder.countPhotos()
            countViews.text = mPersonPhotoFolder.countViews()
            setTitleTextColors()

        }
    }

    private inner class PersonComponentAdapter : FragmentStateAdapter(this) {
        val description = mPersonPhotoFolder.createDate()?.addCreatedText() +
                "\n\n${mPersonPhotoFolder.description()}"
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PhotoListFragment.newInstance(query)
                1 -> TextFieldFragment.newInstance(description)
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
                    componentTitle.alpha = offsetNewValue
                    countPhotos.alpha = offsetNewValue
                    countViews.alpha = offsetNewValue
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
            } else if (abs(i) >= appBarLayout.totalScrollRange) {
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

        val userId = mPersonPhotoFolder.idOwner()
        val idComponent = mPersonPhotoFolder.id()
        val url = when (query.type) {
            QueryTypes.ALBUM_PHOTOS -> "$FLICKR_LINK$userId$ALBUMS_PREFIX$idComponent"
            QueryTypes.GALLERY_PHOTOS -> "$FLICKR_LINK$userId$GALLERIES_PREFIX$idComponent"
            else -> ""
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(url), "text/html")
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
//            Toast.makeText(requireContext(), R.string.internet_connection_error, Toast.LENGTH_SHORT)
//                .show()
            Snackbar.make(binding.root, R.string.internet_connection_error, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorWhite, null))
                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                .show()
        }
    }
}

class TextFieldFragment : Fragment(), Converter {
    private var text = ""

    companion object {
        fun newInstance(text: String): TextFieldFragment {
            val args = Bundle()
            val fragment = TextFieldFragment()
            args.putString(TEXT_FRAGMENT_BUNDLE, text)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        text = arguments?.getString(TEXT_FRAGMENT_BUNDLE) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = layoutInflater.inflate(R.layout.texfield_desciption, container, false)
        val textView = view.findViewById<TextView>(R.id.description)
        textView.text = text.spanned()
        return view
    }
}