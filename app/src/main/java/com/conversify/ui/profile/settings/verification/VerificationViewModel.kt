package com.conversify.ui.profile.settings.verification

import android.app.Application
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Uploader
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.profile.CreateEditProfileRequest
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * Created by Manish Bhargav
 */
class VerificationViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    val verificationApi by lazy { SingleLiveEvent<Resource<ApiResponse<ProfileDto>>>() }


    fun editProfile(request: CreateEditProfileRequest, postImage: File?) {
        if (postImage == null) {
            postSettingsVerification(request)
        } else {
            verificationApi.value = Resource.loading()
            s3Uploader.upload(postImage)
                    .addUploadCompleteListener { imageUrl ->
                        request.imageOriginal = imageUrl
                        request.imageThumbnail = imageUrl
                        postSettingsVerification(request)
                    }
                    .addUploadFailedListener {
                        verificationApi.value = Resource.error(AppError.WaitingForNetwork)
                    }
        }
    }

    private fun postSettingsVerification(request: CreateEditProfileRequest) {
        verificationApi.value = Resource.loading()

        RetrofitClient.conversifyApi
                .editProfile(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            verificationApi.value = Resource.success()
                        } else {
                            verificationApi.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        verificationApi.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun getProfile() = profile

    override fun onCleared() {
        super.onCleared()
        s3Uploader.clear()
    }

}