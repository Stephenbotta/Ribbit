package com.pulse.ui.profile.settings.verification

import android.app.Application
import com.pulse.data.local.UserManager
import com.pulse.data.local.models.AppError
import com.pulse.data.remote.RetrofitClient
import com.pulse.data.remote.aws.S3Uploader
import com.pulse.data.remote.aws.S3Utils
import com.pulse.data.remote.failureAppError
import com.pulse.data.remote.getAppError
import com.pulse.data.remote.models.ApiResponse
import com.pulse.data.remote.models.Resource
import com.pulse.ui.base.BaseViewModel
import com.pulse.utils.SingleLiveEvent
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
                        val map = hashMapOf<String, String>()
                        map["passportDocUrl"] = imageUrl
                        postSettingsVerification(map)
                    }
                    .addUploadFailedListener {
                        verificationApi.value = Resource.error(AppError.WaitingForNetwork)
                    }
        }
    }

    private fun postSettingsVerification(map: HashMap<String, String>) {
        verificationApi.value = Resource.loading()

        RetrofitClient.conversifyApi
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