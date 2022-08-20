package com.armageddon.android.flickrdroid.ui.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.AppPreferences
import com.armageddon.android.flickrdroid.common.AppPreferences.getAppTheme
import com.armageddon.android.flickrdroid.common.AppPreferences.getGalleryViewColumns
import com.armageddon.android.flickrdroid.common.AppPreferences.getSlideShowInterval
import com.armageddon.android.flickrdroid.common.AppPreferences.setAppTheme
import com.armageddon.android.flickrdroid.common.AppPreferences.setGalleryViewColumns
import com.armageddon.android.flickrdroid.common.AppPreferences.setPhotoDownloadQuality
import com.armageddon.android.flickrdroid.common.AppPreferences.setSlideShowInterval
import com.armageddon.android.flickrdroid.common.Converter
import com.armageddon.android.flickrdroid.common.LogoIcon
import com.armageddon.android.flickrdroid.databinding.FragmentSettingsBinding
import com.armageddon.android.flickrdroid.ui.fragments.detail.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.cache.DiskCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

private const val PRIVACY_URL = "https://www.flickr.com/help/privacy"

class SettingsFragment: Fragment(), Converter {
    private var _binding : FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private var mCallBacks : CallBacks? = null

    interface CallBacks {
        fun onThemeChanged()
        fun onColumnsChanged()
        fun onAboutClick()
    }

