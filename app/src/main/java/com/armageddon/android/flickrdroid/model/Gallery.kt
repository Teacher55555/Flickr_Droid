package com.armageddon.android.flickrdroid.model

import java.io.Serializable

data class Gallery (
    val id: String,
    val dateCreate: String?,
    val photosCount: String,
    val viewsCount: String,
    val commentsCount: String,
    val title: String,
    val description: String,
    val owner: String,
    val username: String,
    val coverUrlList: List<String>?
) : Serializable, PersonPhotoFolder {

    override fun id() = id
    override fun title() = title
    override fun coverUrl() = coverUrlList?.get(0)
    override fun countPhotos() = photosCount
    override fun countViews() = viewsCount
    override fun createDate() = dateCreate
    override fun description() = description
    override fun idOwner() = owner
    override fun ownerUserName() = username
}