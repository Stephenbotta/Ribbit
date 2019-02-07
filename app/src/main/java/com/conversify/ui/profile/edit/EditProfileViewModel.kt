package com.conversify.ui.profile.edit

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
import com.conversify.data.remote.models.loginsignup.UsernameAvailabilityResponse
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
class EditProfileViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    val usernameAvailability by lazy { SingleLiveEvent<Resource<Boolean>>() }
    private var usernameAvailabilityCall: Call<ApiResponse<UsernameAvailabilityResponse>>? = null

    val editProfile by lazy { SingleLiveEvent<Resource<ApiResponse<ProfileDto>>>() }

    fun editProfile(request: CreateEditProfileRequest, postImage: File?) {
        if (postImage == null) {
            updateProfileApi(request)
        } else {
            editProfile.value = Resource.loading()
            s3Uploader.upload(postImage)
                    .addUploadCompleteListener { imageUrl ->
                        request.imageOriginal = imageUrl
                        request.imageThumbnail = imageUrl
                        updateProfileApi(request)
                    }
                    .addUploadFailedListener {
                        editProfile.value = Resource.error(AppError.WaitingForNetwork)
                    }
        }
    }

    private fun updateProfileApi(request: CreateEditProfileRequest) {
        editProfile.value = Resource.loading()

        RetrofitClient.conversifyApi
                .editProfile(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>, response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            val profile = response.body()?.data
                            if (profile != null) {
                                UserManager.saveProfile(profile)
                            }
                            editProfile.value = Resource.success()
                        } else {
                            editProfile.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        editProfile.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun checkUsernameAvailability(username: String) {
        usernameAvailability.value = Resource.loading()

        usernameAvailabilityCall?.cancel()
        val call = RetrofitClient.conversifyApi.usernameAvailability(username)
        usernameAvailabilityCall = call
        call.enqueue(object : Callback<ApiResponse<UsernameAvailabilityResponse>> {
            override fun onResponse(call: Call<ApiResponse<UsernameAvailabilityResponse>>,
                                    response: Response<ApiResponse<UsernameAvailabilityResponse>>) {
                if (response.isSuccessful) {
                    val isAvailable = response.body()?.data?.isAvailable ?: false
                    usernameAvailability.value = Resource.success(isAvailable)
                } else {
                    usernameAvailability.value = Resource.error(response.getAppError())
                }
            }

            override fun onFailure(call: Call<ApiResponse<UsernameAvailabilityResponse>>, t: Throwable) {
                if (!call.isCanceled) {
                    usernameAvailability.value = Resource.error(t.failureAppError())
                }
            }
        })
    }

    fun updateUsernameAvailability(isAvailable: Boolean) {
        if (!isAvailable)
            usernameAvailabilityCall?.cancel()
    }

    fun getProfile() = profile

    override fun onCleared() {
        super.onCleared()
        s3Uploader.clear()
        usernameAvailabilityCall?.cancel()
    }

}