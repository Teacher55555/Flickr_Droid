package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.GalleryListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleViewBinding
import com.armageddon.android.flickrdroid.model.Gallery
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"

class GalleryListFragment: Fragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleViewBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = GalleryListAdapter()
    private val viewModel: CommonViewModel by activityViewModels()
    lateinit var query: Query

    companion object {
        fun newInstance(query: Query) = GalleryListFragment().apply {
            val args = Bundle().apply {
                putSerializable(QUERY_TYPE, query)
            }
            arguments = args
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = (arguments?.getSerializable(QUERY_TYPE) as Query).apply {
            oauthToken = AppPreferences.getOauthToken(requireContext()) ?: ""
            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext()) ?: ""
        }
    }

    interface CallBacks {
        fun onItemClick(query: Query, personPhotoHolder: PersonPhotoFolder)
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
        _binding = RecycleViewBinding.inflate(inflater, container, false)
        binding.swipeRefreshLayout.setColors()
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            mAdapter.refresh()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                        val error = (loadStates.refresh as LoadState.Error).error
                        binding.apply {
                            spinKit.visibility = View.INVISIBLE
                            recyclerView.apply {
                                visibility = View.VISIBLE
                                adapter = ErrorAdapter(error as FlickrException) {
                                    mAdapter.refresh()
                                    swipeRefreshLayout.isRefreshing = false
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


        lifecycleScope.launch{
            viewModel.getGalleryFlow(query).collectLatest(mAdapter::submitData)
        }
    }

    private inner class GalleryListAdapter
        : PagingDataAdapter<Gallery, GalleryListAdapter.GalleryItemHolder>(object
        : DiffUtil.ItemCallback<Gallery>() {
        override fun areItemsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Gallery, newItem: Gallery): Boolean {
            return oldItem == newItem
        }
    }) {

        override fun onBindViewHolder(holder: GalleryItemHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.gallery_list_item, parent, false)
            return GalleryItemHolder(view)
        }


        inner class GalleryItemHolder (view: View)
            : RecyclerView.ViewHolder (view), View.OnClickListener {
            val binding = GalleryListItemBinding.bind(view)
            private lateinit var mGallery : Gallery

            init {
                itemView.setOnClickListener(this)
            }

            fun bind (gallery: Gallery) {
                mGallery = gallery

                Glide.with(itemView)
                    .load(gallery.coverUrlList?.getOrNull(0))
                    .error(R.drawable.logo)
                    .into(binding.galleryIcon1)

                Glide.with(itemView)
                    .load(gallery.coverUrlList?.getOrNull(1))
                    .into(binding.galleryIcon2)

                Glide.with(itemView)
                    .load(gallery.coverUrlList?.getOrNull(2))
                    .into(binding.galleryIcon3)

                binding.apply {
                    galleryName.text = gallery.title
                    galleryPhotoCount.text = gallery.photosCount
                    galleryViewsCount.text = gallery.viewsCount
                    galleryCommentsCount.text = gallery.commentsCount
                    galleryDate.text = gallery.dateCreate
                }
            }

            override fun onClick(v: View?) {
                val query = Query(
                    type = QueryTypes.GALLERY_PHOTOS,
                    id = mGallery.id
                )
                mCallBacks?.onItemClick(query, mGallery)
            }

        }
    }
}