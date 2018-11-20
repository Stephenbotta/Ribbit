package com.conversify.ui.createvenue

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.conversify.data.MemoryCache
import com.conversify.data.local.models.AppError
import com.conversify.data.remote.RetrofitClient
import com.conversify.data.remote.aws.S3Utils
import com.conversify.data.remote.failureAppError
import com.conversify.data.remote.getAppError
import com.conversify.data.remote.models.ApiResponse
import com.conversify.data.remote.models.Resource
import com.conversify.data.remote.models.loginsignup.InterestDto
import com.conversify.data.remote.models.venues.CreateEditVenueRequest
import com.conversify.data.remote.models.venues.VenueDto
import com.conversify.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File

class CreateVenueViewModel : ViewModel() {
    val interests by lazy { MutableLiveData<Resource<List<InterestDto>>>() }
    val createVenue by lazy { SingleLiveEvent<Resource<VenueDto>>() }

    fun hasInterests(): Boolean = MemoryCache.hasInterests()

    fun getInterests() {
        if (hasInterests()) {
            interests.value = Resource.success(MemoryCache.getInterests())
            return
        }

        interests.value = Resource.loading()

        RetrofitClient.conversifyApi
                .getInterests()
                .enqueue(object : Callback<ApiResponse<List<InterestDto>>> {
                    override fun onResponse(call: Call<ApiResponse<List<InterestDto>>>,
                                            response: Response<ApiResponse<List<InterestDto>>>) {
                        if (response.isSuccessful) {
                            val allInterests = response.body()?.data ?: emptyList()
                            interests.value = Resource.success(allInterests)
                            MemoryCache.updateInterests(allInterests)
                        } else {
                            interests.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<List<InterestDto>>>, t: Throwable) {
                        interests.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun createVenue(request: CreateEditVenueRequest, selectedVenueImageFile: File?, selectedVerificationFile: File?) {
        createVenue.value = Resource.loading()

        if (selectedVenueImageFile == null && selectedVerificationFile == null) {
            createVenueApi(request)
            return
        }

        var venueImageUploaded = selectedVenueImageFile == null
        var verificationUploaded = selectedVerificationFile == null

        var venueImageId: Int? = null
        var verificationFileId: Int? = null

        val transferListener = object : TransferListener {
            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
            }

            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    when (id) {
                        venueImageId -> {
                            val uploadedImageUrl = S3Utils.getResourceUrl(selectedVenueImageFile?.name
                                    ?: "")
                            Timber.i("Venue image uploaded : $uploadedImageUrl")
                            request.imageOriginalUrl = uploadedImageUrl
                            request.imageThumbnailUrl = uploadedImageUrl
                            venueImageUploaded = true
                        }

                        verificationFileId -> {
                            val uploadedVerificationUrl = S3Utils.getResourceUrl(selectedVerificationFile?.name
                                    ?: "")
                            Timber.i("Venue verification uploaded : $uploadedVerificationUrl")
                            request.ownerVerificationDocumentUrl = uploadedVerificationUrl
                            verificationUploaded = true
                        }
                    }

                    if (venueImageUploaded && verificationUploaded) {
                        createVenueApi(request)
                    }

                } else if (state == TransferState.FAILED ||
                        state == TransferState.WAITING_FOR_NETWORK ||
                        state == TransferState.PAUSED) {
                    createVenue.value = Resource.error(AppError.WaitingForNetwork)
                    venueImageId?.let { S3Utils.TRANSFER_UTILITY.cancel(it) }
                    verificationFileId?.let { S3Utils.TRANSFER_UTILITY.cancel(it) }
                }
            }

            override fun onError(id: Int, ex: java.lang.Exception?) {
                Timber.w(ex)
                val error = AppError.GeneralError(ex?.localizedMessage ?: "")
                createVenue.value = Resource.error(error)
                venueImageId?.let { S3Utils.TRANSFER_UTILITY.cancel(it) }
                verificationFileId?.let { S3Utils.TRANSFER_UTILITY.cancel(it) }
            }
        }

        if (selectedVenueImageFile != null) {
            val venueImageObserver = S3Utils.TRANSFER_UTILITY
                    .upload(selectedVenueImageFile.name, selectedVenueImageFile)
            venueImageObserver.setTransferListener(transferListener)
            venueImageId = venueImageObserver.id
        }

        if (selectedVerificationFile != null) {
            val verificationObserver = S3Utils.TRANSFER_UTILITY
                    .upload(selectedVerificationFile.name, selectedVerificationFile)
            verificationObserver.setTransferListener(transferListener)
            verificationFileId = verificationObserver.id
        }
    }

    private fun createVenueApi(request: CreateEditVenueRequest) {
        RetrofitClient.conversifyApi
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