package com.armageddon.android.flickrdroid.model;

import android.content.Context;
import android.text.Html;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.LogoIcon;

import java.time.Duration;
import java.util.Calendar;
import java.util.Locale;


public class Comment implements LogoIcon {
    public static final int TIME_NAME = 0;
    public static final int TIME_VALUE = 1;
    private static final String DEUTSCH_TEXT = "vor ";
    private String id;
    private String author;
    private String authorname;
    private String iconserver;
    private String iconfarm;
    private long datecreate;
    private String realname;
    private String _content;
    private Locale mLocale;
    private boolean isDeutschLocale;

    public String getId() {
        return id;
    }

    public String getAuthorId() {
        return author;
    }

    public String getAuthorName() {
        if (realname == null || realname.length() == 0) {
            return authorname;
        }
        return realname;
    }

    public long getDatecreate() {
        return datecreate;
    }

    public String getRealname() {
        return realname;
    }

    public String getComment() {
        return Html.fromHtml(_content,Html.FROM_HTML_MODE_LEGACY).toString();
    }

    public String getAuthorLogo (String size) {
        return getUrl(iconfarm, iconserver, author, size);
    }

    public String getCommentTime (Context context, int field) {

        if (mLocale == null) {
            mLocale = context.getResources().getConfiguration().getLocales().get(0);
            if (mLocale.equals(Locale.GERMAN) || mLocale.equals(Locale.GERMANY)) {
                isDeutschLocale = true;
            }
        }

        Calendar oldCalendar = Calendar.getInstance();
        oldCalendar.setTimeInMillis(datecreate * 1000);

        Calendar newCalendar = Calendar.getInstance();

        int yearsGone = newCalendar.get(Calendar.YEAR) - oldCalendar.get(Calendar.YEAR);
        if (yearsGone > 0) {
            if (field == TIME_VALUE) {
                String str = String.valueOf(yearsGone);
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str;
                }
                return str;
            } else {
                if (yearsGone > 4) {
                    return context.getString(R.string.years_ago);
                } else if (yearsGone > 1) {
                    return context.getString(R.string.years_2_4_ago);
                } else {
                    return context.getString(R.string.year_ago);
                }
            }
        }

        Duration duration = Duration.between(oldCalendar.toInstant(), newCalendar.toInstant());

        int monthsGone = (int) duration.toDays() / 30;
        if (monthsGone > 0) {
            if (field == TIME_VALUE) {
                String str = String.valueOf(monthsGone);
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str;
                }
                return str;
            } else {
                if (monthsGone == 1) {
                    return context.getString(R.string.month_ago);
                } else {
                    return context.getString(R.string.months_ago);
                }
            }
        }

        int daysGone = (int) duration.toDays();
        if (daysGone > 0) {
            if (field == TIME_VALUE) {
                String str = String.valueOf(daysGone);
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str;
                }
                return str;
            } else {
                if (daysGone == 1) {
                    return context.getString(R.string.day_ago);
                } else {
                    return context.getString(R.string.days_ago);
                }
            }
        }

        int hoursGone = (int) duration.toHours();
        if (hoursGone > 0) {
            if (field == TIME_VALUE) {
                String str = String.valueOf(hoursGone);
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str;
                }
                return str;
            } else {
                if (hoursGone == 1) {
                    return context.getString(R.string.hour_ago);
                } else {
                    return context.getString(R.string.hours_ago);
                }
            }
        }

        int minGone = (int) duration.toMinutes();
        if (minGone > 0) {
            if (field == TIME_VALUE) {
                String str = String.valueOf(minGone);
                if (isDeutschLocale) {
                    str = DEUTSCH_TEXT + str;
                }
                return str;
            } else {
                if (minGone == 1) {
                    return context.getString(R.string.minute_ago);
                } else {
                    return context.getString(R.string.minutes_ago);
                }
            }
        }

        if (field == TIME_VALUE) {
            return context.getString(R.string.now);
        }

        return "";
    }

}
