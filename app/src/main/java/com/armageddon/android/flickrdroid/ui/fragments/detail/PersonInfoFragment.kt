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
import com.armageddon.android.flickrdroid.common.Converter
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.common.setColors
import com.armageddon.android.flickrdroid.databinding.FragmentPersonInfoBinding
import com.armageddon.android.flickrdroid.network.execptions.ConnectionException
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.ui.adapters.ErrorAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import kotlinx.coroutines.launch

private const val USER_ID = "user_id"

class PersonInfoFragment: Fragment(), Converter {
    private var mCallBacks: CallBacks? = null
    private var _binding: FragmentPersonInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommonViewModel by activityViewModels ()
    private lateinit var mUserID: String

    companion object {
        fun newInstance(userID: String) = PersonInfoFragment().apply {
            val args = Bundle().apply {
                putString(USER_ID, userID)
            }
            arguments = args
        }
    }

    interface CallBacks {
        fun onPersonContactListClick(query: Query)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUserID = arguments?.getString(USER_ID) ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPersonInfoBinding.inflate(layoutInflater, container, false)
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
                        fetchPerson()
                    }
                }
            }
        }
        lifecycleScope.launch { fetchPerson() }
    }

    private suspend fun fetchPerson() {
        val response = viewModel.getPerson(mUserID)
        if (_binding == null) { return }
        binding.apply {
            if (response.stat == RESPONSE_DATA_OK) {
                val person = response.data!!
                spinKit.visibility = View.GONE
                mainLayout.visibility = View.VISIBLE
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
            } else {
                spinKit.visibility = View.INVISIBLE
                recyclerView.apply {
                    visibility = View.VISIBLE
                    layoutManager = LinearLayoutManager(context)
                    adapter = ErrorAdapter(ConnectionException()) {
                        spinKit.visibility = View.VISIBLE
                        mainLayout.visibility = View.INVISIBLE
                        recyclerView.visibility = View.GONE
                        lifecycleScope.launch { fetchPerson() }
                    }
                }
            }
        }
    }
}