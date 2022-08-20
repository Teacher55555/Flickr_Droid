package com.armageddon.android.flickrdroid.model

import android.content.Context
import android.text.Spanned
import com.armageddon.android.flickrdroid.common.GroupPrivacy

class GroupInfo (
    val description : Spanned,
    val rules : Spanned,
    val privacy: Int,
    val dateCreate: String,
    val adminMessage: Spanned,
)  {


    fun getFullDescription(context: Context) : String {
        val stringId = GroupPrivacy.getTextPrivacy(privacy)
        val textPrivacy = context.getString(stringId)
        return  "<u><b>Group description:</b></u><br>$description" +
                "<br><br><u><b>Group rules:</b></u><br>$rules" +
                "<br><br><u><b>Admin message:</b></u><br>$adminMessage" +
                "<br><br><u><b>Group privacy:</b></u><br>$textPrivacy" +
                "<br><br><u><b>Group created:</b></u><br>$dateCreate"
    }
}