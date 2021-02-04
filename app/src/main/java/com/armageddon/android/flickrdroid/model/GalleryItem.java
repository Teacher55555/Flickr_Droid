package com.armageddon.android.flickrdroid.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.Html;
import android.text.Spanned;

import com.armageddon.android.flickrdroid.common.License;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.common.PhotoPrivacy;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class GalleryItem implements Serializable, LogoIcon {
    public static final int PHOTO_SIZE_MAX = 0;
    public static final int PHOTO_SIZE_640 = 1;
    public static final int PHOTO_SIZE_1024 = 2;
    public static final int PHOTO_SIZE_2048 = 3;

    private Map<String, Map<String,String>> sizes;

    private UUID itemId;
    private String id;
    private String title;
    private String url_sq;
    private String url_s;
    private String url_m;
    private String url_z;
    private String url_l;
    private String url_h;
    private String url_k;
    private String url_3k;
    private String url_4k;
    private String url_5k;
    private String url_6k;
    private String width_sq;
    private String height_sq;
    private String height_s;
    private String width_s;
    private String height_m;
    private String width_m;
    private String height_z;
    private String width_z;
    private String height_l;
    private String width_l;
    private String height_h;
    private String width_h;
    private String height_k;
    private String width_k;
    private String height_3k;
    private String width_3k;
    private String height_4k;
    private String width_4k;
    private String height_5k;
    private String width_5k;
    private String height_6k;
    private String width_6k;
    private double latitude;
    private double longitude;
    private String owner;
    private String ownername;
    private String realname;
    private String secret;
    private String server;
    private String farm;
    private String views;
    private String tags;
    private String iconserver;
    private String iconfarm;
    private String count_comments;
    private String count_faves;
    private String camera;
    private int license;
    private int isfavorite;
    private int ispublic;
    private int isfriend;
    private int isfamily;
    private String datetaken;
    private Description description;
    private static class Description implements Serializable {private String _content;}

    private static class PhotoSize {
        String url;
        String width;
        String heigth;

        public PhotoSize(String url, String width, String heigth) {
            this.url = url;
            this.width = width;
            this.heigth = heigth;
        }

        public String getUrl() {
            return url;
        }
        public String getWidth() {
            return width;
        }
        public String getHeigth() {
            return heigth;
        }
    }


    public GalleryItem() {
        itemId = UUID.randomUUID();
    }

    private void initPhotoSizeMap () {
        sizes = new LinkedHashMap<>();
        if (url_6k != null) {
            Map<String, String> map6k = new HashMap<>();
            map6k.put(url_6k, width_6k + " * " + height_6k);
            sizes.put(url_6k,map6k);
        }

        if (url_5k != null) {
            Map<String, String> map5k = new HashMap<>();
            map5k.put(url_5k, width_5k + " * " + height_5k);
            sizes.put(url_5k,map5k);
        }
        if (url_4k != null) {
            Map<String, String> map4k = new HashMap<>();
            map4k.put(url_4k, width_4k + " * " + height_4k);
            sizes.put(url_4k,map4k);
        }
        if (url_3k != null) {
            Map<String, String> map3k = new HashMap<>();
            map3k.put(url_3k, width_3k + " * " + height_3k);
            sizes.put(url_3k,map3k);
        }
        if (url_k != null) {
            Map<String, String> mapk = new HashMap<>();
            mapk.put(url_k, width_k + " * " + height_k);
            sizes.put(url_k,mapk);
        }
        if (url_h != null) {
            Map<String, String> maph = new HashMap<>();
            maph.put(url_h, width_h + " * " + height_h);
            sizes.put(url_h,maph);
        }
        if (url_l != null) {
            Map<String, String> mapl = new HashMap<>();
            mapl.put(url_l, width_l + " * " + height_l);
            sizes.put(url_l,mapl);
        }
        if (url_z != null) {
            Map<String, String> mapz = new HashMap<>();
            mapz.put(url_z, width_z + " * " + height_z);
            sizes.put(url_z,mapz);
        }
        if (url_m != null) {
            Map<String, String> mapm = new HashMap<>();
            mapm.put(url_m, width_m + " * " + height_m);
            sizes.put(url_m,mapm);
        }
        if (url_s != null) {
            Map<String, String> maps = new HashMap<>();
            maps.put(url_s, width_s + " * " + height_s);
            sizes.put(url_s,maps);
        }
        if (url_sq != null) {
            Map<String, String> mapsq = new HashMap<>();
            mapsq.put(url_sq, width_sq + " * " + height_sq);
            sizes.put(url_sq,mapsq);
        }
    }



    public String getPhotoHighestResUrl () {
        if (sizes == null) {
            initPhotoSizeMap();
        }

        Iterator<String> iterator = sizes.keySet().iterator();
        if(iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    public String getPhotoHighestRes () {
        if (sizes == null) {
            initPhotoSizeMap();
        }

//        Iterator<Map<String,String>> iterator = sizes.values().iterator();
        Map <String,String> map = sizes.values().iterator().next();
        return map.values().iterator().next();
    }



    public int getPrivacy () {
        int privacy = 1;
        if (ispublic == 1) {
            privacy = PhotoPrivacy.PUBLIC;
        } else if (isfriend == 1 && isfamily == 1) {
            privacy = PhotoPrivacy.FRIENDS_AND_FAMILY;
        } else  if (isfamily == 1) {
            privacy = PhotoPrivacy.FAMILY;
        } else if (isfriend == 1) {
            privacy = PhotoPrivacy.FRIENDS;
        }
        return privacy;
    }

    public boolean isFamily() {
        return isfamily == 1;
    }

    public boolean isFriend() {
        return isfriend == 1;
    }

    public boolean isPublic() {
        return ispublic == 1;
    }

    public boolean isFriendAndFamily() {
        return isfriend == 1 && isfamily == 1;
    }

    public void setPrivacy (int privacy) {
        ispublic = 0;
        isfriend = 0;
        isfamily = 0;

        switch (privacy) {
            case PhotoPrivacy.PUBLIC : ispublic = 1; break;
            case PhotoPrivacy.FRIENDS : isfriend = 1; break;
            case PhotoPrivacy.FAMILY : isfamily = 1; break;
            case PhotoPrivacy.FRIENDS_AND_FAMILY :  isfamily = 1; isfriend = 1; break;
        }
    }





    public String getDateTaken() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        LocalDateTime date = LocalDateTime.parse(datetaken, formatter);

        DateTimeFormatter resultFormatter =
                DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault());
        return date.format(resultFormatter);
    }



    public String getOwner() {
        return owner;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GalleryItem(String name, String url_z) {
        this.title = name;
        this.url_z = url_z;
    }

    public GalleryItem(String id, String name, String url_s) {
        this.id = id;
        this.title = name;
        this.url_s = url_s;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return title;
    }

    public void setName(String name) {
        this.title = name;
    }

    public String getUrl_s() {
        return url_s;
    }

    public void setUrl_s(String url_s) {
        this.url_s = url_s;
    }

    public String getUrl_m() {
        return url_m;
    }

    public String getUrl_z() {
        return url_z;
    }


    public String getUrl_k() {
        return url_k;
    }

    public String getSecret() {
        return secret;
    }

    public String getServer() {
        return server;
    }

    public String getFarm() {
        return farm;
    }

    public String getViews() {
        return views;
    }

    public ArrayList<String> getTags() {
        if (tags.length() == 0) {
            return null;
        }
       return new ArrayList<>(Arrays.asList(tags.split(" ")));
    }

    public String getUrl_l() {
        return url_l;
    }

    public String getUrl_h() {
        return url_h;
    }

    public String getUrl_3k() {
        return url_3k;
    }

    public String getUrl_4k() {
        return url_4k;
    }

    public String getUrl_5k() {
        return url_5k;
    }

    public String getUrl_6k() {
        return url_6k;
    }

    public String getLicense() {
         return License.values()[license].getLicense();
    }

    public String getOwnerIcon (String size) {
        if (Integer.parseInt(iconfarm) == 0 && Integer.parseInt(iconserver) == 0){
            return null;
        }
        return getUrl(iconfarm, iconserver, owner, size);
    }

    public String getOwnername () {
        if (realname == null) {
            return ownername;
        }
        return realname.length() == 0 ? ownername : realname;
    }

    public String getRealname() {
        return realname;
    }

    public String getOwnerName2() {
        return ownername;
    }

    public String getCount_comments() {
            return count_comments;
    }

    public void increaseCommentsCount() {
        int count = Integer.parseInt(count_comments) + 1;
        count_comments = String.valueOf(count);
    }

    public void decreaseCommentsCount () {
        int count = Integer.parseInt(count_comments) - 1;
        if (count < 0) {
            count = 0;
        }
        count_comments = String.valueOf(count);
    }

    public String getCount_faves() {
        return count_faves;
    }

    public void increaseFavesCount () {
       int count = Integer.parseInt(count_faves) + 1;
       count_faves = String.valueOf(count);
    }

    public void decreaseFavesCount () {
        int count = Integer.parseInt(count_faves) - 1;
        if (count < 0) {
            count = 0;
        }
        count_faves = String.valueOf(count);
    }

    public UUID getItemId() {
        return itemId;
    }

    public String getCamera() {
        if (camera.length() >0) {
            return camera;
        }
        return null ;
    }

    public String getLocation (Context context) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());


        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            String str = "";
            if (city != null) {
                str = city + ", ";
            }

            if (state != null) {
                str += state + ", ";
            }

            if (country != null) {
                str += country;
            }

            return str;

        } catch (IOException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPhotoUrl (int size) {
        switch (size) {
            default: return getPhotoHighestResUrl();
            case PHOTO_SIZE_640: return url_z;
            case PHOTO_SIZE_1024: return url_l;
            case PHOTO_SIZE_2048: return url_k;
        }
    }

    public Spanned getDescription() {

        if (description._content.trim().length() > 0) {
            description._content = description._content.replaceAll("\n","<br>");
            return Html.fromHtml(description._content,Html.FROM_HTML_MODE_LEGACY);
        }
        return null;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public boolean isfavorite() {
        return isfavorite != 0;
    }

    public void setFavorite (boolean isFavorite) {
        if (isFavorite) {
            this.isfavorite = 1;
        } else {
            this.isfavorite = 0;
        }
    }

//    public void removeGeo() {
//        longitude = 0;
//        latitude = 0;
//    }
}
