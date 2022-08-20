package com.armageddon.android.flickrdroid.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Converter
import com.armageddon.android.flickrdroid.databinding.ErrorLayout2Binding
import com.armageddon.android.flickrdroid.network.execptions.FlickrException

class ErrorAdapter (private val error: FlickrException, val refresh: () -> Unit)
    : RecyclerView.Adapter<ErrorAdapter.ErrorViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ErrorViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.error_layout_2, parent, false)
        return ErrorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ErrorViewHolder, position: Int) {
        holder.onBind(error, refresh)
    }

    override fun getItemCount() = 1

    class ErrorViewHolder(view: View) : RecyclerView.ViewHolder(view), Converter {
        private val binding = ErrorLayout2Binding.bind(view)

        fun onBind(error: FlickrException, refreshUI: () -> Unit) {
            binding.apply {
                message.text = itemView.context.getString(error.getTextMessage())
                icon.setImageDrawable(ContextCompat.getDrawable(itemView.context, error.getDrawable()))
                if (error.showRefreshButton()) {
                    button.visibility = View.VISIBLE
                    button.setOnClickListener { refreshUI() }
                }
            }
        }
    }
}