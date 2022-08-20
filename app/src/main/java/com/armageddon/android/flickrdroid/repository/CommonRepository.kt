package com.armageddon.android.flickrdroid.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.armageddon.android.flickrdroid.R
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.model.*
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.api.PAGE_SIZE
import com.armageddon.android.flickrdroid.network.execptions.*
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import java.io.InterruptedIOException
import java.net.UnknownHostException

class CommonRepository (
    val backend: FlickrFetchr
) {

    inner class GroupPagingSource (
        private val query: Query
    ) : PagingSource<Int, Group>() {

        override fun getRefreshKey(state: PagingState<Int, Group>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Group> {
            try {
                val nextPage = params.key ?: 1
                val response = when (query.type) {
                    QueryTypes.GROUP -> backend.fetchGroupSearch(nextPage, PAGE_SIZE, query)
                    QueryTypes.USER_GROUP -> backend.fetchUserGroups(query)
                    QueryTypes.USER_GROUP_CAN_ADD_PHOTOS -> backend.fetchGroupsUserCanAddPhoto(nextPage, 400, query)
                    else -> backend.fetchGroupSearch(nextPage, PAGE_SIZE, query )
                }

                val data = response.dataArray
                val currentPage = response.page
                val totalPages = response.pages

                if (data.isEmpty()) {
                    return when (query.type) {
                        QueryTypes.GROUP -> LoadResult.Error(GroupNotFoundException())
                        else -> LoadResult.Error(NoGroupException())
                    }
                }

                return LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (currentPage == totalPages) null else nextPage + 1
                )
            }
            catch (e: UnknownHostException) {
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


    inner class AlbumsPagingSource (
        private val query: Query

    ) : PagingSource<Int, Album>() {

        override fun getRefreshKey(state: PagingState<Int, Album>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Album> {
            try {
                val nextPage = params.key ?: 1
                val response = backend.fetchAlbumsList(nextPage, PAGE_SIZE, query)
                val data = response.dataArray
                val currentPage = response.page
                val totalPages = response.pages

                if (data.isEmpty()) {
                    return LoadResult.Error(NoAlbumsException())
                }


                return LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (currentPage == totalPages) null else nextPage + 1
                )
            }
            catch (e: UnknownHostException) {
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


    inner class GalleryPagingSource (
        private val query: Query
    ) : PagingSource<Int, Gallery>() {

        override fun getRefreshKey(state: PagingState<Int, Gallery>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Gallery> {
            try {
                val nextPage = params.key ?: 1
                val response = backend.fetchGalleryList(nextPage, PAGE_SIZE, query )
                val data = response.dataArray
                val currentPage = response.page
                val totalPages = response.pages

                if (data.isEmpty()) {
                    return LoadResult.Error(NoGalleriesException())
                }

                return LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (currentPage == totalPages) null else nextPage + 1
                )
            }

            catch (e: UnknownHostException) {
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

    inner class PersonContactsPagingSource (
        private val query: Query
    ) : PagingSource<Int, PersonContact>() {

        override fun getRefreshKey(state: PagingState<Int, PersonContact>): Int? {
            return state.anchorPosition?.let { anchorPosition ->
                val anchorPage = state.closestPageToPosition(anchorPosition)
                anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
            }
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PersonContact> {
            try {
                val nextPage = params.key ?: 1
                val response = backend.fetchPersonContactList(nextPage, PAGE_SIZE, query )
                val data = response.dataArray
                val currentPage = response.page
                val totalPages = response.pages

                return LoadResult.Page(
                    data = data,
                    prevKey = null,
                    nextKey = if (currentPage == totalPages) null else nextPage + 1
                )
            }
            catch (e: UnknownHostException) {
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

    suspend fun getPerson(userId: String)
            : FlickrResponse<Person> {
        return backend.fetchPerson(userId)
    }

    suspend fun fetchUserId(userNameOrEmail: String)
            : FlickrResponse<Person> {
        return backend.fetchUserId(userNameOrEmail)
    }

    suspend fun fetchGroupInfo(query: Query)
            : FlickrResponse<GroupInfo> {
        return backend.fetchGroupInfo(query)
    }




}