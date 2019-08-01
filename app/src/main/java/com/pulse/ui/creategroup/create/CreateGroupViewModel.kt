package com.pulse.ui.creategroup.create

import android.arch.lifecycle.ViewModel
import com.pulse.data.local.models.AppError
import com.pulse.data.remote.RetrofitClient
import com.pulse.data.remote.aws.S3Uploader
import com.pulse.data.remote.aws.S3Utils
import com.pulse.data.remote.failureAppError
import com.pulse.data.remote.getAppError
import com.pulse.data.remote.models.Resource
import com.pulse.data.remote.models.groups.CreateEditGroupRequest
import com.pulse.utils.SingleLiveEvent
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