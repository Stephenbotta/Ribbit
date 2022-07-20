package com.ribbit.ui.profile.settings.verification

import android.app.Application
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.aws.S3Uploader
import com.ribbit.data.remote.aws.S3Utils
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.SingleLiveEvent
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
    val verificationApi by lazy { SingleLiveEvent<Resource<ApiResponse<Any>>>() }

    fun settingsVerification(map: HashMap<String, String>, postImage: File?) {
        if (postImage == null) {
            postSettingsVerification(map)
        } else {
            verificationApi.value = Resource.loading()
            s3Uploader.upload(postImage)
                    .addUploadCompleteListener { imageUrl ->
                        val hashMap = hashMapOf<String, String>()
                        hashMap["passportDocUrl"] = imageUrl
                        postSettingsVerification(hashMap)
                    }
                    .addUploadFailedListener {
                        verificationApi.value = Resource.error(AppError.WaitingForNetwork)
                    }
        }
    }

    private fun postSettingsVerification(map: HashMap<String, String>) {
        verificationApi.value = Resource.loading()

        RetrofitClient.ribbitApi
                .postSettingsVerification(map)
                .enqueue(object : Callback<ApiResponse<Any>> {
                    override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            verificationApi.value = Resource.success()
                        } else {
                            verificationApi.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
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