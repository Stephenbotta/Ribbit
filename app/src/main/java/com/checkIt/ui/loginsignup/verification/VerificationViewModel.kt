package com.checkIt.ui.loginsignup.verification

import android.arch.lifecycle.ViewModel
import com.checkIt.data.remote.RetrofitClient
import com.checkIt.data.remote.failureAppError
import com.checkIt.data.remote.getAppError
import com.checkIt.data.remote.models.ApiResponse
import com.checkIt.data.remote.models.Resource
import com.checkIt.data.remote.models.loginsignup.ProfileDto
import com.checkIt.data.remote.models.loginsignup.ResendOtpRequest
import com.checkIt.data.remote.models.loginsignup.VerifyOtpRequest
import com.checkIt.utils.AppConstants
import com.checkIt.utils.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerificationViewModel : ViewModel() {
    private lateinit var profile: ProfileDto
    private var registeredMode = AppConstants.REGISTERED_MODE_PHONE

    val verifyOtp by lazy { SingleLiveEvent<Resource<ProfileDto>>() }
    val resendOtp by lazy { SingleLiveEvent<Resource<Any>>() }

    fun start(profile: ProfileDto, startForResult: Boolean) {
        this.profile = profile
        if (startForResult) {
            registeredMode = AppConstants.REGISTERED_MODE_PHONE
        } else {
            registeredMode = if (profile.email.isNullOrBlank()) {
                AppConstants.REGISTERED_MODE_PHONE
            } else {
                AppConstants.REGISTERED_MODE_EMAIL
            }
        }
    }

    fun isRegisteredModePhone(): Boolean = registeredMode == AppConstants.REGISTERED_MODE_PHONE

    fun verifyOtp(otp: String) {
        verifyOtp.value = Resource.loading()

        val request = if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
            VerifyOtpRequest(
                    otp = otp,
                    countryCode = profile.countryCode,
                    phoneNumber = profile.phoneNumber)
        } else {
            VerifyOtpRequest(
                    otp = otp,
                    email = profile.email)
        }

        RetrofitClient.conversifyApi
                .verifyOtp(request)
                .enqueue(object : Callback<ApiResponse<ProfileDto>> {
                    override fun onResponse(call: Call<ApiResponse<ProfileDto>>,
                                            response: Response<ApiResponse<ProfileDto>>) {
                        if (response.isSuccessful) {
                            verifyOtp.value = Resource.success(response.body()?.data)
                        } else {
                            verifyOtp.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ProfileDto>>, t: Throwable) {
                        verifyOtp.value = Resource.error(t.failureAppError())
                    }
                })
    }

    fun resendOtp() {
        resendOtp.value = Resource.loading()
        val request = if (registeredMode == AppConstants.REGISTERED_MODE_PHONE) {
            ResendOtpRequest(countryCode = profile.countryCode,
                    phoneNumber = profile.phoneNumber)
        } else {
            ResendOtpRequest(email = profile.email)
        }

        RetrofitClient.conversifyApi
                .resendOtp(request)
                .enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            resendOtp.value = Resource.success()
                        } else {
                            resendOtp.value = Resource.error(response.getAppError())
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        resendOtp.value = Resource.error(t.failureAppError())
                    }
                })
    }
}