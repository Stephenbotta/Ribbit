package com.conversify.ui.venues.chat

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.provider.MediaStore
import com.conversify.data.local.UserManager
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Uploader
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.chat.VenueDetailsResponse
import com.conversify.data.remote.models.chat.VenueMemberDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.data.remote.socket.SocketManager
import com.conversify.utils.GetSampledImage
import com.conversify.utils.MediaUtils
import com.conversify.utils.SingleLiveEvent
import io.socket.client.Ack
import io.socket.emitter.Emitter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val newMessage by lazy { SingleLiveEvent<ChatMessageDto>() }
    val oldMessages by lazy { SingleLiveEvent<Resource<PagingResult<List<ChatMessageDto>>>>() }
    val uploadFile by lazy { SingleLiveEvent<Resource<String>>() }
    val sendMessage by lazy { SingleLiveEvent<Resource<ChatMessageDto>>() }

    private val apiCalls by lazy { mutableListOf<Call<*>>() }   // Containing all on-going api calls that needs to be canceled when viewModel is cleared

    private val ownUserId by lazy { UserManager.getUserId() }
    private val chatMessageBuilder by lazy { ChatMessageBuilder(ownUserId) }
    private val socketManager by lazy { SocketManager.getInstance() }
    private val s3ImageUploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }
    private val imageCacheDirectory by lazy {
        getApplication<Application>().externalCacheDir?.absolutePath ?: ""
    }

    private val venueMembers by lazy { arrayListOf<VenueMemberDto>() }

    private lateinit var venue: VenueDto

    private var lastMessageId: String? = null
    private var isChatLoading = false
    private var isLastChatMessageReceived = false

    private var venueDetailsLoaded = false

    private val newMessageListener = Emitter.Listener { args ->
        val chatMessage = chatMessageBuilder.getChatMessageFromSocketArgument(args.firstOrNull())
        Timber.i("New message received:\n$chatMessage")
        if (chatMessage != null && chatMessage.conversationId == venue.conversationId) {
            newMessage.postValue(chatMessage)
        }
    }

    fun start(venue: VenueDto) {
        updateVenue(venue)
        venueDetailsLoaded = false
        lastMessageId = null
        socketManager.on(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        socketManager.connect()
    }

    fun updateVenue(venue: VenueDto) {
        this.venue = venue
    }

    fun isValidForPaging() = !isChatLoading && !isLastChatMessageReceived

    fun isVenueDetailsLoaded() = venueDetailsLoaded

    fun getMembers(): ArrayList<VenueMemberDto> = venueMembers

    fun getVenue(): VenueDto = venue

    fun sendTextMessage(textMessage: String) {
        val message = chatMessageBuilder.buildTextMessage(textMessage)
        newMessage.value = message
        sendMessage(message)
    }

    fun sendImageMessage(image: File) {
        // todo move to async
        val sampledImage = GetSampledImage.sampleImageSync(image.absolutePath, imageCacheDirectory, 650)
        if (sampledImage != null) {
            val message = chatMessageBuilder.buildImageMessage(sampledImage)
            newMessage.value = message
            uploadImage(message)
        } else {
            Timber.w("Sampled image is null")
        }
    }

    fun sendVideoMessage(video: File) {
        val thumbnailImage = MediaUtils.getThumbnailFromVideo(video.absolutePath,
                imageCacheDirectory, MediaStore.Video.Thumbnails.MICRO_KIND)
        if (thumbnailImage != null) {
            val message = chatMessageBuilder.buildVideoMessage(video, thumbnailImage)
            newMessage.value = message
        } else {
            Timber.w("Thumbnail image is null")
        }
    }

    fun resendMessage(chatMessage: ChatMessageDto) {
        Timber.i("Resending message: $chatMessage")

        when (chatMessage.details?.type) {
            ApiConstants.MESSAGE_TYPE_IMAGE -> uploadImage(chatMessage)
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

    fun getOldMessages() {
        oldMessages.value = Resource.loading()
        isChatLoading = true
        val firstPage = lastMessageId == null
        val call = RetrofitClient.conversifyApi.getVenueDetails(venue.id, lastMessageId)
        apiCalls.add(call)
        call.enqueue(object : Callback<ApiResponse<VenueDetailsResponse>> {
            override fun onResponse(call: Call<ApiResponse<VenueDetailsResponse>>,
                                    response: Response<ApiResponse<VenueDetailsResponse>>) {
                if (response.isSuccessful) {
                    if (firstPage) {
                        // Reset for first page
                        isLastChatMessageReceived = false
                        lastMessageId = null

                        // Update venue members on response of first page
                        val members = response.body()?.data?.venueMembers ?: emptyList()
                        venueMembers.clear()
                        venueMembers.addAll(members)

                        // Update the notification for current venue
                        venue.notification = response.body()?.data?.notification
                        venue.memberCount = members.size

                        venueDetailsLoaded = true
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

            override fun onFailure(call: Call<ApiResponse<VenueDetailsResponse>>, throwable: Throwable) {
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
                message.isDelivered = true
                sendMessage.postValue(Resource.success(message))
            }
        })
    }

    private fun getMessageJsonObject(message: ChatMessageDto): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.putOpt("senderId", ownUserId)
        jsonObject.putOpt("groupId", venue.id)
        jsonObject.putOpt("groupType", ApiConstants.TYPE_VENUE)
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

    override fun onCleared() {
        super.onCleared()
        socketManager.off(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        apiCalls.forEach { it.cancel() }
        s3ImageUploader.clear()
    }
}