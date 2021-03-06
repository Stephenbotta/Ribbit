package com.ribbit.data.remote.models.people

import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto

/**
 * Created by Manish Bhargav
 */
data class UserDetails(

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("fullName")
        val fullName: String? = null,

        @field:SerializedName("userName")
        val userName: String? = null,

        @field:SerializedName("bio")
        val bio: String? = null,

        @field:SerializedName("age")
        val age: Int? = null,

        @field:SerializedName("company")
        val company: String? = null,

        @field:SerializedName("designation")
        val designation: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null)