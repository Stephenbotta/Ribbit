package com.pulse.ui.post.newpost

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import com.pulse.data.local.UserManager
import com.pulse.data.local.models.AppError
import com.pulse.data.remote.ApiConstants
import com.pulse.data.remote.RetrofitClient
import com.pulse.data.remote.aws.S3Uploader
import com.pulse.data.remote.aws.S3Utils
import com.pulse.data.remote.failureAppError
import com.pulse.data.remote.getAppError
import com.pulse.data.remote.models.ApiResponse
import com.pulse.data.remote.models.Resource
import com.pulse.data.remote.models.Status
import com.pulse.data.remote.models.groups.GroupDto
import com.pulse.data.remote.models.loginsignup.ImageUrlDto
import com.pulse.data.remote.models.post.CreatePostRequest
import com.pulse.ui.picker.models.MediaSelected
import com.pulse.ui.picker.models.MediaType
import com.pulse.ui.picker.models.UploadStatus
import com.pulse.utils.AppUtils
import com.pulse.utils.FileUtils
import com.pulse.utils.GetSampledImage
import com.pulse.utils.SingleLiveEvent
import com.pulse.utils.video.MediaController
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import kotlin.coroutines.CoroutineContext

class NewPostViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    val yourGroups by lazy { MutableLiveData<Resource<List<GroupDto>>>() }
    val createPost by lazy { SingleLiveEvent<Resource<Any>>() }

    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    private var profile = UserManager.getProfile()
    val uploadFileAcknowledgement = MutableLiveData<Resource<MediaSelected>>()
    private val cacheDirectory by lazy { FileUtils.getAppCacheDirectory(getApplication()) }
    private val cacheDirectoryPath by lazy { cacheDirectory.absolutePath }
    private val mediaController by lazy { MediaController.getInstance() }

    private val parentJob by lazy { Job() }

    fun getProfile() = profile

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + parentJob

    fun getYourGroups() {
        val existingGroups = yourGroups.value?.data
        if (existingGroups != null) {
            return
        }

        yourGroups.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getYourGroups()
                .enqueue(object : Callback<ApiResponse<List<GroupDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<GroupDto>>>,
                                            response: Response<ApiResponse<List<GroupDto>>>) {
                        if (response.isSuccessful) {
                            val groups = response.body()?.data ?: emptyList()
                            yourGroups.value = Resource.success(groups)
                        } else {
                            yourGroups.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<GroupDto>>>, t: Throwable) {
                        yourGroups.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun createPost(postText: String, medias: List<MediaSelected>, request: CreatePostRequest) {
        val hashTags = AppUtils.getHashTagsFromString(postText, false)
        request.hashTags = hashTags
        request.postText = postText
        if (medias.isEmpty()) {
            createPostApi(request)
        } else {
            medias.forEach { media ->
                when (media.type) {
                    MediaType.IMAGE -> {
                        request.media.add(ImageUrlDto(original = media.original,
                                thumbnail = media.thumbnailPath, mediaType = ApiConstants.POST_TYPE_IMAGE))
                    }
                    MediaType.GIF -> {
                        request.media.add(ImageUrlDto(original = media.original,
                                thumbnail = media.thumbnailPath, mediaType = ApiConstants.POST_TYPE_GIF))
                    }
                    MediaType.VIDEO -> {
                        request.media.add(ImageUrlDto(original = media.thumbnailPath,
                                thumbnail = media.thumbnailPath, mediaType = ApiConstants.POST_TYPE_VIDEO,
                                videoUrl = media.original))
                    }
                }
            }
            createPost.value = Resource.loading()
            createPostApi(request)
        }
    }

    fun uploadMedias(medias: List<MediaSelected>) {
        medias.forEach { media ->
            when (media.type) {
                MediaType.IMAGE -> {
                    uploadImageFile(media)
                }
                MediaType.GIF -> {
                    uploadGif(media)
                }
                MediaType.VIDEO -> {
                    uploadVideoFile(media)
                }
            }
        }
    }

    private fun uploadImageFile(media: MediaSelected) {
        launch {
            media.status = UploadStatus.SENDING
            uploadFileAcknowledgement.value = Resource(Status.LOADING, media, null)

            val start = System.currentTimeMillis()
            Timber.i("Started sampling")
            val sampledImage = withContext(Dispatchers.IO) {
                GetSampledImage.sampleImageSync(media.path, cacheDirectoryPath, 650)
            }
            val totalTime = System.currentTimeMillis() - start
            Timber.i("Sampling completed : $totalTime")
            if (sampledImage != null) {
                media.path = sampledImage.path
                uploadImage(media)
            } else {
                media.status = UploadStatus.ERROR
                uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                        AppError.FileUploadFailed(media.mediaId))
                Timber.w("Sampled image is null")
            }
        }
    }

    private fun uploadImage(media: MediaSelected) {
        if (media.original == null) {
            s3Uploader.upload(File(media.path))
                    .addUploadCompleteListener { imageUrl ->
                        media.original = imageUrl
                        media.thumbnailPath = imageUrl
                        media.status = UploadStatus.SENT
                        uploadFileAcknowledgement.value = Resource.success(media)
                    }
                    .addUploadFailedListener {
                        media.status = UploadStatus.ERROR
                        uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                                it.failureAppError())
                    }
        } else {
            Timber.i("Image already uploaded")
        }
    }

    private fun uploadGif(media: MediaSelected) {
        media.status = UploadStatus.SENDING
        uploadFileAcknowledgement.value = Resource(Status.LOADING, media, null)

        s3Uploader.upload(File(media.path))
                .addUploadCompleteListener { gifUrl ->
                    media.original = gifUrl
                    media.thumbnailPath = gifUrl
                    media.status = UploadStatus.SENT
                    uploadFileAcknowledgement.value = Resource.success(media)
                }
                .addUploadFailedListener {
                    media.status = UploadStatus.ERROR
                    uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                            it.failureAppError())
                }
    }

    private fun uploadVideoFile(media: MediaSelected) {
        launch {
            media.status = UploadStatus.SENDING
            uploadFileAcknowledgement.value = Resource(Status.LOADING, media, null)

            val compressedVideoFile = try {
                withContext(Dispatchers.IO) {
                    mediaController.convertVideo(media.path, cacheDirectory)
                }
            } catch (exception: Exception) {
                Timber.e(exception)
                null
            }

            if (compressedVideoFile != null) {
                Timber.i("Video compression successful : $compressedVideoFile")
                media.path = compressedVideoFile.path
                uploadVideo(media)
            } else {
                media.status = UploadStatus.ERROR
                uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                        AppError.FileUploadFailed(media.mediaId))
                Timber.w("Compressed video is null")
            }
        }
    }

    private fun uploadVideo(media: MediaSelected) {
        // Upload the video only if it hasn't been uploaded.
        if (media.original == null) {
            Timber.i("Uploading video")
            s3Uploader.upload(File(media.path))
                    .addUploadCompleteListener { videoUrl ->
                        media.original = videoUrl

                        if (media.thumbnailPath != null && media.original != null) {
                            Timber.i("Proceeding to send video message")
                            media.status = UploadStatus.SENT
                            uploadFileAcknowledgement.value = Resource.success(media)
                        }
                    }
                    .addUploadFailedListener {
                        media.status = UploadStatus.ERROR
                        uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                                it.failureAppError())
                    }
        }

        // Upload the video thumbnail only if it hasn't been uploaded.
        if (media.thumbnail != null && media.thumbnailPath == null) {
            Timber.i("Uploading video thumbnail")
            s3Uploader.upload(media.thumbnail)
                    .addUploadCompleteListener { thumbnailUrl ->
                        media.thumbnailPath = thumbnailUrl

                        if (media.thumbnailPath != null && media.original != null) {
                            Timber.i("Proceeding to send video message")
                            media.status = UploadStatus.SENT
                            uploadFileAcknowledgement.value = Resource.success(media)
                        }
                    }
                    .addUploadFailedListener {
                        media.status = UploadStatus.ERROR
                        uploadFileAcknowledgement.value = Resource(Status.ERROR, media,
                                it.failureAppError())
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

    override fun onCleared() {
        super.onCleared()
        s3Uploader.clear()
        parentJob.cancel()
    }

    fun resendMedia(media: MediaSelected) {
        when (media.type) {
            MediaType.IMAGE -> {
                uploadImage(media)
            }
            MediaType.GIF -> {
                uploadGif(media)
            }
            MediaType.VIDEO -> {
                uploadVideo(media)
            }
        }
    }
}