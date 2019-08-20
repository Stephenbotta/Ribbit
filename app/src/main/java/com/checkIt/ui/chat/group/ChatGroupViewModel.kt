package com.checkIt.ui.chat.group

import android.app.Application
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import com.checkIt.data.local.UserManager
import com.checkIt.data.local.models.AppError
import com.checkIt.data.remote.ApiConstants
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.aws.S3Uploader
import com.checkIt.data.remote.aws.S3Utils
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.PagingResult
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.chat.ChatDeleteDto
import com.checkIt.data.remote.models.chat.ChatMessageDto
import com.checkIt.data.remote.models.chat.MessageStatus
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.socket.SocketManager
import com.checkIt.ui.chat.ChatMessageBuilder
import com.checkIt.ui.chat.individual.ChatIndividualResponse
import com.checkIt.utils.FileUtils
import com.checkIt.utils.GetSampledImage
import com.checkIt.utils.MediaUtils
import com.checkIt.utils.SingleLiveEvent
import com.checkIt.utils.video.MediaController
import io.socket.client.Ack
import io.socket.emitter.Emitter
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File
import kotlin.coroutines.CoroutineContext

class ChatGroupViewModel(application: Application) : AndroidViewModel(application), CoroutineScope {
    val newMessage by lazy { SingleLiveEvent<ChatMessageDto>() }
    val oldMessages by lazy { SingleLiveEvent<Resource<PagingResult<List<ChatMessageDto>>>>() }
    val uploadFile by lazy { SingleLiveEvent<Resource<String>>() }
    val sendMessage by lazy { SingleLiveEvent<Resource<ChatMessageDto>>() }
    val deleteMessage by lazy { SingleLiveEvent<ChatDeleteDto>() }

    private val apiCalls by lazy { mutableListOf<Call<*>>() }   // Containing all on-going api calls that needs to be canceled when viewModel is cleared
    private val parentJob by lazy { Job() }

    private val ownUserId by lazy { UserManager.getUserId() }
    private val chatMessageBuilder by lazy { ChatMessageBuilder(ownUserId) }
    private val socketManager by lazy { SocketManager.getInstance() }
    private val s3ImageUploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    private val cacheDirectory by lazy { FileUtils.getAppCacheDirectory(getApplication()) }
    private val cacheDirectoryPath by lazy { cacheDirectory.absolutePath }
    private val mediaController by lazy { MediaController.getInstance() }

    private lateinit var venue: GroupDto

    private var lastMessageId: String? = null
    private var isChatLoading = false
    private var isLastChatMessageReceived = false

    private var venueDetailsLoaded = false

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + parentJob

    private val newMessageListener = Emitter.Listener { args ->
        val chatMessage = chatMessageBuilder.getChatMessageFromSocketArgument(args.firstOrNull())
        Timber.i("New message received:\n$chatMessage")
        if (chatMessage != null && chatMessage.conversationId == venue.conversationId) {
            newMessage.postValue(chatMessage)
        }
    }

    private val deleteMessageListener = Emitter.Listener { args ->
        val chatMessage = chatMessageBuilder.getChatDeleteMessageFromSocketArgument(args.firstOrNull())
        Timber.i("Delete message confirmation:\n$chatMessage")
        if (chatMessage != null) {
            deleteMessage.postValue(chatMessage)
        }
    }

