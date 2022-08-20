package com.armageddon.android.flickrdroid.network.deserializers

import android.text.Html
import android.text.SpannedString
import com.armageddon.android.flickrdroid.common.License
import com.armageddon.android.flickrdroid.common.LogoIcon
import com.armageddon.android.flickrdroid.model.PhotoInfo
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.LinkedHashMap

class PhotoInfoDeserializer: JsonDeserializer<FlickrResponse<PhotoInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<PhotoInfo> {
        val responseObject = json?.asJsonObject?.getAsJsonObject("photo")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val resultFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())

        val description = responseObject
            ?.getAsJsonObject("description")
            ?.get("_content")?.asString?.let {
                val text = it.replace("\n".toRegex(), "<br>")
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } ?: SpannedString("")

        val views = responseObject?.get("views")?.asString ?: "0"

        val dateTaken = responseObject
            ?.getAsJsonObject("dates")
            ?.get("taken")?.asString
            ?.let {
                val date = LocalDateTime.parse(it, formatter)
                date.format(resultFormatter)
            } ?: ""

        var photoLabel = LogoIcon.Photo.UNKNOWN.prefix
        var photoSize = ""
        var arrayIndex = 1
        val sizesJsonArray = responseObject
            ?.getAsJsonObject("sizes")
            ?.getAsJsonArray("size")

        val sizes : LinkedHashMap<String, String> = LinkedHashMap()
        sizesJsonArray?.forEach {
            val label = it.asJsonObject.get("label").asString
            val url = it.asJsonObject.get("source").asString
            photoLabel = try {
              LogoIcon.Photo.getLabel(label).prefix
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
                LogoIcon.Photo.UNKNOWN.prefix
            }
            if (arrayIndex == sizesJsonArray.size()) {
                val width = it.asJsonObject.get("width").asString
                val height = it.asJsonObject.get("height").asString
                photoSize = "$width * $height"
            }
            arrayIndex++
            sizes[photoLabel] = url
        }

        val location = responseObject
            ?.getAsJsonObject("location").let {
                val loc = it?.asJsonObject
                PhotoInfo.Location(
                    loc?.get("latitude")?.asDouble ?: 0.0,
                    loc?.get("longitude")?.asDouble ?: 0.0,
                    loc?.getAsJsonObject("country")?.get("_content")?.asString,
                    loc?.getAsJsonObject("region")?.get("_content")?.asString,
                    loc?.getAsJsonObject("locality")?.get("_content")?.asString
                )
            }

        val tags = responseObject
            ?.getAsJsonObject("tags")
            ?.getAsJsonArray("tag")
            ?.map {
                it.asJsonObject.get("_content").asString
            } ?: emptyList()

        val license = responseObject?.let {
            val lic = it.get("license")?.asInt
            License.values()[lic!!].license
        } ?: License.LIC0.name

        val photoInfo = PhotoInfo(
            description,
            views,
            dateTaken,
            location,
            tags,
            sizes,
            photoSize,
            photoLabel,
            license
        )

        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL

        return FlickrResponse<PhotoInfo>().apply {
            data = photoInfo
            stat = status
        }
    }
}