package com.armageddon.android.flickrdroid.model;

import android.annotation.SuppressLint;
import android.text.Html;
import android.text.Spanned;

import com.armageddon.android.flickrdroid.common.LogoIcon;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Person implements LogoIcon, Serializable {
    public static final int USER_PRO = 1;
    public static final int USER_NOT_PRO = 0;

    private static class Username implements Serializable { private String _content;}
    private static class Realname implements Serializable { private String _content;}
    private static class Location implements Serializable { private String _content;}
    private static class Description implements Serializable { private String _content;}
    private static class Photosurl implements Serializable { private String _content;}
    private static class Photos implements Serializable {
        private Firstdate firstdate;
        private Count count;
        private static class Firstdate implements Serializable {
            private long _content;
        }
        private static class Count implements Serializable {
            private String _content;
        }
    }
    public static class Contact implements Serializable, LogoIcon {
       private String nsid;
       private String username;
       private String iconserver;
       private String iconfarm;

        public String getUsername() {
            return username;
        }

        public String getNsid() {
            return nsid;
        }

        public String getUrl(String size) {
            return getUrl(iconfarm, iconserver, nsid, size);
        }
    }

    private String contactsCount;
    private String id;
    private String nsid;
    private int ispro;
    private String iconserver;
    private String iconfarm;
    private Username username;
    private Realname realname;
    private Location location;
    private Description description;
    private Photosurl photosurl;
    private Photos photos;
    private List<Contact> contacts;


    public String getLocation() {
        if (location == null ||location._content.length() == 0) {
            return null;
        }
        return location._content;
    }

    public String getPhotosurl() {
        return photosurl._content;
    }

    public String getPhotoFirstDate() {
        if (photos == null || photos.firstdate._content == 0) {
            return null;
        }
        long longDate = photos.firstdate._content * 1000;
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(longDate);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return  format.format(calendar.getTime());
    }

    public void setContactsCount(String contactsCount) {
        this.contactsCount = contactsCount;
    }

    public String getContactsCount() {
        return contactsCount;
    }

    public String getRealname() {
        if (realname == null || realname._content.length() == 0) {
            return null;
        }
        return realname._content;
    }

    public String getUsername() {
        return username._content;
    }

    public Spanned getDescription() {
        description._content = description._content.replaceAll("\n","<br>");
        if (description == null || description._content.length() == 0) {
            return null;
        }
        return Html.fromHtml(description._content,Html.FROM_HTML_MODE_LEGACY);
    }

    public String getIconUrl (String size) {
        if (Integer.parseInt(iconfarm) == 0 && Integer.parseInt(iconserver) == 0){
            return null;
        }
       return getUrl(iconfarm, iconserver, nsid, size);
    }

    public String getPublicPhotoCount() {
        int value = Integer.parseInt(photos.count._content);
        NumberFormat format = DecimalFormat.getInstance(Locale.US);
        return format.format(value);
    }

    public String getIconserver() {
        return iconserver;
    }

    public String getIconfarm() {
        return iconfarm;
    }

    public String getId() {
        return id;
    }

    public String getNsid() {
        return nsid;
    }

    public int isPro() {
        return ispro;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
    }

    public List<Contact> getContacts() {
        return contacts;
    }

}
