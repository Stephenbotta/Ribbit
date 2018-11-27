package com.conversify.ui.venues.chat

import android.arch.lifecycle.ViewModel
import com.conversify.data.local.UserManager
import com.conversify.data.remote.ApiConstants
import com.conversify.data.remote.RetrofitClient
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
import com.conversify.utils.SingleLiveEvent
import io.socket.client.Ack
import io.socket.emitter.Emitter
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class ChatViewModel : ViewModel() {
    val newMessage by lazy { SingleLiveEvent<ChatMessageDto>() }
    val oldMessages by lazy { SingleLiveEvent<Resource<PagingResult<List<ChatMessageDto>>>>() }

    private val apiCalls by lazy { mutableListOf<Call<*>>() }   // Containing all on-going api calls that needs to be canceled when viewModel is cleared

    private val ownUserId by lazy { UserManager.getUserId() }
    private val chatMessageBuilder by lazy { ChatMessageBuilder(ownUserId) }
    private val socketManager by lazy { SocketManager.getInstance() }

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
        this.venue = venue
        venueDetailsLoaded = false
        lastMessageId = null
        socketManager.on(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        socketManager.connect()
    }

    fun isValidForPaging() = !isChatLoading && !isLastChatMessageReceived

    fun isVenueDetailsLoaded() = venueDetailsLoaded

    fun getMembers(): ArrayList<VenueMemberDto> = venueMembers

    fun getVenue(): VenueDto = venue

    fun sendTextMessage(textMessage: String) {
        val message = chatMessageBuilder.buildTextMessage(textMessage)
        sendMessage(message)
        newMessage.value = message
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
                Timber.i("Send message acknowledge\n$acknowledgement")
            }
        })
    }

    private fun getMessageJsonObject(message: ChatMessageDto): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.putOpt("senderId", ownUserId)
        jsonObject.putOpt("groupId", venue.id)
        jsonObject.putOpt("groupType", ApiConstants.TYPE_VENUE)
        jsonObject.putOpt("type", ApiConstants.MESSAGE_TYPE_TEXT)
        jsonObject.putOpt("message", message.details?.message)
        return jsonObject
    }

    override fun onCleared() {
        super.onCleared()
        socketManager.off(SocketManager.EVENT_NEW_MESSAGE, newMessageListener)
        apiCalls.forEach { it.cancel() }
    }
}