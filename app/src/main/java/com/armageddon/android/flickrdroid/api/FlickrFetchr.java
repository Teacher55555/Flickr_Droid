package com.armageddon.android.flickrdroid.api;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.armageddon.android.flickrdroid.api.oauth.HttpMethod;
import com.armageddon.android.flickrdroid.api.oauth.OAuthConfig;
import com.armageddon.android.flickrdroid.api.oauth.OAuthConfigBuilder;
import com.armageddon.android.flickrdroid.api.oauth.OAuthSignature;
import com.armageddon.android.flickrdroid.common.PhotoPrivacy;
import com.armageddon.android.flickrdroid.common.QueryPreferences;
import com.armageddon.android.flickrdroid.model.Album;
import com.armageddon.android.flickrdroid.model.Comment;
import com.armageddon.android.flickrdroid.model.GalleryItem;
import com.armageddon.android.flickrdroid.model.Group;
import com.armageddon.android.flickrdroid.model.GroupSearchItem;
import com.armageddon.android.flickrdroid.model.Person;
import com.armageddon.android.flickrdroid.model.PhotoExif;
import com.armageddon.android.flickrdroid.model.PhotoInfo;
import com.armageddon.android.flickrdroid.model.PhotoSize;
import com.armageddon.android.flickrdroid.model.UserGallery;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.armageddon.android.flickrdroid.api.OauthActivity.CONSUMER_KEY;
import static com.armageddon.android.flickrdroid.api.OauthActivity.CONSUMER_SECRET;
import static com.armageddon.android.flickrdroid.api.OauthActivity.OAUTH_SIGNATURE_METHOD;
import static com.armageddon.android.flickrdroid.api.OauthActivity.OAUTH_VERSION;

/**
 * Flickr API class.
 * Each method returns RequestResponse<E> requestResponse witch contains data from Flickr.com
 */

public class FlickrFetchr {
    private static final int CONNECTION_TIMEOUT = 8000;
    private static final String TAG = "FlickrFetchr";
    private static final String FLICKR_URL = "https://api.flickr.com/services/rest/";
    public static final String PER_PAGE = "125";

    private static final String SEARCH_PHOTO_METHOD = "flickr.photos.search";
    private static final String SEARCH_RECENT_PHOTO_METHOD = "flickr.photos.getRecent";
    private static final String INTERESTING_PHOTO_METHOD = "flickr.interestingness.getList";
    private static final String SEARCH_PERSON_BY_NAME_METHOD = "flickr.people.findByUsername";
    private static final String SEARCH_PERSON_BY_EMAIL_METHOD = "flickr.people.findByEmail";
    private static final String PERSON_GET_INFO_METHOD = "flickr.people.getInfo";
    private static final String SEARCH_GROUP = "flickr.groups.search";
    private static final String PHOTO_GET_SIZES = "flickr.photos.getSizes";
    private static final String PHOTO_GET_INFO = "flickr.photos.getInfo";
    private static final String PHOTO_GET_FAVS = "flickr.photos.getFavorites";
    private static final String PHOTO_GET_EXIF = "flickr.photos.getExif";
    private static final String PHOTO_GET_PUBLIC = "flickr.people.getPublicPhotos";
    private static final String PHOTO_GET_USER_CAMERA_ROLL = "flickr.people.getPhotos";
    private static final String PHOTO_GET_USER_CONTACTS_PHOTOS = "flickr.photos.getContactsPhotos";
    private static final String PHOTO_GET_USER_GALLERY = "flickr.galleries.getList";
    private static final String PHOTO_GET_USER_GROUPS = "flickr.people.getPublicGroups";
    private static final String PHOTO_GET_USER_ALL_GROUPS = "flickr.people.getGroups";
    private static final String PHOTO_GET_USER_ALBUMS = "flickr.photosets.getList";
    private static final String PHOTO_GET_USER_PUBLIC_FAVES = "flickr.favorites.getPublicList";
    private static final String PHOTO_GET_USER_FAVES = "flickr.favorites.getList";
    private static final String GET_USER_CONTACTS = "flickr.contacts.getPublicList";
    private static final String GET_ALBUM_PHOTOS = "flickr.photosets.getPhotos";
    private static final String GET_GALLERY_PHOTOS = "flickr.galleries.getPhotos";
    private static final String GET_GROUP_PHOTOS = "flickr.groups.pools.getPhotos";
    private static final String GET_GROUP_INFO = "flickr.groups.getInfo";
    private static final String GET_PHOTO_COMMENTS = "flickr.photos.comments.getList";
    private static final String SET_PHOTO_PRIVACY = "flickr.photos.setPerms";
    private static final String ADD_COMMENTS = "flickr.photos.comments.addComment";
    private static final String DEL_COMMENTS = "flickr.photos.comments.deleteComment";
    private static final String ADD_FAVORITES = "flickr.favorites.add";
    private static final String REMOVE_FAVORITES = "flickr.favorites.remove";


    private static final String EXTRAS = "isfavorite,url_sq,url_s,url_m,url_z,url_k,url_l,url_3k," +
            "url_4k,url_5k,url_6k,url_h,geo,icon_server,owner_name,realname,count_comments," +
            "count_faves,views,date_taken,camera,description,tags,license";

