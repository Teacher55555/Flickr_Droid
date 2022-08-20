package com.armageddon.android.flickrdroid.network.api


import android.util.Log
import com.armageddon.android.flickrdroid.common.PhotoPrivacy.*
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.*
import com.armageddon.android.flickrdroid.network.deserializers.*
import com.armageddon.android.flickrdroid.network.oauth.HttpMethod
import com.armageddon.android.flickrdroid.network.oauth.OAuthConfig
import com.armageddon.android.flickrdroid.network.oauth.OAuthConfigBuilder
import com.armageddon.android.flickrdroid.network.oauth.OAuthSignature
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration


private const val NETWORK_TAG = "Network Interceptor"
const val FLICKR_URL = "https://api.flickr.com/services/rest/"
const val PAGE_SIZE = 100
const val PREFETCH_DISTANCE = 50
const val TIME_OUT = 20L
const val INITIAL_LOAD_SIZE = true


class FlickrFetchr {

    private val flickrApi: FlickrApi
    private val photoItemType = object : TypeToken<FlickrResponse<Photo>>() {}.type
    private val photoInfoType = object : TypeToken<FlickrResponse<PhotoInfo>>() {}.type
    private val photoExifType = object : TypeToken<FlickrResponse<PhotoExif>>() {}.type
    private val groupType = object : TypeToken<FlickrResponse<Group>>() {}.type
    private val albumType = object : TypeToken<FlickrResponse<Album>>() {}.type
    private val galleryType = object : TypeToken<FlickrResponse<Gallery>>() {}.type
    private val personContactListType = object : TypeToken<FlickrResponse<PersonContact>>() {}.type
    private val personType = object : TypeToken<FlickrResponse<Person>>() {}.type
    private val groupInfoType = object : TypeToken<FlickrResponse<GroupInfo>>() {}.type
    private val photoCommentType = object : TypeToken<FlickrResponse<PhotoComment>>() {}.type

    init {
        val gsonConverter = GsonBuilder()
            .registerTypeAdapter(photoInfoType, PhotoInfoDeserializer())
            .registerTypeAdapter(photoExifType, PhotoExifDeserializer())
            .registerTypeAdapter(photoItemType, PhotoItemDeserializer())
            .registerTypeAdapter(groupType, GroupDeserializer())
            .registerTypeAdapter(albumType, AlbumDeserializer())
            .registerTypeAdapter(galleryType, GalleryDeserializer())
            .registerTypeAdapter(personContactListType, PersonsContactDeserializer())
            .registerTypeAdapter(personType, PersonDeserializer())
            .registerTypeAdapter(groupInfoType, GroupInfoDeserializer())
            .registerTypeAdapter(photoCommentType, PhotoCommentDeserializer())
            .create()

        val clientBuilder = OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(TIME_OUT))
            .addInterceptor { chain ->
                val request = chain.request()
//                Log.e("request", request.url.toString())
                val response = chain.proceed(request)
//                response.body?.string()?.let { Log.e("response", it) }
                response
            }

        val retrofit = Retrofit.Builder()
            .baseUrl(FLICKR_URL)
            .addConverterFactory(GsonConverterFactory.create(gsonConverter))
            .client(clientBuilder.build())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    suspend fun fetchInterestingPhotos(page: Int, perPage: Int, query: Query)
    : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchInterestingPhotos(page, perPage, query.date)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                        .addQueryParam("method", INTERESTING_PHOTO_METHOD.replace("?method=","") )
                        .addQueryParam("page", page.toString())
                        .addQueryParam("per_page", perPage.toString())
                        .create()

                flickrApi.fetchInterestingPhotos(page, perPage,
                    query.date,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }


    suspend fun fetchPhotoSearch(page: Int, perPage: Int, query: Query)
    : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchPhotosSearch(page, perPage, query.text, query.tags, query.sort)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                        .addQueryParam("method", SEARCH_PHOTO_METHOD.replace("?method=","") )
                        .addQueryParam("page", page.toString())
                        .addQueryParam("per_page", perPage.toString())
                        .addQueryParam("text", query.text)
                        .addQueryParam("tags", query.tags)
                        .addQueryParam("sort", query.sort)
                        .create()

