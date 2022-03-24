package com.ribbit.ui.creategroup.create

import androidx.lifecycle.ViewModel
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.aws.S3Uploader
import com.ribbit.data.remote.aws.S3Utils
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.groups.CreateEditGroupRequest
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class CreateGroupViewModel : ViewModel() {
    val createGroup by lazy { SingleLiveEvent<Resource<Any>>() }

    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }

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
        RetrofitClient.ribbitApi
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