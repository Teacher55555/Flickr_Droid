package com.armageddon.android.flickrdroid.model

import java.io.Serializable

data class Album (
    val id: String,
    val owner: String,
    val username: String,
    val viewsCount: String,
    val photosCount: String,
    val title: String,
    val description: String,
    val dateCreate: String?,
    val coverUrl: String,
) : Serializable, PersonPhotoFolder {

    override fun id() = id
    override fun title() = title
    override fun coverUrl() = coverUrl
    override fun countPhotos() = photosCount
    override fun countViews() = viewsCount
    override fun createDate() = dateCreate
    override fun description() = description
    override fun idOwner() = owner
    override fun ownerUserName() = username

}