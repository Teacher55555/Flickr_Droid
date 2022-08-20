package com.armageddon.android.flickrdroid.network.deserializers

import android.text.Html
import android.text.SpannedString
import com.armageddon.android.flickrdroid.common.GroupPrivacy
import com.armageddon.android.flickrdroid.model.GroupInfo
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class GroupInfoDeserializer: JsonDeserializer<FlickrResponse<GroupInfo>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): FlickrResponse<GroupInfo> {
        val responseObject = json?.asJsonObject?.getAsJsonObject("group")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        val resultFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())

        val description = responseObject
            ?.getAsJsonObject("description")
            ?.get("_content")?.asString?.let {
                val text = it.replace("\n".toRegex(), "<br>")
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } ?: SpannedString("")

        val rules = responseObject
            ?.getAsJsonObject("rules")
            ?.get("_content")?.asString?.let {
                val text = it.replace("\n".toRegex(), "<br>")
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } ?: SpannedString("")

        val adminMessage = responseObject
            ?.getAsJsonObject("blast")
            ?.get("_content")?.asString?.let {
                val text = it.replace("\n".toRegex(), "<br>")
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } ?: SpannedString("")

        val dateCreate = responseObject
            ?.getAsJsonObject("datecreate")
            ?.get("_content")?.asString
            ?.let {
                val date = LocalDateTime.parse(it, formatter)
                date.format(resultFormatter)
            } ?: ""

        val privacy = responseObject
            ?.getAsJsonObject("privacy")
            ?.get("_content")?.asInt
            ?: GroupPrivacy.PRIVATE.privacy


        val groupInfo = GroupInfo(
            description,
            rules,
            privacy,
            dateCreate,
            adminMessage

        )

        val status = json?.asJsonObject?.get("stat")?.asString ?: RESPONSE_DATA_FAIL

        return FlickrResponse<GroupInfo>().apply {
            data = groupInfo
            stat = status
        }
    }
}