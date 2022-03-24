package com.ribbit.ui.conversenearby.post

import android.app.Application
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.aws.S3Uploader
import com.ribbit.data.remote.aws.S3Utils
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.post.CreatePostRequest
import com.ribbit.ui.base.BaseViewModel
import com.ribbit.utils.AppUtils
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * Created by Manish Bhargav
 */
class SubmitPostViewModel(application: Application) : BaseViewModel(application) {

    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    val createPost by lazy { SingleLiveEvent<Resource<Any>>() }

    fun createPost(request: CreatePostRequest, postImage: File?) {
        val hashTags = AppUtils.getHashTagsFromString(request.postText ?: "", false)
        request.hashTags = hashTags
        if (postImage == null) {
            createPostApi(request)
        } else {
            createPost.value = Resource.loading()
            s3Uploader.upload(postImage)
                    .addUploadCompleteListener { imageUrl ->
                        request.imageOriginal = imageUrl
                        request.imageThumbnail = imageUrl
                        createPostApi(request)
                    }
                    .addUploadFailedListener {
                        createPost.value = Resource.error(AppError.WaitingForNetwork)
                    }
        }
    }

    private fun createPostApi(request: CreatePostRequest) {
        createPost.value = Resource.loading()

        RetrofitClient.ribbitApi
                .createPost(request)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            createPost.value = Resource.success()
                        } else {
                            createPost.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        createPost.value = Resource.error(t.failureAppError())
                    }
                })
    }

    override fun onCleared() {
        super.onCleared()
        s3Uploader.clear()
    }

}