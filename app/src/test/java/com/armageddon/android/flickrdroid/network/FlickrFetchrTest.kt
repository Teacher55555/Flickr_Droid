@file:OptIn(ExperimentalCoroutinesApi::class)

package com.armageddon.android.flickrdroid.network

import com.armageddon.android.flickrdroid.common.Query
import com.armageddon.android.flickrdroid.common.QueryTypes
import com.armageddon.android.flickrdroid.network.api.FlickrFetchr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

private const val PAGE_FLOW = 1
private const val PER_PAGE_FLOW = 100
private const val USER_NAME = "heikof704"
private const val USER_ID = "196327491@N04"
private const val USER_EMAIL = "heikof704@gmail.com"
private const val GROUP_ID = "3191023@N20"
private const val GROUP_NAME = "2932332@N23"
private const val GROUP_DATE_CREATED = "27 November 2018"
private const val GALLERY_ID = "196327491@N04"
private const val INTERESTING_PHOTO_DATE = "2010-10-10"
private const val PHOTO_SEARCH_TEXT = "sun"
private const val PHOTO_SEARCH_TAGS = "love animals"
private const val LONGITUDE = "13.413244"
private const val LATITUDE = "52.521992"
private const val MAP_SEARCH_FIRST_TITLE_RESULT = "U-Bahn station Schweitzer Platz"
private const val MAP_SEARCH_TEXT = "platz"

class FlickrFetchrTest {

    private val flickrFetchr = FlickrFetchr()

    @Test
    fun checkFetchInterestingPhotos() = runTest  {
        val query = Query(
            type = QueryTypes.INTERESTING,
            date = INTERESTING_PHOTO_DATE
        )
        val response = flickrFetchr.fetchInterestingPhotos(PAGE_FLOW, PER_PAGE_FLOW, query)
        val dataSize = response.dataArray.size
        val data = response.dataArray
        assertEquals("Holovácz 1", data[0].title)
        assertEquals(78, dataSize)
    }

    @Test
    fun checkFetchPhotoSearch() = runTest  {
        val query = Query(
            type = QueryTypes.SEARCH,
            text = PHOTO_SEARCH_TEXT,
            sort = QueryTypes.Sort.RELEVANCE.sort
        )
        val response = flickrFetchr.fetchPhotoSearch(PAGE_FLOW, PER_PAGE_FLOW, query)
        val dataSize = response.dataArray.size
        val data = response.dataArray
        assertEquals(PHOTO_SEARCH_TEXT, data[0].title)
        assertEquals(100, dataSize)
    }

    @Test
    fun checkFetchMapPhotos() = runTest  {
        val query = Query(
            type = QueryTypes.MAP,
            text = MAP_SEARCH_TEXT,
            latitude = LATITUDE,
            longitude = LONGITUDE,
            sort = QueryTypes.Sort.RELEVANCE.sort
        )
        val response = flickrFetchr.fetchPhotoSearch(PAGE_FLOW, PER_PAGE_FLOW, query)
        val dataSize = response.dataArray.size
        val data = response.dataArray
        assertEquals(MAP_SEARCH_TEXT, data[0].title)
        assertEquals(100, dataSize)
    }

    @Test
    fun checkPersonResponse() = runTest {
        val response = flickrFetchr.fetchPerson(USER_ID)
        val person = response.data!!
        assertEquals(USER_NAME, person.userName)
    }


    @Test
    fun checkGetUserIdByEmailResponse() = runTest {
        val response = flickrFetchr.fetchUserId(USER_EMAIL)
        val person = response.data!!
        assertEquals(USER_ID, person.id)
    }

    @Test
    fun checkGetUserIdByUserNameResponse() = runTest {
        val response = flickrFetchr.fetchUserId(USER_NAME)
        val person = response.data!!
        assertEquals(USER_ID, person.id)
    }

    @Test
    fun checkGroupInfoResponse() = runTest {
        val query = Query(
            id = GROUP_ID
        )
        val response = flickrFetchr.fetchGroupInfo(query)
        val dateCreated = response.data!!.dateCreate
        assertEquals(GROUP_DATE_CREATED, dateCreated)
    }

    @Test
    fun checkUserGallerySize() = runTest  {
        val query = Query(
            type = QueryTypes.GALLERY,
            id = USER_ID
        )
        val response = flickrFetchr.fetchGalleryList(PAGE_FLOW, PER_PAGE_FLOW, query)
        val dataSize = response.dataArray.size
        assertEquals(1, dataSize)
    }

}
