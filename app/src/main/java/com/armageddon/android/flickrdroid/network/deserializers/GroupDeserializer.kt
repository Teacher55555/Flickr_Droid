package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.Group
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class GroupDeserializer : JsonDeserializer<FlickrResponse<Group>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Group> {
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val responseObject = json?.asJsonObject?.getAsJsonObject("groups")
        val responseDataArray = responseObject?.getAsJsonArray("group")
        val groupType = object : TypeToken<ArrayList<Group>>() {}.type
        return FlickrResponse<Group>().apply {
            responseDataArray?.let {
                dataArray = Gson().fromJson(it.toString(), groupType)
            }
            responseObject?.let {
                page = it.get("page")?.asInt ?: 0
                pages = it.get("pages")?.asInt ?: 0
                perpage = it.get("perpage")?.asInt ?: 0
                total = it.get("total")?.asInt ?: 0
                stat = status
            }
        }
    }
}