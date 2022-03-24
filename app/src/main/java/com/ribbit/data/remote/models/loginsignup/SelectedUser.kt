package com.ribbit.data.remote.models.loginsignup

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Manish Bhargav
 */
@Parcelize
data class SelectedUser(
        @field:SerializedName("_id")
        var id: String? = null,

        @field:SerializedName("imageUrl")
        var image: ImageUrlDto? = null,

        @field:SerializedName("fullName")
        var fullName: String? = null,

        @field:SerializedName("userName")
        var userName: String? = null) : Parcelable