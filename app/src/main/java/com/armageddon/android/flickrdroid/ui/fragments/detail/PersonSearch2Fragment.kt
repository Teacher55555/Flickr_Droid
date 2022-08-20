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
import com.armageddon.android.flickrdroid.network.execptions.UserNotFoundException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_NO_INTERNET_CONNECTION
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

private const val QUERY_TYPE = "queryType"

class PersonSearch2Fragment: Fragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: PersonSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommonViewModel by activityViewModels ()
    private lateinit var mQuery: Query

    companion object {
        fun newInstance(query: Query) = PersonSearch2Fragment().apply {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            swipeLayout.apply {
                setColors()
                setOnRefreshListener {
                    swipeLayout.isRefreshing = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        spinKit.visibility = View.VISIBLE
                        mainLayout.visibility = View.INVISIBLE
                        recyclerView.visibility = View.GONE
                        searchPerson()
                    }
                }
            }
        }
        lifecycleScope.launch { searchPerson() }
    }

    private suspend fun searchPerson () {
        val response = viewModel.fetchUserId(mQuery.text)
        if (_binding == null) { return }
        binding.apply {
            when (response.stat) {
                RESPONSE_DATA_OK -> {
                    val person = response.data!!
                    spinKit.visibility = View.GONE
                    mainLayout.visibility = View.VISIBLE
                    Glide.with(this@PersonSearch2Fragment)
                        .load(person.getIconUrl())
                        .error(R.drawable.icon_user_logout)
                        .into(binding.personIcon)
                    personIconLayout.setOnClickListener {
                        val query = Query(
                            type = QueryTypes.PERSON,
                            id = person.id
                        )
                        mCallBacks?.onPersonClick(query, person)
                    }
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
                }
                RESPONSE_NO_INTERNET_CONNECTION -> {
                    spinKit.visibility = View.INVISIBLE
                    recyclerView.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(context)
                        adapter = ErrorAdapter(ConnectionException()) {
                            spinKit.visibility = View.VISIBLE
                            mainLayout.visibility = View.INVISIBLE
                            recyclerView.visibility = View.GONE
                            lifecycleScope.launch { searchPerson() }
                        }
                    }
                }
                else -> {
                    spinKit.visibility = View.INVISIBLE
                    recyclerView.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(context)
                        adapter = ErrorAdapter(UserNotFoundException()) {}
                    }
                }
            }
        }
    }
}


