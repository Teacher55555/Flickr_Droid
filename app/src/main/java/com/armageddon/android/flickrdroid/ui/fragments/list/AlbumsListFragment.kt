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
import com.armageddon.android.flickrdroid.databinding.AlbumListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleViewBinding
import com.armageddon.android.flickrdroid.model.Album
import com.armageddon.android.flickrdroid.model.PersonPhotoFolder
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"

class AlbumsListFragment : Fragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleViewBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = AlbumsListAdapter()
    private val viewModel: CommonViewModel by activityViewModels()
    lateinit var query: Query

    companion object {
        fun newInstance(query: Query) = AlbumsListFragment().apply {
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
        fun onItemClick(query: Query, personPhotoFolder: PersonPhotoFolder)
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
                        // At the first launch
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
            viewModel.getAlbumsFlow(query).collectLatest(mAdapter::submitData)
        }
    }

    private inner class AlbumHolder (view: View) : RecyclerView.ViewHolder (view), View.OnClickListener {
        val bindingHolder = AlbumListItemBinding.bind(view)
        private lateinit var mAlbum : PersonPhotoFolder

        init {
            view.setOnClickListener(this)
        }

        fun bind(album: Album) {
            mAlbum = album

            Glide.with(itemView)
                .load(album.coverUrl)
                .error(R.drawable.logo)
                .into(bindingHolder.albumIcon)

            bindingHolder.apply {
                albumName.text = album.title
                albumPhotoCount.text = album.photosCount

                when (album.dateCreate.isNullOrBlank()) {
                    true -> {
                        albumViewsLayout.visibility = View.GONE
                        albumDateLayout.visibility = View.GONE
                    }
                    false -> {
                        albumViewsLayout.visibility = View.VISIBLE
                        albumDateLayout.visibility = View.VISIBLE
                        albumViewsCount.text = album.viewsCount
                        albumDate.text = album.dateCreate
                    }
                }
            }
        }

        override fun onClick(v: View?) {
            val query = Query(
                type = QueryTypes.ALBUM_PHOTOS,
                id = mAlbum.id()
            )
            mCallBacks?.onItemClick(query, mAlbum)
        }
    }


    private inner class AlbumsListAdapter
        : PagingDataAdapter<Album, AlbumHolder> (object : DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
           return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }
    } ) {
        override fun onBindViewHolder(holder: AlbumHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumHolder {
            val view = layoutInflater.inflate(R.layout.album_list_item, parent, false)
            return AlbumHolder(view)
        }
    }
}