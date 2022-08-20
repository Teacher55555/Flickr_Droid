package com.armageddon.android.flickrdroid.model

import android.text.Spanned
import kotlin.collections.LinkedHashMap

class PhotoInfo (
    val description: Spanned,
    val views: String,
    val photoDateTaken: String,
    val location: Location,
    val tags: List<String>,
    val sizes: LinkedHashMap<String, String>,
    val size: String,
    val prefix: String,
    val license: String,
    var camera: String = ""
) {
    class Location (
       val latitude: Double = 0.0,
       val longitude: Double = 0.0,
       val country: String? = null,
       val region: String? = null,
       val locality: String? = null,
    ) {

        fun getAddress() : String {
            return listOfNotNull(country, region, locality)
                .joinToString(", ")
                .trim(',')
        }


        override fun toString() = when (latitude == 0.0) {
            true -> ""
            else -> super.toString()
        }
      }



}