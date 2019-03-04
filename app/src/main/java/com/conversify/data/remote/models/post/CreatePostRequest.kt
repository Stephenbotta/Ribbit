package com.conversify.data.remote.models.post

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CreatePostRequest(val groupId: String? = null,
                             var postText: String? = null,
                             var imageOriginal: String? = null,
                             var imageThumbnail: String? = null,
                             var hashTags: List<String>? = null,
                             var locationLong: Double? = null,
                             var locationLat: Double? = null,
                             var meetingTime: Long? = null,
                             var expirationTime: Long? = null,
                             var locationName: String? = null,
                             var locationAddress: String? = null,
                             var postType: String? = null,
                             var postingIn: String? = null,
                             var selectedPeople: List<String>? = null,
                             var selectInterests: List<String>? = null) : Parcelable