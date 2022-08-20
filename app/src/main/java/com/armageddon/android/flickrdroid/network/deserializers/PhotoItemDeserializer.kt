package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class PhotoItemDeserializer: JsonDeserializer<FlickrResponse<Photo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Photo> {
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        var errorMessage = ""
        if (status == RESPONSE_DATA_FAIL) {
            errorMessage = json?.asJsonObject?.get("message")?.asString ?: RESPONSE_DATA_FAIL
            return FlickrResponse<Photo>().apply {
                message = errorMessage
            }
        }
        val resultDataArray: List<Photo>
        val photoItemType = object : TypeToken<ArrayList<Photo>>() {}.type
        var responseObject = json?.asJsonObject?.getAsJsonObject("photos")
        if (responseObject != null ) {
            val responseDataArray = responseObject.getAsJsonArray("photo")
            resultDataArray = Gson().fromJson(responseDataArray.toString(), photoItemType)
        } else {
            responseObject = json?.asJsonObject?.getAsJsonObject("photoset")
            val responseDataArray = responseObject?.getAsJsonArray("photo")
            val owner = responseObject?.get("owner")?.asString ?: ""
            val ownerName = responseObject?.get("ownername")?.asString ?: ""
            resultDataArray = Gson().fromJson<List<Photo>?>(responseDataArray.toString(), photoItemType).map {
               it.apply {
                   this.owner = owner
                   this.ownername = ownerName
               }
            }
        }

        return FlickrResponse<Photo>().apply {
            dataArray = resultDataArray
            val perPage = responseObject?.get("perpage")?.asInt ?: responseObject!!.get("per_page").asInt
            responseObject?.let {
                page = it.get("page").asInt
                pages = it.get("pages").asInt
                perpage = perPage
                total = it.get("total").asInt
                stat = status
                message = errorMessage
            }
        }
    }
}