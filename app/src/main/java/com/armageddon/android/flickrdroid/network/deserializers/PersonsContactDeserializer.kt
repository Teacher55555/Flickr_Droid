package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.PersonContact
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class PersonsContactDeserializer : JsonDeserializer<FlickrResponse<PersonContact>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<PersonContact> {
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val responseObject = json?.asJsonObject?.getAsJsonObject("contacts")
        val responseDataArray = responseObject?.getAsJsonArray("contact")
        val contactItemType = object : TypeToken<ArrayList<PersonContact>>() {}.type
        return FlickrResponse<PersonContact>().apply {
            dataArray = try {
                Gson().fromJson(responseDataArray.toString(), contactItemType)
            } catch (e : NullPointerException) {
                emptyList()
            }
            responseObject?.let {
                page = it.get("page").asInt
                pages = it.get("pages").asInt
                perpage = it.get("per_page").asInt
                total = it.get("total").asInt
                stat = status
            }
        }
    }
}