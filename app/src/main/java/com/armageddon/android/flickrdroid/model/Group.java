package com.armageddon.android.flickrdroid.model;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

import com.armageddon.android.flickrdroid.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Group {
    private String id;
    private Name name;
    private Description description;
    private Rules rules;
    private Members members;
    private PoolCount pool_count;
    private TopicCount topic_count;
    private DateCreate datecreate;
    private Blast blast;

    private int is_member;
    private static class Name {private String _content;}
    private static class Description {private String _content;}
    private static class Rules {private String _content;}
    private static class Members {private String _content;}
    private static class PoolCount {private String _content;}
    private static class TopicCount {private String _content;}
    private static class DateCreate {private String _content;}
    private static class Blast {private String _content;}

    public boolean isMember () {
        return is_member != 0;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name._content;
    }

    public String getDescription(Context context) {
        if (description._content.length() == 0) {
            return context.getString(R.string.group_default_description);
        }
        return description._content;
    }

    public String getRules(Context context) {
        if (rules._content.length() == 0) {
            return context.getString(R.string.group_default_rules);
        }
        return rules._content;
    }

    public String getMembers() {
        return members._content;
    }

    public String getPool_count() {
        return pool_count._content;
    }

    public String getTopic_count() {
        return topic_count._content;
    }

    public String getDatecreate() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        LocalDateTime date = LocalDateTime.parse(datecreate._content, formatter);

        DateTimeFormatter resultFormatter =
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault());
        return date.format(resultFormatter);
    }

    public String getBlast() {
        if (blast == null) {
            return "";
        }
        return blast._content;
    }

    public Spanned getFullInfo (Context context) {
        description._content = description._content.replaceAll("\n","<br>");
        String str = "<u><b>Group description:</b></u><br>" +
                getDescription(context) + "<br><br>" +
                "<u><b>Group rules:</b></u><br>" +
                getRules(context) + "<br><br>";
        if (blast != null) {
            str += "<u><b>Admin message:</b></u><br>" + getBlast() + "<br><br>";
        }

        str += "<u><b>Group created:</b></u><br>" + getDatecreate();
        return  Html.fromHtml(str,Html.FROM_HTML_MODE_COMPACT);
    }


}