    private static final Uri ENDPOINT = Uri
            .parse(FLICKR_URL)
            .buildUpon()
            .appendQueryParameter("api_key", CONSUMER_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .build();


    public byte [] getUrlBytes (String str) throws IOException {
        URL url = new URL(str);
        HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
        connection.setReadTimeout(CONNECTION_TIMEOUT);
        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        try {

            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + str);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int arraylength;
            byte [] buffer = new byte[1024];
            while ((arraylength = in.read(buffer)) > 0) {
                out.write(buffer, 0,arraylength);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrlString (String str) throws IOException {
        return new String(getUrlBytes(str));
    }

    public RequestResponse<?> setPhotoPrivacy (Context context,String photoId, int photoPrivacy) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();

        String isPublic = "0";
        String isFriend = "0";
        String isFamily = "0";

        switch (photoPrivacy) {
            case PhotoPrivacy.PUBLIC : isPublic = "1"; break;
            case PhotoPrivacy.FRIENDS : isFriend = "1"; break;
            case PhotoPrivacy.FAMILY : isFamily = "1"; break;
            case PhotoPrivacy.FRIENDS_AND_FAMILY :  isFamily = "1"; isFriend = "1"; break;
        }

        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback","1")
                .addQueryParam("format", "json")
                .addQueryParam("method", SET_PHOTO_PRIVACY)
                .addQueryParam("photo_id", photoId)
                .addQueryParam("is_public", isPublic)
                .addQueryParam("is_friend", isFriend)
                .addQueryParam("is_family", isFamily)
                .create();


        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method","HMAC-SHA1")
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", "1.0")
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("method", SET_PHOTO_PRIVACY)
                .appendQueryParameter("photo_id", photoId)
                .appendQueryParameter("is_public", isPublic)
                .appendQueryParameter("is_friend", isFriend)
                .appendQueryParameter("is_family", isFamily)
                .build().toString();


        RequestBody dummy = RequestBody.create("null", null);
        RequestResponse<?> requestResponse = new RequestResponse<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(s)
                .method("POST", dummy)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            JSONObject jsonBody = new JSONObject(str);
            requestResponse.setResponseDataStat(jsonBody.getString("stat"));

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to set privacy", e);
            requestResponse.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return requestResponse;
        }
        return requestResponse;
    }


    public RequestResponse<?> postComment (Context context,String photoId, String commentText) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();


        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback","1")
                .addQueryParam("format", "json")
                .addQueryParam("method", ADD_COMMENTS)
                .addQueryParam("photo_id", photoId)
                .addQueryParam("comment_text", commentText)
                .create();


        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method","HMAC-SHA1")
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", "1.0")
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("method", ADD_COMMENTS)
                .appendQueryParameter("photo_id", photoId)
                .appendQueryParameter("comment_text", commentText)
                .build().toString();


        RequestBody dummy = RequestBody.create("null", null);
        RequestResponse<?> requestResponse = new RequestResponse<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(s)
                .method("POST", dummy)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            JSONObject jsonBody = new JSONObject(str);
            requestResponse.setResponseDataStat(jsonBody.getString("stat"));

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to add comment", e);
            requestResponse.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return requestResponse;
        }

        return requestResponse;

    }

    public RequestResponse<?> delComment (Context context,String commentId) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();


        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback","1")
                .addQueryParam("format", "json")
                .addQueryParam("method", DEL_COMMENTS)
                .addQueryParam("comment_id", commentId)
                .create();


