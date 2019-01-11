package com.conversify.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.MenuItem
import com.conversify.R
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.models.PagingResult
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.chat.ChatMessageDto
import com.conversify.data.remote.models.chat.MessageStatus
import com.conversify.data.remote.models.people.UserCrossedDto
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.longToast
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.chat.individual.ChatIndividualViewModel
import com.conversify.ui.custom.AppToast
import com.conversify.ui.images.ImagesActivity
import com.conversify.ui.venues.details.VenueDetailsActivity
import com.conversify.ui.videoplayer.VideoPlayerActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import com.conversify.utils.MediaPicker
import com.conversify.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_chat.*
import permissions.dispatcher.*

@RuntimePermissions
class ChatActivity : BaseActivity(), ChatAdapter.Callback {
    companion object {
        private const val FLAG_VALUE = "FLAG_VALUE"

        private const val EXTRA_VENUE = "EXTRA_VENUE"
        private const val EXTRA_INDIVIDUAL_CHAT = "EXTRA_INDIVIDUAL_CHAT"


        fun getStartIntent(context: Context, venue: VenueDto, flag: Int): Intent {
            return Intent(context, ChatActivity::class.java)
                    .putExtra(FLAG_VALUE, flag)
                    .putExtra(EXTRA_VENUE, venue)
        }

        fun getStartIntentForIndividualChat(context: Context, userCrossed: UserCrossedDto, flag: Int): Intent {
            return Intent(context, ChatActivity::class.java)
                    .putExtra(FLAG_VALUE, flag)
                    .putExtra(EXTRA_INDIVIDUAL_CHAT, userCrossed)
        }


    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    private lateinit var mediaPicker: MediaPicker
    private var flag = 0
    private lateinit var viewModelIndividual: ChatIndividualViewModel


    private val newMessageObserver = Observer<ChatMessageDto> {
        it ?: return@Observer
        setResult(Activity.RESULT_OK)
        adapter.addNewMessage(it)
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    private val oldMessagesObserver = Observer<Resource<PagingResult<List<ChatMessageDto>>>> {
        it ?: return@Observer
        when (it.status) {
            Status.SUCCESS -> {
                swipeRefreshLayout.isRefreshing = false
                adapter.addOldMessages(it.data?.result ?: emptyList())
                // Scroll to bottom for first page
                if (it.data?.isFirstPage == true) {
                    rvChat.scrollToPosition(adapter.itemCount - 1)
                }
            }

            Status.ERROR -> {
                swipeRefreshLayout.isRefreshing = false
                handleError(it.error)
            }

            Status.LOADING -> {
                swipeRefreshLayout.isRefreshing = true
            }
        }
    }

    private val sendMessageObserver = Observer<Resource<ChatMessageDto>> {
        it ?: return@Observer
        when (it.status) {
            Status.SUCCESS -> {
                adapter.updateMessageStatus(it.data?.localId ?: "", MessageStatus.SENT)
            }

            Status.ERROR -> {
                handleError(it.error)
            }

            Status.LOADING -> {
            }
        }
    }

    private val uploadFileObserver = Observer<Resource<String>> {
        it ?: return@Observer
        when (it.status) {
            Status.SUCCESS -> {
            }

            Status.ERROR -> {
                if (it.error is AppError.FileUploadFailed) {
                    adapter.updateMessageStatus(it.error.id, MessageStatus.ERROR)
                }
            }

            Status.LOADING -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        flag = intent.getIntExtra(FLAG_VALUE, 0)
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            val venue = intent.getParcelableExtra<VenueDto>(EXTRA_VENUE)
            viewModel = ViewModelProviders.of(this)[ChatViewModel::class.java]
            viewModel.start(venue)

            mediaPicker = MediaPicker(this)
            mediaPicker.setAllowVideo(true)
            setListeners(flag)
            observeChanges(flag)
            setupChatRecycler(flag)
            setupToolbar(venue)
            getOldMessages(flag)
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            val userCrossed = intent.getParcelableExtra<UserCrossedDto>(EXTRA_INDIVIDUAL_CHAT)

            viewModelIndividual = ViewModelProviders.of(this)[ChatIndividualViewModel::class.java]
            viewModelIndividual.start(userCrossed)

            mediaPicker = MediaPicker(this)
            mediaPicker.setAllowVideo(true)
            setListeners(flag)
            observeChanges(flag)
            setupChatRecycler(flag)
            setupToolbar(userCrossed)
            getOldMessages(flag)
        }

    }

    private fun setListeners(flag: Int) {
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            mediaPicker.setImagePickerListener { imageFile ->
                viewModel.sendImageMessage(imageFile)
            }

            mediaPicker.setVideoPickerListener { videoFile ->
                if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                    viewModel.sendVideoMessage(videoFile)
                } else {
                    AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                }
            }

            ivVenue.setOnClickListener { showVenueDetails() }
            tvVenueName.setOnClickListener { showVenueDetails() }

            btnAttachment.setOnClickListener { showImagePickerWithPermissionCheck() }

            fabSend.setOnClickListener { sendTextMessage(flag) }
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            mediaPicker.setImagePickerListener { imageFile ->
                viewModelIndividual.sendImageMessage(imageFile)
            }

            mediaPicker.setVideoPickerListener { videoFile ->
                if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                    viewModelIndividual.sendVideoMessage(videoFile)
                } else {
                    AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                }
            }

            ivVenue.setOnClickListener { }
            tvVenueName.setOnClickListener { }

            btnAttachment.setOnClickListener { showImagePickerWithPermissionCheck() }

            fabSend.setOnClickListener { sendTextMessage(flag) }
        }
    }

    private fun observeChanges(flag: Int) {
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            viewModel.newMessage.observeForever(newMessageObserver)
            viewModel.oldMessages.observeForever(oldMessagesObserver)
            viewModel.sendMessage.observeForever(sendMessageObserver)
            viewModel.uploadFile.observeForever(uploadFileObserver)
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            viewModelIndividual.newMessage.observeForever(newMessageObserver)
            viewModelIndividual.oldMessages.observeForever(oldMessagesObserver)
            viewModelIndividual.sendMessage.observeForever(sendMessageObserver)
            viewModelIndividual.uploadFile.observeForever(uploadFileObserver)
        }
    }

    private fun setupChatRecycler(flag: Int) {
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            swipeRefreshLayout.isEnabled = false
            adapter = ChatAdapter(this, this)
            rvChat.adapter = adapter
            (rvChat.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!rvChat.canScrollVertically(-1) && viewModel.isValidForPaging()) {
                        viewModel.getOldMessages()
                    }
                }
            })
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            swipeRefreshLayout.isEnabled = false
            adapter = ChatAdapter(this, this)
            rvChat.adapter = adapter
            (rvChat.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            rvChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (!rvChat.canScrollVertically(-1) && viewModelIndividual.isValidForPaging()) {
                        viewModelIndividual.getOldMessages()
                    }
                }
            })
        }
    }

    private fun setupToolbar(venue: VenueDto) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowTitleEnabled(false)
        }

        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = venue.name
    }


    private fun setupToolbar(userCrossed: UserCrossedDto) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowTitleEnabled(false)
        }

        GlideApp.with(this)
                .load(userCrossed.crossedUser?.image?.thumbnail)
                .into(ivVenue)

        tvVenueName.text = userCrossed.crossedUser?.fullName
    }


    private fun getOldMessages(flag: Int) {
        if (isNetworkActiveWithMessage()) {
            if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
                viewModel.getOldMessages()
            } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
                viewModelIndividual.getOldMessages()
            }
        }
    }

    private fun sendTextMessage(flag: Int) {
        val message = etMessage.text.toString().trim()
        if (message.isNotBlank() && isNetworkActiveWithMessage()) {
            etMessage.setText("")
            if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
                viewModel.sendTextMessage(message)
            } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
                viewModelIndividual.sendTextMessage(message)
            }
        }
    }

    private fun showVenueDetails() {
        if (viewModel.isVenueDetailsLoaded()) {
            val intent = VenueDetailsActivity.getStartIntent(this, viewModel.getVenue(), viewModel.getMembers())
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_DETAILS)
        }
    }

    override fun onImageMessageClicked(chatMessage: ChatMessageDto) {
        // Display local image file if exists, otherwise display online image
        val localImage = chatMessage.localFile?.absolutePath
        val onlineImage = chatMessage.details?.image?.original
        ImagesActivity.start(this, arrayListOf(localImage ?: onlineImage ?: ""))
    }

    override fun onVideoMessageClicked(chatMessage: ChatMessageDto) {
        val localVideo = chatMessage.localFile?.absolutePath
        val onlineVideo = chatMessage.details?.video?.original
        val videoPath = localVideo ?: onlineVideo ?: ""
        if (videoPath.isNotBlank()) {
            VideoPlayerActivity.start(this, videoPath)
        } else {
            shortToast(R.string.chat_message_invalid_video_path)
        }
    }

    override fun onResendMessageClicked(chatMessage: ChatMessageDto) {
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            viewModel.resendMessage(chatMessage)
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            viewModelIndividual.resendMessage(chatMessage)
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showImagePicker() {
        mediaPicker.show()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.permission_rationale_camera_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageDenied() {
        longToast(R.string.permission_denied_camera_storage)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageNeverAsk() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_camera_storage, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConstants.REQ_CODE_VENUE_DETAILS &&
                resultCode == Activity.RESULT_OK && data != null) {
            val venue = data.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
            if (venue != null) {
                if (venue.isMember == false) {
                    // Will be false if user has exit the venue
                    setResult(Activity.RESULT_OK, data)
                    finish()
                } else {
                    // Otherwise update the venue
                    viewModel.updateVenue(venue)
                }
            }
        } else {
            mediaPicker.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (flag == AppConstants.REQ_CODE_VENUE_CHAT) {
            viewModel.newMessage.removeObserver(newMessageObserver)
            viewModel.oldMessages.removeObserver(oldMessagesObserver)
            viewModel.sendMessage.removeObserver(sendMessageObserver)
            viewModel.uploadFile.removeObserver(uploadFileObserver)
        } else if (flag == AppConstants.REQ_CODE_INDIVIDUAL_CHAT) {
            viewModelIndividual.newMessage.removeObserver(newMessageObserver)
            viewModelIndividual.oldMessages.removeObserver(oldMessagesObserver)
            viewModelIndividual.sendMessage.removeObserver(sendMessageObserver)
            viewModelIndividual.uploadFile.removeObserver(uploadFileObserver)
        }
        mediaPicker.clear()
    }
}