    fun start(venue: GroupDto) {
        updateGroup(venue)
        venueDetailsLoaded = false
        lastMessageId = null
        socketManager.on(SocketManager.EVENT_DELETE_MESSAGE, deleteMessageListener)
        socketManager.on(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        socketManager.connect()
    }

    fun updateGroup(venue: GroupDto) {
        this.venue = venue
    }

    fun isValidForPaging() = !isChatLoading && !isLastChatMessageReceived

    fun getGroup(): GroupDto = venue

    fun sendTextMessage(textMessage: String) {
        val message = chatMessageBuilder.buildTextMessage(textMessage)
        newMessage.value = message
        sendMessage(message)
    }

    fun sendImageMessage(image: File) {
        launch {
            val start = System.currentTimeMillis()
            Timber.i("Started sampling")
            val sampledImage = withContext(Dispatchers.IO) {
                GetSampledImage.sampleImageSync(image.absolutePath, cacheDirectoryPath, 650)
            }
            val totalTime = System.currentTimeMillis() - start
            Timber.i("Sampling completed : $totalTime")
            if (sampledImage != null) {
                val message = chatMessageBuilder.buildImageMessage(sampledImage)
                newMessage.value = message
                uploadImage(message)
            } else {
                Timber.w("Sampled image is null")
            }
        }
    }

    fun sendVideoMessage(video: File) {
        launch {
            val thumbnailImage = withContext(Dispatchers.IO) {
                MediaUtils.getThumbnailFromVideo(video.absolutePath,
                        cacheDirectoryPath, MediaStore.Video.Thumbnails.MINI_KIND)
            }
            if (thumbnailImage != null) {
                val message = chatMessageBuilder.buildVideoMessage(video, thumbnailImage)
                newMessage.value = message

                val compressedVideoFile = try {
                    withContext(Dispatchers.IO) {
                        mediaController.convertVideo(video.absolutePath, cacheDirectory)
                    }
                } catch (exception: Exception) {
                    Timber.e(exception)
                    null
                }

                if (compressedVideoFile != null) {
                    Timber.i("Video compression successful : $compressedVideoFile")
                    message.localFile = compressedVideoFile
                    uploadVideo(message)
                } else {
                    message.messageStatus = MessageStatus.ERROR
                    Timber.w("Compressed video is null")
                }
            } else {
                Timber.w("Thumbnail image is null")
            }
        }
    }

    fun resendMessage(chatMessage: ChatMessageDto) {
        Timber.i("Resending message: $chatMessage")

        when (chatMessage.details?.type) {
            ApiConstants.MESSAGE_TYPE_IMAGE -> uploadImage(chatMessage)
            ApiConstants.MESSAGE_TYPE_VIDEO -> uploadVideo(chatMessage)
        }
    }

    private fun uploadImage(message: ChatMessageDto) {
        val localImage = message.localFile

        if (localImage == null) {
            Timber.w("Local image file is null for message $message")
            val error = AppError.FileUploadFailed(message.localId ?: "")
            uploadFile.value = Resource.error(error)
        } else {
            s3ImageUploader.upload(localImage)
                    .addUploadCompleteListener { imageUrl ->
                        message.details?.image?.thumbnail = imageUrl
                        message.details?.image?.original = imageUrl
                        uploadFile.value = Resource.success(message.localId)
                        sendMessage(message)
                    }
                    .addUploadFailedListener {
                        val error = AppError.FileUploadFailed(message.localId ?: "")
                        uploadFile.value = Resource.error(error)
                    }
        }
    }

    private fun uploadVideo(message: ChatMessageDto) {
        // If video url and thumbnail url already exist, then proceed to send message.
        if (message.details?.video?.original != null &&
                message.details.image?.original != null) {
            Timber.i("Both video and video thumbnail are already uploaded")
            uploadFile.value = Resource.success(message.localId)
            sendMessage(message)
            return
        }

        val localVideo = message.localFile
        val localVideoThumbnail = message.localFileThumbnail

        if (localVideo != null && localVideoThumbnail != null) {
            // Upload the video only if it hasn't been uploaded.
            if (message.details?.video?.original == null) {
                Timber.i("Uploading video")
                s3ImageUploader.upload(localVideo)
                        .addUploadCompleteListener { videoUrl ->
                            message.details?.video?.thumbnail = videoUrl
                            message.details?.video?.original = videoUrl

                            if (message.details?.video?.original != null &&
                                    message.details?.image?.original != null) {
                                Timber.i("Proceeding to send video message")
                                uploadFile.value = Resource.success(message.localId)
                                sendMessage(message)
                            }
                        }
                        .addUploadFailedListener {
                            val error = AppError.FileUploadFailed(message.localId ?: "")
                            uploadFile.value = Resource.error(error)
                        }
            }

            // Upload the video thumbnail only if it hasn't been uploaded.
            if (message.details?.image?.original == null) {
                Timber.i("Uploading video thumbnail")
                s3ImageUploader.upload(localVideoThumbnail)
                        .addUploadCompleteListener { thumbnailUrl ->
                            message.details?.image?.thumbnail = thumbnailUrl
                            message.details?.image?.original = thumbnailUrl

                            if (message.details?.video?.original != null &&
                                    message.details.image?.original != null) {
                                Timber.i("Proceeding to send video message")
                                uploadFile.value = Resource.success(message.localId)
                                sendMessage(message)
                            }
                        }
                        .addUploadFailedListener {
                            val error = AppError.FileUploadFailed(message.localId ?: "")
                            uploadFile.value = Resource.error(error)
                        }
            }
        } else {
            Timber.w("Local video file or thumbnail file is null for message $message")
            val error = AppError.FileUploadFailed(message.localId ?: "")
            uploadFile.value = Resource.error(error)
        }
    }

    fun getOldMessages() {
        oldMessages.value = Resource.loading()
        isChatLoading = true
        val firstPage = lastMessageId == null
        val call = RetrofitClient.conversifyApi.getIndividualChat(venue.conversationId, lastMessageId)
        apiCalls.add(call)
        call.enqueue(object : Callback<ApiResponse<ChatIndividualResponse>> {
            override fun onResponse(call: Call<ApiResponse<ChatIndividualResponse>>,
                                    response: Response<ApiResponse<ChatIndividualResponse>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastChatMessageReceived = false
                        lastMessageId = null

                    }

                    val messages = response.body()?.data?.chatMessages ?: emptyList()
                    if (messages.isEmpty()) {
                        isLastChatMessageReceived = true
                        Timber.d("Last chat message received")
                    } else {
                        Timber.d("Next page available for chat messages")
                        lastMessageId = messages[0].id  // Update last received message id

                        // Set flag to true for all own messages
                        messages.forEach { message ->
                            if (message.sender?.id == ownUserId) {
                                message.ownMessage = true
                            }
                        }
                    }
                    oldMessages.value = Resource.success(PagingResult(firstPage, messages))
                } else {
                    oldMessages.value = Resource.error(response.getAppError())
                }
                isChatLoading = false
                apiCalls.remove(call)
            }

            override fun onFailure(call: Call<ApiResponse<ChatIndividualResponse>>, throwable: Throwable) {
                if (!call.isCanceled) {
                    oldMessages.value = Resource.error(throwable.failureAppError())
                    isChatLoading = false
                }
                apiCalls.remove(call)
            }
        })
    }

    private fun sendMessage(message: ChatMessageDto) {
        val arguments = getMessageJsonObject(message)
        socketManager.emit(SocketManager.EVENT_SEND_MESSAGE, arguments, Ack {
            val acknowledgement = it.firstOrNull()
            if (acknowledgement != null && acknowledgement is JSONObject) {
                val acknowledgeMessage = chatMessageBuilder.getChatMessageFromSocketArgument(acknowledgement)
                Timber.i("Send message acknowledge\n$acknowledgement")
                message.id = acknowledgeMessage?.id
                message.conversationId = acknowledgeMessage?.conversationId
                venue.conversationId = acknowledgeMessage?.conversationId
                message.isDelivered = true
                sendMessage.postValue(Resource.success(message))
            }
        })
    }

    private fun getMessageJsonObject(message: ChatMessageDto): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.putOpt("senderId", ownUserId)
        jsonObject.putOpt("groupId", venue.id)
        jsonObject.putOpt("groupType", ApiConstants.TYPE_GROUP)
        jsonObject.putOpt("conversationId", venue.conversationId)
        jsonObject.putOpt("type", message.details?.type)
        jsonObject.putOpt("message", message.details?.message)

        when (message.details?.type) {
            ApiConstants.MESSAGE_TYPE_IMAGE -> {
                jsonObject.putOpt("imageUrl", message.details.image?.original)
            }

            ApiConstants.MESSAGE_TYPE_VIDEO -> {
                jsonObject.putOpt("videoUrl", message.details.video?.original)

                // Contains the thumbnail of the video
                jsonObject.putOpt("imageUrl", message.details.image?.original)
            }
        }
        return jsonObject
    }

    fun deleteMessage(message: ChatMessageDto) {
        val arguments = deleteMessageJsonObject(message)
        socketManager.emit(SocketManager.EVENT_DELETE_MESSAGE, arguments, Ack {
            val acknowledgement = it.firstOrNull()
            if (acknowledgement != null && acknowledgement is JSONObject) {
                val acknowledgeMessage = chatMessageBuilder.getChatMessageFromSocketArgument(acknowledgement)
                Timber.i("Send message acknowledge\n$acknowledgement")
                message.id = acknowledgeMessage?.id
            }
        })
    }

    private fun deleteMessageJsonObject(message: ChatMessageDto): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.putOpt("senderId", ownUserId)
        jsonObject.putOpt("groupId", venue.id)
        jsonObject.putOpt("type", "GROUP")
        jsonObject.putOpt("messageId", message.id)
        return jsonObject
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.off(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        socketManager.off(SocketManager.EVENT_DELETE_MESSAGE, deleteMessageListener)
        apiCalls.forEach { it.cancel() }
        parentJob.cancel()
        s3ImageUploader.clear()
    }
}