package com.ribbit.data.remote.models.survey

import android.os.Parcelable
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import kotlinx.android.parcel.Parcelize

class GetSurveyList {
    val info: List<SurveyList>? = null
}

class GetSurveyInfo {
    val info: List<SurveyInfo>? = null
}

@Parcelize
data class SurveyList(
        var name: String? = null,
        var totalTime: Int? = null,
        var _id: String? = null,
        var questionCount: Int? = null,
        var media: List<ImageUrlDto>? = null
) : Parcelable

data class SurveyInfo(
        var name: String? = null,
        var totalTime: Int? = null,
        var surveyId: String? = null,
        val options: List<Options>? = null,
        var questionCount: Int? = null,
        var media: ImageUrlDto? = null
)

@Parcelize
data class Options(
        var name: String? = null,
        var _id: String? = null,
        var isSelected: Boolean = false
) : Parcelable