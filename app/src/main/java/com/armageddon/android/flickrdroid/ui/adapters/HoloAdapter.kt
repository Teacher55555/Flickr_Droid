package com.armageddon.android.flickrdroid.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.ActivityUtils
import com.armageddon.android.flickrdroid.common.AppPreferences
import com.armageddon.android.flickrdroid.common.Converter
import com.bumptech.glide.Glide

private const val HOLO_ITEMS_SIZE = 50

class HollowAdapter : RecyclerView.Adapter<HollowAdapter.HollowHolder> (), Converter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HollowHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.photo_list_item, parent, false)
        return HollowHolder(view)
    }

    override fun onBindViewHolder(holder: HollowHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return HOLO_ITEMS_SIZE
    }

    inner class HollowHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = itemView.findViewById(R.id.photo_view)
        val frameLayout: FrameLayout = itemView.findViewById(R.id.frameLayout)
        val density = itemView.context.resources.displayMetrics.density
        val columns = AppPreferences.getGalleryViewColumns(itemView.context)

        init {
            frameLayout.layoutParams = pxToDp(frameLayout, density, columns)
        }

        fun bind() {
            Glide.with(itemView)
                .load("null")
                .placeholder(R.drawable.placeholder)
                .into(imageView)
        }
    }

}