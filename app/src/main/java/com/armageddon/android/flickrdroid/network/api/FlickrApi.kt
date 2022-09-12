package com.armageddon.android.flickrdroid.network.api

import com.armageddon.android.flickrdroid.model.*
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query



const val KEY = "4e8208756970a8da78fe888ecca66d0a"
const val SECRET = "7bc878d3927325a7"

const val EXTRAS = "isfavorite,url_sq,url_s,url_m,url_z,url_k,url_l,url_3k," +
        "url_4k,url_5k,url_6k,url_h,geo,icon_server,owner_name,realname,count_comments," +
        "count_faves,views,date_taken,camera,description,tags,license"
const val EXTRAS_NOT_GEO = "isfavorite,url_sq,url_s,url_m,url_z,url_k,url_l,url_3k," +
        "url_4k,url_5k,url_6k,url_h,icon_server,owner_name,realname,count_comments," +
        "count_faves,views,date_taken,camera,description,tags,license"
const val EXTRAS_PHOTO_ITEM = "&extras=icon_server,owner_name,realname,count_comments,count_faves,isfavorite"
const val EXTRAS_PHOTO_INFO = "&extras=sizes"
const val EXTRAS_PHOTO_EXIF = "&extras=icon_server,owner_name,realname,count_comments,count_faves"
const val EXTRAS_ALBUM_LIST = "&primary_photo_extras=url_z, original_format"
const val EXTRAS_GALLERY_LIST = "&cover_photos=1&primary_photo_cover_size=z"
const val EXTRAS_PERSON = "&extras=contacts"
const val EXTRAS_GEO_LOCATION = "&extras=geo"
const val EXTRAS_MAP_SEARCH = "&has_geo=1&extras=geo,icon_server,owner_name,realname,count_comments,count_faves,camera,isfavorite"
const val GET_USER_GROUPS_CAN_ADD_PHOTO = "?method=flickr.groups.pools.getGroups"
const val ADD_ALBUM_PHOTO = "?method=flickr.photosets.addPhoto"
const val ADD_GROUP_PHOTO = "?method=flickr.groups.pools.add"
const val SEARCH_PHOTO_METHOD = "?method=flickr.photos.search"
const val SEARCH_RECENT_PHOTO_METHOD = "?method=flickr.photos.getRecent"
const val INTERESTING_PHOTO_METHOD = "?method=flickr.interestingness.getList"
const val SEARCH_PERSON_BY_NAME_METHOD = "?method=flickr.people.findByUsername"
const val SEARCH_PERSON_BY_EMAIL_METHOD = "?method=flickr.people.findByEmail"
const val PERSON_GET_INFO_METHOD = "?method=flickr.people.getInfo"
const val SEARCH_GROUP = "?method=flickr.groups.search"
const val PHOTO_GET_SIZES = "?method=flickr.photos.getSizes"
const val PHOTO_GET_INFO = "?method=flickr.photos.getInfo"
const val PHOTO_GET_EXIF = "?method=flickr.photos.getExif"
const val USER_FAVORITES_PHOTOS = "?method=flickr.favorites.getList"
const val PHOTO_GET_USER_PUBLIC_FAVES = "?method=flickr.favorites.getPublicList"
const val USER_PUBLIC_PHOTOS = "?method=flickr.people.getPublicPhotos"
const val USER_PUBLIC_GROUPS = "?method=flickr.people.getPublicGroups"
const val PHOTO_GET_USER_CAMERA_ROLL = "?method=flickr.people.getPhotos"
const val PHOTO_GET_USER_CONTACTS_PHOTOS = "?method=flickr.photos.getContactsPhotos"
const val PHOTO_GET_USER_GALLERY = "?method=flickr.galleries.getList"
const val PHOTO_GET_USER_GROUPS = "?method=flickr.people.getPublicGroups"
const val PHOTO_GET_USER_ALL_GROUPS = "?method=flickr.people.getGroups"
const val PHOTO_GET_USER_ALBUMS = "?method=flickr.photosets.getList"
const val PHOTO_GET_USER_FAVES = "?method=flickr.favorites.getList"
const val GET_USER_CONTACTS = "?method=flickr.contacts.getPublicList"
const val GET_ALBUM_PHOTOS = "?method=flickr.photosets.getPhotos"
const val GET_GALLERY_PHOTOS = "?method=flickr.galleries.getPhotos"
const val GET_GROUP_PHOTOS = "?method=flickr.groups.pools.getPhotos"
const val GET_GROUP_INFO = "?method=flickr.groups.getInfo"
const val GET_PHOTO_COMMENTS = "?method=flickr.photos.comments.getList"
const val SET_PHOTO_PRIVACY = "?method=flickr.photos.setPerms"
const val ADD_COMMENTS = "?method=flickr.photos.comments.addComment"
const val DEL_COMMENTS = "?method=flickr.photos.comments.deleteComment"
const val ADD_FAVORITES = "?method=flickr.favorites.add"
const val REMOVE_FAVORITES = "?method=flickr.favorites.remove"

private const val BODY =
        "&oauth_consumer_key=" + KEY +
        "&oauth_signature_method=HMAC-SHA1" +
        "&oauth_version=1.0" +
        "&format=json" +
        "&nojsoncallback=1"

