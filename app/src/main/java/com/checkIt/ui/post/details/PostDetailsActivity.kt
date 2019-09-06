package com.checkIt.ui.post.details

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.checkIt.R
import com.checkIt.data.local.UserManager
import com.checkIt.data.remote.models.Status
import com.checkIt.data.remote.models.groups.GroupDto
import com.checkIt.data.remote.models.groups.GroupPostDto
import com.checkIt.data.remote.models.loginsignup.ImageUrlDto
import com.checkIt.data.remote.models.loginsignup.InterestDto
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.people.UserCrossedDto
import com.checkIt.data.remote.models.post.PostReplyDto
import com.checkIt.databinding.BottomSheetDialogDeleteCommentBinding
import com.checkIt.extensions.*
import com.checkIt.ui.base.BaseActivity
import com.checkIt.ui.custom.SocialEditText
import com.checkIt.ui.people.details.PeopleDetailsActivity
import com.checkIt.ui.post.newpost.NewPostActivity
import com.checkIt.ui.profile.ProfileActivity
import com.checkIt.utils.AppConstants
import com.checkIt.utils.GlideApp
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_post_details.*
import timber.log.Timber

class PostDetailsActivity : BaseActivity(), PostDetailsAdapter.Callback, UserMentionAdapter.Callback, PopupMenu.OnMenuItemClickListener {
    companion object {
        private const val EXTRA_POST = "EXTRA_POST"
        private const val EXTRA_FOCUS_REPLY_EDIT_TEXT = "EXTRA_FOCUS_REPLY_EDIT_TEXT"
        private const val EXTRA_MEDIA_ID = "EXTRA_MEDIA_ID"

        private const val CHILD_MENTIONS = 0
        private const val CHILD_MENTIONS_LOADING = 1

        fun getStartIntent(context: Context, post: GroupPostDto, focusReplyEditText: Boolean = false, media: ImageUrlDto? = null): Intent {
            val intent = Intent(context, PostDetailsActivity::class.java)
            intent.putExtra(EXTRA_POST, post)
            intent.putExtra(EXTRA_FOCUS_REPLY_EDIT_TEXT, focusReplyEditText)
            if (media != null)
                intent.putExtra(EXTRA_MEDIA_ID, media)
            return intent
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[PostDetailsViewModel::class.java] }
    private val focusReplyEditText by lazy { intent.getBooleanExtra(EXTRA_FOCUS_REPLY_EDIT_TEXT, false) }
    private lateinit var postDetailsAdapter: PostDetailsAdapter
    private lateinit var userMentionAdapter: UserMentionAdapter
    private var replyingToTopLevelReply: PostReplyDto? = null
    private lateinit var groupPost: GroupPostDto
    private val ownUserId by lazy { UserManager.getUserId() }
    private var media: ImageUrlDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        btnBack.setOnClickListener { onBackPressed() }

        groupPost = intent.getParcelableExtra(EXTRA_POST)

        if (intent.hasExtra(EXTRA_MEDIA_ID))
            media = intent.getParcelableExtra(EXTRA_MEDIA_ID)

        val groupName = groupPost.group?.name ?: ""
        val categoryName = String.format("[%s]", groupPost.category?.name ?: "")

        if (groupPost.user?.id == ownUserId && media == null) {
            btnMore.visible()
        } else {
            btnMore.gone()
        }

        if (media != null) {
            groupPost.isLiked = media?.isLiked
        }
        viewModel.start(groupPost, media)

        if (groupName.isNotBlank()) {
            btnBack.text = String.format("%s %s", groupName, categoryName)
        }

        updatePostLikedState(groupPost.isLiked ?: false)
        setupPostRecycler()
        setupUserMentionRecycler()
        setListeners()
        observeChanges()
        setupPostReplyEditText()
        getReplies()
    }

    private fun setupPostRecycler() {
        postDetailsAdapter = PostDetailsAdapter(GlideApp.with(this), media, this)
        rvPostDetails.adapter = postDetailsAdapter
        (rvPostDetails.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        postDetailsAdapter.displayHeader(listOf(viewModel.getPostDetailsHeader()))
    }

    private fun setupUserMentionRecycler() {
        userMentionAdapter = UserMentionAdapter(GlideApp.with(this), this)
        rvUserMentions.adapter = userMentionAdapter
    }

    private fun setListeners() {
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            getReplies()
        }
        ivLikePost.setOnClickListener {
            if (isNetworkActive()) {
                val isLiked = !viewModel.isPostLiked()
                viewModel.likeUnlikePost(isLiked)
                updatePostLikedState(isLiked)
                postDetailsAdapter.notifyHeaderChanged()
            }
        }

        ivCloseReplyingTo.setOnClickListener {
            llReplyingTo.gone()
            etReply.setText("")
            replyingToTopLevelReply = null
        }

        fabSendReply.setOnClickListener {
            if (isNetworkActive()) {
                val topLevelReply = replyingToTopLevelReply
                if (topLevelReply == null) {
                    viewModel.addPostReply(etReply.text.toString())
                } else {
                    viewModel.addPostSubReply(etReply.text.toString(), topLevelReply)
                    replyingToTopLevelReply = null
                }
                etReply.setText("")
                etReply.clearFocus()
                etReply.hideKeyboard()
                llReplyingTo.gone()
            }
        }

        btnMore.setOnClickListener { optionMenu(btnMore) }
    }

