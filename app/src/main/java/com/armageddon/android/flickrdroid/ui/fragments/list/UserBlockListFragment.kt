package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.databinding.BlockedUserListItemBinding
import com.armageddon.android.flickrdroid.databinding.FragmentBlockedUserListBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.network.execptions.NoBlockedUsersException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.BlockListViewModel
import com.bumptech.glide.Glide

class UserBlockListFragment : MenuFragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: FragmentBlockedUserListBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = BlockedUserListAdapter()
    private val viewModel: BlockListViewModel by activityViewModels()
    override fun getToolbar(): Toolbar {
       return binding.toolbar
    }

    companion object {
        fun newInstance() = UserBlockListFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
    }

    override fun onDetach() {
        mCallBacks = null
        super.onDetach()
    }

    interface CallBacks {
        fun onUserIconClick(person: Person)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentBlockedUserListBinding.inflate(inflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        binding.apply {
            toolbar.title = getString(R.string.blocked_list)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        binding.recyclerView.adapter = mAdapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.blockListLiveData.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                binding.recyclerView.adapter = ErrorAdapter (NoBlockedUsersException()) {}
            } else {
                mAdapter.submitList(it)
            }
        }
    }

    private inner class BlockedUserHolder (view: View)
        : RecyclerView.ViewHolder(view), View.OnClickListener {
        val bindingHolder = BlockedUserListItemBinding.bind(view)
        lateinit var mPerson: Person

        init {
            itemView.setOnClickListener (this)
        }

        fun bind (person: Person) {
            mPerson = person
            Glide.with(itemView)
                .load(person.getIconUrl())
                .error(R.drawable.no_logo_account)
                .into(bindingHolder.authorIcon)
            bindingHolder.authorName.text = person.getSimpleName()
            bindingHolder.unlockUserButton.setOnClickListener {
                viewModel.unblockPerson(person.id)
                if (mAdapter.itemCount == 0) {
                    binding.recyclerView.adapter = ErrorAdapter (NoBlockedUsersException()) {}
                }
            }
        }

        override fun onClick(v: View?) {
            mCallBacks?.onUserIconClick(mPerson)
        }
    }

    private inner class BlockedUserListAdapter
        : ListAdapter<Person, BlockedUserHolder>(object : DiffUtil.ItemCallback<Person>() {
        override fun areItemsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Person, newItem: Person): Boolean {
            return oldItem == newItem
        }}
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserHolder {
            val view = layoutInflater.inflate(R.layout.blocked_user_list_item, parent, false)
            return BlockedUserHolder(view)
        }

        override fun onBindViewHolder(holder: BlockedUserHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }
        }

    }
}