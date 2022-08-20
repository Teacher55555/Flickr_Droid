package com.armageddon.android.flickrdroid.ui.fragments.list

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthToken
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthTokenSecret
import com.armageddon.android.flickrdroid.databinding.CameraRollListItemBinding
import com.armageddon.android.flickrdroid.databinding.FragmentCameraRollBinding
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_NO_INTERNET_CONNECTION
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_TIMEOUT
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.adapters.PhotoDataPagingAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"
private const val SELECT_MODE = "select_mode"

class CameraRollFragment : MenuFragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: FragmentCameraRollBinding? = null
    private val binding get() = _binding!!
    private var mAdapter = PhotoPagingAdapter()
    private val viewModel: GalleryItemViewModel by activityViewModels()
    lateinit var mQuery: Query
    private var mAddButton: MenuItem? = null
    private var mSelectButton: MenuItem? = null
    var mSelectMode = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
            R.id.app_bar_menu_button -> {
                if (binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED)
                mDrawerLayout.openDrawer(GravityCompat.END, true)
            }
            R.id.app_bar_select_button -> onItemMenuSelectClick()
            R.id.app_bar_add_group_album -> onAddGroupAlbum()
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.camera_roll_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
        mAddButton = menu.findItem(R.id.app_bar_add_group_album)
        mSelectButton = menu.findItem(R.id.app_bar_select_button)
        if(mSelectMode) {
            mSelectButton?.isVisible = false
            mAddButton?.isVisible = true
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SELECT_MODE, mSelectMode)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
        mSelectMode = savedInstanceState?.getBoolean(SELECT_MODE) ?: false
        binding.toolbar.title = when (mSelectMode) {
            true -> {
                (activity as AppCompatActivity).apply {
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }

                val itemsSelected = viewModel.selectedItems
                    .values
                    .toList()
                    .filter { it.isNotBlank() }
                    .size
                getString(R.string.photos_selected, itemsSelected)
            }
            false -> {
                getString(R.string.user_camera_roll)
            }
        }
    }

    fun addPhotosToAlbum (albumID: String) {
        val photoList = viewModel.selectedItems.values.toList().filter { it.isNotBlank() }
        photoList.forEach { id ->
            lifecycleScope.launch {
                val query = Query(
                    id = id,
                    text = albumID,
                    oauthToken = mQuery.oauthToken,
                    oauthTokenSecret = mQuery.oauthTokenSecret
                )
                val response = viewModel.addAlbumPhoto(query)
                if (response.stat == RESPONSE_NO_INTERNET_CONNECTION ||
                        response.stat == RESPONSE_TIMEOUT) {
                    Snackbar.make(binding.root, R.string.photos_not_added_to_album, Snackbar.LENGTH_LONG)
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                        .show()
                    returnTransition
                }
            }
        }

        Snackbar.make(binding.root, R.string.photos_added_to_album, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(R.color.colorWhite, null))
            .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
            .show()
        onMenuDesectedMode()
    }

    fun addPhotosToGroup (groupID: String) {
        val photoList = viewModel.selectedItems.values.toList().filter { it.isNotBlank() }
        photoList.forEach { id ->
            lifecycleScope.launch {
                val query = Query(
                    id = id,
                    text = groupID,
                    oauthToken = mQuery.oauthToken,
                    oauthTokenSecret = mQuery.oauthTokenSecret
                )
                val response = viewModel.addGroupPhoto(query)
                if (response.stat == RESPONSE_NO_INTERNET_CONNECTION ||
                    response.stat == RESPONSE_TIMEOUT) {
                    Snackbar.make(binding.root, R.string.photos_not_added_to_group_internet, Snackbar.LENGTH_LONG)
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                        .show()
                    returnTransition
                }
            }
        }

        Snackbar.make(binding.root, R.string.photos_added_to_group, Snackbar.LENGTH_SHORT)
            .setTextColor(resources.getColor(R.color.colorWhite, null))
            .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
            .show()
        onMenuDesectedMode()
    }

    private fun expandedPanelCheck() : Boolean {
        return binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED
    }

    private inner class GroupAlbumListAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 2
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val query = Query(
                        type = QueryTypes.ALBUM,
                        id = mQuery.id
                    )
                    AlbumsListFragment.newInstance(query)
                }
                1 -> {
                    val query = Query(
                        type = QueryTypes.USER_GROUP_CAN_ADD_PHOTOS,
                        oauthToken = mQuery.oauthToken,
                        oauthTokenSecret = mQuery.oauthTokenSecret
                    )
                    GroupsListFragment.newInstance(query)
                }
                else -> Fragment()
            }
        }
    }

    private fun setTabLayoutMediator(tabLayout: TabLayout, viewPager: ViewPager2) {
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.user_albums)
                1 -> getString(R.string.user_groups)
                else -> ""
            }
        }.attach()
    }

    fun onAddGroupAlbum () {
        val photoList = viewModel.selectedItems.values.toList().filter { it.isNotBlank() }
        if (photoList.isEmpty()) {
            Snackbar.make(binding.root, R.string.no_selected_photo_for_add_group_album, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorBlack, null))
                .setBackgroundTint(resources.getColor(R.color.colorLightGrey, null))
                .show()
            return
        }
        if (expandedPanelCheck()) return
        binding.apply {
            slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
            viewPager.adapter = GroupAlbumListAdapter()
            viewPager.offscreenPageLimit = 2
            setTabLayoutMediator(tabLayout, viewPager)
            panelClose.setOnClickListener {
                slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }

            slidingLayout.addPanelSlideListener(object : SlidingUpPanelLayout.PanelSlideListener{
                override fun onPanelSlide(panel: View?, slideOffset: Float) {

                }

                override fun onPanelStateChanged(
                    panel: View?,
                    previousState: SlidingUpPanelLayout.PanelState?,
                    newState: SlidingUpPanelLayout.PanelState?
                ) {
                    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        slidingLayout.isTouchEnabled = false

                    }
                }
            })
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun onMenuDesectedMode() {
         if (binding.slidingLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED) {
             binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
         }
        (activity as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = getString(R.string.user_camera_roll)
        }
        viewModel.selectedItems.clear()
        mSelectMode = false
        mAdapter.notifyDataSetChanged()
        activity?.invalidateOptionsMenu()
    }



    @SuppressLint("NotifyDataSetChanged")
    fun onItemMenuSelectClick() {
        mSelectButton?.isVisible = false
        mAddButton?.isVisible = true
        (activity as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            val selectedPhotosCount = viewModel.selectedItems.filterValues { true }.size
            binding.toolbar.title = getString(R.string.photos_selected, selectedPhotosCount)

        }
        mSelectMode = true
        mAdapter.notifyDataSetChanged()
    }

    companion object {
        fun newInstance(): CameraRollFragment {
            return CameraRollFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuery = Query(
            type = QueryTypes.CAMERA_ROLL,
            id = AppPreferences.getUserId(requireContext())!!,
            oauthToken = getOauthToken(requireContext())!!,
            oauthTokenSecret = getOauthTokenSecret(requireContext())!!
        )
    }

    interface CallBacks {
        fun onGalleryItemClick(query: Query)
        fun onRefresh(visibility: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
    }

    override fun onDetach() {
        super.onDetach()
        mCallBacks = null
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCameraRollBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setColors()
        binding.swipeRefreshLayout.setOnRefreshListener {
            mAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(
                context, AppPreferences.
                getGalleryViewColumns(inflater.context))
            adapter = mAdapter
        }
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(mSelectMode)
            binding.toolbar.title = getString(R.string.user_camera_roll)
        }
        binding.toolbar.title = when (mSelectMode) {
            true -> {
                val itemsSelected = viewModel.selectedItems
                    .values
                    .toList()
                    .filter { it.isNotBlank() }
                    .size
                getString(R.string.photos_selected, itemsSelected)
            }
            false -> {
                getString(R.string.user_camera_roll)
            }
        }

        lifecycleScope.launch {
            mAdapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.source.refresh) {
                    is LoadState.Loading -> {
                        binding.spinKit.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.INVISIBLE
                    }
                    is LoadState.NotLoading -> {
                        binding.spinKit.visibility = View.INVISIBLE
                        binding.recyclerView.visibility = View.VISIBLE
                    }
                    is LoadState.Error -> {
                        // at the first launch
                        val error = (loadStates.refresh as LoadState.Error).error
                        binding.apply {
                            spinKit.visibility = View.INVISIBLE
                            recyclerView.apply {
                                visibility = View.VISIBLE
                                layoutManager = LinearLayoutManager(context)
                                adapter = ErrorAdapter(error as FlickrException) {
                                    mAdapter.refresh()
                                    swipeRefreshLayout.isRefreshing = false
                                    layoutManager = GridLayoutManager(
                                        context,
                                        AppPreferences.getGalleryViewColumns(requireContext())
                                    )
                                    adapter = mAdapter
                                }
                            }
                        }
                    }
                }

                when (loadStates.append) {
                    is LoadState.Loading -> mCallBacks?.onRefresh(View.VISIBLE)
                    is LoadState.NotLoading -> mCallBacks?.onRefresh(View.INVISIBLE)
                    is LoadState.Error -> {
                        val error = (loadStates.append as LoadState.Error).error
                        val message = (error as FlickrException).getTextMessage()
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                            .setTextColor(resources.getColor(R.color.colorWhite, null))
                            .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                            .show()
                        mCallBacks?.onRefresh(View.INVISIBLE)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.getFlow(mQuery).collectLatest(mAdapter::submitData)
        }
    }

    private inner class GalleryItemHolder(view: View)
        : PhotoDataPagingAdapter.GalleryHolder(view), View.OnClickListener {
        val holderBinding = CameraRollListItemBinding.bind(view)
        val density = itemView.context.resources.displayMetrics.density
        val columns = AppPreferences.getGalleryViewColumns(itemView.context)
        lateinit var item: Photo

        private fun setPrivacyImage(statusImage: ImageView, privacy: Int) {
            when (privacy) {
                0 -> statusImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.photo_status_public
                    )
                )
                1 -> statusImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.photo_status_lock
                    )
                )
                2 -> statusImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.photo_status_friends
                    )
                )
                3 -> statusImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.photo_status_family
                    )
                )
                4 -> statusImage.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.photo_status_friends_and_family
                    )
                )
            }
        }

        init {
            holderBinding.frameLayout.layoutParams = pxToDp(holderBinding.frameLayout, density, columns)
            itemView.setOnClickListener(this@GalleryItemHolder)
        }

        inner class PrivacyAdapter : ArrayAdapter<String>(
            requireContext(),
            R.layout.checkbox_list_item_photo_privacy,
            listOf(
                getString(R.string.photo_privacy_public),
                getString(R.string.photo_privacy_private),
                getString(R.string.photo_privacy_friends),
                getString(R.string.photo_privacy_family),
                getString(R.string.photo_privacy_family_friends)
            )
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val checkedTextView = view.findViewById<CheckedTextView>(android.R.id.text1)
                val arabicLocale = checkedTextView
                    .textLocale
                    .language
                    .equals("ar")
                when(position) {
                    0 -> checkedTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.photo_status_public.takeIf { !arabicLocale } ?: 0,
                            0,
                            R.drawable.photo_status_public.takeIf { arabicLocale } ?: 0,
                            0)
                    1 -> checkedTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.photo_status_lock.takeIf { !arabicLocale } ?: 0,
                            0,
                            R.drawable.photo_status_lock.takeIf { arabicLocale } ?: 0,
                            0)
                    2 -> checkedTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.photo_status_friends.takeIf { !arabicLocale } ?: 0,
                            0,
                            R.drawable.photo_status_friends.takeIf { arabicLocale } ?: 0,
                            0)
                    3 -> checkedTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.photo_status_family.takeIf { !arabicLocale } ?: 0,
                            0,
                            R.drawable.photo_status_family.takeIf { arabicLocale } ?: 0,
                            0)
                    4 -> checkedTextView
                        .setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.photo_status_friends_and_family.takeIf { !arabicLocale } ?: 0,
                            0,
                            R.drawable.photo_status_friends_and_family.takeIf { arabicLocale } ?: 0,
                            0)
                }
                return view
            }
            }

        @SuppressLint("InflateParams")
        override fun bind(item: Photo) {
            this.item = item
            val position = this.absoluteAdapterPosition
            if (mSelectMode) {
                holderBinding.photoSelectShape.apply {
                    if (!viewModel.selectedItems[position].isNullOrBlank()) {
                        setImageResource(R.drawable.ic_round_check_circle_24)
                        setBackgroundResource(R.drawable.selected_photo_background)
                    }
                    else {
                        setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
                        setBackgroundResource(0)
                    }
                    visibility = View.VISIBLE
                }
            } else {
                holderBinding.photoSelectShape.visibility = View.INVISIBLE
            }

            Glide.with(itemView)
                .load(item.getPhotoUrl(LogoIcon.Photo.MEDIUM_640.prefix))
                .placeholder(R.drawable.placeholder)
                .into(holderBinding.photoView)


            holderBinding.photoStatus.apply {
                setPrivacyImage(this, item.privacy)
                setOnClickListener {
                    if (mSelectMode) {
                        return@setOnClickListener
                    }
                    val dialogView = layoutInflater.inflate(R.layout.list, null) as ListView
                    val dialog = AlertDialog.Builder(requireContext())
                        .setView(dialogView)
                        .create()

                    dialogView.apply {
                        adapter = PrivacyAdapter()
                        choiceMode = ListView.CHOICE_MODE_SINGLE
                        setItemChecked(item.privacy, true)
                        setOnItemClickListener { parent, view, position, id ->
                            lifecycleScope.launch {
                                holderBinding.privacyProgress.visibility = View.VISIBLE
                                val query = Query(
                                    oauthToken = getOauthToken(requireContext())!!,
                                    oauthTokenSecret = getOauthTokenSecret(requireContext())!!,
                                    id = item.id,
                                    photoPrivacy = PhotoPrivacy.values()[position]
                                )
                                val response = viewModel.setPhotoPrivacy(query)
                                if (response.stat == RESPONSE_DATA_FAIL) {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.internet_connection_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    item.privacy = PhotoPrivacy.values()[position].state
                                    setPrivacyImage(holderBinding.photoStatus, item.privacy)
                                }
                                holderBinding.privacyProgress.visibility = View.GONE
//                                mCallBacks?.onRefresh(View.INVISIBLE)
                            }
                            dialog.cancel()
                        }
                    }
                    dialog.show()
                }
            }
        }


        override fun onClick(v: View?) {
            if (expandedPanelCheck()) return
            when (mSelectMode) {
                true -> {
                    val position = this.absoluteAdapterPosition
                    holderBinding.photoSelectShape.apply {
                        if (!viewModel.selectedItems[position].isNullOrBlank()) {
                            setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
                            setBackgroundResource(0)
                            viewModel.selectedItems[position] = ""
                        } else {
                            setImageResource(R.drawable.ic_round_check_circle_24)
                            setBackgroundResource(R.drawable.selected_photo_background)
                            viewModel.selectedItems[position] = item.id
                        }
                        val itemsSelected = viewModel.selectedItems
                            .values
                            .toList()
                            .filter { it.isNotBlank() }
                            .size
                        binding.toolbar.title = getString(R.string.photos_selected, itemsSelected)
                    }
                }
                false -> {
                    viewModel.markerPosition = layoutPosition
                    mCallBacks?.onGalleryItemClick(mQuery)
                }
            }

        }
    }

    private inner class PhotoPagingAdapter : PhotoDataPagingAdapter() {
        override fun onViewRecycled(holder: GalleryHolder) {
            super.onViewRecycled(holder)
            if (mSelectMode) {
                val holderBinding = CameraRollListItemBinding.bind(holder.itemView)
                val position = holder.absoluteAdapterPosition
                holderBinding.photoSelectShape.apply {
                    if (!viewModel.selectedItems[position].isNullOrBlank()) {
                        setImageResource(R.drawable.ic_round_check_circle_24)
                        setBackgroundResource(R.drawable.selected_photo_background)
                    } else {
                        setImageResource(R.drawable.ic_baseline_radio_button_unchecked_24)
                        setBackgroundResource(0)
                    }
                    visibility = View.VISIBLE

                }
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
            val view = layoutInflater.inflate(R.layout.camera_roll_list_item, parent, false)
            return GalleryItemHolder(view)
        }
    }
}
