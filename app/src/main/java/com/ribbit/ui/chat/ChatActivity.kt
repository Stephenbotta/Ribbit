package com.ribbit.ui.chat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.local.models.AppError
import com.ribbit.data.remote.models.PagingResult
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.chat.ChatDeleteDto
import com.ribbit.data.remote.models.chat.ChatMessageDto
import com.ribbit.data.remote.models.chat.MessageStatus
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.extensions.*
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.chat.group.ChatGroupViewModel
import com.ribbit.ui.chat.group.ChatListGroupViewModel
import com.ribbit.ui.chat.individual.ChatIndividualViewModel
import com.ribbit.ui.chat.individual.ChatListIndividualViewModel
import com.ribbit.ui.custom.AppToast
import com.ribbit.ui.groups.details.GroupDetailsActivity
import com.ribbit.ui.images.ImagesActivity
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.picker.MediaFragment
import com.ribbit.ui.picker.models.MediaSelected
import com.ribbit.ui.picker.models.MediaType
import com.ribbit.ui.venues.details.VenueDetailsActivity
import com.ribbit.ui.videoplayer.VideoPlayerActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import com.ribbit.utils.PermissionUtils
import kotlinx.android.synthetic.main.activity_chat.*
import permissions.dispatcher.*
import java.io.File

@RuntimePermissions
abstract class ChatActivity : BaseActivity(), ChatAdapter.Callback, ChatAdapter.ActionCallback, MediaFragment.MediaCallback {
    companion object {
        private const val EXTRA_FLAG = "EXTRA_FLAG"

        private const val EXTRA_VENUE = "EXTRA_VENUE"
        private const val EXTRA_INDIVIDUAL_CHAT = "EXTRA_INDIVIDUAL_CHAT"
        private const val EXTRA_GROUP_CHAT = "EXTRA_GROUP_CHAT"

        fun getStartIntent(context: Context, venue: VenueDto, flag: Int): Intent {
            return Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_VENUE, venue)
        }

