package com.armageddon.android.flickrdroid.model;

import com.armageddon.android.flickrdroid.common.ExifTags;

import java.io.Serializable;
import java.util.List;

public class PhotoExif implements Serializable {
    List<PhotoExifData> data;

    public PhotoExif(List<PhotoExifData> data) {
        this.data = data;
    }

    public static class PhotoExifData implements Serializable {
        private String tag;
        private Raw raw;
        private Clean clean;

        private static class Raw implements Serializable {private String _content;}
        private static class Clean implements Serializable{private String _content;}


    }

   public String getAperture () {
        for (PhotoExifData item : data) {
            if (item.tag.equals(ExifTags.FNumber.name())) {
                return item.clean == null ? item.raw._content : item.clean._content;
            }
        }
        return null;
    }

   public String getFocalLength () {
        for (PhotoExifData item : data) {
            if (item.tag.equals(ExifTags.FocalLength.name())) {
                return item.clean == null ? item.raw._content : item.clean._content;
            }
        }
        return null;
    }

   public String getExposure () {
        for (PhotoExifData item : data) {
            if (item.tag.equals(ExifTags.ExposureTime.name())) {
                return item.clean == null ? item.raw._content : item.clean._content;
            }
        }
        return null;
    }

   public String getISO () {
        for (PhotoExifData item : data) {
            if (item.tag.equals(ExifTags.ISO.name())) {
                return item.clean == null ? item.raw._content : item.clean._content;
            }
        }
        return null;
    }

    public String getLens () {
        for (PhotoExifData item : data) {
            if (item.tag.equals(ExifTags.Lens.name())) {
                return item.clean == null ? item.raw._content : item.clean._content;
            }
        }
        return null;
    }
}
