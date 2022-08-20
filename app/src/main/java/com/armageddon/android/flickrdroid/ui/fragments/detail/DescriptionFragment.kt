package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.FragmentDescriptionBinding
import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.ui.viewmodels.CommonViewModel
import kotlinx.coroutines.launch

private const val QUERY_TYPE_BUNDLE = "query_type_bundle"
private const val GROUP_BUNDLE = "group_bundle"

class DescriptionFragment : Fragment(), Converter {
    private var _binding: FragmentDescriptionBinding? = null
    private val binding get() = _binding!!
    private val commonViewModel: CommonViewModel by activityViewModels()
    private lateinit var mQueryTypes: QueryTypes
    private lateinit var mQuery: Query
    private lateinit var mGroup: Group

    companion object {
        fun newInstance(query: Query, group: Group): DescriptionFragment {
            val args = Bundle()
            val fragment = DescriptionFragment()
            args.putSerializable(QUERY_TYPE_BUNDLE, query)
            args.putSerializable(GROUP_BUNDLE, group)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mGroup = arguments?.getSerializable(GROUP_BUNDLE) as Group
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDescriptionBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            mainLayout.visibility = View.INVISIBLE
            spinKit.visibility = View.VISIBLE
            swipeLayout.apply {
                setColors()
                setOnRefreshListener {
                    mainLayout.visibility = View.INVISIBLE
                    spinKit.visibility = View.INVISIBLE
                    swipeLayout.isRefreshing = false
                    spinKit.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        fetchGroupInfo()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch { fetchGroupInfo() }
    }

   private suspend fun fetchGroupInfo() {
        binding.descriptionView.text = when (mGroup.groupInfoResponse == null) {
            true -> {
                val query = Query(
                    id = mGroup.nsid,
                    oauthToken = AppPreferences.getOauthToken(requireContext()) ?: "",
                    oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext()) ?: ""
                )
                val response = commonViewModel.getGroupInfo(query)
                if (response.stat == RESPONSE_DATA_OK) {
                    mGroup.groupInfoResponse = response
                    response.data?.getFullDescription(requireContext())?.spanned()
                } else { "" }
            }
            false -> {
                mGroup.groupInfoResponse!!.data!!.getFullDescription(requireContext()).spanned()
            }
        }
       if (_binding == null) { return }
       binding.mainLayout.visibility = View.VISIBLE
       binding.spinKit.visibility = View.INVISIBLE
    }
}