    private fun observeChanges() {
        viewModel.replies.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val replies = resource.data?.result ?: emptyList()
                    postDetailsAdapter.setLoading(false)
                    postDetailsAdapter.addItems(replies)
                    if (resource.data?.isFirstPage == true) {
                        postDetailsAdapter.notifyHeaderChanged()
                    }

                    Handler().postDelayed({
                        rvPostDetails?.smoothScrollToPosition(postDetailsAdapter.itemCount - 1)
                    }, 500)
                    // By default views related to reply edit text are not visible.
                    if (!etReply.isVisible()) {
                        dividerReplyEditText.gone()
                        etReply.visible()
                        ivLikePost.visible()
                    }
                    if (focusReplyEditText) {
                        etReply.showKeyboard()
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    postDetailsAdapter.setLoading(false)
                }

                Status.LOADING -> {
                    postDetailsAdapter.setLoading(true)
                }
            }
        })

        viewModel.subReplies.observe(this, Observer { resource ->
            val subReply = resource?.data ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    postDetailsAdapter.displaySubReplies(subReply)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    postDetailsAdapter.notifyParentReplyChange(subReply.parentReply)
                }

                Status.LOADING -> {
                    postDetailsAdapter.notifyParentReplyChange(subReply.parentReply)
                }
            }
        })

        viewModel.addPostReply.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { newReply ->
                        postDetailsAdapter.addReply(newReply)
                        postDetailsAdapter.notifyHeaderChanged()
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

        viewModel.addPostSubReply.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.let { newSubReply ->
                        postDetailsAdapter.addSubReply(newSubReply)
                        postDetailsAdapter.notifyHeaderChanged()
                    }
                }

                Status.ERROR -> {
                    handleError(resource.error)
                }

                Status.LOADING -> {
                }
            }
        })

        viewModel.mentionSuggestions.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    val mentions = resource.data ?: emptyList()
                    userMentionAdapter.displayMentions(mentions)
                    viewFlipperUserMentions.visible()
                    viewFlipperUserMentions.displayedChild = CHILD_MENTIONS
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    viewFlipperUserMentions.gone()
                }

                Status.LOADING -> {
                    viewFlipperUserMentions.visible()
                    viewFlipperUserMentions.displayedChild = CHILD_MENTIONS_LOADING
                }
            }
        })

        viewModel.deletePost.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    finish()
                }

                Status.ERROR -> {
                    // Ignored
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })

        viewModel.deleteCommentReply.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    getReplies()
                }

                Status.ERROR -> {
                    // Ignored
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    // Ignored
                }
            }
        })

    }

    private fun optionMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.menu_post_more)
        when (groupPost.postType ?: "") {
            AppConstants.POST_TYPE_REGULAR -> {
                popup.menu.getItem(1).isVisible = true
            }
        }
