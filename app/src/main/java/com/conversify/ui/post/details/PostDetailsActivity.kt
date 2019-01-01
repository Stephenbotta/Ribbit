package com.conversify.ui.post.details

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.SimpleItemAnimator
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.custom.SocialEditText
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_post_details.*
import timber.log.Timber

class PostDetailsActivity : BaseActivity(), PostDetailsAdapter.Callback, UserMentionAdapter.Callback {
    companion object {
        private const val EXTRA_POST = "EXTRA_POST"
        private const val EXTRA_FOCUS_REPLY_EDIT_TEXT = "EXTRA_FOCUS_REPLY_EDIT_TEXT"

        private const val CHILD_MENTIONS = 0
        private const val CHILD_MENTIONS_LOADING = 1

        fun getStartIntent(context: Context, post: GroupPostDto, focusReplyEditText: Boolean = false): Intent {
            val intent = Intent(context, PostDetailsActivity::class.java)
            intent.putExtra(EXTRA_POST, post)
            intent.putExtra(EXTRA_FOCUS_REPLY_EDIT_TEXT, focusReplyEditText)
            return intent
        }
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[PostDetailsViewModel::class.java] }
    private val focusReplyEditText by lazy { intent.getBooleanExtra(EXTRA_FOCUS_REPLY_EDIT_TEXT, false) }
    private lateinit var postDetailsAdapter: PostDetailsAdapter
    private lateinit var userMentionAdapter: UserMentionAdapter
    private var replyingToTopLevelReply: PostReplyDto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        btnBack.setOnClickListener { onBackPressed() }

        val groupPost = intent.getParcelableExtra<GroupPostDto>(EXTRA_POST)
        viewModel.start(groupPost)

        updatePostLikedState(groupPost.isLiked ?: false)
        setupPostRecycler()
        setupUserMentionRecycler()
        setListeners()
        observeChanges()
        setupPostReplyEditText()
        getReplies()
    }

    private fun setupPostRecycler() {
        postDetailsAdapter = PostDetailsAdapter(GlideApp.with(this), this)
        rvPostDetails.adapter = postDetailsAdapter
        (rvPostDetails.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        postDetailsAdapter.displayItems(listOf(viewModel.getPostDetailsHeader()))
    }

    private fun setupUserMentionRecycler() {
        userMentionAdapter = UserMentionAdapter(GlideApp.with(this), this)
        rvUserMentions.adapter = userMentionAdapter
    }

    private fun setListeners() {
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

                    // By default views related to reply edit text are not visible.
                    if (!etReply.isVisible()) {
                        dividerReplyEditText.visible()
                        etReply.visible()
                        ivLikePost.visible()

                        if (focusReplyEditText) {
                            etReply.showKeyboard()
                        }
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

    override fun onUserProfileClicked(profile: ProfileDto) {
    }

    override fun onHashtagClicked(tag: String) {
    }

    override fun onUsernameMentionClicked(username: String) {
    }

    override fun onReplyClicked(reply: PostReplyDto, isTopLevelReply: Boolean) {
        val userName = if (isTopLevelReply) {
            reply.commentBy?.userName
        } else {
            reply.replyBy?.userName
        }

        tvReplyingTo.text = getString(R.string.post_details_label_replying_to_with_username, userName)
        llReplyingTo.visible()
        etReply.setText(String.format("@%s ", userName))
        etReply.setSelection(etReply.text?.length ?: 0)
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
        // todo - Open topic groups and handle post state changes
        /*val intent = TopicGroupsActivity.getStartIntent(this, category)
        startActivity(intent)*/
    }

    override fun onUserMentionClicked(user: ProfileDto) {
        etReply.setTextWithoutTextChangedTrigger("@" + user.userName)
        etReply.setSelection(etReply.text?.length ?: 0)
        viewFlipperUserMentions.gone()
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(AppConstants.ACTION_GROUP_POST_UPDATED_POST_DETAILS)
                        .putExtra(AppConstants.EXTRA_GROUP_POST, viewModel.getGroupPost()))
    }
}