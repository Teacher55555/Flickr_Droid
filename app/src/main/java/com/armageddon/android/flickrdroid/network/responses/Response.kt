package com.armageddon.android.flickrdroid.network.responses

abstract class Response <T> (
    var page: Int = 0,
    var pages: Int = 0,
    var perpage: Int = 0,
    var total: Int = 0,
    var dataArray : List<T> = emptyList() ) {
}