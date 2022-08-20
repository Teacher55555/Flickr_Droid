package com.armageddon.android.flickrdroid.ui.fragments.controllers

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.databinding.FragmentPhotoTopBinding
import com.armageddon.android.flickrdroid.ui.adapters.CategoryItemAdapter
import com.armageddon.android.flickrdroid.ui.fragments.MenuFragment
import com.armageddon.android.flickrdroid.ui.fragments.dialog.CALENDAR_BUNDLE_KEY
import com.armageddon.android.flickrdroid.ui.fragments.dialog.DatePickerFragment
import com.armageddon.android.flickrdroid.ui.fragments.list.PhotoListFragment
import java.text.SimpleDateFormat
import java.util.*

private const val GOOGLE_PLAY_PAGE =
    "https://play.google.com/store/apps/details?id=com.armageddon.android.flickrdroid"
const val CALENDAR_REQUEST_KEY = "calendar_request_key"
const val CALENDAR_KEY = "calendar_key"
const val CATEGORY_INDEX_KEY = "category_index_key"
const val MAP_FRAGMENT_TAG = "f1"
const val PHOTO_LIST_FRAGMENT_TAG = "f0"
private const val CATEGORY_TITLE = "category_top_title"
private const val FILTER = "filter"


class TopPhotoFragment : MenuFragment(), CategoryItemAdapter.CallBacks {
    private val calendarQueryFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val calendarTitleFormat = SimpleDateFormat("dd.MM.yyyy", Locale.US)
    private var _binding: FragmentPhotoTopBinding? = null
    private val binding get() = _binding!!

    private var calendar = GregorianCalendar().apply { add(Calendar.DATE, -1) }
    private var query = Query(type = QueryTypes.INTERESTING)
    private lateinit var mSearchItem: MenuItem
    private lateinit var mBurgerMenuItem: MenuItem
    private lateinit var mCloseButton: MenuItem
    private lateinit var mDatePickerButton: MenuItem
    private var mCallBack: CallBacks? = null
    private lateinit var mCategoryAdapter: CategoryItemAdapter

    override fun onStop() {
        super.onStop()
        if (_binding != null) {
            binding.drawerLayout.closeDrawers()
        }
    }

    private fun setPhotoListFragment(query: Query, forceSet: Boolean = false) {
        val isFragmentContainerEmpty = childFragmentManager.findFragmentById(R.id.fragment_list_container)
        if (isFragmentContainerEmpty == null || forceSet) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_list_container, PhotoListFragment.newInstance(query))
                .commit()
        }
    }

    interface CallBacks {
       fun onSearchIconClick()
       fun onMapIconClick()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBack = context as CallBacks
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
        mCallBack = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            CALENDAR_REQUEST_KEY, this) { requestKey, bundle ->
            if (requestKey != CALENDAR_REQUEST_KEY) {
                return@setFragmentResultListener
            }
            calendar = bundle.getSerializable(CALENDAR_BUNDLE_KEY) as GregorianCalendar
            activity?.apply {
                invalidateOptionsMenu()
                viewModelStore.clear()
            }
            val query = Query(
                type = QueryTypes.INTERESTING,
                date = calendarQueryFormat.format(calendar.time)
            )
            setPhotoListFragment(query, true)
        }
    }

    companion object {
        fun newInstance() = TopPhotoFragment()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.categoryRecycleView.apply {
            mCategoryAdapter = CategoryItemAdapter(this@TopPhotoFragment)
            adapter = mCategoryAdapter
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            setHasFixedSize(true)
        }
        val fragment = childFragmentManager.findFragmentById(R.id.fragment_list_container)
        val currentQuery = (fragment as PhotoListFragment).query
        if (currentQuery.date.isNotBlank()) {
            val (year, month, day) = currentQuery.date.split("-")
            calendar = GregorianCalendar(year.toInt(), month.toInt() - 1, day.toInt())
        }
        if (currentQuery.type == QueryTypes.INTERESTING) {
            mCategoryAdapter.clearFilter()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.gallery_show_activity_menu, menu)
        mSearchItem = menu.findItem(R.id.app_bar_search_button)
        binding.title.text = when (mCategoryAdapter.getTitle().isNullOrBlank()) {
            true -> {
                binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.icon_calendar_seearch_action_menu)
                calendarTitleFormat.format(calendar.time)
            }
            else -> {
                binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
                    requireContext(),
                    R.drawable.ic_baseline_close_24)
                mCategoryAdapter.getTitle()
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbar
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> when (mCategoryAdapter.getTitle().isNullOrBlank()) {
                true -> {
                    val location = IntArray(2).apply {
                        binding.title.getLocationOnScreen(this)
                    }
                    DatePickerFragment.newInstance(calendar, location).show(childFragmentManager, null)
                }
                false -> { // when X - pressed
                    calendar = GregorianCalendar().apply { add(Calendar.DATE, -1) }
                    setPhotoListFragment(query, true)
                    (binding.categoryRecycleView.adapter as CategoryItemAdapter).clearFilter()
                    activity?.invalidateOptionsMenu()
                }
            }
            R.id.app_bar_search_button -> mCallBack?.onSearchIconClick()
            R.id.app_bar_map -> mCallBack?.onMapIconClick()
            R.id.app_bar_menu_button -> binding.drawerLayout.openDrawer(GravityCompat.END, true)
        }
        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhotoTopBinding.inflate(inflater, container, false)
        initSlideMenu(binding.drawerLayout, binding.navView.getChildAt(1))
        setPhotoListFragment(query)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.title.setOnClickListener {
//            if (categoryTitle.isNullOrBlank()) {
            if (mCategoryAdapter.getTitle().isNullOrBlank()) {
                val location = IntArray(2).apply {
                   it.getLocationOnScreen(this)
                }
                DatePickerFragment.newInstance(calendar,location).show(childFragmentManager, null)
            }
        }
        setHasOptionsMenu(true)

    }

    override fun callCategoryFilter(title: String?) {
        when (title.isNullOrBlank()) {
            true -> activity?.invalidateOptionsMenu()
            else -> {
                val query = Query(
                    type = QueryTypes.CATEGORY,
                    text = title,
                    sort = QueryTypes.Sort.INTERESTINGNESS_DESC.sort
                )
                activity?.apply {
                    invalidateOptionsMenu()
                    viewModelStore.clear()
                }
                setPhotoListFragment(query, true)
            }
        }

    }
}







