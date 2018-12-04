package com.conversify.ui.creategroup

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Uploader
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.groups.CreateEditGroupRequest
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.repository.InterestsRepository
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateGroupViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }
    val createGroup by lazy { SingleLiveEvent<Resource<Any>>() }

    private val interestsRepository by lazy { InterestsRepository.getInstance() }
    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }

    fun hasCachedInterests(): Boolean = interestsRepository.hasCachedInterests()

    fun getInterests() {
        if (hasCachedInterests()) {
            interests.value = Resource.success(interestsRepository.getCachedInterests())
            return
        }

        interestsRepository.getInterests(object : InterestsRepository.GetInterestsCallback {
            override fun onGetInterestsLoading() {
                interests.value = Resource.loading()
            }

            override fun onGetInterestsSuccess(allInterests: List<InterestDto>) {
                interests.value = Resource.success(allInterests)
            }

            override fun onGetInterestsFailed(error: AppError) {
                interests.value = Resource.error(error)
            }
        })
    }

    fun createGroup(request: CreateEditGroupRequest, selectedGroupImageFile: File?) {
        createGroup.value = Resource.loading()

        if (selectedGroupImageFile != null) {
            s3Uploader.upload(selectedGroupImageFile)
                    .addUploadCompleteListener { groupImageUrl ->
                        request.imageOriginalUrl = groupImageUrl
                        request.imageThumbnailUrl = groupImageUrl
                        createGroupApi(request)
                    }
                    .addUploadFailedListener {
                        createGroup.value = Resource.error(AppError.WaitingForNetwork)
                    }
        } else {
            createGroupApi(request)
        }
    }

    private fun createGroupApi(request: CreateEditGroupRequest) {
        RetrofitClient.conversifyApi
                .createGroup(request)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            createGroup.value = Resource.success()
                        } else {
                            createGroup.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        createGroup.value = Resource.error(t.failureAppError())
                    }
                })
    }
}