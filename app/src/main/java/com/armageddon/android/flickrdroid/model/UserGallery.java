package com.armageddon.android.flickrdroid.model;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class UserGallery {
    private String gallery_id;
    private long date_create;
    private String count_photos;
    private String count_views;
    private String count_comments;
    private String title;
    private String description;
    private String owner;

    public String getOwner() {
        return owner;
    }

    public String getGalleryId() {
        return gallery_id;
    }

    public String getCountPhotos() {
        return count_photos;
    }

    public String getCountViews() {
        return count_views;
    }

    public String getCountComments() {
        return count_comments;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description.length() > 0 ? description : null;
    }


    private List<String> cover_photos;

    public UserGallery(String owner,
                       String gallery_id,
                       long date_create,
                       String count_photos,
                       String count_views,
                       String count_comments,
                       String title,
                       String description,
                       List<String> cover_photos) {
        this.owner = owner;
        this.gallery_id = gallery_id;
        this.date_create = date_create;
        this.count_photos = count_photos;
        this.count_views = count_views;
        this.count_comments = count_comments;
        this.title = title;
        this.description = description;
        this.cover_photos = cover_photos;
    }

    public String getDate_create() {
        long longDate = date_create * 1000;
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(longDate);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        return  format.format(calendar.getTime());
    }

    public String getCoverPhotos(int index) {
        if (cover_photos.size() > index) {
            return cover_photos.get(index);
        }
        return null;
    }

}
