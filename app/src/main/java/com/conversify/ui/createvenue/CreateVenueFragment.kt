package com.conversify.ui.createvenue

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.conversify.R
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.extensions.hideKeyboard
import com.conversify.extensions.shortToast
import com.conversify.ui.base.BaseFragment
import com.conversify.utils.*
import com.conversify.utils.PermissionUtils
import com.google.android.gms.location.places.ui.PlacePicker
import kotlinx.android.synthetic.main.fragment_create_venue.*
import permissions.dispatcher.*

@RuntimePermissions
class CreateVenueFragment : BaseFragment() {
    companion object {
        const val TAG = "CreateVenueFragment"
        private const val ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY"

        fun newInstance(category: InterestDto): Fragment {
            val fragment = CreateVenueFragment()
            val arguments = Bundle()
            arguments.putParcelable(ARGUMENT_CATEGORY, category)
            fragment.arguments = arguments
            return fragment
        }
    }

    private lateinit var category: InterestDto
    private lateinit var imagePicker: ImagePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun getFragmentLayoutResId(): Int = R.layout.fragment_create_venue

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        category = arguments?.getParcelable(ARGUMENT_CATEGORY) as InterestDto
        imagePicker = ImagePicker(this)

        setListeners()

        tvCategory.text = category.name
    }

    private fun setListeners() {
        imagePicker.setImagePickerListener { imageFile ->
            GlideApp.with(this)
                    .load(imageFile)
                    .placeholder(R.color.greyImageBackground)
                    .error(R.color.greyImageBackground)
                    .into(ivVenue)
        }

        ivVenue.setOnClickListener { showImagePickerWithPermissionCheck() }
        etVenueLocation.setOnClickListener {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(activity), AppConstants.REQ_CODE_PLACE_PICKER)
        }

        etDateTime.setOnClickListener {
            DateTimePicker(requireActivity(),
                    minDateMillis = System.currentTimeMillis()) { selectedDateTime ->
                etDateTime.setText(DateTimeUtils.getFormattedVenueDateTime(selectedDateTime))
            }.show()
        }
        clUploadDocument.setOnClickListener { }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_fragment_create_venue, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuCreateVenue) {
            etVenueTitle.hideKeyboard()
            etVenueTitle.clearFocus()
            etVenueTags.clearFocus()
            etVenueOwnerName.clearFocus()
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    @NeedsPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showImagePicker() {
        imagePicker.showImagePicker()
    }

    @OnShowRationale(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(requireActivity(), R.string.permission_rationale_camera_storage, request)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageDenied() {
        activity?.shortToast(R.string.permission_denied_camera_storage)
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun cameraStorageNeverAsk() {
        PermissionUtils.showAppSettingsDialog(this, R.string.permission_never_ask_camera_storage, AppConstants.REQ_CODE_APP_SETTINGS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConstants.REQ_CODE_PLACE_PICKER -> {
                if (resultCode == Activity.RESULT_OK) {
                    val place = PlacePicker.getPlace(context, data)
                    etVenueLocation.setText(place.address)
                }
            }

            else -> {
                imagePicker.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        imagePicker.clear()
    }
}