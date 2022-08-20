package com.armageddon.android.flickrdroid.model

import android.content.Context
import android.text.Html
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.LogoIcon
import com.google.gson.annotations.SerializedName
import java.time.Duration
import java.util.*

const val TIME_NAME = 0
const val TIME_VALUE = 1
private const val DEUTSCH_TEXT = "vor "

data class PhotoComment (
    val id: String,
    val author: String,
    @SerializedName("author_is_deleted") val isDeleted: Int,
    @SerializedName("authorname") val authorName: String,
    @SerializedName("realname") val authorRealName: String,
    val iconserver: String,
    val iconfarm: String,
    val datecreate: Long,
    @SerializedName("_content") val content: String,
) : LogoIcon
{
    private var locale: Locale? = null
    private var isDeutschLocale = false

    fun getComment(): String {
        return Html.fromHtml(content, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    fun getUserName() = authorRealName.ifBlank { authorName }

    fun getAuthorLogo(size: String = LogoIcon.Icon.ICON_NORMAL_100PX.prefix): String {
        return getIconUrl(iconfarm, iconserver, author, size)
    }

    fun getCommentTime(context: Context, field: Int): String? {
        if (locale == null) {
            locale = context.resources.configuration.locales[0]
            if (locale == Locale.GERMAN || locale == Locale.GERMANY) {
                isDeutschLocale = true
            }
        }
        val oldCalendar = Calendar.getInstance()
        oldCalendar.timeInMillis = datecreate * 1000
        val newCalendar = Calendar.getInstance()
        val yearsGone = newCalendar[Calendar.YEAR] - oldCalendar[Calendar.YEAR]
        if (yearsGone > 0) {
            return if (field == TIME_VALUE) {
                var str = yearsGone.toString()
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str
                }
                str
            } else {
                when {
                    yearsGone > 4 -> context.getString(R.string.years_ago)
                    yearsGone > 1 -> context.getString(R.string.years_2_4_ago)
                    else -> context.getString(R.string.year_ago)
                }
            }
        }
        val duration = Duration.between(oldCalendar.toInstant(), newCalendar.toInstant())
        val monthsGone = duration.toDays().toInt() / 30
        if (monthsGone > 0) {
            return if (field == TIME_VALUE) {
                var str = monthsGone.toString()
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str
                }
                str
            } else {
                if (monthsGone == 1) { context.getString(R.string.month_ago) }
                else { context.getString(R.string.months_ago) }
            }
        }
        val daysGone = duration.toDays().toInt()
        if (daysGone > 0) {
            return if (field == TIME_VALUE) {
                var str = daysGone.toString()
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str
                }
                str
            } else {
                if (daysGone == 1) { context.getString(R.string.day_ago) }
                else { context.getString(R.string.days_ago) }
            }
        }
        val hoursGone = duration.toHours().toInt()
        if (hoursGone > 0) {
            return if (field == TIME_VALUE) {
                var str = hoursGone.toString()
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str
                }
                str
            } else {
                if (hoursGone == 1) { context.getString(R.string.hour_ago) }
                else { context.getString(R.string.hours_ago) }
            }
        }
        val minGone = duration.toMinutes().toInt()
        if (minGone > 0) {
            return if (field == TIME_VALUE) {
                var str = minGone.toString()
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str
                }
                str
            } else {
                if (minGone == 1) { context.getString(R.string.minute_ago) }
                else { context.getString(R.string.minutes_ago) }
            }
        }
        return if (field == TIME_VALUE) { context.getString(R.string.now) }
        else ""
    }
}