    companion object {
        fun newInstance(): SettingsFragment {
            val args = Bundle()
            val fragment = SettingsFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> activity?.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
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
        _binding = FragmentSettingsBinding.inflate(layoutInflater, container, false)
        (activity as AppCompatActivity).apply {
            setSupportActionBar(binding.toolbar)
            setHasOptionsMenu(true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cashCalc()

        binding.apply {
            cacheSizeView.setOnClickListener {
                lifecycleScope.launch(Dispatchers.IO) {
                    Glide.get(requireContext()).clearDiskCache()
                    cashCalc()
                }
            }
            when (getGalleryViewColumns(requireContext())) {
                1 -> settings1InRowButton
                    .setColorFilter(getAttrColor(requireActivity(), R.attr.colorAccent))
                2 -> settings2InRowButton
                    .setColorFilter(getAttrColor(requireActivity(), R.attr.colorAccent))
                3 -> settings3InRowButton
                    .setColorFilter(getAttrColor(requireActivity(), R.attr.colorAccent))
            }
            settings1InRowButton.setOnClickListener {
                setColumns(it, settings2InRowButton, settings3InRowButton, 1)
            }
            settings2InRowButton.setOnClickListener {
                setColumns(it, settings1InRowButton, settings3InRowButton, 2)
            }
            settings3InRowButton.setOnClickListener {
                setColumns(it, settings1InRowButton, settings2InRowButton, 3)
            }

            when (AppPreferences.getPhotoDownloadQuality(requireContext())) {
                LogoIcon.Photo.MAX.prefix -> setItemColor(photoDownloadMaxButton, true)
                LogoIcon.Photo.LARGE_2048.prefix -> setItemColor(photoDownload2048Button, true)
                LogoIcon.Photo.LARGE_1024.prefix -> setItemColor(photoDownload1024Button, true)
                LogoIcon.Photo.MEDIUM_640.prefix -> setItemColor(photoDownload640Button, true)
            }

            photoDownload640Button.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.PHOTO_DOWNLOAD_QUALITY,
                    LogoIcon.Photo.MEDIUM_640.prefix,
                    v,
                    photoDownload1024Button,
                    photoDownload2048Button,
                    photoDownloadMaxButton
                )
            }

            photoDownload1024Button.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.PHOTO_DOWNLOAD_QUALITY,
                    LogoIcon.Photo.LARGE_1024.prefix,
                    v,
                    photoDownload640Button,
                    photoDownload2048Button,
                    photoDownloadMaxButton
                )
            }

            photoDownload2048Button.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.PHOTO_DOWNLOAD_QUALITY,
                    LogoIcon.Photo.LARGE_2048.prefix,
                    v,
                    photoDownload640Button,
                    photoDownload1024Button,
                    photoDownloadMaxButton
                )
            }

            photoDownloadMaxButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.PHOTO_DOWNLOAD_QUALITY,
                    LogoIcon.Photo.MAX.prefix,
                    v,
                    photoDownload640Button,
                    photoDownload1024Button,
                    photoDownload2048Button
                )
            }

            when (getSlideShowInterval(requireContext())) {
                SLIDESHOW_INTERVAL_3_SEC -> setItemColor(slideshow3SecButton, true)
                SLIDESHOW_INTERVAL_4_SEC -> setItemColor(slideshow4SecButton, true)
                SLIDESHOW_INTERVAL_5_SEC -> setItemColor(slideshow5SecButton, true)
                SLIDESHOW_INTERVAL_10_SEC -> setItemColor(slideshow10SecButton, true)
                SLIDESHOW_INTERVAL_15_SEC -> setItemColor(slideshow15SecButton, true)
            }

            slideshow3SecButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.SLIDE_SHOW_DURATION,
                    SLIDESHOW_INTERVAL_3_SEC.toString(),
                    v,
                    slideshow4SecButton,
                    slideshow5SecButton,
                    slideshow10SecButton,
                    slideshow15SecButton
                )
            }

            slideshow4SecButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.SLIDE_SHOW_DURATION,
                    SLIDESHOW_INTERVAL_4_SEC.toString(),
                    v,
                    slideshow3SecButton,
                    slideshow5SecButton,
                    slideshow10SecButton,
                    slideshow15SecButton
                )
            }

            slideshow5SecButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.SLIDE_SHOW_DURATION,
                    SLIDESHOW_INTERVAL_5_SEC.toString(),
                    v,
                    slideshow3SecButton,
                    slideshow4SecButton,
                    slideshow10SecButton,
                    slideshow15SecButton
                )
            }

            slideshow10SecButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.SLIDE_SHOW_DURATION,
                    SLIDESHOW_INTERVAL_10_SEC.toString(),
                    v,
                    slideshow3SecButton,
                    slideshow4SecButton,
                    slideshow5SecButton,
                    slideshow15SecButton
                )
            }

            slideshow15SecButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.SLIDE_SHOW_DURATION,
                    SLIDESHOW_INTERVAL_15_SEC.toString(),
                    v,
                    slideshow3SecButton,
                    slideshow4SecButton,
                    slideshow5SecButton,
                    slideshow10SecButton
                )
            }

            when (getAppTheme(requireContext())) {
                ActivityUtils.THEME_DEFAULT -> setItemColor(themeDefaultButton, true)
                ActivityUtils.THEME_WHITE -> setItemColor(themeLightButton, true)
                ActivityUtils.THEME_DARK -> setItemColor(themeDarkButton, true)
            }

            themeDefaultButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.APP_THEME,
                    ActivityUtils.THEME_DEFAULT.toString(),
                    v,
                    themeLightButton,
                    themeDarkButton
                )
            }

            themeLightButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.APP_THEME,
                    ActivityUtils.THEME_WHITE.toString(),
                    v,
                    themeDefaultButton,
                    themeDarkButton
                )
            }

            themeDarkButton.setOnClickListener { v: View ->
                setMenuItemActive(
                    MenuMethod.APP_THEME,
                    ActivityUtils.THEME_DARK.toString(),
                    v,
                    themeDefaultButton,
                    themeLightButton
                )
            }

            historySwitch.apply {
                isChecked = AppPreferences.getHistorySearch(requireContext())
                setOnCheckedChangeListener { _, isChecked ->
                    AppPreferences.setHistorySearch(requireContext(), isChecked)
                }
            }
            aboutTextView.setOnClickListener {
                mCallBacks?.onAboutClick()
            }
            privacyPolicyView.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(PRIVACY_URL)
                startActivity(i)
            }
        }
    }


    private fun calculateSize(dir: File?): Long {
        if (dir == null) return 0
        if (!dir.isDirectory) return dir.length()
        var result: Long = 0
        val children = dir.listFiles()
        if (children != null)
            for (child in children)
                result += calculateSize(child)
        return result
    }


    private fun cashCalc () {
        val dir = File(requireActivity().cacheDir, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR)
        var totalSize: Long = 0
            totalSize += calculateSize(dir)
        val sizeText = Formatter
            .formatFileSize(requireContext(), totalSize)
        binding.cacheSizeView.text = sizeText
        binding.progress.visibility = View.INVISIBLE
    }

    private fun setColumns(
        imageView: View, buttonClear1: ImageView, buttonClear2: ImageView,
        columns: Int
    ) {
        if (getGalleryViewColumns(requireContext()) != columns) {
            (imageView as ImageView).setColorFilter(getAttrColor(requireActivity(), R.attr.colorAccent))
            setGalleryViewColumns(requireContext(), columns)
            buttonClear1.clearColorFilter()
            buttonClear2.clearColorFilter()
            mCallBacks?.onColumnsChanged()
        }
    }

    private fun setItemColor(button: Button, isActive: Boolean) {
        if (isActive) {
            button.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.settings_active_background
            )
            button.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite))
        } else {
            button.background = ContextCompat.getDrawable(
                requireActivity(),
                R.drawable.settings_default_background
            )
            button.setTextColor(getAttrColor(requireActivity(), R.attr.titleTextColor))
        }
    }

    private fun setMenuItemActive(
        menuMethod: MenuMethod,
        menuValue: String,
        activeButton: View,
        vararg resetButtons: Button
    ) {
        when (menuMethod) {
            MenuMethod.PHOTO_DOWNLOAD_QUALITY -> setPhotoDownloadQuality(
                requireContext(), menuValue
            )
            MenuMethod.SLIDE_SHOW_DURATION -> setSlideShowInterval(
                requireContext(), menuValue.toLong()
            )
            MenuMethod.APP_THEME -> {
                val newTheme = menuValue.toInt()
                val oldTheme = getAppTheme(requireContext())
                if (newTheme != oldTheme) {
                    ActivityUtils.isThemeChanged = true
                    setAppTheme(requireContext(), newTheme)
                }
                mCallBacks?.onThemeChanged()
            }
        }
        setItemColor((activeButton as Button), true)
        for (btn in resetButtons) {
            setItemColor(btn, false)
        }
    }

    private enum class MenuMethod {
        PHOTO_DOWNLOAD_QUALITY,
        SLIDE_SHOW_DURATION,
        APP_THEME
    }
}