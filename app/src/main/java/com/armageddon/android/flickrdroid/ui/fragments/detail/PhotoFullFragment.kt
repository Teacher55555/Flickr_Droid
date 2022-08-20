package com.armageddon.android.flickrdroid.ui.fragments.detail

import android.Manifest
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthToken
import com.armageddon.android.flickrdroid.common.AppPreferences.getOauthTokenSecret
import com.armageddon.android.flickrdroid.databinding.ErrorLayoutBinding
import com.armageddon.android.flickrdroid.databinding.FragmentPhotoFullBinding
import com.armageddon.android.flickrdroid.databinding.PhotoInfoSlidingUpPanelBinding
import com.armageddon.android.flickrdroid.databinding.ViewPagerBinding
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.model.PhotoExif
import com.armageddon.android.flickrdroid.model.PhotoInfo
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_OK
import com.armageddon.android.flickrdroid.ui.adapters.PhotoDataPagingAdapter
import com.armageddon.android.flickrdroid.ui.viewmodels.GalleryItemViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.sothree.slidinguppanel.ScrollableViewHelper
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.lang.ClassCastException
import kotlin.math.abs

const val SLIDESHOW_INTERVAL_3_SEC = 3000L
const val SLIDESHOW_INTERVAL_4_SEC = 4000L
const val SLIDESHOW_INTERVAL_5_SEC = 5000L
const val SLIDESHOW_INTERVAL_10_SEC = 10000L
const val SLIDESHOW_INTERVAL_15_SEC = 15000L
private const val SLIDE_SHOW_KEY = "slide_show"
private const val QUERY_TYPE = "query_type"
private const val MAP_ZOOM = 5f
private const val DOUBLE_TAP_SCALE = 3.5f
private const val MAX_SCALE = 6f
private const val ALPHA_DEFAULT = 1.0F
private const val PANEL_FALL_DAWN_ANIM_TIME = 200L
private const val GALLERY_ITEM = "item"
private const val PHOTO_INFO_FLAG = "photo_info"
private const val FILE_NAME_TEMPLATE = "Flick_image"
private const val FILE_TYPE_TEMPLATE = ".jpg"

private const val REPORT_URL = "https://www.flickrhelp.com/hc/en-us/requests/new"

class PhotoFullFragment : Fragment() {
    private var _binding: ViewPagerBinding? = null
    private val binding get() = _binding!!
    private val mAdapter = PhotoPagingAdapter()
    private val viewModel: GalleryItemViewModel by activityViewModels ()
    private var mPhotoInfoVisible = true
    private var mIsSlideShowRun = false
    private lateinit var mJob : Job
    private lateinit var query: Query
    private lateinit var animUpPanelHide: Animation
    private lateinit var animDownPanelHide: Animation
    private lateinit var animUpPanelShow: Animation
    private lateinit var animDownPanelShow: Animation
    private lateinit var animRiseUp: Animation
    private lateinit var animFallDawn: Animation
    private lateinit var animClockwise: Animation
    private lateinit var animCClockwise: Animation
    private lateinit var mRequestPermissionLauncher : ActivityResultLauncher<String>
    private lateinit var mServicePhoto: Photo
    private var mServiceDrawable: Drawable? = null
    private lateinit var mService: (photo: Photo, drawable: Drawable?) -> Unit
    private var mDownloadQuality: String = LogoIcon.Photo.MAX.prefix


    private var mCallBacks: CallBacks? = null

    companion object {
        fun newInstance(query: Query) = PhotoFullFragment().apply {
            val args = Bundle().apply {
                putSerializable(QUERY_TYPE, query)
            }
            arguments = args
        }
    }

    interface CallBacks {
        fun onRefresh(visibility: Int)
        fun onError (query: Query)
        fun onUserIconClick(person: Person)
        fun onCommentIconClick(photoId: String)
        fun onTagClick(tag: String, openKeyboard: Boolean)
    }

    override fun onStop() {
        ActivityUtils.showStatusBar(requireActivity())
        super.onStop()
    }

