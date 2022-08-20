package com.armageddon.android.flickrdroid.model

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.armageddon.android.flickrdroid.common.*
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.google.gson.annotations.SerializedName
import java.io.IOException
import java.io.Serializable
import java.lang.IndexOutOfBoundsException
import java.util.*

//const val PHOTO_SIZE_MAX = 0
//const val PHOTO_SIZE_640 = 1
//const val PHOTO_SIZE_1024 = 2
//const val PHOTO_SIZE_2048 = 3

data class Photo  (
    val id: String = "",
    val title: String = "",
    var owner: String = "",
    var ownername: String = "",
    val realname: String = "",
    val secret: String = "",
    val server: String = "",
    val farm: String = "",
    val iconserver: String = "",
    val iconfarm: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @SerializedName("count_comments") var countComments: String = "",
    @SerializedName("count_faves") var countFaves: String = "",
    val camera: String = "",
    private var isfavorite: Int = 0,
    private var ispublic: Int = 0,
    private var isfriend: Int = 0,
    private var isfamily: Int = 0,
) : LogoIcon, Serializable {

    var photoInfoResponse: FlickrResponse<PhotoInfo>? = null
    var exifResponse: FlickrResponse<PhotoExif>? = null

    fun getDownloadUrl(quality: String) : String? {
        return photoInfoResponse?.let { response ->
            response.data?.let {
                val lastLabel = it.prefix
                val photoSizes = it.sizes
                photoSizes.getOrElse(quality) {
                    photoSizes[lastLabel]
                }
            }
        }
    }

    var privacy
    get() = when {
            ispublic == 1 -> PhotoPrivacy.PUBLIC.state
            isfriend == 1 && isfamily == 1 -> PhotoPrivacy.FRIENDS_AND_FAMILY.state
            isfamily == 1 -> PhotoPrivacy.FAMILY.state
            isfriend == 1 -> PhotoPrivacy.FRIENDS.state
            else -> PhotoPrivacy.PRIVATE.state
    }
    set(value) {
        ispublic = 0
        isfriend = 0
        isfamily = 0

        when (value) {
            PhotoPrivacy.PUBLIC.state -> ispublic = 1
            PhotoPrivacy.FRIENDS_AND_FAMILY.state -> { isfriend = 1; isfamily = 1 }
            PhotoPrivacy.FAMILY.state -> isfamily = 1
            PhotoPrivacy.FRIENDS.state -> isfriend = 1
        }
    }

    fun getOwnerIcon(size: String = LogoIcon.Icon.ICON_NORMAL_100PX.prefix) = getIconUrl(iconfarm, iconserver, owner, size)
    fun getOwnerName() = realname.ifBlank { ownername }
    fun increaseCommentsCount() { countComments = (countComments.toInt() + 1).toString() }
    fun decreaseCommentsCount() {
        var count: Int = countComments.toInt() - 1
        if (count < 0) { count = 0 }
        countComments = count.toString()
    }
    fun increaseFavesCount() { countFaves = (countFaves.toInt() + 1).toString() }
    fun decreaseFavesCount() {
        var count: Int = countFaves.toInt() - 1
        if (count < 0) { count = 0 }
        countFaves = count.toString()
    }

    fun getLocation(context: Context?): String? {
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val city = addresses[0].locality
            val state = addresses[0].adminArea
            val country = addresses[0].countryName
            var str: String? = ""
            if (city != null) {
                str = "$city, "
            }
            if (state != null) {
                str += "$state, "
            }
            if (country != null) {
                str += country
            }
            str
        } catch (e: IOException ) {
            e.printStackTrace()
            null
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            null
        }
    }


    fun getPhotoUrl(size: String = LogoIcon.Photo.LARGE_1024.prefix) : String {
        return getPhotoUrl(server, id, secret, size)
    }

    var isFavorite
        get() = isfavorite != 0
        set(value) { isfavorite = when (value) {
                true -> 1
                else -> 0
            }
        }
}

