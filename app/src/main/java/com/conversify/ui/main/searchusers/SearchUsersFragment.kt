package com.conversify.ui.main.searchusers

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
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

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_search_users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inItClasses()
        setupInterestsRecycler()
        setData(viewModel.getProfile().interests!!)
        setListener()
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
        goLarge.setOnClickListener {  }
        ivGo.setOnClickListener {  }
        go.setOnClickListener {  }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(requireContext(), data)
                    val latitude = place.latLng.latitude
                    val longitude = place.latLng.longitude
                    val locationName = place.name?.toString()
                    val locationAddress = place.address?.toString()
                    tvLocationAddress.text = AppUtils.getFormattedAddress(locationName, locationAddress)
                    tvLocationAddress.visible()
                }
            }

            AppConstants.REQ_CODE_CHOOSE_INTERESTS -> {
                if (requestCode == AppConstants.REQ_CODE_CHOOSE_INTERESTS && resultCode == Activity.RESULT_OK) {
                    setData(viewModel.updateProfile().interests!!)
                }
            }
        }
    }

    override fun onEditInterestsClicked() {
        val fragment = ChooseInterestsFragment.newInstance(true)
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


}