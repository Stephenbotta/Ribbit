package com.conversify.data.remote.models.venues

import com.google.gson.annotations.SerializedName

data class CreateEditVenueRequest(

        @field:SerializedName("participantIds")
        var participantIds: List<String>?=null,

        @field:SerializedName("venueGroupId")
        var id: String? = null,

        @field:SerializedName("venueTitle")
        var title: String? = null,

        @field:SerializedName("venueLocationLat")
        var latitude: Double? = null,

        @field:SerializedName("venueLocationLong")
        var longitude: Double? = null,

        @field:SerializedName("venueLocationName")
        var locationName: String? = null,

        @field:SerializedName("venueLocationAddress")
        var locationAddress: String? = null,

        @field:SerializedName("venueTags")
        var tags: List<String>? = null,

        @field:SerializedName("categoryId")
        var categoryId: String? = null,

        @field:SerializedName("isPrivate")
        var isPrivate: Int? = null,

        @field:SerializedName("groupImageOriginal")
        var imageOriginalUrl: String? = null,

        @field:SerializedName("groupImageThumbnail")
        var imageThumbnailUrl: String? = null,

        @field:SerializedName("venueTime")
        var dateTimeMillis: Long? = null,

        @field:SerializedName("venueAdditionalDetailsName")
        var ownerName: String? = null,

        @field:SerializedName("venueAdditionalDetailsDocs")
        var ownerVerificationDocumentUrl: String? = null)