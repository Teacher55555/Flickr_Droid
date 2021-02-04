package com.armageddon.android.flickrdroid.model;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Album  {
   private String id;
   private String owner;
   private String count_views;
   private String count_photos;
   private String username;
   private long date_create;
   private Title title;
   private Description description;
   private PrimaryPhotoExtras primary_photo_extras;
   private static class Title {String _content;}
   private static class Description {String _content;}
   private static class PrimaryPhotoExtras {String url_q;}

    public String getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setCount_photos(String count_photos) {
        this.count_photos = count_photos;
    }

    public void setTitle(String albumTitle) {
        title = new Title();
        title._content = albumTitle;
    }

    public String getCountViews() {
        return count_views;
    }

    public String getCountPhotos() {
        return count_photos;
    }

    public String getDateCreate() {
       if (date_create < 1) {
           return null;
       }
        long longDate = date_create * 1000;
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(longDate);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return format.format(calendar.getTime());
    }

    public String getTitle() {
        return title._content;
    }

    public String getDescription() {
       if (description == null) {
           return null;
       }

       if (description._content.length() == 0) {
           return null;
       }
        return description._content;
    }

    public String getCoverPhotoUrl() {
        return primary_photo_extras.url_q;
    }

    public void setPrimaryCoverPhotoUrl (String url) {
       primary_photo_extras = new PrimaryPhotoExtras();
       primary_photo_extras.url_q = url;
    }

    public String getUsername() {
        return username;
    }
}
