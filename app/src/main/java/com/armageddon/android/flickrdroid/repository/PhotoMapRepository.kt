package com.armageddon.android.flickrdroid.repository

import android.graphics.Bitmap
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.model.Photo
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.execptions.ConnectionException
import com.armageddon.android.flickrdroid.network.execptions.TimeOutException
import com.armageddon.android.flickrdroid.network.execptions.UnclassifiedException
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import java.io.InterruptedIOException
import java.net.UnknownHostException


class PhotoMapRepository(private val backend : FlickrFetchr) {

    var stopSignal = false
    var bitmapsConvertProgress = 0
    val mBitmapsMap = mutableMapOf<Photo, Bitmap>()
    var isWorkDone = false


    suspend fun fetchMapPhotos(
        query: Query
    ) : FlickrResponse<Photo> {
        return backend.fetchMapPhotos(query = query)
    }


    inner class PhotoMapPagingSource (
        private val photoList: List<Photo>,
    ) : PagingSource<Int, Photo>() {

        override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
            return try {
                val data = photoList
                if (data.isEmpty()) {
                    throw IllegalArgumentException()
                }
                LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = null
                )
            } catch (e: Exception) {
                e.printStackTrace()
                LoadResult.Error(e)
            }
            catch (e: UnknownHostException) {
                e.printStackTrace()
                return LoadResult.Error(ConnectionException())
            }
            catch (e: InterruptedIOException) {
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