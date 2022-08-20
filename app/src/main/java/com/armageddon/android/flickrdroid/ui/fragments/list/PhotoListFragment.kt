package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.PhotoListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleViewBinding
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.adapters.PhotoDataPagingAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"

class PhotoListFragment : Fragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleViewBinding? = null
    private val binding get() = _binding!!
    private var mAdapter = PhotoPagingAdapter()
    private val viewModel: GalleryItemViewModel by activityViewModels ()
    lateinit var query: Query
    private var isRestored = false

    companion object {
    fun newInstance(query: Query) = PhotoListFragment().apply {
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


    val refreshUI = {
        mAdapter.refresh()
        binding.swipeRefreshLayout.isRefreshing = false
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(
                context, AppPreferences.
                getGalleryViewColumns(requireContext()))
            adapter = mAdapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = RecycleViewBinding.inflate(inflater, container, false)
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState!= null) {
            isRestored = true
        }
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
                        // ПРИ ПЕРВОЙ ЗАГРУЗКЕ
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
                                        AppPreferences.getGalleryViewColumns(requireContext()))
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
            viewModel.getFlow(query).collectLatest(mAdapter::submitData)
        }
    }

    private inner class GalleryItemHolder(view: View)
        : PhotoDataPagingAdapter.GalleryHolder(view), View.OnClickListener {
        val holderBinding = PhotoListItemBinding.bind(view)
        val density = itemView.context.resources.displayMetrics.density
        val columns = AppPreferences.getGalleryViewColumns(itemView.context)
        lateinit var item: Photo

        init {
            holderBinding.frameLayout.layoutParams = pxToDp(holderBinding.frameLayout, density, columns)
            itemView.setOnClickListener(this@GalleryItemHolder)
        }

        override fun bind(item: Photo) {
            this.item = item

            Glide.with(itemView)
                .load(item.getPhotoUrl(LogoIcon.Photo.MEDIUM_640.prefix))
                .placeholder(R.drawable.placeholder)
                .into(holderBinding.photoView)
        }

        override fun onClick(v: View?) {
            viewModel.markerPosition = layoutPosition
            mCallBacks?.onGalleryItemClick(query)
        }
        }

    private inner class PhotoPagingAdapter : PhotoDataPagingAdapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
            val view = layoutInflater.inflate(R.layout.photo_list_item, parent, false)
            return GalleryItemHolder(view)
        }
    }
}