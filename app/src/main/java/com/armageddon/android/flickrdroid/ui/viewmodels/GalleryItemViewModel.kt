package com.armageddon.android.flickrdroid.ui.viewmodels

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.model.*
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.api.PAGE_SIZE
import com.armageddon.android.flickrdroid.network.api.PREFETCH_DISTANCE
import com.armageddon.android.flickrdroid.network.responses.*
import com.armageddon.android.flickrdroid.repository.PhotoItemRepository
import com.armageddon.android.flickrdroid.repository.PhotoMapRepository
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import java.io.InterruptedIOException
import java.net.UnknownHostException
import java.util.*

class GalleryItemViewModel : ViewModel() {
    private val flickrFetchr = FlickrFetchr()
    private val galleryRepository = PhotoItemRepository(flickrFetchr)
    private val mapPhotoRepository = PhotoMapRepository(flickrFetchr)
    var calendar = GregorianCalendar().apply { add(Calendar.DATE, -1) }

    var mapMarkers: MutableMap<Photo, Bitmap>? = null
    var mapCameraPosition: CameraPosition? = null
    var mapPhotoDetailList = emptyList<Photo>()
    lateinit var bounds: LatLngBounds

    var selectedItems : MutableMap<Int,String> = mutableMapOf()
    var filter = false
    private val pagingConfig = PagingConfig(
        PAGE_SIZE,
        PREFETCH_DISTANCE,
        initialLoadSize = 1
    )

    var markerPosition: Int = 0
    var isCommentsQuantityChanged = false

    lateinit var interestingQuery: Query
    lateinit var searchQuery: Query
    lateinit var categoryQuery: Query
    lateinit var mapQuery: Query
    lateinit var publicPhotoQuery: Query
    lateinit var favoritesQuery: Query
    lateinit var albumPhotosQuery: Query
    lateinit var galleyPhotosQuery: Query
    lateinit var groupPhotosQuery: Query
    lateinit var followingsPhotoQuery: Query
    lateinit var cameraRollQuery: Query


    //Работает!!!!!
    private val interestingFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(interestingQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val cameraRollFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(cameraRollQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val searchFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(searchQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val followingsFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(followingsPhotoQuery)
    }.flow
        .cachedIn(viewModelScope)
    private val categoryFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(categoryQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val mapFlow = Pager(pagingConfig) {
        mapPhotoRepository.PhotoMapPagingSource(mapPhotoDetailList)
    }.flow

    private val publicPhotoFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(publicPhotoQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val favoritesFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(favoritesQuery)
    }.flow
        .cachedIn(viewModelScope)
//
    private val albumPhotosFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(albumPhotosQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val galleryPhotosFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(galleyPhotosQuery)
    }.flow
        .cachedIn(viewModelScope)

    private val groupPhotosFlow = Pager(pagingConfig) {
        galleryRepository.GalleryItemPagingSource(groupPhotosQuery)
    }.flow
        .cachedIn(viewModelScope)

    fun getFlow(query: Query) = when(query.type) {
        QueryTypes.CAMERA_ROLL -> {
            cameraRollQuery = query
            cameraRollFlow
        }
        QueryTypes.USER_FOLLOWINGS_PHOTOS -> {
            followingsPhotoQuery = query
            followingsFlow
        }
        QueryTypes.INTERESTING -> {
            interestingQuery = query
            interestingFlow
        }
        QueryTypes.SEARCH -> {
           searchQuery = query
           searchFlow
        }
        QueryTypes.CATEGORY -> {
            categoryQuery = query
            categoryFlow
        }
        QueryTypes.MAP ->  {
            mapQuery = query
            mapFlow
        }
        QueryTypes.PUBLIC_PHOTOS ->  {
            publicPhotoQuery = query
            publicPhotoFlow
        }
        QueryTypes.FAVORITES_PHOTOS -> {
            favoritesQuery = query
            favoritesFlow
        }
        QueryTypes.ALBUM_PHOTOS -> {
            albumPhotosQuery = query
            albumPhotosFlow
        }
        QueryTypes.GALLERY_PHOTOS -> {
            galleyPhotosQuery = query
            galleryPhotosFlow
        }
        QueryTypes.GROUP_PHOTOS -> {
            groupPhotosQuery = query
            groupPhotosFlow
        }
        else -> {
            interestingQuery = query
            interestingFlow
        }
    }

    suspend fun getPhotoInfo (query: Query) : FlickrResponse<PhotoInfo> {
        val response : FlickrResponse<PhotoInfo> = try {
            galleryRepository.getPhotoInfoResponse(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun getPhotoExif (query: Query) : FlickrResponse<PhotoExif> {
        val response : FlickrResponse<PhotoExif> = try {
            galleryRepository.getPhotoExifResponse(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }  catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun getPhotosForMap (query: Query) : FlickrResponse<Photo> {
        val response : FlickrResponse<Photo> = try {
            galleryRepository.fetchMapPhotos(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_TIMEOUT)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun getPhotoComments(query: Query): FlickrResponse<PhotoComment> {
        val response : FlickrResponse<PhotoComment> = try {
            galleryRepository.fetchPhotoComments(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun addFavoritePhoto(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.addFavoritePhoto(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun removeFavoritePhoto(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.removeFavoritePhoto(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun setPhotoPrivacy(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.setPhotoPrivacy(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun addPhotoComment(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.addPhotoComment(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

    suspend fun delPhotoComment(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.delPhotoComment(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }

        return response
    }

    suspend fun addAlbumPhoto(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.addAlbumPhoto(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_TIMEOUT)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
        }
        return response
    }

    suspend fun addGroupPhoto(query: Query): FlickrResponse<String> {
        val response : FlickrResponse<String> = try {
            galleryRepository.addGroupPhoto(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_TIMEOUT)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
        }
        return response
    }

}