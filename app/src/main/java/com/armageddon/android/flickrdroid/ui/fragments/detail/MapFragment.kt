package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.common.ActivityUtils.theme
import com.armageddon.android.flickrdroid.databinding.FragmentMapBinding
import com.armageddon.android.flickrdroid.databinding.MapBottomSlidePanelBinding
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_TIMEOUT
import com.armageddon.android.flickrdroid.ui.adapters.CategoryMapAdapter
import com.armageddon.android.flickrdroid.ui.fragments.dialog.LoadingMapFragment
import com.armageddon.android.flickrdroid.ui.fragments.dialog.PhotoFilterDialog
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*


private const val MAP_ZOOM = 5f
private const val SEEK_MIDDLE_PROGRESS = 50
private const val SEEK_MAX = 250
private const val SEEK_MAX_SDK_INT_27 = 255
private const val SEARCH_TYPE_WORLDWIDE = 0
private const val SEARCH_TYPE_NEAR_ME = 1
private const val REQUEST_LOCATION_PERMISSIONS = 9
private const val PANEL_SLIDE_UP_DELAY = 100L
private const val CATEGORY_INDEX_KEY = "category_map_index_key"

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

class MapFragment : Fragment(), Converter, LoadingMapFragment.CallBacks, CategoryMapAdapter.CallBacks {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var panelBinding : MapBottomSlidePanelBinding
    private lateinit var mMap: GoogleMap
    private var mCurrentLocation: Location? = null
    private lateinit var mRequestPermissionLauncher : ActivityResultLauncher<Array<String>>
    private var mSearchType = SEARCH_TYPE_WORLDWIDE
    private val mQueryType = QueryTypes.MAP
    private lateinit var mQuery : Query
    private lateinit var coroutine: Job
    private var mStopSignal = false
    private var width = 300
    private var heigth = 300
    private var mBitmapsMap = mutableMapOf<Photo, Bitmap>()
    private lateinit var mLoadDialog: LoadingMapFragment
    private val viewModel: GalleryItemViewModel by activityViewModels()
    private lateinit var mCategoryAdapter: CategoryMapAdapter
    private var mCallBacks: CallBacks? = null


    override fun onStop() {
        if (this::mMap.isInitialized) {
            viewModel.mapCameraPosition = mMap.cameraPosition
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            when {
                !mBitmapsMap.isNullOrEmpty() -> {
                    delay(50)
                    onPhotoReady(true)
                }
            }
        }
    }


    interface CallBacks {
        fun onItemClick(query: Query)
    }

    companion object {
        fun newInstance(): MapFragment {
            val args = Bundle()
            val fragment = MapFragment()
            fragment.arguments = args
            return fragment
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
        mRequestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permission: Map<String, Boolean> ->
                if (permission[LOCATION_PERMISSIONS[0]] == true ||
                    permission[LOCATION_PERMISSIONS[1]] == true
                ) {
                    findLocation()
                } else {
                    showDenyLocationDialog()
                }
            }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
        mCallBacks = null
    }

    fun clearCategoryFilter() {
        mCategoryAdapter.clearFilter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater, container, false)
        panelBinding = MapBottomSlidePanelBinding.bind(binding.mapSlidingPanel.root)

        lifecycleScope.launch {
        if (mBitmapsMap.isNullOrEmpty()) {
            delay(200)
            binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }
        }