                flickrApi.fetchPhotosSearch(page, perPage,
                    query.text,
                    query.tags,
                    query.sort,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature,
                )
            }
        }
    }

   suspend fun fetchMapPhotos(page: Int = 1, query: Query)
            : FlickrResponse<Photo> {
       return when (query.oauthToken.isBlank()) {
           true -> when (query.longitude.isBlank()) {
               true -> flickrApi.fetchMapPhotosWorldWide(page,
                   query.perPage,
                   query.text,
                   query.sort
               )
               false -> flickrApi.fetchMapPhotosNearMe(page,
                   query.perPage,
                   query.text,
                   query.sort,
                   query.latitude,
                   query.longitude
               )
           }
           false -> {
               val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                   .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                   .build()

               when (query.longitude.isBlank()) {
                   true -> {
                       val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                           .addQueryParam("nojsoncallback", "1")
                           .addQueryParam("format", "json")
                           .addQueryParam("extras", EXTRAS_MAP_SEARCH.replace("&has_geo=1&extras=",""))
                           .addQueryParam("method", SEARCH_PHOTO_METHOD.replace("?method=","") )
                           .addQueryParam("page", page.toString())
                           .addQueryParam("per_page", query.perPage.toString())
                           .addQueryParam("text", query.text)
                           .addQueryParam("sort", query.sort)
                           .addQueryParam("has_geo", "1")
                           .create()

                       flickrApi.fetchMapPhotosWorldWide(
                           page,
                           query.perPage,
                           query.text,
                           query.sort,
                           query.oauthToken,
                           signature.timestamp,
                           signature.nonce,
                           signature.signature
                       )
                   }
                   false -> {
                       val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                           .addQueryParam("nojsoncallback", "1")
                           .addQueryParam("format", "json")
                           .addQueryParam("extras", EXTRAS_MAP_SEARCH.replace("&has_geo=1&extras=",""))
                           .addQueryParam("method", SEARCH_PHOTO_METHOD.replace("?method=","") )
                           .addQueryParam("page", page.toString())
                           .addQueryParam("per_page", query.perPage.toString())
                           .addQueryParam("text", query.text)
                           .addQueryParam("sort", query.sort)
                           .addQueryParam("has_geo", "1")
                           .addQueryParam("lat", query.latitude)
                           .addQueryParam("lon", query.longitude)
                           .create()

                       flickrApi.fetchMapPhotosNearMe(
                           page,
                           query.perPage,
                           query.text,
                           query.sort,
                           query.latitude,
                           query.longitude,
                           query.oauthToken,
                           signature.timestamp,
                           signature.nonce,
                           signature.signature
                       )
                   }
               }
           }
       }
    }

    suspend fun fetchGroupInfo(query: Query): FlickrResponse<GroupInfo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchGroupInfo(query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", GET_GROUP_INFO.replace("?method=",""))
                    .addQueryParam("group_id", query.id)
                    .create()

                flickrApi.fetchGroupInfo(
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchPhotoInfo(query: Query): FlickrResponse<PhotoInfo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchPhotoInfo(query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS_PHOTO_INFO.replace("&extras=",""))
                        .addQueryParam("method", PHOTO_GET_INFO.replace("?method=","") )
                        .addQueryParam("photo_id", query.id)
                        .create()

                flickrApi.fetchPhotoInfo(
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchPhotoExif(query: Query): FlickrResponse<PhotoExif> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchPhotoExif(query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS_PHOTO_EXIF.replace("&extras=",""))
                        .addQueryParam("method", PHOTO_GET_EXIF.replace("?method=","") )
                        .addQueryParam("photo_id", query.id)
                        .create()

                flickrApi.fetchPhotoExif(
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchGroupSearch(page: Int, perPage: Int, query: Query)
    : FlickrResponse<Group> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchSearchGroup(page, perPage, query.text)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("method", SEARCH_GROUP.replace("?method=","") )
                        .addQueryParam("text", query.text)
                        .addQueryParam("page", page.toString())
                        .addQueryParam("per_page", perPage.toString())
                        .create()

                flickrApi.fetchSearchGroup(
                    page,
                    perPage,
                    query.text,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }
    suspend fun fetchUserGroups(query: Query)
            : FlickrResponse<Group> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchGroupList(query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("method", USER_PUBLIC_GROUPS.replace("?method=","") )
                        .addQueryParam("user_id", query.id)
                        .create()

                flickrApi.fetchGroupList(
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchAlbumsList(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Album> {
        return flickrApi.fetchAlbumList(page, perPage,
            query.id) // user_id
    }

    suspend fun fetchGalleryList(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Gallery> {
        return flickrApi.fetchGalleryList(page, perPage,
            query.id) // user_id
    }

    suspend fun fetchPersonContactList(page: Int, perPage: Int, query: Query)
            : FlickrResponse<PersonContact> {
        return flickrApi.fetchPersonContactList(page, perPage,
            query.id) // user_id
    }

    suspend fun fetchPerson(userId: String): FlickrResponse<Person> {
        return flickrApi.fetchPersonInfo(userId)
    }

    suspend fun fetchUserId(userNameOrEmail: String) : FlickrResponse<Person> {
        return when (userNameOrEmail.contains("@")) {
            true -> flickrApi.fetchPersonIdByEmail(userNameOrEmail)
            false -> flickrApi.fetchPersonIdByUserName(userNameOrEmail)
        }
    }

    suspend fun fetchUserPublicPhotos(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchUserPublicPhotos(page, perPage, query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                    .addQueryParam("method", USER_PUBLIC_PHOTOS.replace("?method=","") )
                    .addQueryParam("page", page.toString())
                    .addQueryParam("per_page", perPage.toString())
                    .addQueryParam("user_id", query.id)
                    .create()

                flickrApi.fetchUserPublicPhotos(page, perPage,
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchUserFavoritesPhotos(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchUserFavoritesPhotos(page, perPage, query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature: OAuthSignature =
                    oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                        .addQueryParam("nojsoncallback", "1")
                        .addQueryParam("format", "json")
                        .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                        .addQueryParam("method", USER_FAVORITES_PHOTOS.replace("?method=","") )
                        .addQueryParam("page", page.toString())
                        .addQueryParam("per_page", perPage.toString())
                        .addQueryParam("user_id", query.id)
                        .create()

                flickrApi.fetchUserFavoritesPhotos(page, perPage,
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchAlbumPhotos(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchAlbumPhotos(page, perPage, query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                    .addQueryParam("method", GET_ALBUM_PHOTOS.replace("?method=",""))
                    .addQueryParam("page", page.toString())
                    .addQueryParam("per_page", perPage.toString())
                    .addQueryParam("photoset_id", query.id)
                    .create()

                flickrApi.fetchAlbumPhotos(page, perPage,
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchGalleryPhotos(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchGalleryPhotos(page, perPage, query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                    .addQueryParam("method", GET_GALLERY_PHOTOS.replace("?method=",""))
                    .addQueryParam("page", page.toString())
                    .addQueryParam("per_page", perPage.toString())
                    .addQueryParam("gallery_id", query.id)
                    .create()

                flickrApi.fetchGalleryPhotos(page, perPage,
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchGroupPhotos(page: Int, perPage: Int, query: Query)
            : FlickrResponse<Photo> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchGroupPhotos(page, perPage, query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                    .addQueryParam("method", GET_GROUP_PHOTOS.replace("?method=",""))
                    .addQueryParam("page", page.toString())
                    .addQueryParam("per_page", perPage.toString())
                    .addQueryParam("group_id", query.id)
                    .create()

                flickrApi.fetchGroupPhotos(page, perPage,
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }

    suspend fun fetchPhotoComments(query: Query): FlickrResponse<PhotoComment> {
        return when (query.oauthToken.isBlank()) {
            true -> flickrApi.fetchPhotoComments(query.id)
            false -> {
                val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
                    .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
                    .build()

                val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                    .addQueryParam("nojsoncallback", "1")
                    .addQueryParam("format", "json")
                    .addQueryParam("method", GET_PHOTO_COMMENTS.replace("?method=",""))
                    .addQueryParam("photo_id", query.id)
                    .create()

                flickrApi.fetchPhotoComments(
                    query.id,
                    query.oauthToken,
                    signature.timestamp,
                    signature.nonce,
                    signature.signature
                )
            }
        }
    }


    suspend fun fetchUserContactsPhotos(page: Int, perPage: Int, query: Query) : FlickrResponse<Photo> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature: OAuthSignature =
            oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
                .addQueryParam("method", PHOTO_GET_USER_CONTACTS_PHOTOS.replace("?method=","") )
                .addQueryParam("page", page.toString())
                .addQueryParam("per_page", perPage.toString())
                .create()

        return flickrApi.fetchUserFollowingsPhotos(page, perPage,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun addPhotoComment(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
            .addQueryParam("nojsoncallback", "1")
            .addQueryParam("format", "json")
            .addQueryParam("method", ADD_COMMENTS.replace("?method=",""))
            .addQueryParam("photo_id", query.id)
            .addQueryParam("comment_text", query.text)
            .create()

        return flickrApi.addPhotoComment(
            query.id,
            query.text,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun delPhotoComment(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
            .addQueryParam("nojsoncallback", "1")
            .addQueryParam("format", "json")
            .addQueryParam("method", DEL_COMMENTS.replace("?method=",""))
            .addQueryParam("comment_id", query.id)
            .create()

        return flickrApi.delPhotoComment(
            query.id,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }
    suspend fun addGroupPhoto(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature: OAuthSignature =
            oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("method", ADD_GROUP_PHOTO.replace("?method=",""))
                .addQueryParam("photo_id", query.id)
                .addQueryParam("group_id", query.text)
                .create()

        return flickrApi.addGroupPhoto(
            query.id,   //photo_id
            query.text, //group_id
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun addAlbumPhoto(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature: OAuthSignature =
            oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("method", ADD_ALBUM_PHOTO.replace("?method=",""))
                .addQueryParam("photo_id", query.id)
                .addQueryParam("photoset_id", query.text)
                .create()

        return flickrApi.addAlbumPhoto(
            query.id,   //photo_id
            query.text, //album_id
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun addFavoritePhoto(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature: OAuthSignature =
            oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("method", ADD_FAVORITES.replace("?method=",""))
                .addQueryParam("photo_id", query.id)
                .create()

        return flickrApi.addFavoritePhoto(
            query.id,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun removeFavoritePhoto(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature = oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
            .addQueryParam("nojsoncallback", "1")
            .addQueryParam("format", "json")
            .addQueryParam("method", REMOVE_FAVORITES.replace("?method=",""))
            .addQueryParam("photo_id", query.id)
            .create()

        return flickrApi.removeFavoritePhoto(
            query.id,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun setPhotoPrivacy(query: Query) : FlickrResponse<String> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        var isPublic = 0
        var isFriend = 0
        var isFamily = 0

        when (query.photoPrivacy) {
            PUBLIC -> isPublic = 1
            FRIENDS_AND_FAMILY -> { isFriend = 1; isFamily = 1 }
            FRIENDS -> isFriend = 1
            FAMILY -> isFamily = 1
            else -> {}
        }

        val signature: OAuthSignature =
            oauthConfig.buildSignature(HttpMethod.POST, FLICKR_URL)
                .addQueryParam("nojsoncallback", "1")
                .addQueryParam("format", "json")
                .addQueryParam("method", SET_PHOTO_PRIVACY.replace("?method=",""))
                .addQueryParam("photo_id", query.id)
                .addQueryParam("is_public", isPublic.toString())
                .addQueryParam("is_friend", isFriend.toString())
                .addQueryParam("is_family", isFamily.toString())
                .create()

        return flickrApi.setPhotoPrivacy(
            query.id,
            isPublic.toString(),
            isFriend.toString(),
            isFamily.toString(),
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun fetchCameraRoll(page: Int, perPage: Int, query: Query) : FlickrResponse<Photo> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature: OAuthSignature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
            .addQueryParam("nojsoncallback", "1")
            .addQueryParam("format", "json")
            .addQueryParam("extras", EXTRAS_PHOTO_ITEM.replace("&extras=",""))
            .addQueryParam("method", PHOTO_GET_USER_CAMERA_ROLL.replace("?method=","") )
            .addQueryParam("page", page.toString())
            .addQueryParam("per_page", perPage.toString())
            .addQueryParam("user_id", query.id)
            .create()

        return flickrApi.fetchCameraRoll(
            query.id,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }

    suspend fun fetchGroupsUserCanAddPhoto(page: Int, perPage: Int, query: Query) : FlickrResponse<Group> {
        val oauthConfig: OAuthConfig = OAuthConfigBuilder(KEY, SECRET)
            .setTokenKeys(query.oauthToken, query.oauthTokenSecret)
            .build()

        val signature = oauthConfig.buildSignature(HttpMethod.GET, FLICKR_URL)
            .addQueryParam("nojsoncallback", "1")
            .addQueryParam("format", "json")
            .addQueryParam("method", GET_USER_GROUPS_CAN_ADD_PHOTO.replace("?method=","") )
            .addQueryParam("page", page.toString())
            .addQueryParam("per_page", perPage.toString())
            .create()

        return flickrApi.fetchGroupsUserCanAddPhoto(
            page,
            perPage,
            query.oauthToken,
            signature.timestamp,
            signature.nonce,
            signature.signature
        )
    }
}


