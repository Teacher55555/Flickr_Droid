package com.armageddon.android.flickrdroid.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes.*
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.model.PhotoComment
import com.armageddon.android.flickrdroid.model.PhotoExif
import com.armageddon.android.flickrdroid.model.PhotoInfo
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.api.PAGE_SIZE
import com.armageddon.android.flickrdroid.network.execptions.*
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import java.io.InterruptedIOException
import java.lang.IllegalArgumentException
import java.net.UnknownHostException


class PhotoItemRepository (val backend: FlickrFetchr) {

    suspend fun addGroupPhoto(query: Query)
            : FlickrResponse<String> {
        return backend.addGroupPhoto(query)
    }

    suspend fun addAlbumPhoto(query: Query)
            : FlickrResponse<String> {
        return backend.addAlbumPhoto(query)
    }

    suspend fun addPhotoComment(query: Query)
            : FlickrResponse<String> {
        return backend.addPhotoComment(query)
    }

    suspend fun delPhotoComment(query: Query)
            : FlickrResponse<String> {
        return backend.delPhotoComment(query)
    }

    suspend fun setPhotoPrivacy(query: Query)
    : FlickrResponse<String> {
        return backend.setPhotoPrivacy(query)
    }

    suspend fun addFavoritePhoto(
        query: Query)
    : FlickrResponse<String> {
        return backend.addFavoritePhoto(query)
    }

    suspend fun removeFavoritePhoto(
        query: Query)
            : FlickrResponse<String> {
        return backend.removeFavoritePhoto(query)
    }

    suspend fun getPhotoInfoResponse(
        query: Query)
    : FlickrResponse<PhotoInfo> {
        return backend.fetchPhotoInfo(query)
    }

    suspend fun getPhotoExifResponse(
        query: Query)
    : FlickrResponse<PhotoExif> {
        return backend.fetchPhotoExif(query)
    }

   suspend fun fetchMapPhotos(
        query: Query)
            : FlickrResponse<Photo> {
        return backend.fetchMapPhotos(query = query)
    }

    suspend fun fetchPhotoComments(query: Query)
            : FlickrResponse<PhotoComment> {
        return backend.fetchPhotoComments(query)
    }

    inner class GalleryItemPagingSource (
        private val query: Query
    ) : PagingSource<Int, Photo> () {

        override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
            try {
                val nextPage = params.key ?: 1

                val response = when (query.type) {
                    CAMERA_ROLL -> backend.fetchCameraRoll(nextPage, PAGE_SIZE, query)
                    USER_FOLLOWINGS_PHOTOS -> backend.fetchUserContactsPhotos(nextPage, PAGE_SIZE, query)
                    INTERESTING -> backend.fetchInterestingPhotos(nextPage, PAGE_SIZE, query) // date
                    SEARCH, RECENT -> backend.fetchPhotoSearch(nextPage, PAGE_SIZE, query)
                    PUBLIC_PHOTOS -> backend.fetchUserPublicPhotos(nextPage, PAGE_SIZE, query)
                    CATEGORY -> backend.fetchPhotoSearch(nextPage, PAGE_SIZE,query)
                    FAVORITES_PHOTOS -> backend.fetchUserFavoritesPhotos(nextPage, PAGE_SIZE, query)
                    GROUP_PHOTOS ->  backend.fetchGroupPhotos(nextPage, PAGE_SIZE, query)
                    GALLERY_PHOTOS ->  backend.fetchGalleryPhotos(nextPage, PAGE_SIZE, query)
                    ALBUM_PHOTOS ->  backend.fetchAlbumPhotos(nextPage, PAGE_SIZE, query)
                    else -> backend.fetchInterestingPhotos(nextPage, PAGE_SIZE, query) // date
                }

                val data = response.dataArray
                val currentPage = response.page
                val totalPages = response.pages

                if (data.isEmpty()) {
                    return when (query.type) {
                        FAVORITES_PHOTOS -> LoadResult.Error(NoFavoritesException())
                        PUBLIC_PHOTOS -> LoadResult.Error(NoPublicPhotosException())
                        INTERESTING -> LoadResult.Error(NoInterestingPhotosException())
                        SEARCH -> LoadResult.Error(SearchException())
                        else -> LoadResult.Error(NoPhotosException())
                    }
                }

                return LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (currentPage == totalPages) null else nextPage + 1
                )
            }
            catch (e: NullPointerException) {
                e.printStackTrace()
                return LoadResult.Error(e)
            }

            catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return LoadResult.Error(e)
            } catch (e: UnknownHostException) {
                e.printStackTrace()
                return LoadResult.Error(ConnectionException())
            } catch (e : InterruptedIOException) {
                e.printStackTrace()
                return LoadResult.Error(TimeOutException())
            }
            catch (e: Exception) {
                e.printStackTrace()
                return LoadResult.Error(UnclassifiedException(R.string.internet_connection_error))
            }
        }
    }
}