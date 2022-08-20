package com.armageddon.android.flickrdroid.model

import java.io.Serializable

interface PersonPhotoFolder : Serializable {
    fun id (): String
    fun title () : String
    fun coverUrl (): String?
    fun countPhotos (): String
    fun countViews (): String = ""
    fun countMembers (): String = ""
    fun createDate (): String? = ""
    fun description (): String = ""
    fun idOwner (): String = ""
    fun ownerUserName (): String = ""
}