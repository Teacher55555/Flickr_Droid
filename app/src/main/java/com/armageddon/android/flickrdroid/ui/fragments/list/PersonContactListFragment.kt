package com.armageddon.android.flickrdroid.ui.fragments.list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.ContactListItemBinding
import com.armageddon.android.flickrdroid.databinding.RecycleviewWithSlidemenuBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.model.PersonContact
import com.armageddon.android.flickrdroid.network.execptions.FlickrException
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "query_type"
private const val BACK_ARROW = "back_arrow"

open class PersonContactListFragment : MenuFragment() {
    private var mCallBacks: CallBacks? = null
    private var _binding: RecycleviewWithSlidemenuBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = PersonContactAdapter()
    private val viewModel: CommonViewModel by activityViewModels()
    lateinit var query: Query
    private var showBackArrow = true

    companion object {
        fun newInstance(query: Query, showBackArrow: Boolean = true)  = PersonContactListFragment().apply {
            val args = Bundle().apply {
                putSerializable(QUERY_TYPE, query)
                putBoolean(BACK_ARROW, showBackArrow)
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
        showBackArrow = arguments?.getBoolean(BACK_ARROW) ?: true
    }

    interface CallBacks {
        fun onPersonClick(person: Person)
//        fun onPersonClick(queryType: QueryTypes)
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
        _binding = RecycleviewWithSlidemenuBinding.inflate(inflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        binding.toolbar.title = getString(R.string.contacts)
        binding.swipeRefreshLayout.setColors()
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            mAdapter.refresh()
        }
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        return binding.root
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!showBackArrow) {
            (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)
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
                                layoutManager = LinearLayoutManager(context)
                                adapter = ErrorAdapter(error as FlickrException) {
                                    mAdapter.refresh()
                                    swipeRefreshLayout.isRefreshing = false
                                    layoutManager = GridLayoutManager(context, 3)
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
            viewModel.getPersonContactsFlow(query).collectLatest(mAdapter::submitData)
        }
    }

    private inner class PersonContactAdapter
        : PagingDataAdapter<PersonContact, PersonContactAdapter.PersonContactHolder>(object
        : DiffUtil.ItemCallback<PersonContact>() {
        override fun areItemsTheSame(oldItem: PersonContact, newItem: PersonContact): Boolean {
            return oldItem.nsid == newItem.nsid
        }

        override fun areContentsTheSame(oldItem: PersonContact, newItem: PersonContact): Boolean {
            return oldItem == newItem
        }
    }) {

        override fun onBindViewHolder(holder: PersonContactHolder, position: Int) {
            getItem(position)?.let { holder.bind(it) }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonContactHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate(R.layout.contact_list_item, parent, false)
            return PersonContactHolder(view)
        }


        inner class PersonContactHolder (view: View)
            : RecyclerView.ViewHolder (view), View.OnClickListener {
            val binding = ContactListItemBinding.bind(view)
            lateinit var mContact : PersonContact

            init {
                itemView.setOnClickListener(this)
            }

            fun bind (contact: PersonContact) {
                mContact = contact

                Glide.with(itemView)
                    .load(contact.getUrl(LogoIcon.Icon.ICON_NORMAL_100PX.prefix))
                    .error(R.drawable.icon_person_filled)
                    .into(binding.userIcon)

                binding.contactName.text = contact.username
            }

            override fun onClick(v: View?) {
                val person = Person(
                    id = mContact.nsid,
                    userName = mContact.username,
                    iconFarm = mContact.iconfarm,
                    iconServer = mContact.iconserver
                )
                mCallBacks?.onPersonClick(person)
            }

        }
    }
}