        fun getStartIntentForIndividualChat(context: Context, userCrossed: UserCrossedDto, flag: Int): Intent {
            return Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_INDIVIDUAL_CHAT, userCrossed)
        }

        fun getStartIntentForGroupChat(context: Context, group: GroupDto, flag: Int): Intent {
            return Intent(context, ChatActivity::class.java)
                    .putExtra(EXTRA_FLAG, flag)
                    .putExtra(EXTRA_GROUP_CHAT, group)
        }
    }

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter
    /*private lateinit var mediaPicker: MediaPicker*/
    private val flag by lazy { intent.getIntExtra(EXTRA_FLAG, AppConstants.REQ_CODE_INDIVIDUAL_CHAT) }
    private var type = ""
    private lateinit var viewModelIndividual: ChatIndividualViewModel
    private lateinit var viewModelChatIndividual: ChatListIndividualViewModel
    private lateinit var viewModelChatGroup: ChatListGroupViewModel
    private lateinit var viewModelGroup: ChatGroupViewModel
    private lateinit var conversationId: String

    private val newMessageObserver = Observer<ChatMessageDto> {
        it ?: return@Observer
        setResult(Activity.RESULT_OK)
        tvLabelEmptyChat.gone()
        adapter.addNewMessage(it)
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    private val deleteMessageObserver = Observer<ChatDeleteDto> {
        it ?: return@Observer
        setResult(Activity.RESULT_OK)
        adapter.removeMessage(it)
        if (adapter.itemCount == 0) {
            tvLabelEmptyChat.visible()
        } else {
            tvLabelEmptyChat.gone()
        }
        rvChat.scrollToPosition(adapter.itemCount - 1)
    }

    private val oldMessagesObserver = Observer<Resource<PagingResult<List<ChatMessageDto>>>> {
        it ?: return@Observer
        when (it.status) {
            Status.SUCCESS -> {
                swipeRefreshLayout.isRefreshing = false
                val items = it.data?.result ?: emptyList()
                if (items.isNotEmpty())
                    tvLabelEmptyChat.visibility = View.GONE
                adapter.addOldMessages(items)
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
                tvLabelEmptyChat.visibility = View.GONE
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
                tvLabelEmptyChat.visibility = View.GONE
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
        inItClasses()
    }

    private fun inItClasses() {
        toolbar()
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                val venue = intent.getParcelableExtra<VenueDto>(EXTRA_VENUE)
                viewModel = ViewModelProviders.of(this)[ChatViewModel::class.java]
                if (venue != null) {
                    viewModel.start(venue)
                }
                type = "VENUE"
                if (venue != null) {
                    setupToolbar(venue)
                }
                if (venue != null) {
                    conversationId = venue.conversationId ?: ""
                }
            }
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                val userCrossed = intent.getParcelableExtra<UserCrossedDto>(EXTRA_INDIVIDUAL_CHAT)
                viewModelIndividual = ViewModelProviders.of(this)[ChatIndividualViewModel::class.java]
                if (userCrossed != null) {
                    viewModelIndividual.start(userCrossed)
                }
                type = "INDIVIDUAL"
                if (userCrossed != null) {
                    setupToolbar(userCrossed)
                }
                if (userCrossed != null) {
                    conversationId = userCrossed.conversationId ?: ""
                }
            }
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                val userCrossed = intent.getParcelableExtra<UserCrossedDto>(EXTRA_INDIVIDUAL_CHAT)
                viewModelChatIndividual = ViewModelProviders.of(this)[ChatListIndividualViewModel::class.java]
                if (userCrossed != null) {
                    viewModelChatIndividual.start(userCrossed)
                }
                type = "INDIVIDUAL"
                if (userCrossed != null) {
                    setupToolbarProfile(userCrossed)
                }
                if (userCrossed != null) {
                    conversationId = userCrossed.conversationId ?: ""
                }
            }
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                val userCrossed = intent.getParcelableExtra<UserCrossedDto>(EXTRA_INDIVIDUAL_CHAT)
                viewModelChatGroup = ViewModelProviders.of(this)[ChatListGroupViewModel::class.java]
                if (userCrossed != null) {
                    viewModelChatGroup.start(userCrossed)
                }
                type = "GROUP"
                if (userCrossed != null) {
                    setupToolbarProfile(userCrossed)
                }
                conversationId = userCrossed?.conversationId ?: ""
            }
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                val group = intent.getParcelableExtra<GroupDto>(EXTRA_GROUP_CHAT)
                viewModelGroup = ViewModelProviders.of(this)[ChatGroupViewModel::class.java]
                if (group != null) {
                    viewModelGroup.start(group)
                }
                type = "GROUP"
                if (group != null) {
                    setupToolbar(group)
                }
                conversationId = group?.conversationId ?: ""
            }
        }

        UserManager.saveConversationId(conversationId)
        /*mediaPicker = MediaPicker(this)
        mediaPicker.setAllowVideo(true)*/
        setListeners(flag)
        observeChanges(flag)
        setupChatRecycler(flag)
        getOldMessages(flag)
    }

    private fun setListeners(flag: Int) {
        btnAttachment.setOnClickListener { showImagePickerWithPermissionCheck() }
        fabSend.setOnClickListener { sendTextMessage(flag) }
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                /*mediaPicker.setImagePickerListener { imageFile ->
                    if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                        viewModel.sendImageMessage(imageFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                    }
                }
                mediaPicker.setVideoPickerListener { videoFile ->
                    if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                        viewModel.sendVideoMessage(videoFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                    }
                }*/
                ivVenue.setOnClickListener { showVenueDetails() }
                tvVenueName.setOnClickListener { showVenueDetails() }
            }
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                /*mediaPicker.setImagePickerListener { imageFile ->
                    if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                        viewModelIndividual.sendImageMessage(imageFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                    }
                }
                mediaPicker.setVideoPickerListener { videoFile ->
                    if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                        viewModelIndividual.sendVideoMessage(videoFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                    }
                }*/
                ivVenue.setOnClickListener { }
                tvVenueName.setOnClickListener {}
            }
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                /*mediaPicker.setImagePickerListener { imageFile ->
                    if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                        viewModelChatIndividual.sendImageMessage(imageFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                    }
                }
                mediaPicker.setVideoPickerListener { videoFile ->
                    if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                        viewModelChatIndividual.sendVideoMessage(videoFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                    }
                }*/
                ivVenue.setOnClickListener { openUserProfile() }
                tvVenueName.setOnClickListener {
                    openUserProfile()
                }
            }
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                /*mediaPicker.setImagePickerListener { imageFile ->
                    if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                        viewModelChatGroup.sendImageMessage(imageFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                    }
                }
                mediaPicker.setVideoPickerListener { videoFile ->
                    if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                        viewModelChatGroup.sendVideoMessage(videoFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                    }
                }*/
                ivVenue.setOnClickListener { showGroupDetails(AppConstants.REQ_CODE_LISTING_GROUP_DETAILS) }
                tvVenueName.setOnClickListener { showGroupDetails(AppConstants.REQ_CODE_LISTING_GROUP_DETAILS) }
            }
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                /*mediaPicker.setImagePickerListener { imageFile ->
                    if (imageFile.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                        viewModelGroup.sendImageMessage(imageFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                    }
                }
                mediaPicker.setVideoPickerListener { videoFile ->
                    if (videoFile.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                        viewModelGroup.sendVideoMessage(videoFile)
                    } else {
                        AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                    }
                }*/
                ivVenue.setOnClickListener { showGroupDetails(AppConstants.REQ_CODE_GROUP_DETAILS) }
                tvVenueName.setOnClickListener { showGroupDetails(AppConstants.REQ_CODE_GROUP_DETAILS) }
            }
        }
    }

    private fun openUserProfile() {
        val data = intent.getParcelableExtra<UserCrossedDto>(EXTRA_INDIVIDUAL_CHAT)
        val intent = data?.let {
            PeopleDetailsActivity.getStartIntent(this, it,
                AppConstants.REQ_CODE_BLOCK_USER, data?.profile?.id ?: "")
        }
        startActivity(intent)
    }

    private fun observeChanges(flag: Int) {
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                viewModel.newMessage.observeForever(newMessageObserver)
                viewModel.oldMessages.observeForever(oldMessagesObserver)
                viewModel.sendMessage.observeForever(sendMessageObserver)
                viewModel.uploadFile.observeForever(uploadFileObserver)
                viewModel.deleteMessage.observeForever(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                viewModelIndividual.newMessage.observeForever(newMessageObserver)
                viewModelIndividual.oldMessages.observeForever(oldMessagesObserver)
                viewModelIndividual.sendMessage.observeForever(sendMessageObserver)
                viewModelIndividual.uploadFile.observeForever(uploadFileObserver)
                viewModelIndividual.deleteMessage.observeForever(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                viewModelChatIndividual.newMessage.observeForever(newMessageObserver)
                viewModelChatIndividual.oldMessages.observeForever(oldMessagesObserver)
                viewModelChatIndividual.sendMessage.observeForever(sendMessageObserver)
                viewModelChatIndividual.uploadFile.observeForever(uploadFileObserver)
                viewModelChatIndividual.deleteMessage.observeForever(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                viewModelChatGroup.newMessage.observeForever(newMessageObserver)
                viewModelChatGroup.oldMessages.observeForever(oldMessagesObserver)
                viewModelChatGroup.sendMessage.observeForever(sendMessageObserver)
                viewModelChatGroup.uploadFile.observeForever(uploadFileObserver)
                viewModelChatGroup.deleteMessage.observeForever(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                viewModelGroup.newMessage.observeForever(newMessageObserver)
                viewModelGroup.oldMessages.observeForever(oldMessagesObserver)
                viewModelGroup.sendMessage.observeForever(sendMessageObserver)
                viewModelGroup.uploadFile.observeForever(uploadFileObserver)
                viewModelGroup.deleteMessage.observeForever(deleteMessageObserver)
            }
        }
    }

    private fun setupChatRecycler(flag: Int) {
        swipeRefreshLayout.isEnabled = false
        adapter = ChatAdapter(this, this, this)
        rvChat.adapter = adapter
        (rvChat.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvChat.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                when (flag) {
                    AppConstants.REQ_CODE_VENUE_CHAT -> {
                        if (!rvChat.canScrollVertically(-1) && viewModel.isValidForPaging())
                            viewModel.getOldMessages()
                    }
                    AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                        if (!rvChat.canScrollVertically(-1) && viewModelIndividual.isValidForPaging())
                            viewModelIndividual.getOldMessages()
                    }
                    AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                        if (!rvChat.canScrollVertically(-1) && viewModelChatIndividual.isValidForPaging())
                            viewModelChatIndividual.getOldMessages()
                    }
                    AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                        if (!rvChat.canScrollVertically(-1) && viewModelChatGroup.isValidForPaging())
                            viewModelChatGroup.getOldMessages()
                    }
                    AppConstants.REQ_CODE_GROUP_CHAT -> {
                        if (!rvChat.canScrollVertically(-1) && viewModelGroup.isValidForPaging())
                            viewModelGroup.getOldMessages()
                    }
                }
            }
        })
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            setDisplayShowTitleEnabled(false)
        }
    }

    private fun setupToolbar(venue: VenueDto) {
        GlideApp.with(this)
                .load(venue.imageUrl?.thumbnail)
                .into(ivVenue)
        tvVenueName.text = venue.name
    }

    private fun setupToolbar(group: GroupDto) {
        GlideApp.with(this)
                .load(group.imageUrl?.thumbnail)
                .into(ivVenue)
        tvVenueName.text = group.name
    }

    private fun setupToolbar(userCrossed: UserCrossedDto) {
        GlideApp.with(this)
                .load(userCrossed.crossedUser?.image?.thumbnail)
                .into(ivVenue)
        tvVenueName.text = userCrossed.crossedUser?.userName
    }

    private fun setupToolbarProfile(userCrossed: UserCrossedDto) {
        GlideApp.with(this)
                .load(userCrossed.profile?.image?.thumbnail)
                .into(ivVenue)
        tvVenueName.text = userCrossed.profile?.userName
    }

    private fun getOldMessages(flag: Int) {
        if (isNetworkActiveWithMessage()) {
            when (flag) {
                AppConstants.REQ_CODE_VENUE_CHAT -> viewModel.getOldMessages()
                AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> viewModelIndividual.getOldMessages()
                AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> viewModelChatIndividual.getOldMessages()
                AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> viewModelChatGroup.getOldMessages()
                AppConstants.REQ_CODE_GROUP_CHAT -> viewModelGroup.getOldMessages()
            }
        }
    }

    private fun sendTextMessage(flag: Int) {
        val message = etMessage.text.toString().trim()
        if (message.isNotBlank() && isNetworkActiveWithMessage()) {
            etMessage.setText("")
            when (flag) {
                AppConstants.REQ_CODE_VENUE_CHAT -> viewModel.sendTextMessage(message)
                AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> viewModelIndividual.sendTextMessage(message)
                AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> viewModelChatIndividual.sendTextMessage(message)
                AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> viewModelChatGroup.sendTextMessage(message)
                AppConstants.REQ_CODE_GROUP_CHAT -> viewModelGroup.sendTextMessage(message)
            }
        }
    }

    private fun showVenueDetails() {
        if (viewModel.isVenueDetailsLoaded()) {
            val intent = VenueDetailsActivity.getStartIntent(this, viewModel.getVenue(), viewModel.getMembers())
            startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_DETAILS)
        }
    }

    private fun showGroupDetails(flag: Int) {
        val id = when (flag) {
            AppConstants.REQ_CODE_LISTING_GROUP_DETAILS ->
                viewModelChatGroup.getGroup().profile?.id ?: ""
            AppConstants.REQ_CODE_GROUP_DETAILS ->
                viewModelGroup.getGroup().id ?: ""
            else -> ""
        }
        val intent = GroupDetailsActivity.getStartIntent(this, id, flag)
        startActivityForResult(intent, flag)
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
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> viewModel.resendMessage(chatMessage)
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> viewModelIndividual.resendMessage(chatMessage)
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> viewModelChatIndividual.resendMessage(chatMessage)
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> viewModelChatGroup.resendMessage(chatMessage)
            AppConstants.REQ_CODE_GROUP_CHAT -> viewModelGroup.resendMessage(chatMessage)
        }
    }

    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showImagePicker() {
        /*mediaPicker.show()*/
        val mediaFragment = MediaFragment.newInstance(1)
        mediaFragment.setListeners(this)
        mediaFragment.show(supportFragmentManager, MediaFragment.TAG)
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

    fun onOptionsItemSelected(item: MenuItem?): Boolean? {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> item?.let { super.onOptionsItemSelected(it) }
        }
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onResume() {
        super.onResume()
        UserManager.saveConversationId(conversationId)
    }

    override fun onPause() {
        super.onPause()
        UserManager.removeConversationId()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppConstants.REQ_CODE_VENUE_DETAILS -> {
                    if (data != null && data.hasExtra(AppConstants.EXTRA_VENUE) &&
                            data.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE) != null) {
                        val venue = data.getParcelableExtra<VenueDto>(AppConstants.EXTRA_VENUE)
                        if (venue != null) {
                            if (venue.isMember == false) {
                                // Will be false if user has exit the venue
                                setResult(Activity.RESULT_OK, data)
                                finish()
                            } else {
                                // Otherwise update the venue
                                if (venue != null) {
                                    viewModel.updateVenue(venue)
                                }
                            }
                        }
                    }
                }

                AppConstants.REQ_CODE_LISTING_GROUP_DETAILS -> {
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                AppConstants.REQ_CODE_GROUP_DETAILS -> {
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                /*else -> {
                mediaPicker.onActivityResult(requestCode, resultCode, data)
                }*/
            }
        }
    }

    private fun removeObserver(flag: Int) {
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                viewModel.newMessage.removeObserver(newMessageObserver)
                viewModel.oldMessages.removeObserver(oldMessagesObserver)
                viewModel.sendMessage.removeObserver(sendMessageObserver)
                viewModel.uploadFile.removeObserver(uploadFileObserver)
                viewModel.deleteMessage.removeObserver(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                viewModelIndividual.newMessage.removeObserver(newMessageObserver)
                viewModelIndividual.oldMessages.removeObserver(oldMessagesObserver)
                viewModelIndividual.sendMessage.removeObserver(sendMessageObserver)
                viewModelIndividual.uploadFile.removeObserver(uploadFileObserver)
                viewModelIndividual.deleteMessage.removeObserver(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                viewModelChatIndividual.newMessage.removeObserver(newMessageObserver)
                viewModelChatIndividual.oldMessages.removeObserver(oldMessagesObserver)
                viewModelChatIndividual.sendMessage.removeObserver(sendMessageObserver)
                viewModelChatIndividual.uploadFile.removeObserver(uploadFileObserver)
                viewModelChatIndividual.deleteMessage.removeObserver(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                viewModelChatGroup.newMessage.removeObserver(newMessageObserver)
                viewModelChatGroup.oldMessages.removeObserver(oldMessagesObserver)
                viewModelChatGroup.sendMessage.removeObserver(sendMessageObserver)
                viewModelChatGroup.uploadFile.removeObserver(uploadFileObserver)
                viewModelChatGroup.deleteMessage.removeObserver(deleteMessageObserver)
            }
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                viewModelGroup.newMessage.removeObserver(newMessageObserver)
                viewModelGroup.oldMessages.removeObserver(oldMessagesObserver)
                viewModelGroup.sendMessage.removeObserver(sendMessageObserver)
                viewModelGroup.uploadFile.removeObserver(uploadFileObserver)
                viewModelGroup.deleteMessage.removeObserver(deleteMessageObserver)
            }
        }
    }

    // Screen touch keyboard close
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE)
                && view is EditText && !view.javaClass.name.startsWith("android.webkit.")) {
            val scrooges = IntArray(2)
            view.getLocationOnScreen(scrooges)
            val x = ev.rawX + view.left - scrooges[0]
            val y = ev.rawY + view.top - scrooges[1]
            if (x < view.left || x > view.right || y < view.top || y > view.bottom)
                window.decorView.rootView.hideKeyboard()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeObserver(flag)
        /*mediaPicker.clear()*/
    }

    override fun onDeleteImage(chatMessage: ChatMessageDto) {
        when (flag) {
            AppConstants.REQ_CODE_VENUE_CHAT -> {
                viewModel.deleteMessage(chatMessage)
            }
            AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                viewModelIndividual.deleteMessage(chatMessage)
            }
            AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                viewModelChatIndividual.deleteMessage(chatMessage)
            }
            AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                viewModelChatGroup.deleteMessage(chatMessage)
            }
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                viewModelGroup.deleteMessage(chatMessage)
            }
        }
        adapter.removeMsgPosition(chatMessage)
        if (adapter.itemCount == 0) {
            tvLabelEmptyChat.visible()
        } else {
            tvLabelEmptyChat.gone()
        }
    }

    override fun onImageShow(chatMessage: ChatMessageDto) {
        onImageMessageClicked(chatMessage)
    }

    override fun onVideoShow(chatMessage: ChatMessageDto) {
        onVideoMessageClicked(chatMessage)
    }

    override fun captureCallback(file: MediaSelected) {
        sendMedia(file)
    }

    override fun selectFromGalleryCallback(files: List<MediaSelected>) {
        files.forEach { file -> sendMedia(file) }
    }

    private fun sendMedia(file: MediaSelected) {
        val media = File(file.path)
        when (file.type) {
            MediaType.IMAGE -> {
                if (media.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                    when (flag) {
                        AppConstants.REQ_CODE_VENUE_CHAT -> {
                            viewModel.sendImageMessage(media)
                        }
                        AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                            viewModelIndividual.sendImageMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                            viewModelChatIndividual.sendImageMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                            viewModelChatGroup.sendImageMessage(media)
                        }
                        AppConstants.REQ_CODE_GROUP_CHAT -> {
                            viewModelGroup.sendImageMessage(media)
                        }
                    }
                } else {
                    AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                }
            }
            MediaType.GIF -> {
                if (media.length() < AppConstants.MAXIMUM_IMAGE_SIZE) {
                    when (flag) {
                        AppConstants.REQ_CODE_VENUE_CHAT -> {
                            viewModel.sendGifMessage(media)
                        }
                        AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                            viewModelIndividual.sendGifMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                            viewModelChatIndividual.sendGifMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                            viewModelChatGroup.sendGifMessage(media)
                        }
                        AppConstants.REQ_CODE_GROUP_CHAT -> {
                            viewModelGroup.sendGifMessage(media)
                        }
                    }
                } else {
                    AppToast.longToast(applicationContext, R.string.message_select_smaller_image)
                }
            }
            MediaType.VIDEO -> {
                if (media.length() < AppConstants.MAXIMUM_VIDEO_SIZE) {
                    when (flag) {
                        AppConstants.REQ_CODE_VENUE_CHAT -> {
                            viewModel.sendVideoMessage(media)
                        }
                        AppConstants.REQ_CODE_INDIVIDUAL_CHAT -> {
                            viewModelIndividual.sendVideoMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_INDIVIDUAL_CHAT -> {
                            viewModelChatIndividual.sendVideoMessage(media)
                        }
                        AppConstants.REQ_CODE_LISTING_GROUP_CHAT -> {
                            viewModelChatGroup.sendVideoMessage(media)
                        }
                        AppConstants.REQ_CODE_GROUP_CHAT -> {
                            viewModelGroup.sendVideoMessage(media)
                        }
                    }
                } else {
                    AppToast.longToast(applicationContext, R.string.message_select_smaller_video)
                }
            }
        }
    }
}