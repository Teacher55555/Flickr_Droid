package com.armageddon.android.flickrdroid.common

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils.THEME_WHITE
import com.armageddon.android.flickrdroid.common.ActivityUtils.theme


object ActivityUtils {
    const val THEME_DEFAULT = 0
    const val THEME_WHITE = 1
    const val THEME_DARK = 2
    var theme = THEME_DEFAULT
        private set
    var isThemeChanged = false


    fun hideStatusBar(activity: FragmentActivity) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    fun showStatusBar(activity: FragmentActivity) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            activity.window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    /** Set the theme of the activity, according to the configuration. */
    fun onActivityCreateSetTheme(activity: AppCompatActivity) {
        theme = AppPreferences.getAppTheme(activity)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && theme == THEME_DEFAULT) {
            val nightModeFlags = activity.resources.configuration.uiMode and
                    Configuration.UI_MODE_NIGHT_MASK
            theme = when (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                true -> THEME_DARK
                false -> THEME_WHITE
            }
        }
        onActivityCreateSetTheme(activity, theme)
    }

    /** Manually set the theme of the activity, according to the configuration. */
    fun onActivityCreateSetTheme(activity: AppCompatActivity, theme: Int) {
        when (theme) {
            THEME_DARK -> activity.setTheme(R.style.DarkMyTheme)
            else -> activity.setTheme(R.style.AppTheme)
        }
    }
}

fun <T: View> T.startAnimAndGone (animationId: Int)  {
    val animation = AnimationUtils.loadAnimation(this.context, animationId)
        .apply { setAnimationListener(object :
            Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) { visibility = View.GONE }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
    startAnimation(animation)
}

fun <T: View> T.startAnimAndShow (animationId: Int)  {
    this.visibility = View.VISIBLE
    val animation = AnimationUtils.loadAnimation(this.context, animationId)
    startAnimation(animation)
}

fun SwipeRefreshLayout.setColors () {
    val colorPinkFlickr = resources.getColor(R.color.colorPinkFlickr, null)
    val colorBlueFlickr = resources.getColor(R.color.colorBlueFlickr, null)
    val backgroundColor = when (theme) {
        THEME_WHITE -> {
            setColorSchemeColors(colorBlueFlickr)
            R.color.colorWhite
        }
        else -> {
            setColorSchemeColors(colorPinkFlickr)
            R.color.colorGreyDark
        }
    }
    setProgressBackgroundColorSchemeResource(backgroundColor)
}