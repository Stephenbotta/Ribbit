package com.conversify.ui.conversenearby.post

import android.app.Application
import android.arch.lifecycle.MutableLiveData
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Uploader
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.post.CreatePostRequest
import com.conversify.data.repository.InterestsRepository
import com.conversify.ui.base.BaseViewModel
import com.conversify.utils.AppUtils
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * Created by Manish Bhargav
 */
class PostNearByViewModel(application: Application) : BaseViewModel(application) {

    private var profile = UserManager.getProfile()
    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    val usernameAvailability by lazy { SingleLiveEvent<Resource<Boolean>>() }
    val createPost by lazy { SingleLiveEvent<Resource<Any>>() }
    private val interestsRepository by lazy { InterestsRepository.getInstance() }
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }

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

        RetrofitClient.conversifyApi
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

    fun getInterests() {

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

    fun getProfile() = profile

    fun updateProfile(): ProfileDto {
        profile = UserManager.getProfile()
        return profile
    }

    override fun onCleared() {
        super.onCleared()
        s3Uploader.clear()
    }

}