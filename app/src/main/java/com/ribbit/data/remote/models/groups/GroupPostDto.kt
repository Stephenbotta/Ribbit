package com.ribbit.data.remote.models.groups

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.post.PostReplyDto
import kotlinx.android.parcel.Parcelize
import org.threeten.bp.ZonedDateTime

@Parcelize
data class GroupPostDto(
        @field:SerializedName("postText")
        val postText: String? = null,

        @field:SerializedName("imageUrl")
        val imageUrl: ImageUrlDto? = null,

        @field:SerializedName("postBy")
        val user: ProfileDto? = null,

        @field:SerializedName("likeCount")
        var likesCount: Int? = null,

        @field:SerializedName("_id")
        val id: String? = null,

        @field:SerializedName("createdOn")
        val createdOnDateTime: ZonedDateTime? = null,

        @field:SerializedName("liked")
        var isLiked: Boolean? = null,

        @field:SerializedName("type")
        val type: String? = null,

        @field:SerializedName("groupId")
        val group: GroupDto? = null,

        @field:SerializedName("postCategoryId")
        val category: InterestDto? = null,

        @field:SerializedName("comment")
        val replies: List<PostReplyDto>? = null,

        @field:SerializedName("locationName")
        val locationName: String? = null,

        @field:SerializedName("locationAddress")
        val locationAddress: String? = null,

        @field:SerializedName("postType")
        val postType: String? = null,

        @field:SerializedName("postingIn")
        val postingIn: String? = null,

        @field:SerializedName("commentCount")
        var repliesCount: Int? = null,

        @field:SerializedName("selectedPeople")
        var selectedPeople: ArrayList<String> = ArrayList(),

        @field:SerializedName("media")
        var media: ArrayList<ImageUrlDto> = ArrayList(),

        @field:SerializedName("selectInterests")
        val interests: ArrayList<InterestDto>? = null


) : Parcelable