package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.FollowingsListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleviewToolbarBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.adapters.PhotoDataPagingAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"
private const val MENU_SHOW = "menu_show"

class FollowingsListFragment : MenuFragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleviewToolbarBinding? = null
    private val binding get() = _binding!!
    private var mAdapter = PhotoPagingAdapter()
    private val viewModel: GalleryItemViewModel by activityViewModels()
    lateinit var query: Query
    private var menuShow = false

    companion object {
        fun newInstance(menuShow: Boolean = false) = FollowingsListFragment().apply {
            val args = Bundle().apply {
                putBoolean(MENU_SHOW, menuShow)
            }
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuShow = arguments?.getBoolean(MENU_SHOW) ?: false
        query = Query(
            type = QueryTypes.USER_FOLLOWINGS_PHOTOS,
            oauthToken = AppPreferences.getOauthToken(requireContext())!!,
            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext())!!
        )
    }

    interface CallBacks {
        fun onGalleryItemClick(query: Query)
        fun onRefresh(visibility: Int)
        fun onUserIconClick(person: Person)
        fun onCommentIconClick(photoId: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isCommentsQuantityChanged) {
            mAdapter.refresh()
            viewModel.isCommentsQuantityChanged = false
        }
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
        _binding = RecycleviewToolbarBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setColors()
        binding.swipeRefreshLayout.setOnRefreshListener {
            mAdapter.refresh()
            binding.swipeRefreshLayout.isRefreshing = false
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1), menuShow)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
//            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            binding.toolbar.title = getString(R.string.personal_page_followings)
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
                        // ПРИ ПЕРВОЙ ЗАГРУЗКЕ
                        val error = (loadStates.refresh as LoadState.Error).error
                        binding.apply {
                            spinKit.visibility = View.INVISIBLE
                            recyclerView.apply {
                                visibility = View.VISIBLE
//                                layoutManager = LinearLayoutManager(context)
                                adapter = ErrorAdapter(error as FlickrException) {
                                    mAdapter.refresh()
                                    swipeRefreshLayout.isRefreshing = false
//                                    layoutManager = GridLayoutManager(
//                                        context,
//                                        AppPreferences.getGalleryViewColumns(requireContext())
//                                    )
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
            viewModel.getFlow(query).collectLatest(mAdapter::submitData)
        }
    }

    private inner class FollowingsItemHolder(view: View)
        : PhotoDataPagingAdapter.GalleryHolder(view) {

        val holderBinding = FollowingsListItemBinding.bind(view)
        lateinit var item: Photo

        override fun bind(item: Photo) {
            this.item = item

            Glide.with(itemView)
                .load(item.getOwnerIcon())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.no_logo_account)
                .into(holderBinding.ownerIcon)

            Glide.with(itemView)
                .load(item.getPhotoUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_outline_broken_image_24)
                .into(holderBinding.image)

            holderBinding.apply {
                image.setOnClickListener {
                    viewModel.markerPosition = layoutPosition
                    mCallBacks?.onGalleryItemClick(query)
                }
                fun callPerson () {
                    val person = Person(
                        id = item.owner,
                        userName = item.ownername,
                        realName = item.realname,
                        iconFarm = item.iconfarm,
                        iconServer = item.iconserver
                    )
                    mCallBacks?.onUserIconClick(person)
                }
                ownerIcon.setOnClickListener { callPerson() }
                ownerName.setOnClickListener { callPerson() }
                ownerName.text = item.getOwnerName()
                commentsQuantityText.text = item.countComments
                commentsQuantityText.setOnClickListener {
                    mCallBacks?.onCommentIconClick(item.id)
                }
                imageTitle.text = item.title

                favQuantityText.apply {
                    fun setFavoriteIconAndCount () {
                        text = item.countFaves
                        if (item.isFavorite) {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_star_followings_filled, 0, 0, 0)
                        } else {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_star_followings, 0, 0, 0)
                        }
                    }
                    setFavoriteIconAndCount()
                    setOnClickListener {
                        val oauthToken = AppPreferences.getOauthToken(requireContext()) ?: ""
                        val query = Query(
                            type = QueryTypes.ADD_FAVORITE_PHOTO,
                            id = item.id,
                            oauthToken = oauthToken,
                            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext())!!
                        )
                        lifecycleScope.launch {
                            mCallBacks?.onRefresh(View.VISIBLE)
                            val response = when (item.isFavorite) {
                                true -> viewModel.removeFavoritePhoto(query)
                                false -> viewModel.addFavoritePhoto(query)
                            }
                            if (response.stat == RESPONSE_DATA_FAIL) {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.internet_connection_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                item.isFavorite = !item.isFavorite
                                item.countFaves = when (item.isFavorite) {
                                    true -> (item.countFaves.toInt() + 1).toString()
                                    false -> (item.countFaves.toInt() - 1).toString()
                                }
                                setFavoriteIconAndCount()
                            }
                            mCallBacks?.onRefresh(View.INVISIBLE)
                        }
                    }
                }

            }
        }

    }

    private inner class PhotoPagingAdapter : PhotoDataPagingAdapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
            val view = layoutInflater.inflate(R.layout.followings_list_item, parent, false)
            return FollowingsItemHolder(view)
        }
    }
}
