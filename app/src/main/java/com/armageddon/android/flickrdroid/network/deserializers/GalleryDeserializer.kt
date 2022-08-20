package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.Gallery
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.UnsupportedOperationException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class GalleryDeserializer : JsonDeserializer<FlickrResponse<Gallery>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<Gallery> {
        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.US)
        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL
        val responseObject = json?.asJsonObject?.getAsJsonObject("galleries")
        val responseDataArray = responseObject?.getAsJsonArray("gallery")

        val data = mutableListOf<Gallery>()

        responseDataArray?.forEach { it ->
            val field = it.asJsonObject
            val galleryId = field.get("gallery_id").asString
            val owner = field.get("owner").asString
            val username = field.get("username").asString
            val countViews = field.get("count_views").asString
            val countPhotos = field.get("count_photos").asString
            val countComments = field.get("count_comments").asString
            val title = field.get("title").asJsonObject.get("_content").asString
            val description = field.get("description").asJsonObject.get("_content").asString
            val dateCreate = field.get("date_create").asString.toInt()
            val dateCreateFormatted = when (dateCreate < 1) {
                true -> null
                false -> {
                    val calendar: Calendar = GregorianCalendar()
                        .apply { timeInMillis = dateCreate * 1000L }
                    dateFormatter.format(calendar.time)
                }
            }
            val coverArray = try {
                field.get("cover_photos").asJsonObject.getAsJsonArray("photo")
            } catch (e : UnsupportedOperationException) {
                null
            } catch (e: NullPointerException) {
                e.printStackTrace()
                null
            }

            val coverUrlList = try {
                 coverArray?.map { it.asJsonObject.get("url").asString }
            } catch (e : UnsupportedOperationException) {
                null
            }

            val gallery = Gallery(
                galleryId,
                dateCreateFormatted,
                countPhotos,
                countViews,
                countComments,
                title,
                description,
                owner,
                username,
                coverUrlList
            )

            data.add(gallery)
        }

        return FlickrResponse<Gallery>().apply {
            dataArray = data
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