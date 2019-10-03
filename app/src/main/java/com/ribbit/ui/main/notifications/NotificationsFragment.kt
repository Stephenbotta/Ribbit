package com.ribbit.ui.main.notifications

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.ribbit.R
import com.ribbit.data.local.models.MessageEvent
import com.ribbit.data.remote.PushType
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.groups.GroupDto
import com.ribbit.data.remote.models.groups.GroupPostDto
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import com.ribbit.data.remote.models.notifications.NotificationDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.databinding.DialogConverseNearbyNavigateBinding
import com.ribbit.extensions.handleError
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.custom.LoadingDialog
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.post.details.PostDetailsActivity
import com.ribbit.utils.AppConstants
import com.ribbit.utils.GlideApp
import com.ribbit.utils.MapUtils
import kotlinx.android.synthetic.main.fragment_notifications.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class NotificationsFragment : BaseFragment(), NotificationsAdapter.Callback {

    companion object {
        const val TAG = "NotificationsFragment"
        private const val CHILD_NOTIFICATIONS = 0
        private const val CHILD_NO_NOTIFICATIONS = 1
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[NotificationsViewModel::class.java] }
    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var loadingDialog: LoadingDialog

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(requireContext())
        EventBus.getDefault().register(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { refreshData() }
        clearNotification.setOnClickListener { clearNotifications() }
        setupNotificationsRecycler()
        observeChanges()
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        when (event.type) {
            AppConstants.EVENT_PUSH_NOTIFICATION -> refreshData()
        }
    }

    private fun refreshData() {
        getNotifications()
    }

    private fun setupNotificationsRecycler() {
        notificationsAdapter = NotificationsAdapter(GlideApp.with(this), this)
        rvNotifications.adapter = notificationsAdapter

        rvNotifications.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvNotifications.canScrollVertically(1) && viewModel.validForPaging())
                    getNotifications(false)
            }
        })
    }

    private fun observeChanges() {
        viewModel.notifications.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    swipeRefreshLayout.isRefreshing = false
                    val notifications = resource.data?.result ?: emptyList()
                    val firstPage = resource.data?.isFirstPage ?: true
                    if (firstPage) {
                        notificationsAdapter.displayNotifications(notifications)
                    } else {
                        notificationsAdapter.addNotifications(notifications)
                    }

                    checkForNoNotificationsState()
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

        viewModel.joinVenueRequest.observe(this, Observer { resource ->
            resource ?: return@Observer

            when (resource.status) {
                Status.SUCCESS -> {
                    // todo refresh channels and venues for updated member count
                    loadingDialog.setLoading(false)
                    resource.data?.let { notification ->
                        notificationsAdapter.removeNotification(notification)
                        checkForNoNotificationsState()
                    }
                }

                Status.ERROR -> {
                    loadingDialog.setLoading(false)
                    handleError(resource.error)
                }

                Status.LOADING -> {
                    loadingDialog.setLoading(true)
                }
            }
        })
    }

    private fun getNotifications(firstPage: Boolean = true) {
        if (isNetworkActiveWithMessage()) {
            viewModel.getNotifications(firstPage)
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun clearNotifications() {
        if (isNetworkActiveWithMessage()) {
            viewModel.clearNotifications()
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showDialog(notification: NotificationDto) {
        val inflater = layoutInflater
        val binding = DataBindingUtil.inflate<DialogConverseNearbyNavigateBinding>(inflater, R.layout.dialog_converse_nearby_navigate, null, false)
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        /*val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window?.attributes)
        val dialogHeight = lp.height
        val MAX_HEIGHT = (resources.displayMetrics.heightPixels * 0.90).toInt()

        if (dialogHeight > MAX_HEIGHT) {*/
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, (resources.displayMetrics.heightPixels * 0.90).toInt())
        /*} else {
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }*/
        dialog.setContentView(binding.root)
        GlideApp.with(requireContext()).load(notification.sender?.image?.original).into(binding.ivProfilePic)
        if (notification.location != null) {
            val lon = notification.location[0]
            val lat = notification.location[1]
            val url = MapUtils.getStaticMapWithMarker(requireContext(), lat, lon)
            GlideApp.with(requireContext()).load(url).into(binding.ivMapMarker)
        }

        binding.crossPath.text = when (notification.type) {
            PushType.ALERT_LOOK_NEARBY_PUSH -> {
                "${notification.sender?.userName} has crossed your path at ${notification.locationName}"
            }
            PushType.ALERT_CONVERSE_NEARBY_PUSH -> {
                activity?.getString(R.string.notifications_label_converse_nearby, notification.sender?.userName)
            }
            else -> {
                ""
            }
        }
        dialog.show()
        binding.btnOkay.setOnClickListener {
            notification.sender?.let { profile ->
                onUserProfileClicked(profile)
            }
            dialog.dismiss()
        }
        binding.btnShowPost.setOnClickListener {
            notification.postId?.let { groupPostDto ->
                onGroupPostClicked(groupPostDto)
            }
            dialog.dismiss()
        }
        binding.btnCancel.setOnClickListener { dialog.dismiss() }
    }


    private fun checkForNoNotificationsState() {
        viewSwitcher.displayedChild = if (notificationsAdapter.itemCount == 0) {
            CHILD_NO_NOTIFICATIONS
        } else {
            CHILD_NOTIFICATIONS
        }
        checkEnableClearNotification()
    }

    private fun checkEnableClearNotification() {
        when (viewSwitcher.displayedChild) {
            CHILD_NOTIFICATIONS -> clearNotification.isEnabled = true
            CHILD_NO_NOTIFICATIONS -> clearNotification.isEnabled = false
        }
    }

    override fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.acceptRejectInviteRequest(acceptRequest, notification)
        }
    }

    override fun onUserProfileClicked(profile: ProfileDto) {
        val data = UserCrossedDto()
        data.profile = profile
        val intent = PeopleDetailsActivity.getStartIntent(requireActivity(), data,
                AppConstants.REQ_CODE_BLOCK_USER, data.profile?.id ?: "")
        startActivity(intent)
    }

    override fun onGroupPostClicked(groupPost: GroupPostDto) {
        val intent = PostDetailsActivity.getStartIntent(requireActivity(), groupPost, true)
        startActivityForResult(intent, AppConstants.REQ_CODE_POST_DETAILS)
    }

    override fun onCrossedPathClicked(notification: NotificationDto) {
        showDialog(notification)
    }

    override fun onGroupClicked(group: GroupDto) {
        val intent = ChatActivity.getStartIntentForGroupChat(requireContext(), group, AppConstants.REQ_CODE_GROUP_CHAT)
        startActivityForResult(intent, AppConstants.REQ_CODE_GROUP_CHAT)
    }

    override fun onFollowRequestAction(action: Boolean, notification: NotificationDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.acceptFollowRequest(action, notification)
        }
    }

    override fun onVenueClicked(venue: VenueDto) {
        val intent = ChatActivity.getStartIntent(requireContext(), venue, AppConstants.REQ_CODE_VENUE_CHAT)
        startActivityForResult(intent, AppConstants.REQ_CODE_VENUE_CHAT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
        EventBus.getDefault().unregister(this)
    }

}