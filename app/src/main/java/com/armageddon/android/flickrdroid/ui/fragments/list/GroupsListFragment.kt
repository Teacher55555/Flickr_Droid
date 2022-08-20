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
import com.armageddon.android.flickrdroid.databinding.GroupListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleViewBinding
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"

class GroupsListFragment: Fragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleViewBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = GroupListAdapter()
    private val viewModel: CommonViewModel by activityViewModels()
    lateinit var query: Query

    companion object {
        fun newInstance(query: Query) = GroupsListFragment().apply {
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
        fun onGroupClick(query: Query, group: Group)
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
            viewModel.getGroupFlow(query).collectLatest(mAdapter::submitData)
        }
    }

   private inner class GroupListAdapter
        : PagingDataAdapter<Group, GroupListAdapter.GroupItemHolder>(object
        : DiffUtil.ItemCallback<Group>() {
        override fun areItemsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem.nsid == newItem.nsid
        }

        override fun areContentsTheSame(oldItem: Group, newItem: Group): Boolean {
            return oldItem == newItem
        }
    }) {

        override fun onBindViewHolder(holder: GroupItemHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupItemHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.group_list_item, parent, false)
            return GroupItemHolder(view)
        }


        inner class GroupItemHolder (view: View)
            : RecyclerView.ViewHolder (view), View.OnClickListener {
            val binding = GroupListItemBinding.bind(view)
            lateinit var mGroup: Group

            init {
                itemView.setOnClickListener(this)
            }

            fun bind (group: Group) {
                mGroup = group

                Glide.with(itemView)
                    .load(group.getGroupIcon())
                    .error(R.drawable.logo)
                    .into(binding.groupIcon)

                binding.apply {
                    groupName.text = group.name
                    groupMembersCount.text = group.members
                    groupPoolCount.text = group.photosCount

                    when(group.topicsCount.isNullOrBlank()) {
                        true -> groupTopicsLayout.visibility = View.GONE
                        false -> groupTopicCount.text = group.topicsCount
                    }

                    when (group.privacy.takeIf { !it.isNullOrBlank() }?.toInt()) {
                        GroupPrivacy.PRIVATE.privacy -> setGroupStatus(
                            R.string.group_list_private,
                            R.color.colorPinkFlickr)
                        GroupPrivacy.PRIVACY_INVITE.privacy -> setGroupStatus(
                            R.string.group_list_invite,
                            R.color.colorBlueFlickr)
                        GroupPrivacy.PRIVACY_PUBLIC.privacy -> setGroupStatus(
                            R.string.group_list_public,
                            R.color.colorGreen)
                        else -> binding.groupStatus.visibility = View.GONE
                    }
                }
            }

            override fun onClick(v: View?) {
                val query = Query(
                    type = QueryTypes.GROUP_PHOTOS,
                    id = mGroup.nsid
                )
                mCallBacks?.onGroupClick(query, mGroup)
            }

            private fun setGroupStatus(stringId: Int, colorId: Int) {
                binding.groupStatus.apply {
                    text = itemView.context.getString(stringId)
                    setBackgroundColor(itemView.context.getColor(colorId))
                }
            }
        }
    }

    class ErrorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun onBind(message: String?) {
//            binding.errorText.text = message
        }
    }

    inner class ErrorAdapter (private val message: String?) : RecyclerView.Adapter<ErrorViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
            val view = layoutInflater.inflate(R.layout.contact_list_item, parent, false)
            return ErrorViewHolder(view)
        }

        override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
            holder.onBind(message)
        }

        override fun getItemCount() = 1

    }
}