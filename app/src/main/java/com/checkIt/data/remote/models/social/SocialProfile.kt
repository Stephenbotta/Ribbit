package com.checkIt.data.remote.models.social

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SocialProfile(val source: Int,
                         val socialId: String,
                         val fullName: String,
                         val firstName: String,
                         val lastName: String,
                         val profileImage: String,
                         val email: String) : Parcelable