//        val m = popup.menu as MenuBuilder     //  visible the icon for menu
//        m.setOptionalIconsVisible(true)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_EDIT_POST -> {
                if (resultCode == Activity.RESULT_OK) {
                    getReplies()
                }
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.deletePost -> {
                viewModel.deletePost(groupPost.id ?: "")
                true
            }
            R.id.editPost -> {
                val intent = NewPostActivity.getStartIntentForEdit(this,
                        viewModel.getGroupPost(), AppConstants.REQ_CODE_EDIT_POST)
                startActivityForResult(intent, AppConstants.REQ_CODE_EDIT_POST)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupPostReplyEditText() {
        etReply.setTextChangedListener(object : SocialEditText.OnTextChangedListener {
            override fun onTextChanged(text: String) {
                if (text.isBlank()) {
                    ivLikePost.visible()
                    fabSendReply.hide()
                } else {
                    ivLikePost.gone()
                    fabSendReply.show()
                }
            }
        })

        etReply.setSuggestionListener(object : SocialEditText.SuggestionListener {
            override fun onMentionSuggestionReceived(mentionText: String) {
                Timber.i("Mention suggestion received : $mentionText")
                if (isNetworkActive()) {
                    viewModel.getMentionSuggestions(mentionText)
                } else {
                    viewModel.cancelGetMentionSuggestions()
                    viewFlipperUserMentions.gone()
                }
            }

            override fun onHashtagSuggestionReceived(hashtagText: String) {
                Timber.i("Hashtag suggestion received : $hashtagText")
                viewModel.cancelGetMentionSuggestions()
                viewFlipperUserMentions.gone()
            }

            override fun onSuggestionQueryCleared() {
                Timber.i("Mention suggestion removed")
                viewModel.cancelGetMentionSuggestions()
                viewFlipperUserMentions.gone()
            }
        })
    }

    private fun getReplies() {
        if (isNetworkActiveWithMessage()) {
            viewModel.getPostWithReplies()
        }
    }

    private fun updatePostLikedState(isLiked: Boolean) {
        ivLikePost.setImageResource(if (isLiked) {
            R.drawable.ic_heart_selected
        } else {
            R.drawable.ic_heart_normal
        })
    }

    override fun onLikesCountClicked(post: GroupPostDto) {
    }

    override fun onLikesCountClicked(reply: PostReplyDto) {
    }

    override fun onGroupClicked(group: GroupDto) {
    }

    override fun onLongPress(parentReply: PostReplyDto) {
        val map = hashMapOf<String, String>()
        when (ownUserId) {
            groupPost.user?.id -> {
                if (parentReply.comment == null) {
                    map["replyId"] = parentReply.id ?: ""
                } else if (parentReply.reply == null) {
                    map["commentId"] = parentReply.id ?: ""
                }
            }
            parentReply.commentBy?.id -> {
                map["commentId"] = parentReply.id ?: ""
            }
            parentReply.replyBy?.id -> {
                map["replyId"] = parentReply.id ?: ""
            }
        }
        if (map.isNotEmpty())
            bottomSheet(map)
    }

    private fun deleteComment(hashMap: HashMap<String, String>?) {
        viewModel.deleteCommentReply(hashMap)
    }

    private fun bottomSheet(hashMap: HashMap<String, String>?) {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogDeleteCommentBinding>(inflater, R.layout.bottom_sheet_dialog_delete_comment, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvDelete.setOnClickListener {
            deleteComment(hashMap)
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        val data = UserCrossedDto()
        data.profile = profile
        if (profile.id == ownUserId) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data,
                    AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
            startActivity(intent)
        }
    }

    override fun onHashTagClicked(tag: String) {
    }

    override fun onUsernameMentionClicked(username: String) {
    }

    override fun onReplyClicked(reply: PostReplyDto, isTopLevelReply: Boolean) {
        val userName = if (isTopLevelReply) {
            reply.commentBy?.userName
        } else {
            reply.replyBy?.userName
        }

        // Show replying to view and update the text with the username
        tvReplyingTo.text = getString(R.string.post_details_label_replying_to_with_username, userName)
        llReplyingTo.visible()

        // Set selection to the end and show keyboard
        etReply.setTextWithoutTextChangedTrigger(String.format("@%s ", userName))
        etReply.setSelectionAtEnd()
        etReply.showKeyboard()

        replyingToTopLevelReply = if (isTopLevelReply) {
            reply
        } else {
            postDetailsAdapter.getReply(reply.parentReplyId ?: "")
        }
    }

    override fun onLoadRepliesClicked(parentReply: PostReplyDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getSubReplies(parentReply)
        }
    }

    override fun onShowAllRepliesClicked(parentReply: PostReplyDto) {
        postDetailsAdapter.showAllSubReplies(parentReply)
    }

    override fun onHideAllRepliesClicked(parentReply: PostReplyDto) {
        postDetailsAdapter.hideAllSubReplies(parentReply)
    }

    override fun onLikeReplyClicked(reply: PostReplyDto, isLiked: Boolean, topLevelReply: Boolean) {
        viewModel.likeUnlikeReply(reply, isLiked, topLevelReply)
    }

    override fun onGroupCategoryClicked(category: InterestDto) {
        /*val intent = TopicGroupsActivity.getStartIntent(this, category)
        startActivity(intent)*/
    }

    override fun onUserMentionSuggestionClicked(user: ProfileDto) {
        etReply.updateMentionBeforeCursor(user.userName + " ")
        viewFlipperUserMentions.gone()
    }

    override fun onUserMentionLongPressed(user: ProfileDto) {
        shortToast(user.userName ?: "")
    }

    private fun bottomSheet(user: ProfileDto) {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<BottomSheetDialogDeleteCommentBinding>(inflater, R.layout.bottom_sheet_dialog_delete_comment, null, false)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(binding.root)
        bottomSheetDialog.show()
        binding.tvDelete.setOnClickListener {
            deleteReply(user)
            bottomSheetDialog.dismiss()
        }
        binding.tvCancel.setOnClickListener { bottomSheetDialog.dismiss() }
    }

    private fun deleteReply(user: ProfileDto) {
        val map = hashMapOf<String, String>()
        map["replyId"] = user.id ?: ""
        viewModel.deleteCommentReply(map)
    }

    override fun onBackPressed() {
        if (viewFlipperUserMentions.isVisible()) {
            viewModel.cancelGetMentionSuggestions()
            viewFlipperUserMentions.gone()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (media == null) {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(Intent(AppConstants.ACTION_GROUP_POST_UPDATED_POST_DETAILS)
                            .putExtra(AppConstants.EXTRA_GROUP_POST, viewModel.getGroupPost()))
        } else {
            setResult(Activity.RESULT_OK)
        }
    }
}