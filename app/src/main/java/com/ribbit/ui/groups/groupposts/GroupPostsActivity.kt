package com.ribbit.ui.groups.groupposts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ribbit.R
import com.ribbit.data.local.UserManager
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ImageUrlDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.handleError
import com.ribbit.extensions.hideKeyboard
import com.ribbit.extensions.isNetworkActive
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseActivity
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.groups.PostCallback
import com.ribbit.ui.groups.details.GroupDetailsActivity
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.post.details.PostDetailsActivity
import com.ribbit.ui.post.details.PostDetailsViewModel
import com.ribbit.ui.post.newpost.NewPostActivity
import com.ribbit.ui.profile.ProfileActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
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
    private lateinit var loadingDialog: LoadingDialog

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
        loadingDialog = LoadingDialog(this)
        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            getGroupPosts()
        }
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

        val dividerItemDecoration = androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL)
        val dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_recycler)
        if (dividerDrawable != null) {
            dividerItemDecoration.setDrawable(dividerDrawable)
        }
        rvPosts.addItemDecoration(dividerItemDecoration)
        (rvPosts.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false
        rvPosts.adapter = postsAdapter
        rvPosts.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
//                    swipeRefreshLayout.isRefreshing = false
                    loadingDialog.setLoading(false)
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
                    loadingDialog.setLoading(false)
//                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
//                    swipeRefreshLayout.isRefreshing = true
                    loadingDialog.setLoading(true)
                }
            }
        })

        groupPostsViewModel.exitGroup.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
//                    swipeRefreshLayout.isRefreshing = false
                    group.isMember = false
                    val data = Intent()
                    data.putExtra(AppConstants.EXTRA_GROUP, group)
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
//                    swipeRefreshLayout.isRefreshing = false
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
//                    swipeRefreshLayout.isRefreshing = true
                }
            }
        })


        postDetailsViewModel.addPostReply.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    loadingDialog.setLoading(false)
                    groupPostsViewModel.getGroupPosts(true)
                }

                Status.ERROR -> {
                    handleError(resource.error)
                    loadingDialog.setLoading(false)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
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
        m.getItem(0).title = if (group.adminId == UserManager.getUserId()) {
            getString(R.string.group_more_options_label_channel_delete)
        } else {
            getString(R.string.group_more_options_label_exit)
        }
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
                showDialog()
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
                val intent = GroupDetailsActivity.getStartIntent(this, group.id
                        ?: "", AppConstants.REQ_CODE_GROUP_DETAILS_MORE_OPTIONS)
                startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_DETAILS_MORE_OPTIONS)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun showDialog() {
        val message = if (group.adminId == UserManager.getUserId()) {
            getString(R.string.group_details_label_delete_group_question)
        } else {
            getString(R.string.group_details_label_exit_group_question)
        }
        val action = if (group.adminId == UserManager.getUserId()) {
            getString(R.string.group_more_options_label_channel_delete)
        } else {
            getString(R.string.group_more_options_label_exit)
        }
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton(action) { _, _ ->
                    if (isNetworkActiveWithMessage()) {
                        groupPostsViewModel.exitGroup()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show()
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
            AppConstants.REQ_CODE_GROUP_DETAILS_MORE_OPTIONS -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        Timber.i("User profile clicked : $profile")
        val data = UserCrossedDto()
        data.profile = profile
        if (profile.id == UserManager.getUserId()) {
            startActivity(Intent(this, ProfileActivity::class.java))
        } else {
            val intent = PeopleDetailsActivity.getStartIntent(this, data,
                    AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
            startActivity(intent)
        }
    }

    override fun onPostClicked(post: GroupPostDto, focusReplyEditText: Boolean) {
        Timber.i("Post clicked : $post\nFocus reply edit text : $focusReplyEditText")
        val intent = PostDetailsActivity.getStartIntent(this, post, focusReplyEditText)
        startActivity(intent)
    }

    override fun onPostMediaClicked(post: GroupPostDto, focusReplyEditText: Boolean, media: ImageUrlDto?) {

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

    override fun onAddCommentClicked(post: GroupPostDto, comment: String) {
        Timber.i("Add the comment : $comment")
//        shortToast(comment)
        postDetailsViewModel.start(post)
        postDetailsViewModel.addPostReply(comment)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(postUpdatedReceiver)
    }

    // Screen touch keyboard close
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val view = currentFocus
        if (view != null && (ev.action == MotionEvent.ACTION_UP || ev.action == MotionEvent.ACTION_MOVE) && view is EditText && !view.javaClass.name.startsWith("android.webkit.")) {
            val scrooges = IntArray(2)
            view.getLocationOnScreen(scrooges)
            val x = ev.rawX + view.left - scrooges[0]
            val y = ev.rawY + view.top - scrooges[1]
            if (x < view.left || x > view.right || y < view.top || y > view.bottom) {
                window.decorView.rootView.hideKeyboard()
                window.decorView.rootView.clearFocus()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

}