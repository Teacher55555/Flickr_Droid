package com.armageddon.android.flickrdroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.model.GroupInfo
import com.armageddon.android.flickrdroid.model.Person
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import com.armageddon.android.flickrdroid.network.api.PAGE_SIZE
import com.armageddon.android.flickrdroid.network.api.PREFETCH_DISTANCE
import com.armageddon.android.flickrdroid.network.responses.FlickrResponse
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_DATA_FAIL
import com.armageddon.android.flickrdroid.network.responses.RESPONSE_NO_INTERNET_CONNECTION
import com.armageddon.android.flickrdroid.repository.CommonRepository
import java.io.InterruptedIOException
import java.net.UnknownHostException

class CommonViewModel : ViewModel() {
    private val flickrFetchr = FlickrFetchr()
    private val commonRepository = CommonRepository(flickrFetchr)
    private val pagingConfig = PagingConfig(
        PAGE_SIZE,
        PREFETCH_DISTANCE,
        initialLoadSize = 1
    )

    fun getAlbumsFlow(query: Query) =
        Pager(pagingConfig) {
            commonRepository.AlbumsPagingSource(query)
        }.flow
            .cachedIn(viewModelScope)

    fun getGalleryFlow(query: Query) =
        Pager(pagingConfig) {
            commonRepository.GalleryPagingSource(query)
         }.flow
            .cachedIn(viewModelScope)

    fun getPersonContactsFlow (query: Query) =
        Pager(pagingConfig) {
            commonRepository.PersonContactsPagingSource(query)
        }.flow
            .cachedIn(viewModelScope)

    suspend fun getPerson (userId: String) : FlickrResponse<Person> {
        val response : FlickrResponse<Person> = try {
            commonRepository.getPerson(userId)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response

    }


    suspend fun fetchUserId(userNameOrEmail: String) : FlickrResponse<Person> {

        var userIdResponse : FlickrResponse<Person>
        try {
            userIdResponse = commonRepository.fetchUserId(userNameOrEmail)
        } catch (e : UnknownHostException) {
            userIdResponse = FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
            return userIdResponse
        } catch (e : InterruptedIOException) {
            userIdResponse = FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
            return userIdResponse
        } catch (e: Exception) {
            userIdResponse = FlickrResponse(stat = RESPONSE_DATA_FAIL)
            return userIdResponse
        }

        val userId = userIdResponse.data?.id ?: return FlickrResponse(stat = RESPONSE_DATA_FAIL)

        var personResponse : FlickrResponse<Person>
        try {
            personResponse = commonRepository.getPerson(userId)
        } catch (e : UnknownHostException) {
            personResponse = FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
            return personResponse
        } catch (e : InterruptedIOException) {
            personResponse = FlickrResponse(stat = RESPONSE_NO_INTERNET_CONNECTION)
            return personResponse
        } catch (e: Exception) {
            personResponse = FlickrResponse(stat = RESPONSE_DATA_FAIL)
            return personResponse
        }

        return personResponse
    }

    fun getGroupFlow(query: Query) = when(query.type) {
        QueryTypes.GROUP -> {
            Pager(pagingConfig) {
                commonRepository.GroupPagingSource(query)
            }.flow
                .cachedIn(viewModelScope)
        }
        QueryTypes.USER_GROUP -> {
            Pager(pagingConfig) {
                commonRepository.GroupPagingSource(query)
            }.flow
                .cachedIn(viewModelScope)
        }
        QueryTypes.USER_GROUP_CAN_ADD_PHOTOS -> {
            Pager(pagingConfig) {
                commonRepository.GroupPagingSource(query)
            }.flow
                .cachedIn(viewModelScope)
        }
        else -> {
            Pager(pagingConfig) {
                commonRepository.GroupPagingSource(query)
            }.flow
                .cachedIn(viewModelScope)
        }
    }

//    suspend fun getGroupInfo(query: Query) : FlickrResponse<GroupInfo> {
//        val response : FlickrResponse<GroupInfo> = try {
//            commonRepository.fetchGroupInfo(query)
//        } catch (e : UnknownHostException) {
//            FlickrResponse(stat = RESPONSE_DATA_FAIL)
//        } catch (e : InterruptedIOException) {
//            FlickrResponse(stat = RESPONSE_DATA_FAIL)
//        } catch (e: Exception) {
//            FlickrResponse(stat = RESPONSE_DATA_FAIL)
//        }
//        return response
//    }

    suspend fun getGroupInfo(query: Query) : FlickrResponse<GroupInfo> {
        val response : FlickrResponse<GroupInfo> = try {
            commonRepository.fetchGroupInfo(query)
        } catch (e : UnknownHostException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e : InterruptedIOException) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        } catch (e: Exception) {
            FlickrResponse(stat = RESPONSE_DATA_FAIL)
        }
        return response
    }

}