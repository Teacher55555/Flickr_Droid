package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.PersonSearchBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.network.execptions.ConnectionException
import com.armageddon.android.flickrdroid.network.execptions.UnclassifiedException
import com.armageddon.android.flickrdroid.network.execptions.UserNotFoundException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "queryType"

class PersonSearchFragment: Fragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: PersonSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommonViewModel by activityViewModels ()
    private lateinit var mQuery: Query

    companion object {
        fun newInstance(query: Query) = PersonSearchFragment().apply {
            val args = Bundle().apply {
                putSerializable(QUERY_TYPE, query)
            }
            arguments = args
        }
    }

    interface CallBacks {
        fun onPersonClick(query: Query, person: Person)
        fun onPersonContactListClick(query: Query)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mQuery = arguments?.get(QUERY_TYPE) as Query
        mQuery = (arguments?.getSerializable(QUERY_TYPE) as Query).apply {
            oauthToken = AppPreferences.getOauthToken(requireContext()) ?: ""
            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext()) ?: ""
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
        mCallBacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PersonSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private suspend fun fetchPerson () {
        val personId = mQuery.id
        binding.apply {
            mainLayout.visibility = View.INVISIBLE
            recyclerView.visibility = View.GONE
            spinKit.visibility = View.VISIBLE
        }


        val response = when(personId.isBlank()) {
            true -> viewModel.fetchUserId(mQuery.text)
            false -> viewModel.getPerson(mQuery.id)
        }

        if (response.stat != RESPONSE_DATA_OK || response.data == null) {
            binding.apply {
                spinKit.visibility = View.INVISIBLE
                recyclerView.visibility = View.VISIBLE
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
            if (response.data == null) {
                binding.recyclerView.adapter = ErrorAdapter(
                    UnclassifiedException(R.string.internet_connection_error)
                ) {
                    viewLifecycleOwner.lifecycleScope.launch { fetchPerson() }
                }
                return
            }
            binding.recyclerView.adapter = when (response.message.isBlank()) {
                true -> ErrorAdapter(ConnectionException()) {
                    viewLifecycleOwner.lifecycleScope.launch { fetchPerson() }
                }
                false -> ErrorAdapter(UserNotFoundException()) {
                    viewLifecycleOwner.lifecycleScope.launch { fetchPerson() }
                }
            }
            return
        }
        val person = response.data!!

        when (personId.isBlank()) {
            true -> {
                Glide.with(this@PersonSearchFragment)
                    .load(person.getIconUrl())
                    .error(R.drawable.icon_user_logout)
                    .into(binding.personIcon)
                binding.personIconLayout.setOnClickListener {
                    val query = Query(
                        type = QueryTypes.PERSON,
                        id = person.id
                    )
                    mCallBacks?.onPersonClick(query, person)
                }
            }
            false -> {
                binding.personNameLayout.visibility = View.GONE
                binding.personIconLayout.visibility = View.GONE
            }
        }

        binding.apply {
            personProFlag.visibility = person.isProVisibility
            personRealName.bindNotBlank(person.realName)
            personUserName.text = person.userName
            personPhotosCount.text = person.photosCount
            personPhotosDateLayout.bindNotBlank(person.firstDateTaken)
            personContactsCount.text = person.contacts
            personLocationLayout.bindNotBlank(person.location)
            personDescription.text = person.description.spanned()
            userContactsLayout.setOnClickListener {
                if (person.contacts.toInt() > 0) {
                    val query = Query(
                        type = QueryTypes.PERSON_CONTACT_LIST,
                        id = person.id
                    )
                    mCallBacks?.onPersonContactListClick(query)
                }
            }
            mainLayout.visibility = View.VISIBLE
            spinKit.visibility = View.INVISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            spinKit.visibility = View.VISIBLE
            swipeLayout.apply {
                setColors()
                setOnRefreshListener {
                    swipeLayout.isRefreshing = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        fetchPerson() }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch { fetchPerson() }
    }

}