//
        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method","HMAC-SHA1")
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", "1.0")
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("method", DEL_COMMENTS)
                .appendQueryParameter("comment_id", commentId)
                .build().toString();


        RequestBody dummy = RequestBody.create("null", null);
        RequestResponse<?> requestResponse = new RequestResponse<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(s)
                .method("POST", dummy)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            JSONObject jsonBody = new JSONObject(str);
            requestResponse.setResponseDataStat(jsonBody.getString("stat"));

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to del comment", e);
            requestResponse.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return requestResponse;
        }

        return requestResponse;

    }


    public RequestResponse<?> addFavs (Context context,String photoId) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();


        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback","1")
                .addQueryParam("format", "json")
                .addQueryParam("method", ADD_FAVORITES)
                .addQueryParam("photo_id", photoId)
                .create();

        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method","HMAC-SHA1")
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", "1.0")
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("method", ADD_FAVORITES)
                .appendQueryParameter("photo_id", photoId)
                .build().toString();


        RequestBody dummy = RequestBody.create("null", null);
        RequestResponse<?> requestResponse = new RequestResponse<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(s)
                .method("POST", dummy)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            JSONObject jsonBody = new JSONObject(str);
            requestResponse.setResponseDataStat(jsonBody.getString("stat"));

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to add favorites", e);
            requestResponse.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return requestResponse;
        }

        return requestResponse;

    }


    public RequestResponse<?> removeFavs (Context context,String photoId) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();


        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback","1")
                .addQueryParam("format", "json")
                .addQueryParam("method", REMOVE_FAVORITES)
                .addQueryParam("photo_id", photoId)
                .create();

        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method","HMAC-SHA1")
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", "1.0")
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback","1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("method", REMOVE_FAVORITES)
                .appendQueryParameter("photo_id", photoId)
                .build().toString();


        RequestBody dummy = RequestBody.create("null", null);
        RequestResponse<?> requestResponse = new RequestResponse<>();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(s)
                .method("POST", dummy)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String str = response.body().string();
            JSONObject jsonBody = new JSONObject(str);
            requestResponse.setResponseDataStat(jsonBody.getString("stat"));

        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to remove favorites", e);
            requestResponse.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return requestResponse;
        }

        return requestResponse;

    }


    public RequestResponse<UserGallery> fetchUserGalleries(String user_id, String page) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", PHOTO_GET_USER_GALLERY)
                .appendQueryParameter("cover_photos", "1")
                .appendQueryParameter("user_id", user_id)
                .appendQueryParameter("page", page);

        return downloadUserGalleryList(uriBuilder.build().toString());
    }

    private RequestResponse<UserGallery> downloadUserGalleryList(String url) {
        RequestResponse<UserGallery> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject galleriesJsonObject = jsonBody.getJSONObject("galleries");
            JSONArray galleryJsonArray = galleriesJsonObject.getJSONArray("gallery");

            List<UserGallery> userGalleriesList = new ArrayList<>();

            for (int i = 0; i < galleryJsonArray.length(); i++) {
                JSONObject jsonGalleryItem = galleryJsonArray.getJSONObject(i);
                String gallery_id = jsonGalleryItem.getString("gallery_id");
                String owner = jsonGalleryItem.getString("owner");
                long dateCreate = jsonGalleryItem.getLong("date_create");
                String count_photos = jsonGalleryItem.getString("count_photos");
                String count_views  = jsonGalleryItem.getString("count_views");
                String count_comments = jsonGalleryItem.getString("count_comments");
                String title = jsonGalleryItem.getJSONObject("title").getString("_content");
                String description = jsonGalleryItem.getJSONObject("description").getString("_content");
                JSONObject coverJsonObject = jsonGalleryItem.getJSONObject("cover_photos");
                JSONArray coverJsonArray = coverJsonObject.getJSONArray("photo");
                List<String> coverPhotos = new ArrayList<>();
                for (int j = 0; j < coverJsonArray.length(); j++) {
                    coverPhotos.add(coverJsonArray.getJSONObject(j).getString("url"));
                }

                UserGallery userGallery = new UserGallery(owner,
                        gallery_id,
                        dateCreate,
                        count_photos,
                        count_views,
                        count_comments,
                        title,
                        description,
                        coverPhotos
                        );
                userGalleriesList.add(userGallery);
            }

            response = gson.fromJson(galleriesJsonObject.toString(), RequestResponse.class);
            response.setItems(userGalleriesList);
            response.setResponseDataStat(jsonBody.getString("stat"));


            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_USER_HAS_NO_GALLERIES);
            }

        }


        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_USER_HAS_NO_GALLERIES);
        }
        return response;

    }



    public RequestResponse<GalleryItem> fetchUserGroupPhotos(Context context, String group_id, String page) {

        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", GET_GROUP_PHOTOS)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("group_id", group_id)
                    .appendQueryParameter("page", page);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("group_id", group_id)
                    .addQueryParam("method", GET_GROUP_PHOTOS)
                    .addQueryParam("page", page)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("group_id", group_id)
                    .appendQueryParameter("method", GET_GROUP_PHOTOS)
                    .appendQueryParameter("page", page)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
        }
    }


    public RequestResponse<GalleryItem> fetchPublicPhotos(Context context, String user_id, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", PHOTO_GET_PUBLIC)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("user_id", user_id)
                    .appendQueryParameter("page", page);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_USER_HAS_NO_PHOTOS);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("user_id", user_id)
                    .addQueryParam("method", PHOTO_GET_PUBLIC)
                    .addQueryParam("page", page)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("user_id", user_id)
                    .appendQueryParameter("method", PHOTO_GET_PUBLIC)
                    .appendQueryParameter("page", page)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_USER_HAS_NO_PHOTOS);
        }
    }


    public RequestResponse<GalleryItem> fetchUserCameraRollPhotos(Context context, String page) {
     String userId = QueryPreferences.getUserId(context);
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("user_id", userId)
                    .addQueryParam("method", PHOTO_GET_USER_CAMERA_ROLL)
                    .addQueryParam("page", page)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("user_id", userId)
                    .appendQueryParameter("method", PHOTO_GET_USER_CAMERA_ROLL)
                    .appendQueryParameter("page", page)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_USER_HAS_NO_PHOTOS);
        }

    public RequestResponse<GalleryItem> fetchUserContactsPhotos(Context context, String page) {
        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                .setTokenKeys(QueryPreferences.getOauthToken(context),
                        QueryPreferences.getOauthTokenSecret(context))
                .build();


        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("extras", EXTRAS)
                .addQueryParam("method", PHOTO_GET_USER_CONTACTS_PHOTOS)
                .addQueryParam("page", page)
                .addQueryParam("per_page", PER_PAGE)
                .create();

        String s = Uri.parse(FLICKR_URL)
                .buildUpon()
                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                .appendQueryParameter("oauth_nonce", signature.getNonce())
                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                .appendQueryParameter("oauth_signature", signature.getSignature())
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("format", "json")
                .appendQueryParameter("extras", EXTRAS)
                .appendQueryParameter("method", PHOTO_GET_USER_CONTACTS_PHOTOS)
                .appendQueryParameter("page", page)
                .appendQueryParameter("per_page", PER_PAGE)
                .build().toString();

        return downloadGalleryItems(s, RequestResponse.RESPONSE_USER_HAS_NO_FOLLOWINGS);
    }



    public RequestResponse<Group> fetchGroupInfo (String group_id) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", GET_GROUP_INFO)
                .appendQueryParameter("group_id", group_id);

        return downloadGroupInfo (uriBuilder.build().toString());
    }

    private RequestResponse<Group> downloadGroupInfo (String url) {
        RequestResponse<Group> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("group");

            Group group = gson.fromJson(photosJsonObject.toString(), Group.class);

            List<Group> list = new ArrayList<>();
            list.add(group);

            response.setItems(list);
            response.setResponseDataStat(jsonBody.getString("stat"));
            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_USER_HAS_NO_GROUPS);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }

    public RequestResponse<GalleryItem> fetchInterestingPhotos(Context context, String query, String page, String perPage) {
        if (perPage == null) {
            perPage = PER_PAGE;
        }
        if (QueryPreferences.getUserId(context) == null) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", INTERESTING_PHOTO_METHOD)
                .appendQueryParameter("extras", EXTRAS)
                .appendQueryParameter("page", page)
                .appendQueryParameter("per_page", perPage);

        if (query != null) {
            uriBuilder.appendQueryParameter("date", query);
        }
        return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {

            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();

            if (query == null) {
                OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, "https://www.flickr.com/services/rest?")
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS)
                        .addQueryParam("method", INTERESTING_PHOTO_METHOD)
                        .addQueryParam("page", page)
                        .addQueryParam("per_page", perPage)
                        .create();


//
                String s = Uri.parse("https://www.flickr.com/services/rest?")
                        .buildUpon()
                        .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                        .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                        .appendQueryParameter("oauth_signature_method", "HMAC-SHA1")
                        .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                        .appendQueryParameter("oauth_nonce", signature.getNonce())
                        .appendQueryParameter("oauth_version", "1.0")
                        .appendQueryParameter("oauth_signature", signature.getSignature())
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("extras", EXTRAS)
                        .appendQueryParameter("method", INTERESTING_PHOTO_METHOD)
                        .appendQueryParameter("page", page)
                        .appendQueryParameter("per_page", perPage)
                        .build().toString();

                return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
            } else {

                OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, "https://www.flickr.com/services/rest?")
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS)
                        .addQueryParam("method", INTERESTING_PHOTO_METHOD)
                        .addQueryParam("page", page)
                        .addQueryParam("per_page", perPage)
                        .addQueryParam("date", query)
                        .create();

