package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.PhotoComment
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_NO_COMMENTS
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class PhotoCommentDeserializer: JsonDeserializer<FlickrResponse<PhotoComment>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<PhotoComment> {
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        var errorMessage = ""
        if (status == RESPONSE_DATA_FAIL) {
            errorMessage = json?.asJsonObject?.get("message")?.asString ?: RESPONSE_DATA_FAIL
            return FlickrResponse<PhotoComment>().apply {
                message = errorMessage
            }
        }
        val responseObject = json?.asJsonObject?.getAsJsonObject("comments")
        val responseDataArray = responseObject?.getAsJsonArray("comment")
            ?: return FlickrResponse<PhotoComment>().apply {
                stat = RESPONSE_NO_COMMENTS
                message = "NO COMMENTS"
            }
        val commentItemType = object : TypeToken<ArrayList<PhotoComment>>() {}.type
        return FlickrResponse<PhotoComment>().apply {
            dataArray = Gson().fromJson(responseDataArray.toString(), commentItemType)
            responseObject.let {
                stat = status
                message = errorMessage
            }
        }
    }
}