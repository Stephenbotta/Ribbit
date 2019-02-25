package com.conversify.ui.groups.groupposts

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.*
import android.view.MenuItem
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.groups.GroupDto
import com.conversify.data.remote.models.groups.GroupPostDto
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActive
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseActivity
import com.conversify.ui.chat.ChatActivity
import com.conversify.ui.groups.PostCallback
import com.conversify.ui.groups.details.GroupDetailsActivity
import com.conversify.ui.post.details.PostDetailsActivity
import com.conversify.ui.post.details.PostDetailsViewModel
import com.conversify.ui.post.newpost.NewPostActivity
import com.conversify.utils.AppConstants
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_group_posts.*
import timber.log.Timber


class GroupPostsActivity : BaseActivity(), PostCallback, PopupMenu.OnMenuItemClickListener {
    companion object {
        private const val EXTRA_GROUP = "EXTRA_GROUP"
        private const val CHILD_POSTS = 0
        private const val CHILD_NO_POSTS = 1

        fun start(context: Context, group: GroupDto): Intent {
            return Intent(context, GroupPostsActivity::class.java)
                    .putExtra(EXTRA_GROUP, group)
        }
    }

    private val groupPostsViewModel by lazy { ViewModelProviders.of(this)[GroupPostsViewModel::class.java] }
    private val postDetailsViewModel by lazy { ViewModelProviders.of(this)[PostDetailsViewModel::class.java] }
    private val group by lazy { intent.getParcelableExtra<GroupDto>(EXTRA_GROUP) }
    private lateinit var postsAdapter: GroupPostsAdapter
    private var groupPostsLoadedOnce = false

    private val postUpdatedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.hasExtra(AppConstants.EXTRA_GROUP_POST)) {
                val updatedPost = intent.getParcelableExtra<GroupPostDto>(AppConstants.EXTRA_GROUP_POST)
                postsAdapter.updatePost(updatedPost)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_posts)

        groupPostsViewModel.start(group)

        swipeRefreshLayout.setOnRefreshListener { getGroupPosts() }
        registerPostUpdatedReceiver()
        setupPostsRecycler()
        displayGroupDetails(group)
        observeChanges()
        listener()
    }

    private fun listener() {
        btnBack.setOnClickListener { onBackPressed() }
        btnMore.setOnClickListener { optionMenu(it) }
    }

    private fun registerPostUpdatedReceiver() {
        val filter = IntentFilter()
        filter.addAction(AppConstants.ACTION_GROUP_POST_UPDATED_POST_DETAILS)
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(postUpdatedReceiver, filter)
    }

    private fun setupPostsRecycler() {
        postsAdapter = GroupPostsAdapter(GlideApp.with(this), this)

        val dividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_recycler)
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable)
        }
        rvPosts.addItemDecoration(dividerItemDecoration)
        (rvPosts.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        rvPosts.adapter = postsAdapter
        rvPosts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1) && groupPostsViewModel.validForPaging() && isNetworkActive()) {
                    groupPostsViewModel.getGroupPosts(false)
                }
            }
        })
    }

    private fun displayGroupDetails(group: GroupDto) {
        tvTitle.text = group.name
        ivFavourite.setImageResource(if (group.isMember == true) {
            R.drawable.ic_star_selected
        } else {
            R.drawable.ic_star_normal
        })
    }

    private fun observeChanges() {
        groupPostsViewModel.posts.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    sendGroupPostsLoadedBroadcast()
                    swipeRefreshLayout.isRefreshing = false
                    val posts = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        postsAdapter.displayPosts(posts)
                    } else {
                        postsAdapter.addPosts(posts)
                    }

                    viewSwitcher.displayedChild = if (postsAdapter.itemCount == 0) {
                        CHILD_NO_POSTS
                    } else {
                        CHILD_POSTS
                    }
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })

        groupPostsViewModel.exitGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    group.isMember = false
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_GROUP, group)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                Status.ERROR -> {
                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })

    }

    private fun getGroupPosts(firstPage: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            groupPostsViewModel.getGroupPosts(firstPage)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun sendGroupPostsLoadedBroadcast() {
        if (!groupPostsLoadedOnce) {
            groupPostsLoadedOnce = true
            val intent = Intent(AppConstants.ACTION_GROUP_POSTS_LOADED)
            intent.putExtra(AppConstants.EXTRA_GROUP, group)
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(intent)
        }
    }

    private fun shareApp() {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
//        # change the type of data you need to share,
//        # for image use "image/*"
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, AppConstants.PLAY_STORE_URL + packageName)
        startActivity(Intent.createChooser(intent, AppConstants.TITLE_SHARE_VIA))
    }

    @SuppressLint("RestrictedApi")
    private fun optionMenu(v: View) {
        val popup = PopupMenu(this, v)
        popup.inflate(R.menu.menu_group_more_options)
        val m = popup.menu as MenuBuilder
        m.setOptionalIconsVisible(true)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onStart() {
        super.onStart()
        getGroupPosts()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {

            R.id.menuGroupExit -> {
                if (isNetworkActiveWithMessage())
                    groupPostsViewModel.exitGroup()
                return true
            }

            R.id.menuGroupShare -> {
                shareApp()
                return true
            }

            R.id.menuGroupChat -> {
                val intent = ChatActivity.getStartIntentForGroupChat(this, group, AppConstants.REQ_CODE_GROUP_CHAT)
                startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_CHAT)
                return true
            }

            R.id.menuCreateNewPost -> {
                val intent = NewPostActivity.getStartIntent(this, group, AppConstants.REQ_CODE_CREATE_NEW_POST)
                startActivityForResult(intent, AppConstants.REQ_CODE_CREATE_NEW_POST)
                return true
            }

            R.id.menuGroupDetail -> {
                val intent = GroupDetailsActivity.getStartIntent(this, group.id!!, AppConstants.REQ_CODE_GROUP_DETAILS_MORE_OPTIONS)
                startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_DETAILS_MORE_OPTIONS)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_GROUP_CHAT -> {
                if (resultCode == Activity.RESULT_OK /*&& data != null*/) {
//                    setResult(Activity.RESULT_OK, data)
//                    finish()
                }
            }
        }
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        Timber.i("User profile clicked : $profile")
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(this, post, focusReplyEditText)
        startActivity(intent)
    }

    override fun onLikesCountClicked(post: GroupPostDto) {
        Timber.i("Likes count clicked for post : $post")
    }

    override fun onGroupPostLikeClicked(groupPost: GroupPostDto, isLiked: Boolean) {
        postDetailsViewModel.likeUnlikePost(groupPost, isLiked)
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(AppConstants.ACTION_GROUP_POST_UPDATED_GROUP_POSTS_LISTING)
                        .putExtra(AppConstants.EXTRA_GROUP_POST, groupPost))
    }

    override fun onHashtagClicked(tag: String) {
        Timber.i("Hash tag clicked : $tag")
    }

    override fun onUsernameMentionClicked(username: String) {
        Timber.i("Username mention clicked : $username")
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(postUpdatedReceiver)
    }
}