interface FlickrApi {
    @GET(GET_USER_GROUPS_CAN_ADD_PHOTO + BODY)
    suspend fun fetchGroupsUserCanAddPhoto(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<Group>

    @GET( PHOTO_GET_USER_CAMERA_ROLL + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchCameraRoll(
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<Photo>

    @POST( ADD_GROUP_PHOTO + BODY)
    suspend fun addGroupPhoto(
        @Query("photo_id") photoId: String,
        @Query("group_id") groupId: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( ADD_ALBUM_PHOTO + BODY)
    suspend fun addAlbumPhoto(
        @Query("photo_id") photoId: String,
        @Query("photoset_id") photosetId: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( SET_PHOTO_PRIVACY + BODY)
    suspend fun setPhotoPrivacy(
        @Query("photo_id") photoId: String,
        @Query("is_public") isPublic: String,
        @Query("is_friend") isFriend: String,
        @Query("is_family") isFamily: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( ADD_COMMENTS + BODY)
    suspend fun addPhotoComment(
        @Query("photo_id") photoId: String,
        @Query("comment_text") commentText: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( DEL_COMMENTS + BODY)
    suspend fun delPhotoComment(
        @Query("comment_id") commentId: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( REMOVE_FAVORITES + BODY)
    suspend fun removeFavoritePhoto(
        @Query("photo_id") photoId: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @POST( ADD_FAVORITES + BODY)
    suspend fun addFavoritePhoto(
        @Query("photo_id") photoId: String,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<String>

    @GET( PHOTO_GET_USER_CONTACTS_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchUserFollowingsPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("oauth_token") oauthToken: String,
        @Query("oauth_timestamp") oauthTimestamp: String,
        @Query("oauth_nonce") oauthNonce: String,
        @Query("oauth_signature") oauthSignature: String
    ): FlickrResponse<Photo>


    @GET( INTERESTING_PHOTO_METHOD + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchInterestingPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("date") date: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""

    ): FlickrResponse<Photo>

    @GET( GET_ALBUM_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchAlbumPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("photoset_id") photosetId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( GET_GALLERY_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchGalleryPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("gallery_id") galleryId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( GET_GROUP_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchGroupPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("group_id") galleryId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>


    @GET( USER_PUBLIC_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchUserPublicPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( USER_FAVORITES_PHOTOS + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchUserFavoritesPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( SEARCH_RECENT_PHOTO_METHOD + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchRecentPhotos(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( PHOTO_GET_INFO + BODY + EXTRAS_PHOTO_INFO)
    suspend fun fetchPhotoInfo(
        @Query("photo_id") photoId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<PhotoInfo>

    @GET( PHOTO_GET_EXIF + BODY + EXTRAS_PHOTO_EXIF)
    suspend fun fetchPhotoExif(
        @Query("photo_id") photoId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<PhotoExif>

    @GET( SEARCH_PHOTO_METHOD + BODY + EXTRAS_PHOTO_ITEM)
    suspend fun fetchPhotosSearch(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("text") text: String,
        @Query("tags") tags: String,
        @Query("sort") sort: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( SEARCH_PHOTO_METHOD + BODY + EXTRAS_MAP_SEARCH)
   suspend fun fetchMapPhotosWorldWide(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("text") text: String,
        @Query("sort") sort: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( SEARCH_PHOTO_METHOD + BODY + EXTRAS_MAP_SEARCH)
   suspend fun fetchMapPhotosNearMe(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("text") text: String,
        @Query("sort") sort: String,
        @Query("lat") userLat: String,
        @Query("lon") userLon: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Photo>

    @GET( SEARCH_GROUP + BODY)
    suspend fun fetchSearchGroup(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("text") text: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Group>

    @GET( GET_GROUP_INFO + BODY)
    suspend fun fetchGroupInfo(
        @Query("group_id") groupId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<GroupInfo>

    @GET( PHOTO_GET_USER_ALBUMS + BODY + EXTRAS_ALBUM_LIST)
    suspend fun fetchAlbumList(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Album>

    @GET( USER_PUBLIC_GROUPS + BODY)
    suspend fun fetchGroupList(
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Group>

    @GET( PHOTO_GET_USER_GALLERY + BODY + EXTRAS_GALLERY_LIST)
    suspend fun fetchGalleryList(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Gallery>

    @GET( GET_USER_CONTACTS + BODY)
    suspend fun fetchPersonContactList(
        @Query("page") page: Int,
        @Query("per_page") perPage: Int,
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<PersonContact>

    @GET( PERSON_GET_INFO_METHOD + BODY + EXTRAS_PERSON)
    suspend fun fetchPersonInfo(
        @Query("user_id") userID: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<Person>

    @GET( SEARCH_PERSON_BY_NAME_METHOD + BODY)
    suspend fun fetchPersonIdByUserName(
        @Query("username") userID: String
    ): FlickrResponse<Person>

    @GET( SEARCH_PERSON_BY_EMAIL_METHOD + BODY)
    suspend fun fetchPersonIdByEmail(
        @Query("find_email") userEmail: String
    ): FlickrResponse<Person>

    @GET( GET_PHOTO_COMMENTS + BODY)
    suspend fun fetchPhotoComments(
        @Query("photo_id") photoId: String,
        @Query("oauth_token") oauthToken: String = "",
        @Query("oauth_timestamp") oauthTimestamp: String = "",
        @Query("oauth_nonce") oauthNonce: String = "",
        @Query("oauth_signature") oauthSignature: String = ""
    ): FlickrResponse<PhotoComment>


}