//
                String s = Uri.parse("https://www.flickr.com/services/rest?")
                        .buildUpon()
                        .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                        .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                        .appendQueryParameter("oauth_signature_method", "HMAC-SHA1")
                        .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                        .appendQueryParameter("oauth_nonce", signature.getNonce())
                        .appendQueryParameter("oauth_version", "1.0")
                        .appendQueryParameter("oauth_signature", signature.getSignature())
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("extras", EXTRAS)
                        .appendQueryParameter("method", INTERESTING_PHOTO_METHOD)
                        .appendQueryParameter("page", page)
                        .appendQueryParameter("per_page", perPage)
                        .appendQueryParameter("date", query)
                        .build().toString();
                return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
            }
        }
    }

    public RequestResponse<GalleryItem> fetchUserFaves(Context context, String userId, String page) {
        String ownerUserId = QueryPreferences.getUserId(context);
        if (ownerUserId == null || !ownerUserId.equals(userId)) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", PHOTO_GET_USER_PUBLIC_FAVES)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("user_id", userId)
                    .appendQueryParameter("page", page);

            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("method", PHOTO_GET_USER_FAVES)
                    .addQueryParam("page", page)
                    .create();


            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("method", PHOTO_GET_USER_FAVES)
                    .appendQueryParameter("page", page)
                    .build().toString();
            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
        }
    }

    public RequestResponse<GalleryItem> fetchUserGallery(Context context,String galleryId, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", GET_GALLERY_PHOTOS)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("gallery_id", galleryId)
                    .appendQueryParameter("page", page);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS );
        } else {

            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", GET_GALLERY_PHOTOS)
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("gallery_id", galleryId)
                    .addQueryParam("page", page)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("method", GET_GALLERY_PHOTOS)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("gallery_id", galleryId)
                    .appendQueryParameter("page", page)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS );
        }
    }


    public RequestResponse<GalleryItem> fetchInterestingPhotosByTag(Context context, String query, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("safe_search", "1")
                    .appendQueryParameter("sort", "interestingness-desc")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("tags", query);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {

            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", SEARCH_PHOTO_METHOD)
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("safe_search", "1")
                    .addQueryParam("sort", "interestingness-desc")
                    .addQueryParam("page", page)
                    .addQueryParam("tags", query)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("safe_search", "1")
                    .appendQueryParameter("sort", "interestingness-desc")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("tags", query)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
        }

    }

    public RequestResponse<GalleryItem> fetchRecentPhotos(Context context, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", SEARCH_RECENT_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {

            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("page", page)
                    .addQueryParam("method", SEARCH_RECENT_PHOTO_METHOD)
                    .addQueryParam("extras", EXTRAS)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", SEARCH_RECENT_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS);
        }

    }

    public RequestResponse<GalleryItem> fetchRecentPhotosByTag(Context context, String query, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("safe_search", "1")
                    .appendQueryParameter("tags", query);
            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_NO_PHOTOS);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("page", page)
                    .addQueryParam("method", SEARCH_PHOTO_METHOD)
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("safe_search", "1")
                    .addQueryParam("tags", query)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("safe_search", "1")
                    .appendQueryParameter("tags", query)
                    .build().toString();

            return downloadGalleryItems(s, RequestResponse.RESPONSE_NO_PHOTOS );
        }
    }

    public RequestResponse<GalleryItem> fetchSearchPhotos(Context context,
                                                          String query,
                                                          String page,
                                                          Location location,
                                                          String perPage,
                                                          boolean needGeo) {
        if (perPage == null) {
            perPage = PER_PAGE;
        }
        if (QueryPreferences.getUserId(context) == null) {



            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("safe_search", "1")
                    .appendQueryParameter("sort", "interestingness-desc")
                    .appendQueryParameter("text", query)
                    .appendQueryParameter("per_page", perPage);
            if (needGeo) {
                uriBuilder
                        .appendQueryParameter("has_geo", "1");
            }

            if (location != null) {
                uriBuilder
                        .appendQueryParameter("lat", "" + location.getLatitude())
                        .appendQueryParameter("lon", "" + location.getLongitude());
            }

            return downloadGalleryItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_BAD_SEARCH);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();

            OAuthSignature signature;
            String s;

            if (location != null && needGeo) {
                signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("page", page)
                        .addQueryParam("method", SEARCH_PHOTO_METHOD)
                        .addQueryParam("extras", EXTRAS)
                        .addQueryParam("safe_search", "1")
                        .addQueryParam("sort", "interestingness-desc")
                        .addQueryParam("text", query)
                        .addQueryParam("per_page", perPage)
                        .addQueryParam("lat", "" + location.getLatitude())
                        .addQueryParam("lon", "" + location.getLongitude())
                        .addQueryParam("has_geo", "1")
                        .create();

                s = Uri.parse(FLICKR_URL)
                        .buildUpon()
                        .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                        .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                        .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                        .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                        .appendQueryParameter("oauth_nonce", signature.getNonce())
                        .appendQueryParameter("oauth_version", OAUTH_VERSION)
                        .appendQueryParameter("oauth_signature", signature.getSignature())
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("page", page)
                        .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                        .appendQueryParameter("extras", EXTRAS)
                        .appendQueryParameter("safe_search", "1")
                        .appendQueryParameter("sort", "interestingness-desc")
                        .appendQueryParameter("text", query)
                        .appendQueryParameter("per_page", perPage)
                        .appendQueryParameter("lat", "" + location.getLatitude())
                        .appendQueryParameter("lon", "" + location.getLongitude())
                        .appendQueryParameter("has_geo", "1")
                        .build().toString();
            } else if (needGeo) {
                signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("page", page)
                        .addQueryParam("method", SEARCH_PHOTO_METHOD)
                        .addQueryParam("extras", EXTRAS)
                        .addQueryParam("safe_search", "1")
                        .addQueryParam("sort", "interestingness-desc")
                        .addQueryParam("text", query)
                        .addQueryParam("per_page", perPage)
                        .addQueryParam("has_geo", "1")
                        .create();

                s = Uri.parse(FLICKR_URL)
                        .buildUpon()
                        .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                        .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                        .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                        .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                        .appendQueryParameter("oauth_nonce", signature.getNonce())
                        .appendQueryParameter("oauth_version", OAUTH_VERSION)
                        .appendQueryParameter("oauth_signature", signature.getSignature())
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("page", page)
                        .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                        .appendQueryParameter("extras", EXTRAS)
                        .appendQueryParameter("safe_search", "1")
                        .appendQueryParameter("sort", "interestingness-desc")
                        .appendQueryParameter("text", query)
                        .appendQueryParameter("per_page", perPage)
                        .appendQueryParameter("has_geo", "1")
                        .build().toString();
            } else {
                signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("page", page)
                        .addQueryParam("method", SEARCH_PHOTO_METHOD)
                        .addQueryParam("extras", EXTRAS)
                        .addQueryParam("safe_search", "1")
                        .addQueryParam("sort", "interestingness-desc")
                        .addQueryParam("text", query)
                        .addQueryParam("per_page", perPage)
                        .create();

                s = Uri.parse(FLICKR_URL)
                        .buildUpon()
                        .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                        .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                        .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                        .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                        .appendQueryParameter("oauth_nonce", signature.getNonce())
                        .appendQueryParameter("oauth_version", OAUTH_VERSION)
                        .appendQueryParameter("oauth_signature", signature.getSignature())
                        .appendQueryParameter("nojsoncallback", "1")
                        .appendQueryParameter("format", "json")
                        .appendQueryParameter("page", page)
                        .appendQueryParameter("method", SEARCH_PHOTO_METHOD)
                        .appendQueryParameter("extras", EXTRAS)
                        .appendQueryParameter("safe_search", "1")
                        .appendQueryParameter("sort", "interestingness-desc")
                        .appendQueryParameter("text", query)
                        .appendQueryParameter("per_page", perPage)
                        .build().toString();
            }

            return downloadGalleryItems(s, RequestResponse.RESPONSE_BAD_SEARCH );
        }
    }
    public RequestResponse<Person> getPersonInfo (String personUserId) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", PERSON_GET_INFO_METHOD)
                .appendQueryParameter("user_id", personUserId);
        return downloadPersonInfo(uriBuilder.build().toString(), personUserId);
    }

    private RequestResponse<Person> downloadPersonInfo(String url, String userId) {
        RequestResponse<Person> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();

            String personInfoString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + personInfoString);
            JSONObject personInfoBody = new JSONObject(personInfoString);
            JSONObject personInfoObject = personInfoBody.getJSONObject("person");
            Person person = gson.fromJson(personInfoObject.toString(), Person.class);

            RequestResponse<Person.Contact> contactsRequest = getUserContacts(userId);
            person.setContacts(contactsRequest.getItems());
            person.setContactsCount(String.valueOf(contactsRequest.getTotal()));


            List<Person> list = new ArrayList<>();
            list.add(person);
            response.setItems(list);
            response.setResponseDataStat(personInfoBody.getString("stat"));

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
        } catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }

    public RequestResponse<Person> findUser(String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon();
                if (query.contains("@")) {
                    uriBuilder.appendQueryParameter("method", SEARCH_PERSON_BY_EMAIL_METHOD)
                            .appendQueryParameter("find_email", query);
                } else {
                    uriBuilder.appendQueryParameter("method", SEARCH_PERSON_BY_NAME_METHOD)
                            .appendQueryParameter("username", query);
                }
        return downloadPersonItems(uriBuilder.build().toString());
    }

    private RequestResponse<Person> downloadPersonItems(String url) {
        RequestResponse<Person> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("user");
            Person person = gson.fromJson(photosJsonObject.toString(), Person.class);

            response = getPersonInfo(person.getId());


        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
        } catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_BAD_SEARCH);
        }
        return response;
    }

    public RequestResponse<Person.Contact> getUserContacts (String userId) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", GET_USER_CONTACTS)
                .appendQueryParameter("user_id", userId);
        return downloadUserContactsInfo(uriBuilder.build().toString());
    }

    private RequestResponse<Person.Contact> downloadUserContactsInfo(String url) {
        RequestResponse<Person.Contact> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();

            String personContactsString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + personContactsString);
            JSONObject personContactsBody = new JSONObject(personContactsString);
            JSONObject personContactsObject = personContactsBody.getJSONObject("contacts");
            JSONArray personContactsArray = personContactsObject.getJSONArray("contact");


            Type personContactsType = new TypeToken<ArrayList<Person.Contact>>(){}.getType();
            response = gson.fromJson(personContactsObject.toString(), RequestResponse.class);

            response.setItems(gson.fromJson(personContactsArray.toString(),personContactsType));
            response.setResponseDataStat(personContactsBody.getString("stat"));
            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_USER_HAS_NO_CONTACTS);
            }



        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
        } catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }



    public RequestResponse<Album> fetchUserAlbums (Context context, String query, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", PHOTO_GET_USER_ALBUMS)
                    .appendQueryParameter("user_id", query)
                    .appendQueryParameter("primary_photo_extras", "url_q, original_format");
            return downloadUserAlbums(context, uriBuilder.build().toString(), query, Integer.parseInt(page));
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("page", page)
                    .addQueryParam("method", PHOTO_GET_USER_ALBUMS)
                    .addQueryParam("user_id", query)
                    .addQueryParam("primary_photo_extras", "url_q, original_format")
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", PHOTO_GET_USER_ALBUMS)
                    .appendQueryParameter("user_id", query)
                    .appendQueryParameter("primary_photo_extras", "url_q, original_format")
                    .build().toString();

            return downloadUserAlbums(context, s, query, Integer.parseInt(page));
        }


    }

    private RequestResponse<Album> downloadUserAlbums(Context context, String url, String userid, int page) {
        RequestResponse<Album> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("photosets");
            JSONArray photosJsonArray = photosJsonObject.getJSONArray("photoset");

            Type userAlbumType = new TypeToken<ArrayList<Album>>(){}.getType();

            response = gson.fromJson(photosJsonObject.toString(), RequestResponse.class);
            List<Album> albums = gson.fromJson(photosJsonArray.toString(), userAlbumType);

            if (page == 1) {
                {
                    String jsonFavesString;
                    String ownerUserId = QueryPreferences.getUserId(context);
                    if (ownerUserId == null || !ownerUserId.equals(userid)) {
                        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                                .appendQueryParameter("method", PHOTO_GET_USER_PUBLIC_FAVES)
                                .appendQueryParameter("extras", EXTRAS)
                                .appendQueryParameter("user_id", userid)
                                .appendQueryParameter("page", "1");
                        jsonFavesString = getUrlString(uriBuilder.build().toString());
                    } else {
                        OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                                .setTokenKeys(QueryPreferences.getOauthToken(context),
                                        QueryPreferences.getOauthTokenSecret(context))
                                .build();


                        OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                                .addQueryParam("nojsoncallback", "1")
                                .addQueryParam("format", "json")
                                .addQueryParam("extras", EXTRAS)
                                .addQueryParam("method", PHOTO_GET_USER_FAVES)
                                .addQueryParam("page", "1")
                                .create();


//
                       String s = Uri.parse(FLICKR_URL)
                                .buildUpon()
                                .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                                .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                                .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                                .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                                .appendQueryParameter("oauth_nonce", signature.getNonce())
                                .appendQueryParameter("oauth_version", OAUTH_VERSION)
                                .appendQueryParameter("oauth_signature", signature.getSignature())
                                .appendQueryParameter("nojsoncallback", "1")
                                .appendQueryParameter("format", "json")
                                .appendQueryParameter("extras", EXTRAS)
                                .appendQueryParameter("method", PHOTO_GET_USER_FAVES)
                                .appendQueryParameter("page", "1")
                                .build().toString();
                        jsonFavesString = getUrlString(s);

                    }
                    Log.i(TAG, "Received JSON: " + jsonFavesString);
                    JSONObject jsonFavesBody = new JSONObject(jsonFavesString);
                    JSONObject favesJsonObject = jsonFavesBody.getJSONObject("photos");
                    if (favesJsonObject.getInt("pages") > 0) {
                        JSONArray favesJsonArray = favesJsonObject.getJSONArray("photo");
                        JSONObject favesFirstJsonObject = favesJsonArray.getJSONObject(0);
                        if (favesFirstJsonObject != null) {
                            Album album = new Album();
                            album.setOwner(userid);
                            album.setCount_photos(favesJsonObject.getString("total"));
                            album.setTitle("Faves");
                            album.setPrimaryCoverPhotoUrl(favesFirstJsonObject.getString("url_s"));
                            albums.add(0, album);
                        }
                    }
                }
            }


            response.setItems(albums);
            response.setResponseDataStat(jsonBody.getString("stat"));
            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_USER_HAS_NO_ALBUMS);
            }

        }

        catch (IOException ioe ) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_USER_HAS_NO_ALBUMS);
        }
        return response;
    }


    public RequestResponse<GroupSearchItem> fetchUserGroups (Context context, String userId, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", PHOTO_GET_USER_GROUPS)
                    .appendQueryParameter("user_id", userId)
                    .appendQueryParameter("extras", "privacy");
            return downloadGroupItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_USER_HAS_NO_GROUPS);
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", PHOTO_GET_USER_ALL_GROUPS)
                    .addQueryParam("user_id", userId)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("method", PHOTO_GET_USER_ALL_GROUPS)
                    .appendQueryParameter("user_id", userId)
                    .build().toString();

            return downloadGroupItems(s, RequestResponse.RESPONSE_USER_HAS_NO_GROUPS);
        }
    }


    public RequestResponse<GroupSearchItem> fetchGroups (String query, String page) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("page", page)
                .appendQueryParameter("method", SEARCH_GROUP)
                .appendQueryParameter("text", query);
        return downloadGroupItems(uriBuilder.build().toString(), RequestResponse.RESPONSE_BAD_SEARCH);
    }

    public RequestResponse<PhotoSize> getPhotoSize (String photoId) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", PHOTO_GET_SIZES)
                .appendQueryParameter("photo_id", photoId);
        return downloadPhotoSizes(uriBuilder.build().toString());
    }

    private RequestResponse<PhotoSize> downloadPhotoSizes(String url) {
        RequestResponse<PhotoSize> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("sizes");
            JSONArray photosJsonArray = photosJsonObject.getJSONArray("size");

            Type photoType = new TypeToken<ArrayList<PhotoSize>>(){}.getType();

            response.setItems(gson.fromJson(photosJsonArray.toString(),photoType));
            response.setResponseDataStat(jsonBody.getString("stat"));
            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_DATA_FAIL);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }


    public RequestResponse<PhotoExif> getPhotoExif (String photoId) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", PHOTO_GET_EXIF)
                .appendQueryParameter("photo_id", photoId);
        return downloadPhotoExif(uriBuilder.build().toString());
    }

    private RequestResponse<PhotoExif> downloadPhotoExif(String url) {
        RequestResponse<PhotoExif> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photoJsonObject = jsonBody.getJSONObject("photo");
            JSONArray exifJsonArray = photoJsonObject.getJSONArray("exif");

            Type exifType = new TypeToken<ArrayList<PhotoExif.PhotoExifData>>(){}.getType();
            PhotoExif photoExif = new PhotoExif(gson.fromJson(exifJsonArray.toString(),exifType));

            List<PhotoExif> list = new ArrayList<>();
            list.add(photoExif);
            response.setItems(list);
            response.setResponseDataStat(jsonBody.getString("stat"));

//            if (response.getItems().size() == 0) {
//                throw new JSONException(RequestResponse.RESPONSE_DATA_FAIL);
//            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }


    public RequestResponse<PhotoInfo> getPhotoInfo (String photoId) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", PHOTO_GET_INFO)
                .appendQueryParameter("photo_id", photoId);
        return downloadPhotoInfo(uriBuilder.build().toString(), photoId);
    }

    private RequestResponse<PhotoInfo> downloadPhotoInfo(String url, String photoId) {
        RequestResponse<PhotoInfo> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("photo");
            PhotoInfo photoInfo = gson.fromJson(photosJsonObject.toString(), PhotoInfo.class);

            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", PHOTO_GET_FAVS)
                    .appendQueryParameter("photo_id", photoId);
            String jsonFavsString = getUrlString(uriBuilder.build().toString());
            JSONObject jsonFavsBody = new JSONObject(jsonFavsString);
            JSONObject photosFavsObject = jsonFavsBody.getJSONObject("photo");
            String favsQuantity = photosFavsObject.getString("total");

            photoInfo.setFavsQuantity(favsQuantity);

            List<PhotoInfo> list = new ArrayList<>();
            list.add(photoInfo);
            response.setItems(list);
            response.setResponseDataStat(jsonBody.getString("stat"));

            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_DATA_FAIL);
            }

        }


        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }




    private RequestResponse<GroupSearchItem> downloadGroupItems(String url, String requestErrorPattern) {
        RequestResponse<GroupSearchItem> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("groups");
            JSONArray photosJsonArray = photosJsonObject.getJSONArray("group");
            Type groupType = new TypeToken<ArrayList<GroupSearchItem>>(){}.getType();
            response = gson.fromJson(photosJsonObject.toString(), RequestResponse.class);
            response.setItems(gson.fromJson(photosJsonArray.toString(),groupType));
            response.setResponseDataStat(jsonBody.getString("stat"));
            if (response.getItems().size() == 0) {
                throw new JSONException(requestErrorPattern);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(requestErrorPattern);
        }
        return response;
    }


    public RequestResponse<Comment> fetchCommentsList (Context context, String photoId) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("method", GET_PHOTO_COMMENTS)
                    .appendQueryParameter("photo_id", photoId);
            return downloadComments(uriBuilder.build().toString());
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", GET_PHOTO_COMMENTS)
                    .addQueryParam("photo_id", photoId)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("method", GET_PHOTO_COMMENTS)
                    .appendQueryParameter("photo_id", photoId)
                    .build().toString();

            return downloadComments(s);
        }
    }

    private RequestResponse<Comment> downloadComments(String url) {
        RequestResponse<Comment> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject commentsJsonObject = jsonBody.getJSONObject("comments");
            JSONArray commentsJsonArray = commentsJsonObject.getJSONArray("comment");

            Type commentsType = new TypeToken<ArrayList<Comment>>(){}.getType();

            response = gson.fromJson(commentsJsonObject.toString(), RequestResponse.class);
            response.setItems(gson.fromJson(commentsJsonArray.toString(),commentsType));
            response.setResponseDataStat(jsonBody.getString("stat"));


            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_NO_COMMENTS);
            }

        }

        catch (IOException ioe ) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_NO_COMMENTS);
        }
        return response;
    }


    public RequestResponse<GalleryItem> fetchUserAlbum (Context context, String userId, String albumId, String page) {
        if (QueryPreferences.getUserId(context) == null) {
            Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                    .appendQueryParameter("page", page)
                    .appendQueryParameter("method", GET_ALBUM_PHOTOS)
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("user_id", userId)
                    .appendQueryParameter("photoset_id", albumId);

            return downloadAlbumItems(uriBuilder.build().toString());
        } else {
            OAuthConfig oauthConfig = new OAuthConfigBuilder(CONSUMER_KEY, CONSUMER_SECRET)
                    .setTokenKeys(QueryPreferences.getOauthToken(context),
                            QueryPreferences.getOauthTokenSecret(context))
                    .build();


            OAuthSignature signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS)
                    .addQueryParam("method", GET_ALBUM_PHOTOS)
                    .addQueryParam("user_id", userId)
                    .addQueryParam("photoset_id", albumId)
                    .create();

            String s = Uri.parse(FLICKR_URL)
                    .buildUpon()
                    .appendQueryParameter("oauth_consumer_key", CONSUMER_KEY)
                    .appendQueryParameter("oauth_token", QueryPreferences.getOauthToken(context))
                    .appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD)
                    .appendQueryParameter("oauth_timestamp", signature.getTimestamp())
                    .appendQueryParameter("oauth_nonce", signature.getNonce())
                    .appendQueryParameter("oauth_version", OAUTH_VERSION)
                    .appendQueryParameter("oauth_signature", signature.getSignature())
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("extras", EXTRAS)
                    .appendQueryParameter("method", GET_ALBUM_PHOTOS)
                    .appendQueryParameter("user_id", userId)
                    .appendQueryParameter("photoset_id", albumId)
                    .build().toString();

            return downloadAlbumItems(s);
        }
    }



    private RequestResponse<GalleryItem> downloadAlbumItems(String url) {
        RequestResponse<GalleryItem> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("photoset");
            JSONArray photosJsonArray = photosJsonObject.getJSONArray("photo");

            Type galleyItemType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();

            response = gson.fromJson(photosJsonObject.toString(), RequestResponse.class);
            response.setItems(gson.fromJson(photosJsonArray.toString(),galleyItemType));
            response.setResponseDataStat(jsonBody.getString("stat"));

            String owner = photosJsonObject.getString("owner");
            for (GalleryItem item : response.getItems()) {
                item.setOwner(owner);
            }

            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_DATA_FAIL);
            }

        }

        catch (IOException ioe ) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(RequestResponse.RESPONSE_DATA_FAIL);
        }
        return response;
    }


    private RequestResponse<GalleryItem> downloadGalleryItems(String url, String badRequestPattern) {
        RequestResponse<GalleryItem> response = new RequestResponse<>();
        try {
            Gson gson = new Gson();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON PHOTOS: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
            JSONArray photosJsonArray = photosJsonObject.getJSONArray("photo");

            Type galleyItemType = new TypeToken<ArrayList<GalleryItem>>(){}.getType();

            response = gson.fromJson(photosJsonObject.toString(), RequestResponse.class);
            response.setItems(gson.fromJson(photosJsonArray.toString(),galleyItemType));
            response.setResponseDataStat(jsonBody.getString("stat"));
            if (response.getPages() == 0) {
                throw new JSONException(badRequestPattern);
            }

            if (response.getItems().size() == 0) {
                throw new JSONException(RequestResponse.RESPONSE_NO_PHOTOS);
            }

        }

        catch (IOException ioe ) {
            Log.e(TAG, "Failed to fetch items", ioe);
            response.setConnectionStat(RequestResponse.CONNECTION_FAIL);
            return response;
        }
        catch (JSONException jse) {
            Log.e(TAG, "Failed to parse Json", jse);
            response.setResponseDataStat(badRequestPattern);
        }
        return response;
    }



}
