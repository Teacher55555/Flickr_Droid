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

class FavsPhotoDeserializer : JsonDeserializer<FlickrResponse<Photo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Photo> {
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val govno = json?.asJsonObject?.getAsJsonObject("photos")
        val totaliz = govno?.get("total")?.asString
        var errorMessage = ""
        if (status == RESPONSE_DATA_FAIL || totaliz == "0") {
            errorMessage = json?.asJsonObject?.get("message")?.asString ?: RESPONSE_DATA_FAIL
            return FlickrResponse<Photo>().apply {
                message = errorMessage
                message = "govno"
            }
        }
        val responseObject = json?.asJsonObject?.getAsJsonObject("photos")
            ?: json?.asJsonObject?.getAsJsonObject("photoset")
        val responseDataArray = responseObject?.getAsJsonArray("photo")
        val photoItemType = object : TypeToken<ArrayList<Photo>>() {}.type
        return FlickrResponse<Photo>().apply {
            dataArray = Gson().fromJson(responseDataArray.toString(), photoItemType)
            responseObject?.let {
                page = it.get("page").asInt
                pages = it.get("pages").asInt
                perpage = it.get("perpage").asInt
                total = it.get("total").asInt
                stat = status
                message = errorMessage
            }
        }
    }
}