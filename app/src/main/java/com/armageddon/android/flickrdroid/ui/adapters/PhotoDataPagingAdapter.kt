package com.armageddon.android.flickrdroid.ui.adapters

import android.view.View
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.model.Photo

abstract class PhotoDataPagingAdapter
    : PagingDataAdapter<Photo, PhotoDataPagingAdapter.GalleryHolder>(
        object : DiffUtil.ItemCallback<Photo> () {
            override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
               return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
                return oldItem == newItem
            }
        }) {

    override fun onBindViewHolder(holder: GalleryHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    abstract class GalleryHolder (view: View) : RecyclerView.ViewHolder (view) {
        abstract fun bind(item: Photo)
    }
}

