package com.conversify.ui.main.searchusers

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import com.conversify.R
import com.conversify.data.remote.models.Status
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.gone
import com.conversify.extensions.isNetworkActiveWithMessage
import com.conversify.extensions.visible
import com.conversify.ui.base.BaseFragment
import com.conversify.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.conversify.ui.profile.ProfileActivity
import com.conversify.ui.profile.ProfileInterestsAdapter
import com.conversify.utils.AppConstants
import com.conversify.utils.AppUtils
import com.conversify.utils.GlideApp
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_search_users.*

class SearchUsersFragment : BaseFragment(), ProfileInterestsAdapter.Callback {

    companion object {
        const val TAG = "SearchUsersFragment"
    }

    private val viewModel by lazy { ViewModelProviders.of(this)[SearchUsersViewModel::class.java] }
    private lateinit var interestsAdapter: ProfileInterestsAdapter
    private lateinit var searchUsersAdapter: SearchUsersAdapter
    private var count = 1
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var distanceRange = 5
    private var hideContent = true

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inItClasses()
        setupInterestsRecycler()
        setData(viewModel.getProfile().interests ?: emptyList())
        setListener()
        observeInterestMatchedUsersList()
        setAdapter()
    }

    private fun inItClasses() {
        val thumbnail = GlideApp.with(this).load(viewModel.getProfile().image?.thumbnail)
        GlideApp.with(this)
                .load(viewModel.getProfile().image?.original)
                .thumbnail(thumbnail)
                .into(ivProfilePic)
        ivProfilePic.setOnClickListener { startActivity(Intent(requireContext(), ProfileActivity::class.java)) }
    }

    private fun setData(list: List<InterestDto>) {
        interestsAdapter.displayInterests(list)
    }

    private fun setupInterestsRecycler() {
        interestsAdapter = ProfileInterestsAdapter(this)
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexWrap = FlexWrap.WRAP
        rvConnection.layoutManager = layoutManager
        rvConnection.isNestedScrollingEnabled = false
        rvConnection.adapter = interestsAdapter
    }

    private fun setListener() {
        tvSelectLocation.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)
        }
        goLarge.setOnClickListener { startSearching() }
        ivGo.setOnClickListener { startSearching() }
        go.setOnClickListener { startSearching() }
        rlSearchAnimation.setOnClickListener {
            rlSearchAnimation.gone()
            goLarge.visible()
            ivGo.visible()
            tvFindingMatch.gone()
            viewModel.cancelGettingResultsFromApi()
        }
        goLarge.isEnabled = false
        goLarge.isClickable = false
        ivGo.isClickable = false

        sbRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                distanceRange = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        btnViewResults.setOnClickListener {
            showSearchedResults()
        }
    }

    private fun startSearching() {
        tvFindingMatch.visible()
        goLarge.gone()
        go.gone()
        ivGo.gone()
        rlSearchAnimation.visible()
        recursion()
        getMatchedUsersList(true)
    }

    fun getMatchedUsersList(isFirstPage: Boolean) {
        if (isNetworkActiveWithMessage()) {
            count = 1
            viewModel.getMatchedResultsApi(latitude, longitude, distanceRange, isFirstPage,
                    interestsAdapter.getCategoryIds())
        } else {
            rlSearchAnimation.gone()
            goLarge.visible()
            ivGo.visible()
            tvFindingMatch.gone()
            viewModel.cancelGettingResultsFromApi()
        }
    }

    private fun recursion() {
        Handler().postDelayed({
            when (count) {
                1 -> scaleView(ll_request1)
                2 -> scaleView(ll_request2)
                3 -> scaleView(ll_request3)
                4 -> scaleView(ll_request4)
            }
            count++
            if (count < 5)
                recursion()
        }, 0)
    }

    private fun scaleView(view: View) {
        if (activity != null) {
            val animFadeIn = AnimationUtils.loadAnimation(activity, R.anim.scale_translation)
            view.startAnimation(animFadeIn)
            animFadeIn.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    view.clearAnimation()
                    view.gone()
                }

                override fun onAnimationRepeat(animation: Animation) {
                    view.clearAnimation()
                }
            })
        }
    }

    private fun setAdapter() {
        searchUsersAdapter = SearchUsersAdapter(GlideApp.with(requireActivity()))
        rvMatchedResults.adapter = searchUsersAdapter

        rvMatchedResults.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!rvMatchedResults.canScrollVertically(1) && viewModel.validForPaging()) {
                    getMatchedUsersList(false)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(requireContext(), data)
                    latitude = place.latLng.latitude
                    longitude = place.latLng.longitude
                    val locationName = place.name?.toString()
                    val locationAddress = place.address?.toString()
                    tvLocationAddress.text = AppUtils.getFormattedAddress(locationName, locationAddress)
                    tvLocationAddress.visible()
                    goLarge.isEnabled = true
                    goLarge.isClickable = true
                    ivGo.isClickable = true
                    goLarge.visible()
                    ivGo.visible()
                    btnViewResults.gone()
                    go.gone()
                    rlSearchAnimation.gone()
                    tvFindingMatch.gone()
                }
            }

            AppConstants.REQ_CODE_CHOOSE_INTERESTS -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val interests = data.getParcelableArrayListExtra(AppConstants.EXTRA_INTEREST)
                            ?: emptyList<InterestDto>()
                    setData(interests)
                }
            }
        }
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(startedForResult = true, updateInPref = false)
        fragment.setTargetFragment(this, AppConstants.REQ_CODE_CHOOSE_INTERESTS)
        fragmentManager?.apply {
            beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_in, R.anim.slide_up_out,
                            R.anim.slide_down_in, R.anim.slide_down_out)
                    .add(android.R.id.content, fragment, ChooseInterestsFragment.TAG)
                    .addToBackStack(null)
                    .commit()
        }
    }

    private fun observeInterestMatchedUsersList() {
        viewModel.matchedResults.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    rlSearchAnimation.gone()
                    goLarge.gone()
                    ivGo.visible()
                    go.visible()
                    tvFindingMatch.gone()

                    val pagingResult = it.data
                    val firstPage = pagingResult?.isFirstPage ?: false
                    val searchedUsersList = pagingResult?.result ?: emptyList()
                    if (firstPage) {
                        btnViewResults.visible()
                        clFilterData.gone()
                        rvMatchedResults.visible()
                        hideContent = false
                        searchUsersAdapter.displayList(searchedUsersList)
                    } else {
                        searchUsersAdapter.addToList(searchedUsersList)
                    }
                }
                Status.ERROR -> {
                    rlSearchAnimation.gone()
                    goLarge.visible()
                    ivGo.visible()
                    tvFindingMatch.gone()
                }
                Status.LOADING -> {

                }
            }
        })
    }

    private fun showSearchedResults() {
        if (hideContent) {
            clFilterData.gone()
        } else {
            clFilterData.visible()
        }
        hideContent = !hideContent
    }
}