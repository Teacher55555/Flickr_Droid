package com.armageddon.android.flickrdroid.network.deserializers

import com.armageddon.android.flickrdroid.model.PhotoExif
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PhotoExifDeserializer : JsonDeserializer<FlickrResponse<PhotoExif>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<PhotoExif> {
        val responseObject = json?.asJsonObject?.getAsJsonObject("photo")
        val responseDataArray = responseObject?.getAsJsonArray("exif")

        var lensModel = ""
        var fNumber = ""
        var focalLength = ""
        var exposureTime = ""
        var iso = ""

        val camera = responseObject?.get("camera")?.asString ?: ""

        fun getDataFromJson(element: JsonElement) : String {
            return element.asJsonObject?.get("clean")?.asJsonObject?.get("_content")?.asString ?:
            element.asJsonObject.get("raw").asJsonObject.get("_content").asString!!
        }

        responseDataArray?.forEach {
            when (it.asJsonObject.get("tag").asString) {
                "LensModel" -> lensModel = getDataFromJson(it)
                "FNumber" -> fNumber = getDataFromJson(it)
                "FocalLength" -> focalLength = getDataFromJson(it)
                "ExposureTime" -> exposureTime = getDataFromJson(it)
                "ISO" -> iso = getDataFromJson(it)
            }
        }

        val exifData = PhotoExif(
            camera,
            lensModel,
            fNumber,
            focalLength,
            exposureTime,
            iso
        )

        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL

        return FlickrResponse<PhotoExif>().apply {
            data = exifData
            stat = status
        }
    }
}