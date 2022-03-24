package com.ribbit.ui.main.searchusers

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.ribbit.R
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.loginsignup.InterestDto
import com.ribbit.data.remote.models.people.UserCrossedDto
import com.ribbit.extensions.gone
import com.ribbit.extensions.isNetworkActiveWithMessage
import com.ribbit.extensions.shortToast
import com.ribbit.extensions.visible
import com.ribbit.ui.base.BaseFragment
import com.ribbit.ui.chat.ChatActivity
import com.ribbit.ui.loginsignup.chooseinterests.ChooseInterestsFragment
import com.ribbit.ui.people.details.PeopleDetailsActivity
import com.ribbit.ui.profile.ProfileActivity
import com.ribbit.ui.profile.ProfileInterestsAdapter
import com.ribbit.utils.AppConstants
import com.ribbit.utils.AppUtils
import com.ribbit.utils.GlideApp
import kotlinx.android.synthetic.main.fragment_search_users.*

class SearchUsersFragment : BaseFragment(), ProfileInterestsAdapter.Callback, SearchUsersAdapter.Callback {

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
    private val interest by lazy { ArrayList<InterestDto>() }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupInterestsRecycler()
        setListener()
        observeInterestMatchedUsersList()
        setAdapter()
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateProfile()
        interest.clear()
        interest.addAll(viewModel.getProfile().interests ?: emptyList())
        setData(interest)
        inItClasses()
    }

    private fun inItClasses() {
        val thumbnail = GlideApp.with(this).load(viewModel.getProfile().image?.thumbnail)
        GlideApp.with(this)
                .load(viewModel.getProfile().image?.original)
                .thumbnail(thumbnail)
                .into(ivProfilePic)
        tvSelectedRange.text = getString(R.string.search_user_label_progress_count, distanceRange)
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
            /*val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)*/

            val placeFields = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)

            // Start the autocomplete intent.
            val intent = Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, placeFields)
                    .build(requireContext())
            startActivityForResult(intent, AppConstants.REQ_CODE_PLACE_PICKER)
        }
        goLarge.setOnClickListener { startSearching() }
        ivGo.setOnClickListener { startSearching() }
        go.setOnClickListener { startSearching() }
        /*  rlSearchAnimation.setOnClickListener {
              rlSearchAnimation.gone()
              goLarge.visible()
              ivGo.visible()
              tvFindingMatch.gone()
              viewModel.cancelGettingResultsFromApi()
          }*/
        goLarge.isEnabled = false
        goLarge.isClickable = false
        ivGo.isClickable = false

        sbRange.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                distanceRange = progress
                tvSelectedRange.text = getString(R.string.search_user_label_progress_count, distanceRange)
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
            val delayTimeInMillis = if (isFirstPage) 5000L else 0L
            Handler().postDelayed({
                viewModel.getMatchedResultsApi(latitude, longitude, distanceRange, isFirstPage,
                        interestsAdapter.getCategoryIds())
            }, delayTimeInMillis)
        } else {
            rlSearchAnimation.gone()
            goLarge.visible()
            ivGo.visible()
            tvFindingMatch.gone()
//            viewModel.cancelGettingResultsFromApi()
        }
    }

    private fun recursion() {
        val delayTimeInMillis = if (count == 1) 0L else 1000L
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
        }, delayTimeInMillis)
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
        searchUsersAdapter = SearchUsersAdapter(GlideApp.with(requireActivity()), this)
        rvMatchedResults.adapter = searchUsersAdapter

        rvMatchedResults.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
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
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    latitude = place.latLng?.latitude ?: 0.0
                    longitude = place.latLng?.longitude ?: 0.0
                    val locationName = place.name
                    val locationAddress = place.address
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
                    interest.clear()
                    interest.addAll(interests)
                    setData(interest)
                }
            }
        }
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(startedForResult = true, updateInPref = false, interest = interest, count = 1)
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
                    activity?.shortToast("No user found")
                }
                Status.LOADING -> {

                }
            }
        })
    }

    private fun showSearchedResults() {
        if (hideContent) {
            clFilterData.gone()
            btnViewResults.setImageDrawable(activity?.getDrawable(R.drawable.ic_down))
        } else {
            btnViewResults.setImageDrawable(activity?.getDrawable(R.drawable.ic_up))
            clFilterData.visible()
        }
        hideContent = !hideContent
    }

    override fun openChatScreen(user: UserCrossedDto, isDetailShow: Boolean) {
        if (isDetailShow) {
            val intent = PeopleDetailsActivity.getStartIntent(requireContext(), user,
                    AppConstants.REQ_CODE_PEOPLE, user.crossedUser?.id ?: "")
            activity?.startActivity(intent)
        } else {
            val intent = ChatActivity.getStartIntentForIndividualChat(requireContext(), user, AppConstants.REQ_CODE_INDIVIDUAL_CHAT)
            startActivity(intent)
        }
    }
}