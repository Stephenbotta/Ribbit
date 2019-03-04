package com.conversify.ui.main.notifications

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.notifications.NotificationDto
import com.conversify.extensions.handleError
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.custom.LoadingDialog
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_notifications.*

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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout.setOnRefreshListener { getNotifications() }
        clearNotification.setOnClickListener { }
        setupNotificationsRecycler()
        observeChanges()
        getNotifications()
    }

    private fun setupNotificationsRecycler() {
        notificationsAdapter = NotificationsAdapter(GlideApp.with(this), this)
        rvNotifications.adapter = notificationsAdapter

        rvNotifications.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            if(!rvNotifications.canScrollVertically(1) && viewModel.validForPaging())
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

    private fun checkForNoNotificationsState() {
        viewSwitcher.displayedChild = if (notificationsAdapter.itemCount == 0) {
            CHILD_NO_NOTIFICATIONS
        } else {
            CHILD_NOTIFICATIONS
        }
        checkEnableClearNotification()
    }

    private fun checkEnableClearNotification() {
        val clear = viewSwitcher.displayedChild

        when (clear) {

            CHILD_NOTIFICATIONS -> clearNotification.isEnabled = true

            CHILD_NO_NOTIFICATIONS -> clearNotification.isEnabled = false
        }
    }

    override fun onInviteRequestAction(acceptRequest: Boolean, notification: NotificationDto) {
        if (isNetworkActiveWithMessage()) {
            viewModel.acceptRejectInviteRequest(acceptRequest, notification)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadingDialog.setLoading(false)
    }

}