        // location service initializer
        try {
            MapsInitializer.initialize(requireContext())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // map initializer
        binding.mapView.apply {
            onCreate(arguments)
            onStart()
            onResume()
            getMapAsync { googleMap: GoogleMap -> mMap = googleMap }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        panelBinding.apply {
            mCategoryAdapter = CategoryMapAdapter(this@MapFragment)
            panelBinding.categoryRecycleView.apply {
                layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
                adapter = mCategoryAdapter
                setHasFixedSize(true)
            }

            setSearchType(iconWorldwide, iconNearMe, SEARCH_TYPE_WORLDWIDE)
            panelBinding.seekBar.max = when (Build.VERSION.SDK_INT < 28) {
                true -> SEEK_MAX_SDK_INT_27
                false -> SEEK_MAX
            }
            searchButton.apply {
                text = getString(R.string.search_on_map_button_text, seekBar.progress)
                if (theme == ActivityUtils.THEME_DARK) {
                    background = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.search_button_backround_reverse
                    )
                }
            }

            nearMeLinearLayout.setOnClickListener {
                setSearchType(iconNearMe, iconWorldwide, SEARCH_TYPE_NEAR_ME)
            }
            worldwideLinearLayout.setOnClickListener {
                setSearchType(iconWorldwide, iconNearMe, SEARCH_TYPE_WORLDWIDE)
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    searchButton.text = getString(R.string.search_on_map_button_text, progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            inputText.apply {
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_ENTER
                    ) {
                        closeSoftKeyboard()
                        true
                    } else {
                        false
                    }
                }

                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {}

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        when (s?.isEmpty()) {
                            true -> {
                                panelBinding.closeButton.visibility = View.GONE
                                panelBinding.hintText.visibility = View.VISIBLE
                            }
                            else -> {
                                panelBinding.closeButton.visibility = View.VISIBLE
                                panelBinding.hintText.visibility = View.GONE
                            }
                        }
                    }

                    override fun afterTextChanged(s: Editable?) {
                        if (mCategoryAdapter.getTitle() != s.toString().trim()) {
                            mCategoryAdapter.clearAnimation()
                        }
                    }
                })

            }

            filterButton.setOnClickListener {
                val location = IntArray(2).apply {
                    searchTypeLayout.getLocationOnScreen(this)
                }

                inputText.closeSoftKeyboard()
                val filterDialog = PhotoFilterDialog.newInstance(
                    R.style.FilterSearchDialogAnimationStyle,
                    location
                )
                filterDialog.show(requireActivity().supportFragmentManager, null)
            }

            closeButton.setOnClickListener {
                mCategoryAdapter.clearFilter()
                inputText.text.clear()
            }
            searchButton.setOnClickListener {
                if (inputText.text.isNullOrBlank()) {
//                    Toast.makeText(requireContext(), R.string.search_blank_warning, Toast.LENGTH_SHORT)
//                        .show()
                    Snackbar.make(binding.root, R.string.search_blank_warning, Snackbar.LENGTH_SHORT)
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                        .show()
                    return@setOnClickListener
                }
                viewModel.mapCameraPosition = null
                mStopSignal = false
                mCurrentLocation = null
                coroutine = lifecycleScope.launch {
                    when (mSearchType) {
                        SEARCH_TYPE_WORLDWIDE -> findPhotos()
                        SEARCH_TYPE_NEAR_ME -> {
                            // Check GPS is ON
                            val manager = requireActivity()
                                .getSystemService(Context.LOCATION_SERVICE) as LocationManager
                            if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                showAlertMessageNoGps()
                                return@launch
                            }

                            when {
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    LOCATION_PERMISSIONS[0],
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    findLocation()
                                }
                                ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    LOCATION_PERMISSIONS[1],
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    findLocation()
                                }
                                shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS[0]) -> {
                                    showDenyLocationDialog()
                                }
                                shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS[1]) -> {
                                    showDenyLocationDialog()
                                }
                                else -> {
                                    mRequestPermissionLauncher.launch(LOCATION_PERMISSIONS)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun findPhotos() {
        mLoadDialog = LoadingMapFragment.newInstance()
        mLoadDialog.setCallBacks(this)
        mLoadDialog.show(requireActivity().supportFragmentManager, null)
        mLoadDialog.isCancelable = false

        val photoQuantity = panelBinding.seekBar.progress

        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        mQuery = Query(
            type = QueryTypes.MAP,
            text = panelBinding.inputText.text.toString(),
            perPage = photoQuantity,
            sort = QueryTypes.Sort.values()[AppPreferences.getPhotoFilter(requireContext())].sort,
            latitude = if (mCurrentLocation != null) mCurrentLocation!!.latitude.toString() else "",
            longitude = if (mCurrentLocation != null) mCurrentLocation!!.longitude.toString() else "",
            oauthToken = AppPreferences.getOauthToken(requireContext()) ?: "",
            oauthTokenSecret = AppPreferences.getOauthTokenSecret(requireContext()) ?: ""
        )
        val response = viewModel.getPhotosForMap(mQuery)
        if (response.stat != RESPONSE_DATA_OK) {
            mLoadDialog.dismiss()
            val message = when(response.stat) {
                RESPONSE_TIMEOUT -> getString(R.string.time_out_error)
                else -> getString(R.string.no_internet_connection_error)
            }
            Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorWhite, null))
                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                .show()
            return
        }
        val photoList = response.dataArray
        mLoadDialog.setProgressMax(photoList.size)
        mLoadDialog.load(photoList)
    }

    private fun findLocation() {
        val request = LocationRequest.create()
        request.priority =
            LocationRequest.PRIORITY_HIGH_ACCURACY // how to proceed in a situation of choosing between battery consumption and the accuracy of the query;
        request.numUpdates = 1 // how many times the positional data should be updated;
        request.interval = 0 // how often positional data should be updated;
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(activity as Activity)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showDenyLocationDialog()
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            mCurrentLocation = location
            lifecycleScope.launch {
                findPhotos()
            }
        }
    }

    private fun onPhotoReady(restoreState: Boolean) {
        if (!mStopSignal && this::mMap.isInitialized && !mBitmapsMap.isEmpty()) {
            mMap.clear()
            val itemPointsList = mutableListOf<LatLng>()

            // creates markers for each photo and adds them on the map.
            // Snippet is used only to store photoId, but not for showing info.

            var index = 0f
            for ((photo, value) in mBitmapsMap.entries) {
                val itemBitmap = BitmapDescriptorFactory.fromBitmap(value)
                val coordinates = LatLng(photo.latitude, photo.longitude)
                itemPointsList.add(coordinates)
                val itemMarker = MarkerOptions()
                    .position(coordinates)
                    .icon(itemBitmap)
                    .title(photo.title)
                    .snippet(photo.id)
                    .zIndex(mBitmapsMap.size - 1 - index)
                itemMarker.draggable(true)
                mMap.addMarker(itemMarker)
                index++
            }

            val builder = LatLngBounds.Builder()
            for (latLng in itemPointsList) {
                builder.include(latLng)
            }


            // adds user marker on the map
            if (mCurrentLocation != null) {
                val myPoint = LatLng(
                    mCurrentLocation!!.latitude,
                    mCurrentLocation!!.longitude
                )
                val myMarker = MarkerOptions()
                    .position(myPoint)
                    .zIndex(mBitmapsMap.size.toFloat())
                mMap.addMarker(myMarker)
                builder.include(myPoint)
            }
            val bounds = builder.build()
            val margin = resources.getDimensionPixelSize(R.dimen.map_inset_margin)

            if (viewModel.mapCameraPosition != null && restoreState) {
                val restoreUpdate = CameraUpdateFactory.newCameraPosition(viewModel.mapCameraPosition!!)
                mMap.animateCamera(restoreUpdate, 500, null)
            } else {
                val update = CameraUpdateFactory.newLatLngBounds(bounds, margin)
                mMap.animateCamera(update)
            }
            mMap.setOnMarkerClickListener { marker ->

                val list = mBitmapsMap.keys.toList()
                list.forEachIndexed { index, photo ->
                    if (photo.id == marker.snippet) {
                        viewModel.markerPosition = index
                        return@forEachIndexed
                    }
                }

                viewModel.mapPhotoDetailList = list
                binding.mapView.onPause()
                binding.mapView.onStop()
                binding.mapView.onDestroy()
                mCallBacks?.onItemClick(mQuery)
                true
            }
        }
    }

    override fun callCategoryFilter(title: String?) {
        panelBinding.inputText.setText(title)
    }

    private fun EditText.closeSoftKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
            this.clearFocus()
        }
    }

    override fun onShowCancel() {
        coroutine.cancel()
        mStopSignal = true
        binding.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
    }

    override fun onLoadFinished() {
        mBitmapsMap = mLoadDialog.mBitmaps
        mLoadDialog.dismiss()
        onPhotoReady(false)
    }

    private fun showDenyLocationDialog() {
        val builder = AlertDialog.Builder(requireContext(), R.style.DialogAllCapsFalseStyle).apply {
            setMessage(getString(R.string.location_access_deny_warning))
            setNegativeButton(R.string.no_thanks) { dialog, _ -> dialog.cancel() }
            setPositiveButton(R.string.yes_please) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package",activity?.packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun showAlertMessageNoGps() {
        val builder = AlertDialog.Builder(requireContext(), R.style.DialogAllCapsFalseStyle).apply {
            setMessage(getString(R.string.GPS_OFF_warning))
            setCancelable(false)
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel() }
        }
        val alert = builder.create()
        alert.show()
    }

    private fun setSearchType(
        colorizeImage: ImageView,
        clearColorImage: ImageView,
        searchType: Int
    ) {
        colorizeImage.setColorFilter(getAttrColor(requireContext(), R.attr.colorAccent))
        clearColorImage.clearColorFilter()
        mSearchType = searchType
    }
}



