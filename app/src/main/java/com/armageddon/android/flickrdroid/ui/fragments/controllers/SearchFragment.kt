package com.armageddon.android.flickrdroid.ui.fragments.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.databinding.FragmentSearchBinding
import com.armageddon.android.flickrdroid.model.HistoryElement
import com.armageddon.android.flickrdroid.ui.fragments.detail.PersonSearch2Fragment
import com.armageddon.android.flickrdroid.ui.fragments.dialog.FILTER_REQUEST_KEY
import com.armageddon.android.flickrdroid.ui.fragments.dialog.PhotoFilterDialog
import com.armageddon.android.flickrdroid.ui.fragments.list.GroupsListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.HistoryListFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import com.armageddon.android.flickrdroid.ui.viewmodels.HistoryViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent

private const val SEARCH_TAG = "search_tag"
private const val KEYBOARD_KEY = "keyboard_key"

class SearchFragment : Fragment() {
   private var _binding : FragmentSearchBinding? = null
   private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by activityViewModels()
   private val offScreenPageLimit = 3
   private var currentPagerPosition = 1
    private var mSearchTag = ""
    private var mOpenKeyboard = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mSearchTag = arguments?.getString(SEARCH_TAG) ?: ""
        mOpenKeyboard = arguments?.getBoolean(KEYBOARD_KEY, true) ?: true


        childFragmentManager.setFragmentResultListener(
            FILTER_REQUEST_KEY, this) { requestKey, bundle ->
            if (requestKey != FILTER_REQUEST_KEY || binding.inputText.text.isBlank()) {
                return@setFragmentResultListener
            }
            startSearch(binding.inputText.text.toString())
        }
    }


    override fun onDetach() {
        super.onDetach()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }
        return true
    }

    companion object {
        fun newInstance(tag: String = "", openKeyboard: Boolean = true): SearchFragment{
            val args = Bundle()
            val fragment = SearchFragment()
            args.putString(SEARCH_TAG, tag)
            args.putBoolean(KEYBOARD_KEY, openKeyboard)
            fragment.arguments = args
            return fragment
        }
    }


    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(layoutInflater, container, false)
        binding.apply {
            KeyboardVisibilityEvent.setEventListener(
                requireActivity()
            ) { isOpen : Boolean ->
                if (!isOpen) {
                    inputText.clearFocus()
                }
            }
            viewPager.adapter = HistoryAdapter()
            viewPager.offscreenPageLimit = offScreenPageLimit
            closeButton.setOnClickListener {
                inputText.text.clear()
                inputText.requestFocus()
                inputText.showSoftKeyboard()
                viewPager.adapter = HistoryAdapter()
                viewPager.setCurrentItem(currentPagerPosition, false)
            }
            tabLayout.addOnTabSelectedListener(object :TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    hintText.text = when (tab?.position) {
                       0 -> {
                           filterButton.visibility = View.VISIBLE
                           getString(R.string.search_photo_hint)
                       }
                       1 ->{
                           filterButton.visibility = View.GONE
                           binding.filterView.visibility = View.GONE
                           getString(R.string.search_group_hint)
                       }
                       else -> {
                           binding.filterView.visibility = View.GONE
                           filterButton.visibility = View.GONE
                           getString(R.string.search_user_hint)
                       }
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
               tab.text = when (position) {
                   0 -> getString(R.string.photos)
                   1 -> getString(R.string.groups)
                   else -> getString(R.string.person)
                }
            }.attach()

            inputText.apply {
                setOnKeyListener { v, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER
                        && !text.isNullOrBlank()
                    ) {
                        closeSoftKeyboard()
                        val historyElement = HistoryElement(
                            historyType = HistoryType.values()[currentPagerPosition],
                            text = inputText.text.toString().lowercase()
                        )
                        viewModel.addHistoryElement(historyElement)
                        startSearch(inputText.text.toString())
                        viewPager.setCurrentItem(currentPagerPosition, false)
                        true
                    } else {
                        false
                    }
                }
                addTextChangedListener {
                    when (it?.isEmpty()) {
                        true -> {
                            binding.closeButton.visibility = View.GONE
                            binding.hintText.visibility = View.VISIBLE
                        }
                        else -> {
                            binding.closeButton.visibility = View.VISIBLE
                            binding.hintText.visibility = View.GONE
                        }
                    }
                    currentPagerPosition = viewPager.currentItem
                }
            }
            filterButton.setOnClickListener {
                val location = IntArray(2).apply {
                    tabLayout.getLocationOnScreen(this)
                }

                inputText.closeSoftKeyboard()
                val filterDialog = PhotoFilterDialog.newInstance(
                    R.style.FilterSearchDialogAnimationStyle,
                    location
                )
                filterDialog.show(childFragmentManager, null)

            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        when (mSearchTag.isNotBlank()) {
            true ->  {
                startSearch(mSearchTag, 0, false)
            }
            else -> {
                lifecycleScope.launch {
                    if (mOpenKeyboard) {
                        delay(100)
                        binding.inputText
                            .takeIf { it.text.isBlank() }
                            ?.showSoftKeyboard()
                    }
                }
            }
        }
        setHasOptionsMenu(true)
    }

    private inner class HistoryAdapter : FragmentStateAdapter(this) {
        override fun getItemCount() = 3
        override fun createFragment(position: Int) = HistoryListFragment.newInstance(position)

    }

    private inner class SearchAdapter(val text: String) : FragmentStateAdapter(this) {
        override fun getItemCount() = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> {
                    val queryType = QueryTypes.SEARCH
                        .apply { val sortIndex = AppPreferences.getPhotoFilter(requireContext())
                            query1 = text
                            query3 = QueryTypes.Sort.values()[sortIndex].sort
                        }
                    val sortIndex = AppPreferences.getPhotoFilter(requireContext())
                    val query = Query(
                        type = QueryTypes.SEARCH,
                        text = text,
                        sort = QueryTypes.Sort.values()[sortIndex].sort
                    )
                    PhotoListFragment.newInstance(query)
                }
                1 -> {
                    val queryType = QueryTypes.GROUP
                        .apply { query1 = text }
                    val query = Query(
                        type = QueryTypes.GROUP,
                        text = text
                    )
                    GroupsListFragment.newInstance(query)
                }
                2 -> {
                    val queryType = QueryTypes.PERSON_SEARCH
                        .apply {
                            query1 = ""   // user_id
                            query2 = text // userName or E-mail
                        }
                    val query = Query(
                        type = QueryTypes.PERSON_SEARCH,
                        text = text
                    )
                    PersonSearch2Fragment.newInstance(query)
                }
                else -> Fragment()
            }
        }
    }

    private fun EditText.showSoftKeyboard() {
        post {
            if (this.requestFocus()) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun EditText.closeSoftKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
            this.clearFocus()
        }
    }

    fun startSearch (text: String, position: Int = currentPagerPosition, resetViewModel: Boolean = true) {
        binding.inputText.apply {
            closeSoftKeyboard()
            takeIf { it.text.isBlank()}
                ?.setText(text)
        }
        if (resetViewModel) { activity?.viewModelStore?.clear() }
        binding.viewPager.adapter = SearchAdapter(text.trim())
        binding.viewPager.setCurrentItem(position, false)
    }
}