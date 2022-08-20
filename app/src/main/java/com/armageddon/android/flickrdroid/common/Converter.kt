package com.armageddon.android.flickrdroid.common

import android.content.Context
import android.text.Html
import android.text.SpannedString
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

interface Converter {
    fun pxToDp(view: View, density: Float, columns: Int): ViewGroup.LayoutParams {
        val pixels: Int = when (columns) {
            1 -> (400 * density + 0.5f).toInt()
            2 -> (190 * density + 0.5f).toInt()
            else -> (120 * density + 0.5f).toInt()
        }
        val layoutParams = view.layoutParams
        layoutParams.height = pixels
        return layoutParams
    }

    fun dpToPx(scale: Float, sizeInDp: Int) = (sizeInDp * scale + 0.5f).toInt()

    fun getAttrColor(context: Context, color: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(color, typedValue, true)
        return typedValue.data
    }

    fun String.spanned () = this.let {
        val text = it.replace("\n".toRegex(), "<br>")
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
    } ?: SpannedString("")

    fun <T: ViewGroup> T.bindNotBlank(field: String?) {
        this.visibility = when (field.isNullOrBlank()) {
            true -> View.GONE
            else -> {
                val textView = getChildAt(1) as TextView
                textView.text = field
                View.VISIBLE
            }
        }
    }

    fun <T: TextView> T.bindNotBlank(field: String?) {
        this.visibility = when (field.isNullOrBlank()) {
            true -> View.GONE
            else -> {
                this.text = field
                View.VISIBLE
            }
        }
    }

    fun <T: Button> T.bindNotBlank(field: String?) {
        this.visibility = when (field.isNullOrBlank()) {
            true -> View.GONE
            else -> {
                this.text = field
                View.VISIBLE
            }
        }
    }

    fun String.addCreatedText() = "<u><b>Date create</b></u>: $this"

}



//    fun getPlaceHolder (context: Context) = when (ActivityUtils.theme) {
//        ActivityUtils.THEME_DARK -> ContextCompat.getDrawable(context, R.drawable.place_holder_dark)
//        else -> ContextCompat.getDrawable(context, R.drawable.place_holder_white)
//    }


