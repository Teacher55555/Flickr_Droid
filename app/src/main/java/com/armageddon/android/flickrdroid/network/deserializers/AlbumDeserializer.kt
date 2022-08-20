package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.Album
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.*
import java.lang.IllegalStateException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class AlbumDeserializer : JsonDeserializer<FlickrResponse<Album>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Album> {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val responseObject = json?.asJsonObject?.getAsJsonObject("photosets")
        val responseDataArray = responseObject?.getAsJsonArray("photoset")

        val data = mutableListOf<Album>()

        responseDataArray?.forEach { it ->
            val field = it.asJsonObject
            val id = field.get("id").asString
            val owner = field.get("owner").asString
            val username = field.get("username").asString
            val countViews = field.get("count_views").asString
            val countPhotos = field.get("count_photos").asString
            val title = field.get("title").asJsonObject.get("_content").asString
            val description = field.get("description").asJsonObject.get("_content").asString
            val coverUrl = try {
                field.get("primary_photo_extras")?.asJsonObject?.get("url_z")?.asString ?: ""
            } catch (e : IllegalStateException) {
                "no_url"
            }

            val dateCreate = field.get("date_create").asString.toInt()
            val dateCreateFormatted = when (dateCreate < 1) {
                true -> null
                false -> {
                    val calendar: Calendar = GregorianCalendar()
                        .apply { timeInMillis = dateCreate * 1000L }
                    dateFormatter.format(calendar.time)
                }
            }

            val album = Album(
                id,
                owner,
                username,
                countViews,
                countPhotos,
                title,
                description,
                dateCreateFormatted,
                coverUrl
            )

            data.add(album)
        }

        return FlickrResponse<Album>().apply {
            dataArray = data
            responseObject?.let {
                page = it.get("page").asInt
                pages = it.get("pages").asInt
                perpage = it.get("perpage").asInt
                total = it.get("total").asInt
                stat = status
            }
        }
    }

}