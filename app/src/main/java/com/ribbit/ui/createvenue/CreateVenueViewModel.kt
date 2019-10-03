package com.ribbit.ui.createvenue

import androidx.lifecycle.ViewModel
import com.ribbit.data.remote.RetrofitClient
import com.ribbit.data.remote.aws.S3Uploader
import com.ribbit.data.remote.aws.S3Utils
import com.ribbit.data.remote.failureAppError
import com.ribbit.data.remote.getAppError
import com.ribbit.data.remote.models.ApiResponse
import com.ribbit.data.remote.models.Resource
import com.ribbit.data.remote.models.Status
import com.ribbit.data.remote.models.venues.CreateEditVenueRequest
import com.ribbit.data.remote.models.venues.VenueDto
import com.ribbit.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File

class CreateVenueViewModel : ViewModel() {
    val createVenue by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    private val s3Uploader by lazy { S3Uploader(S3Utils.TRANSFER_UTILITY) }

    fun createVenue(request: CreateEditVenueRequest, selectedVenueImageFile: File?, selectedVerificationFile: File?) {
        createVenue.value = Resource.loading()

        if (selectedVenueImageFile == null && selectedVerificationFile == null) {
            createVenueApi(request)
            return
        }

        // If file is not selected then consider it as completed
        var venueImageCompleted = selectedVenueImageFile == null
        var verificationCompleted = selectedVerificationFile == null

        // Upload venue image if exists
        if (selectedVenueImageFile != null) {
            s3Uploader.upload(selectedVenueImageFile)
                    .addUploadCompleteListener { uploadedImageUrl ->
                        Timber.i("Venue image uploaded : $uploadedImageUrl")
                        request.imageOriginalUrl = uploadedImageUrl
                        request.imageThumbnailUrl = uploadedImageUrl
                        venueImageCompleted = true

                        // Proceed to create venue api if both are completed
                        if (venueImageCompleted && verificationCompleted) {
                            createVenueApi(request)
                        }
                    }
                    .addUploadFailedListener {
                        createVenue.value = Resource(Status.ERROR, null, null)
                    }
        }

        // Upload verification file if exists
        if (selectedVerificationFile != null) {
            s3Uploader.upload(selectedVerificationFile)
                    .addUploadCompleteListener { uploadedVerificationUrl ->
                        Timber.i("Venue verification uploaded : $uploadedVerificationUrl")
                        request.ownerVerificationDocumentUrl = uploadedVerificationUrl
                        verificationCompleted = true

                        // Proceed to create venue api if both are completed
                        if (venueImageCompleted && verificationCompleted) {
                            createVenueApi(request)
                        }
                    }
                    .addUploadFailedListener {
                        createVenue.value = Resource(Status.ERROR, null, null)
                    }
        }
    }

    private fun createVenueApi(request: CreateEditVenueRequest) {
        RetrofitClient.ribbitApi
                .createVenue(request)
                .enqueue(object : Callback<ApiResponse<VenueDto>> {
                    override fun onResponse(call: Call<ApiResponse<VenueDto>>,
                                            response: Response<ApiResponse<VenueDto>>) {
                        if (response.isSuccessful) {
                            createVenue.value = Resource.success(response.body()?.data)
                        } else {
                            createVenue.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<VenueDto>>, t: Throwable) {
                        createVenue.value = Resource.error(t.failureAppError())
                    }
                })
    }
}