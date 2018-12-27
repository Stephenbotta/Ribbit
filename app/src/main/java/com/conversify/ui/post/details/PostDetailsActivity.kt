package com.conversify.ui.post.details

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.SimpleItemAnimator
import android.text.Editable
import android.text.TextWatcher
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.data.remote.models.post.PostReplyDto
import com.conversify.extensions.*
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_post_details.*

class PostDetailsActivity : BaseActivity(), PostDetailsAdapter.Callback {
    companion object {
        private const val EXTRA_POST = "EXTRA_POST"
        private const val EXTRA_FOCUS_REPLY_EDIT_TEXT = "EXTRA_FOCUS_REPLY_EDIT_TEXT"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details)

        btnBack.setOnClickListener { onBackPressed() }

        val groupPost = intent.getParcelableExtra<GroupPostDto>(EXTRA_POST)
        viewModel.start(groupPost)

        updatePostLikedState(groupPost.isLiked ?: false)
        setupPostRecycler()
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

    private fun setListeners() {
        ivLikePost.setOnClickListener {
            if (isNetworkActive()) {
                val isLiked = !viewModel.isPostLiked()
                viewModel.likeUnlikePost(isLiked)
                updatePostLikedState(isLiked)
                postDetailsAdapter.notifyHeaderChanged()
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

                    // By default both are not visible.
                    if (!etReply.isVisible()) {
                        etReply.visible()
                        ivLikePost.visible()

                        if (focusReplyEditText) {
                            etReply.requestFocus()
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
                        postDetailsAdapter.addItem(newReply)
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
    }

    private fun setupPostReplyEditText() {
        etReply.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                if (text.isNullOrBlank()) {
                    ivLikePost.visible()
                    fabSendReply.hide()
                } else {
                    ivLikePost.gone()
                    fabSendReply.show()
                }
            }
        })

        fabSendReply.setOnClickListener {
            if (isNetworkActive()) {
                viewModel.addPostReply(etReply.text.toString())
                etReply.setText("")
                etReply.clearFocus()
                etReply.hideKeyboard()
            }
        }
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

    override fun onReplyClicked(reply: PostReplyDto) {
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(AppConstants.ACTION_GROUP_POSTS_UPDATED)
                        .putExtra(AppConstants.EXTRA_GROUP_POST, viewModel.getGroupPost()))
    }
}