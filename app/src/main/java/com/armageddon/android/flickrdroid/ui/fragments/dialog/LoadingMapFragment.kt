package com.armageddon.android.flickrdroid.ui.fragments.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.LogoIcon
import com.armageddon.android.flickrdroid.model.Photo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class LoadingMapFragment : DialogFragment(), View.OnClickListener {

    private var mCallBacks: CallBacks? = null
    var mProgressBar: ProgressBar? = null
    var mProgressLayout: LinearLayout? = null
    var mClose: ImageView? = null
    var mMessage: TextView? = null
    var mErrorMessage: TextView? = null
    var mBitmaps = mutableMapOf<Photo, Bitmap>()
    var stopJob = false


    override fun onClick(v: View?) {
        stopJob = true
        mProgressBar = null
        mCallBacks?.onShowCancel()
        dismiss()
    }

    fun load (photoList: List<Photo>) {
        if (photoList.isEmpty()) {
            mProgressLayout?.visibility = View.INVISIBLE
            mErrorMessage?.visibility = View.VISIBLE
            return
        }
        var count = 0
       mMessage?.text = getString(R.string.add_photos_to_the_map, mProgressBar?.max)
       lifecycleScope.launch(Dispatchers.IO) {
            photoList.forEach {
                val url = it.getPhotoUrl(LogoIcon.Photo.SMALL_240.prefix)
                try {
                    Glide.with(this@LoadingMapFragment)
                        .asBitmap()
                        .load(url)
                        .transform(CenterCrop(), RoundedCorners(25))
                        .into(object : CustomTarget<Bitmap?>(500, 400) {
                            override fun onLoadCleared(placeholder: Drawable?) {}
                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                count++
                                mProgressBar?.progress = count
                                if (count == mProgressBar?.max) {
                                    mCallBacks?.onLoadFinished()
                                }

                                if (stopJob) {
                                    return
                                }
                            }

                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap?>?
                            ) {
                                if (stopJob) {
                                    return
                                }
                                mBitmaps[it] = resource
                                count++
                                mProgressBar?.progress = count
                                if (count == mProgressBar?.max) {
                                    mCallBacks?.onLoadFinished()
                                }
                            }
                        })
                } catch (e: Exception) { }
            }
        }
    }

    interface CallBacks {
        fun onShowCancel()
        fun onLoadFinished()
    }

    companion object {
        fun newInstance(): LoadingMapFragment {
            val args = Bundle()
            val fragment = LoadingMapFragment()
            fragment.arguments = args
            return fragment
        }
    }

    fun setCallBacks(callBacks: CallBacks) {
        mCallBacks = callBacks
    }

    override fun onDetach() {
        super.onDetach()
        mCallBacks = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val window = dialog?.window
        val density = requireActivity().resources.displayMetrics.density

        /* Dialog width and height */
        val width = (300 * density + 0.5f).toInt()
        val height = (150 * density + 0.5f).toInt()
        window?.setLayout(width, height)
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    fun setProgressMax(value: Int) {
        mProgressBar?.max = value
    }


    fun setLoadingProgress(progress: Int) {
        mProgressBar?.progress = progress
        if (progress == mProgressBar?.max) {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v: View = layoutInflater.inflate(R.layout.loading_dialog, null)
        mErrorMessage = v.findViewById(R.id.error_message)
        mProgressBar = v.findViewById(R.id.progress_bar)
        mProgressLayout = v.findViewById(R.id.progress_layout)
        mClose = v.findViewById(R.id.icon_close)
        mMessage = v.findViewById(R.id.message)
        mClose?.setOnClickListener(this)
        return AlertDialog.Builder(activity, R.style.LoadingDialogAnimationStyle)
            .setView(v)
            .show()
    }

}