    override fun onStart() {
        ActivityUtils.hideStatusBar(requireActivity())
        if (viewModel.isCommentsQuantityChanged) {
            mAdapter.refresh()
            viewModel.isCommentsQuantityChanged = false
        }
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = arguments?.getSerializable(QUERY_TYPE) as Query
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallBacks = context as CallBacks
        mDownloadQuality = AppPreferences.getPhotoDownloadQuality(context)
        mRequestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    mService(mServicePhoto, mServiceDrawable)
                }
            }
    }

    override fun onDetach() {
        super.onDetach()
        _binding = null
        mCallBacks = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SLIDE_SHOW_KEY, mIsSlideShowRun)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        mIsSlideShowRun = savedInstanceState?.getBoolean(SLIDE_SHOW_KEY) ?: false
        if (mIsSlideShowRun) slideShow()
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ViewPagerBinding.inflate(inflater, container, false)
        binding.viewPager.adapter = mAdapter
        binding.viewPager.apply {
            adapter = mAdapter
            setPageTransformer(DepthPageTransformer())
        }
        return binding.root
    }

   fun <T: View> T.startAnimAndGone (animation: Animation)  {
        animation.also {
            it.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        startAnimation(animation)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animUpPanelHide = AnimationUtils.loadAnimation(context, R.anim.photo_up_panel_hide)
        animDownPanelHide = AnimationUtils.loadAnimation(context, R.anim.photo_down_panel_hide)
        animUpPanelShow = AnimationUtils.loadAnimation(context, R.anim.photo_up_panel_show)
        animDownPanelShow = AnimationUtils.loadAnimation(context, R.anim.photo_down_panle_show)
        animRiseUp = AnimationUtils.loadAnimation(context, R.anim.item_animation_rise_up)
        animFallDawn = AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        animClockwise = AnimationUtils.loadAnimation(context, R.anim.rotate_clockwise)
            .apply{ fillAfter = true }
        animCClockwise = AnimationUtils.loadAnimation(context, R.anim.rotate_counter_clockwise)
            .apply { fillAfter = true }

        lifecycleScope.launch {
            viewModel.getFlow(query).collectLatest(mAdapter::submitData)
        }

        lifecycleScope.launch {
            mAdapter.loadStateFlow.collectLatest { loadStates ->
                if (loadStates.source.refresh is LoadState.Error ) {
                    if (query.type == QueryTypes.MAP) {
                        mCallBacks?.onError(query)
                    } else {
                        val message = (loadStates.refresh as LoadState.Error).error.message
                        binding.viewPager.setPageTransformer(null)
                        binding.viewPager.adapter = ErrorAdapter(message)
                    }
                }

                when (loadStates.source.refresh) {
                    is LoadState.Loading -> {
                        mCallBacks?.onRefresh(View.VISIBLE)
                    }
                    is LoadState.NotLoading -> {
                        mCallBacks?.onRefresh(View.INVISIBLE)
                    }
                    else -> {}
                }
            }
        }

        binding.viewPager.setCurrentItem(viewModel.markerPosition, false)
    }

    private inner class PhotoFullHolder(view: View)
        : PhotoDataPagingAdapter.GalleryHolder(view) {
        val bindingHolder = FragmentPhotoFullBinding.bind(view)
        lateinit var panelSlideListener : PanelSlideListener


        fun closeSlidingPanel() {
            bindingHolder.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            bindingHolder.slidingLayout.removePanelSlideListener(panelSlideListener)
            bindingHolder.scrollView.visibility = View.INVISIBLE
            bindingHolder.scrollView.removeAllViews()
            bindingHolder.apply {
                photoName.alpha = ALPHA_DEFAULT
                divider.alpha = ALPHA_DEFAULT
            }
        }

        fun photoInfoPanelsCheck () {
            when (mPhotoInfoVisible) {
                false -> { bindingHolder.apply {
                    slidingLayout.isTouchEnabled = false
                    curtain.visibility = View.INVISIBLE
                    photoUpPanel.visibility = View.INVISIBLE
                    photoControlPanel.visibility = View.INVISIBLE
                    bottomShadowView.visibility = View.INVISIBLE
                } }
                else -> { bindingHolder.apply {
                    if (this@PhotoFullFragment::mJob.isInitialized && mJob.isActive) {
                        mJob.cancel()
                        Snackbar.make(binding.root, R.string.slide_show_stopped, Snackbar.LENGTH_SHORT)
                            .setTextColor(resources.getColor(R.color.colorWhite, null))
                            .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
                            .show()
                    }
                    slidingLayout.isTouchEnabled = true
                    curtain.visibility = View.VISIBLE
                    photoUpPanel.visibility = View.VISIBLE
                    photoControlPanel.visibility = View.VISIBLE
                    bottomShadowView.visibility = View.VISIBLE
                } }
            }
        }


        val animateUI : (Boolean) -> Unit = { hideUI ->
            when (hideUI) {
                true -> {
                    bindingHolder.apply {
                        photoUpPanel.startAnimation(animUpPanelHide)
                        photoControlPanel.startAnimation(animDownPanelHide)
                        bottomShadowView.startAnimation(animDownPanelHide)
                        slidingLayout.isTouchEnabled = false
                        curtain.visibility = View.GONE
                        mPhotoInfoVisible = false }
                }
                false -> {
                    bindingHolder.apply {
                        photoUpPanel.startAnimation(animUpPanelShow)
                        photoControlPanel.startAnimation(animDownPanelShow)
                        bottomShadowView.startAnimation(animDownPanelShow)
                        slidingLayout.isTouchEnabled = true
                        curtain.visibility = View.VISIBLE
                        mPhotoInfoVisible = true }
                }
            }
        }

        override fun bind(item: Photo) {
            Glide.with(itemView)
                .load(item.getPhotoUrl())
                .listener(PhotoLoadListener(bindingHolder))
                .error(R.drawable.ic_outline_broken_image_24)
                .into(bindingHolder.photoDetailView)

            Glide.with(itemView)
                .load(item.getOwnerIcon())
                .error(R.drawable.logo_black_face)
                .into(bindingHolder.userIcon)

            bindingHolder.apply {
                fun callPerson () {
                    val person = Person(
                        id = item.owner,
                        userName = item.ownername,
                        realName = item.realname,
                        iconFarm = item.iconfarm,
                        iconServer = item.iconserver
                    )
                    mCallBacks?.onUserIconClick(person)
                }
                userIcon.setOnClickListener { callPerson() }
                userName.setOnClickListener { callPerson() }
                iconClose.setOnClickListener { activity?.onBackPressed() }
                userName.text = item.getOwnerName()

                favQuantityText.apply {
                    fun setFavoriteIconAndCount () {
                        text = item.countFaves
                        if (item.isFavorite) {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_star_filled, 0, 0, 0)
                        } else {
                            setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.icon_star_stroke, 0, 0, 0)
                        }
                    }
                    setFavoriteIconAndCount()
                    setOnClickListener {
                        val oauthToken = getOauthToken(requireContext()) ?: ""
                        if (oauthToken.isBlank()) {
                            Snackbar.make(binding.root, R.string.not_singed_up_warning, Snackbar.LENGTH_SHORT)
                                .setTextColor(resources.getColor(R.color.colorWhite, null))
                                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                                .show()
                            return@setOnClickListener
                        }
                        if (item.owner == AppPreferences.getUserId(requireContext())) {
                            Snackbar.make(binding.root, R.string.own_photo_fave_warning, Snackbar.LENGTH_SHORT)
                                .setTextColor(resources.getColor(R.color.colorWhite, null))
                                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                                .show()
                            return@setOnClickListener
                        }
                        val query = Query(
                            type = QueryTypes.ADD_FAVORITE_PHOTO,
                            id = item.id,
                            oauthToken = oauthToken,
                            oauthTokenSecret = getOauthTokenSecret(requireContext()) ?: ""
                        )
                        lifecycleScope.launch {
                            bindingHolder.slidePanelProgress.visibility = View.VISIBLE
                            val response = when (item.isFavorite) {
                                true -> viewModel.removeFavoritePhoto(query)
                                false -> viewModel.addFavoritePhoto(query)
                            }
                            if (response.stat == RESPONSE_DATA_FAIL) {
                                Snackbar.make(bindingHolder.root, R.string.internet_connection_error, Snackbar.LENGTH_SHORT)
                                    .setTextColor(resources.getColor(R.color.colorWhite, null))
                                    .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                                    .show()
                            } else {
                                item.isFavorite = !item.isFavorite
                                item.countFaves = when (item.isFavorite) {
                                   true -> (item.countFaves.toInt() + 1).toString()
                                   false -> (item.countFaves.toInt() - 1).toString()
                                }
                                setFavoriteIconAndCount()
                            }
                            bindingHolder.slidePanelProgress.visibility = View.INVISIBLE
                        }
                    }
                }
                commentsQuantityText.apply {
                    text = item.countComments
                    setOnClickListener { mCallBacks?.onCommentIconClick(item.id) }
                }

                fun fetchPhotoInfo(service: () -> Unit ) {
                    if (item.photoInfoResponse == null && isAdded) {
                        bindingHolder.slidePanelProgress.visibility = View.VISIBLE
                        viewLifecycleOwner.lifecycleScope.launch {
                            val query = Query(
                                id = item.id,
                                oauthToken = getOauthToken(requireContext()) ?: "",
                                oauthTokenSecret = getOauthTokenSecret(requireContext()) ?: ""
                            )
                            val photoInfoResponse = viewModel.getPhotoInfo(query)
                            photoInfoResponse.data?.camera = item.camera
                            item.photoInfoResponse = photoInfoResponse
                            bindingHolder.slidePanelProgress.visibility = View.GONE
                            service()
                        }
                    } else {
                        service()
                    }
                }

                downloadButton.setOnClickListener {
                    fetchPhotoInfo {
                        runService(
                            item,
                            null,
                            R.string.download_access_warning,
                            downloadService
                        )
                    }
                }
                shareButton.setOnClickListener {
                    fetchPhotoInfo {
                        runService(
                            item,
                            photoDetailView.drawable,
                            R.string.storage_access_warning,
                            shareService
                        )
                    }
                }
                slideshowButton.setOnClickListener {
                    bindingHolder.slidingLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
                    val slideShowInterval = AppPreferences.getSlideShowInterval(requireContext())
                            slideShow()
                    Snackbar.make(binding.root, getString(R.string.slide_show_started, slideShowInterval/1000), Snackbar.LENGTH_SHORT)
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
                        .show()
                            animateUI(true)
                }
                commentsQuantityText.text = item.countComments
                photoName.text = item.title
                photoDetailView.maximumScale = MAX_SCALE
                photoDetailView.setOnDoubleTapListener(DoubleTapListener(this, animateUI))
                panelSlideListener = PanelSlideListener(this)
                slidingLayout.addPanelSlideListener(panelSlideListener)
                slidingLayout.setScrollableViewHelper(NestedScrollableViewHelper(bindingHolder.scrollView))
            }
        }
    }


    private inner class PanelSlideListener(
        val bindingHolder: FragmentPhotoFullBinding,
        ) : SlidingUpPanelLayout.PanelSlideListener, Converter {

        val photoDetailView: View = layoutInflater.inflate(
            R.layout.photo_info_sliding_up_panel,
            view as ViewGroup,
            false)

        private fun initPhotoExif(photoExif: PhotoExif, panelBinding: PhotoInfoSlidingUpPanelBinding) {
            if (photoExif.camera.isNotBlank()) {
                panelBinding.photoCameraLayout.visibility = View.VISIBLE
                panelBinding.photoCamera.text = photoExif.camera
            }

            if (photoExif.isDataEmpty) {
                return
            }

            panelBinding.apply {
                if (photoExif.lensModel.isNotBlank()) {
                    photoLensTitle.visibility = View.VISIBLE
                    photoLensText.apply {
                        visibility = View.VISIBLE
                        text = photoExif.lensModel
                    }
                }

                if (photoExif.fNumber.isNotBlank()) {
                    photoApertureTitle.visibility = View.VISIBLE
                    photoApertureText.apply {
                        visibility = View.VISIBLE
                        text = photoExif.fNumber
                    }
                }

                if (photoExif.focalLength.isNotBlank()) {
                    photoFocalLengthTitle.visibility = View.VISIBLE
                    photoFocalLengthText.apply {
                        visibility = View.VISIBLE
                        text = photoExif.focalLength
                    }
                }

                if (photoExif.exposureTime.isNotBlank()) {
                    photoExposureTitle.visibility = View.VISIBLE
                    photoExposureText.apply {
                        visibility = View.VISIBLE
                        text = photoExif.exposureTime
                    }
                }

                if (photoExif.iso.isNotBlank()) {
                    photoIsoTitle.visibility = View.VISIBLE
                    photoIsoText.apply {
                        visibility = View.VISIBLE
                        text = photoExif.iso
                    }
                }
                panelBinding.photoExifArrow.visibility = View.VISIBLE

                photoCameraLayout.setOnClickListener {
                    if (photoCameraDetailLayout.visibility == View.GONE) {
                        photoCameraDetailLayout.apply {
                            visibility = View.VISIBLE
                            startAnimation(animFallDawn)
                        }
                        panelBinding.photoExifArrow.startAnimation(animClockwise)
                    } else {
                        photoCameraDetailLayout.startAnimAndGone(animRiseUp)
                        panelBinding.photoExifArrow.startAnimation(animCClockwise)
                    }
                }
            }
        }

        private fun initMainFields(photoInfo: PhotoInfo, panelBinding: PhotoInfoSlidingUpPanelBinding) {
            panelBinding.mainSlidingUpPanleLayout.visibility = View.VISIBLE
            photoInfo.apply {
                if (description.isNotBlank()) { panelBinding.apply {
                    photoDescriptionLayout.visibility = View.VISIBLE
                    photoDescription.text = description }
                }

                if (photoDateTaken.isNotBlank()) { panelBinding.apply {
                    dateTakenLayout.visibility = View.VISIBLE
                    dateTaken.text = photoDateTaken }
                }

                if (location.toString().isNotBlank()) { panelBinding.apply {
                    photoLocationLayout.visibility = View.VISIBLE
                    photoLocation.text = location.getAddress()

                    mapView.apply {
                        onCreate(arguments)
                        onStart()
                        onResume()
                        getMapAsync { googleMap: GoogleMap ->
                            val photoPoint = LatLng(location.latitude, location.longitude)
                            val myMarker = MarkerOptions().position(photoPoint)
                            val builder = LatLngBounds.Builder()
                            builder.include(photoPoint)
                            googleMap.apply {
                                addMarker(myMarker)
                                uiSettings.isZoomControlsEnabled = true
                                moveCamera(CameraUpdateFactory.newLatLngZoom(photoPoint, MAP_ZOOM))
                            }
                        }
                        photoLocationLayout.setOnClickListener {
                            if (visibility == View.GONE) {
                                visibility = View.VISIBLE
                                startAnimation(animFallDawn)
                                photoLocationArrow.startAnimation(animClockwise)
                            } else {
                                startAnimAndGone(animRiseUp)
                                photoLocationArrow.startAnimation(animCClockwise)
                            }
                        }
                    } }
                }

                if (tags.isNotEmpty() && panelBinding.tagsWrapper.childCount == 1) { panelBinding.apply {
                    photoTagsLayout.visibility = View.VISIBLE
                    val scale = resources.displayMetrics.density
                    tags.forEach { tag ->
                        val textView = TextView(activity)
                        textView.text = tag
                        textView.setTextColor(requireActivity().getColor(R.color.colorLightGrey))
                        textView.setPadding(
                            dpToPx(scale, 16), 8, dpToPx(scale, 16), 0)
                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            dpToPx(scale, 30))

                        params.setMargins(
                            dpToPx(scale, 8), 0, 0, dpToPx(scale, 10)
                        )
                        textView.apply {
                            layoutParams = params
                            background = ContextCompat.getDrawable(
                                requireActivity(), R.drawable.tag_background)
                            setOnClickListener { mCallBacks?.onTagClick(tag, false) }
                        }
                        tagsWrapper.addView(textView)

                    } }
                }

                panelBinding.apply {
                    viewsQuantity.text = views
                    dateTaken.text = photoDateTaken
                    photoResolution.text = size
                    photoLicense.text = license
                    photoReportLayout.setOnClickListener {
                        Intent().apply {
                            action = Intent.ACTION_VIEW
                            addCategory(Intent.CATEGORY_BROWSABLE)
                            data = Uri.parse(REPORT_URL)
                            startActivity(this)
                        }
                    }
                }

                val fallDawnAnim = AnimationUtils
                    .loadAnimation(activity, R.anim.item_animation_fall_down).also {
                        it.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {}
                            override fun onAnimationEnd(animation: Animation?) {
                                bindingHolder.scrollView.visibility = View.VISIBLE
                            }
                            override fun onAnimationRepeat(animation: Animation?) {}
                        })
                    }
                bindingHolder.scrollView.startAnimation(fallDawnAnim)
            }

        }

        override fun onPanelSlide(panel: View?, slideOffset: Float) {
            bindingHolder.apply {
                photoName.alpha = ALPHA_DEFAULT - slideOffset
                divider.alpha = ALPHA_DEFAULT - slideOffset
            }
        }

        override fun onPanelStateChanged(
            panel: View?,
            previousState: SlidingUpPanelLayout.PanelState?,
            newState: SlidingUpPanelLayout.PanelState?

        ) {

            if (_binding == null) { return }
            binding.viewPager.isUserInputEnabled = when (newState) {
                SlidingUpPanelLayout.PanelState.DRAGGING -> false
                else -> true
            }

            if(newState == SlidingUpPanelLayout.PanelState.DRAGGING
                && previousState == SlidingUpPanelLayout.PanelState.COLLAPSED
                && bindingHolder.scrollView.childCount == 0) {

                val itemNew = mAdapter.peek(binding.viewPager.currentItem)!!
                val panelBinding = PhotoInfoSlidingUpPanelBinding.bind(photoDetailView)
                    bindingHolder.scrollView.addView(photoDetailView)
                val query = Query(
                    id = itemNew.id,
                    oauthToken = getOauthToken(requireContext()) ?: "",
                    oauthTokenSecret = getOauthTokenSecret(requireContext()) ?: ""
                )

                if (itemNew.exifResponse == null) {
                    panelBinding.cameraProgress.visibility = View.VISIBLE
                    lifecycleScope.launch {
                        val exifResponse = viewModel.getPhotoExif(query)
                        itemNew.exifResponse = exifResponse
                        if (exifResponse.stat == RESPONSE_DATA_OK) {
                            initPhotoExif(exifResponse.data!!, panelBinding)
                        }
                        panelBinding.cameraProgress.visibility = View.GONE
                    }
                } else {
                    if (itemNew.exifResponse!!.stat == RESPONSE_DATA_OK) {
                        initPhotoExif(itemNew.exifResponse!!.data!!, panelBinding)
                    }
                }

                if (itemNew.photoInfoResponse == null) {
                    bindingHolder.slidePanelProgress.visibility = View.VISIBLE
                    viewLifecycleOwner.lifecycleScope.launch {
                        val photoInfoResponse = viewModel.getPhotoInfo(query)
                        if (photoInfoResponse.stat == RESPONSE_DATA_OK) {
//                            photoInfoResponse.data?.camera = itemNew.camera
                            itemNew.photoInfoResponse = photoInfoResponse
                            initMainFields(itemNew.photoInfoResponse!!.data!!, panelBinding)
                            bindingHolder.slidePanelProgress.visibility = View.GONE
                        } else {
                            bindingHolder.slidePanelProgress.visibility = View.GONE
                            Snackbar.make(bindingHolder.root, R.string.no_internet_connection_error, Snackbar.LENGTH_SHORT)
                                .setTextColor(resources.getColor(R.color.colorWhite, null))
                                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                                .show()
                        }
                    }
                } else{
                    initMainFields(itemNew.photoInfoResponse!!.data!!, panelBinding)
                }
            }

            if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                try {
                    val photoDetailView = bindingHolder.scrollView.getChildAt(0)
                    val panelBinding = PhotoInfoSlidingUpPanelBinding.bind(photoDetailView)
                    panelBinding.mapView.apply {
                        onPause()
                        onStop()
                        onDestroy()
                    }

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private inner class NestedScrollableViewHelper(
        val nestedScrollView : NestedScrollView
    ) : ScrollableViewHelper() {
        override fun getScrollableViewScrollPosition(
            scrollableView: View?,
            isSlidingUp: Boolean
        ): Int {
            return when (isSlidingUp) {
                true -> nestedScrollView.scrollY
                else -> {
                    val child = nestedScrollView.getChildAt(0)
                    child.bottom - (nestedScrollView.height + nestedScrollView.scrollY)
                }
            }
        }
    }

    private inner class DoubleTapListener(
        private val bindingHolder: FragmentPhotoFullBinding,
        private val animateUI: (Boolean) -> Unit
    ) : GestureDetector.OnDoubleTapListener {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            animateUI(mPhotoInfoVisible)
            if (this@PhotoFullFragment::mJob.isInitialized && mJob.isActive) {
                mJob.cancel()
                Snackbar.make(binding.root, R.string.slide_show_stopped, Snackbar.LENGTH_SHORT)
                    .setTextColor(resources.getColor(R.color.colorWhite, null))
                    .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
                    .show()
            }
            return false
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            bindingHolder.photoDetailView.apply { when(scale != DOUBLE_TAP_SCALE) {
                true -> setScale(DOUBLE_TAP_SCALE, true)
                else -> setScale(bindingHolder.photoDetailView.minimumScale, true)
            } }
            return false
        }

        override fun onDoubleTapEvent(e: MotionEvent?) = false

    }

    private inner class PhotoLoadListener(
        private val binding: FragmentPhotoFullBinding
    ) : RequestListener<Drawable>  {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            binding.apply {
                progressBar.visibility = View.GONE
                photoDetailView.scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            binding.progressBar.visibility = View.GONE
            return false
        }
    }

    private inner class PhotoPagingAdapter : PhotoDataPagingAdapter() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryHolder {
            val view = layoutInflater.inflate(R.layout.fragment_photo_full, parent, false)
            return PhotoFullHolder(view)
        }

        override fun onViewRecycled(holder: GalleryHolder) {
            (holder as PhotoFullHolder).apply {
                closeSlidingPanel()
            }
            super.onViewRecycled(holder)
        }

        override fun onViewAttachedToWindow(holder: GalleryHolder) {
            (holder as PhotoFullHolder).apply {
                photoInfoPanelsCheck()
            }
            super.onViewAttachedToWindow(holder)
        }
    }

    private class DepthPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val layout = view as SlidingUpPanelLayout
            val frameLayout1 = layout.getChildAt(0) as FrameLayout
            val userIconAndNameView = frameLayout1.getChildAt(1)
            userIconAndNameView.translationX = frameLayout1.width * -position
            val frameLayout2 = layout.getChildAt(1) as FrameLayout
            val panelPullButton = frameLayout2.getChildAt(0)
            panelPullButton.translationX = frameLayout1.width * -position
            val photoControlButtons = frameLayout2.getChildAt(1)
            photoControlButtons.translationX = frameLayout1.width * -position
            val photoView = frameLayout1.getChildAt(0)
            val pageWidth = photoView.width
            when {
                position < -1 -> { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    photoView.alpha = 0f
                }
                position <= 0 -> { // [-1,0]
                    // Use the default slide transition when moving to the left page
                    photoView.alpha = 1f
                    photoView.translationX = 0f
                    photoView.scaleX = 1f
                    photoView.scaleY = 1f
                }
                position <= 1 -> { // (0,1]
                    // Fade the page out.
                    photoView.alpha = 1 - position

                    // Counteract the default slide transition
                    photoView.translationX = pageWidth * -position

                    // Scale the page down (between MIN_SCALE and 1)
                    val scaleFactor = (MIN_SCALE
                            + (1 - MIN_SCALE) * (1 - abs(position)))
                    photoView.scaleX = scaleFactor
                    photoView.scaleY = scaleFactor
                }
                else -> { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    photoView.alpha = 0f
                }
            }
        }

        companion object {
            private const val MIN_SCALE = 0.75f
        }
    }

    inner class ErrorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ErrorLayoutBinding.bind(view)

        fun onBind(message: String?) {
            binding.errorText.text = message
            binding.errorText.setOnClickListener {
                mCallBacks?.onError(query)
            }
        }
    }

    inner class ErrorAdapter (private val message: String?) : RecyclerView.Adapter<ErrorViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
            val view = layoutInflater.inflate(R.layout.error_layout, parent, false)
            return ErrorViewHolder(view)
        }

        override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
            holder.onBind(message)
        }

        override fun getItemCount() = 1

    }

    private fun showServiceDenyDialog (message: Int) {

        val builder = AlertDialog.Builder(requireContext(), R.style.DialogAllCapsFalseStyle).apply {
            setMessage(getString(message))
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

    fun slideShow () {
        val animFadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        val animFadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out).apply {
            setAnimationListener(object : Animation.AnimationListener{
                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {
                    binding.viewPager.apply {
                        startAnimation(animFadeIn)
                        if (binding.viewPager.currentItem == mAdapter.itemCount - 1) {
                            setCurrentItem(0, false)
                        } else {
                            setCurrentItem(this.currentItem + 1, false)
                        }
                    }
                }
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
        binding.viewPager.keepScreenOn = true
        val delayTime = AppPreferences.getSlideShowInterval(requireContext())
        mJob = lifecycleScope.launch {
            while (true) {
                delay(delayTime)
                binding.viewPager.startAnimation(animFadeOut)
            }
        }
    }

    val downloadService : (Photo, Drawable?) -> Unit = { photo: Photo, _ ->
        if (_binding != null) {
            val photoUrl = photo.getDownloadUrl(mDownloadQuality)
            when (photoUrl.isNullOrBlank()) {
                true -> {
                    Snackbar.make(
                        binding.root,
                        R.string.no_internet_connection_error,
                        Snackbar.LENGTH_SHORT
                    )
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                        .show()
                }
                else -> {
                    Snackbar.make(binding.root, R.string.download_start, Snackbar.LENGTH_SHORT)
                        .setTextColor(resources.getColor(R.color.colorWhite, null))
                        .setBackgroundTint(resources.getColor(R.color.colorBlueFlickr, null))
                        .show()
                    val dm =
                        requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val r = DownloadManager.Request(Uri.parse(photoUrl))
                    val fileName = "FLICKR_IMG_${System.currentTimeMillis()}.jpg"
                    r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    dm.enqueue(r)
                }
            }
        }
    }


    val shareService : (Photo, Drawable?) -> Unit = { photo: Photo, drawable: Drawable? ->
        try {
            val bitmap = (drawable as BitmapDrawable).bitmap
            val fileName = "${photo.title}\n\n${getString(R.string.original_link)}${photo.getDownloadUrl(LogoIcon.Photo.MAX.prefix)}"
            var fos: OutputStream?
            var imageUri: Uri?
            activity?.contentResolver.also { resolver ->
                imageUri = resolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, ContentValues())
                fos = imageUri?.let { resolver?.openOutputStream(it) }
            }
            fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
            val intent = Intent(Intent.ACTION_SEND)
                .apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_STREAM, imageUri as Uri)
                    putExtra(Intent.EXTRA_TEXT, fileName)
                }
            startActivity(intent)
        } catch (e : ClassCastException) {
            Snackbar.make(binding.root, R.string.no_internet_connection_error, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorWhite, null))
                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                .show()
        } catch (e : NullPointerException) {
            Snackbar.make(binding.root, R.string.internet_connection_error, Snackbar.LENGTH_SHORT)
                .setTextColor(resources.getColor(R.color.colorWhite, null))
                .setBackgroundTint(resources.getColor(R.color.colorPinkFlickr, null))
                .show()
        }

    }

    private fun runService (
        photo: Photo,
        drawable: Drawable?,
        warningMessage: Int,
        service: (photo: Photo, drawable: Drawable?) -> Unit
    ) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                service(photo, drawable)
            }
            shouldShowRequestPermissionRationale(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                showServiceDenyDialog(warningMessage)
            }
            else -> {
                mServicePhoto = photo
                mServiceDrawable = drawable
                mService = service
                mRequestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }








}