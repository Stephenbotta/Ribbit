package com.conversify.ui.profile.edit

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.ProfileDto
import com.conversify.ui.base.BaseActivity
import com.conversify.utils.GlideApp
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : BaseActivity(), View.OnClickListener {

    private val viewModel by lazy { ViewModelProviders.of(this)[EditProfileViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        setData(viewModel.getProfile())
        setListener()
    }

    private fun setData(profile: ProfileDto) {
        GlideApp.with(this)
                .load(profile.image?.original)
                .into(ivProfilePic)
        if (!profile.fullName.isNullOrEmpty())
            editName.setText(profile.fullName)
        if (!profile.userName.isNullOrEmpty())
            editUserName.setText(profile.userName)
        if (!profile.website.isNullOrEmpty())
            editWebsite.setText(profile.website)
        if (!profile.bio.isNullOrEmpty())
            editBio.setText(profile.bio)
        if (!profile.designation.isNullOrEmpty())
            editDesignation.setText(profile.designation)
        if (!profile.company.isNullOrEmpty())
            editBio.setText(profile.company)
        if (!profile.email.isNullOrEmpty())
            editEmail.setText(profile.email)
        if (!profile.fullPhoneNumber.isNullOrEmpty())
            editPhone.setText(profile.fullPhoneNumber)
        if (!profile.gender.isNullOrEmpty())
            editGender.setText(profile.gender)

    }

    private fun setListener() {
        btnBack.setOnClickListener(this)
        tvSave.setOnClickListener(this)
        tvChangeProfilePhoto.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.btnBack -> onBackPressed()

            R.id.tvSave -> {
            }

            R.id.tvChangeProfilePhoto -> {
            }

        }

    }
}