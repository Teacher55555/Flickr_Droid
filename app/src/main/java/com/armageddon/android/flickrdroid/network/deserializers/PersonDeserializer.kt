package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class PersonDeserializer : JsonDeserializer<FlickrResponse<Person>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Person> {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val responseMessage = json?.asJsonObject?.get("message")?.asString ?: ""

        json?.asJsonObject
            ?.getAsJsonObject("user")
            ?.asJsonObject?.get("id")
            ?.asString?.let {
                return  FlickrResponse<Person>().apply {
                    data = Person(it)
                    stat = status
                }
            }

        val responseObject = json?.asJsonObject?.getAsJsonObject("person")

        val id = responseObject?.get("id")?.asString ?: ""
        val isPro = responseObject?.get("ispro")?.asInt
        val iconServer = responseObject?.get("iconserver")?.asString ?: ""
        val iconFarm = responseObject?.get("iconfarm")?.asString ?: ""
        val userName = responseObject?.get("username")?.asJsonObject?.get("_content")?.asString ?: ""
        val realName = responseObject?.get("realname")?.asJsonObject?.get("_content")?.asString ?: ""
        val location = responseObject?.get("location")?.asJsonObject?.get("_content")?.asString ?: ""
        val description = responseObject?.get("description")?.asJsonObject?.get("_content")?.asString ?: ""
        val firstDateTaken = responseObject?.get("photos")
                ?.asJsonObject?.get("firstdate")
                ?.asJsonObject?.get("_content")?.let {
                    if (it.isJsonNull) { null }
                    else { it.asInt }
                }
        val firstDateTakenFormatted = firstDateTaken?.let {
            val calendar = GregorianCalendar().apply { timeInMillis = it * 1000L }
            dateFormatter.format(calendar.time)
        } ?: ""
        val photosCountPublic = responseObject?.get("photos")
            ?.asJsonObject?.get("count")
            ?.asJsonObject?.get("_content")
            ?.asString ?: ""
        val photosCount = responseObject?.get("upload_count")?.asString ?: ""
        val contactsCount = try {
            responseObject?.get("contacts")?.asString ?: ""
        } catch (e: UnsupportedOperationException) {
            "0"
        }

        val person = Person (
            id,
            isPro == 1,
            iconServer,
            iconFarm,
            userName,
            realName,
            location,
            description,
            firstDateTakenFormatted,
            photosCount.ifBlank { photosCountPublic },
            contactsCount
        )

        return FlickrResponse<Person>().apply {
            data = person
            stat = status
            message = responseMessage
        }
    }
}