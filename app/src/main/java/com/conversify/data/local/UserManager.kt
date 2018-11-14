package com.conversify.data.local

import android.location.Location
import com.conversify.data.remote.models.loginsignup.ProfileDto
import java.util.*

object UserManager {
    fun isFirstAppLaunch(): Boolean {
        return PrefsManager.get().getBoolean(PrefsManager.PREF_FIRST_APP_LAUNCH, true)
    }

    fun setFirstAppLaunched() {
        PrefsManager.get().save(PrefsManager.PREF_FIRST_APP_LAUNCH, false)
    }

    private fun getAccessToken(): String {
        return PrefsManager.get().getString(PrefsManager.PREF_ACCESS_TOKEN, "")
    }

    fun getAuthorization(): String? {
        val accessToken = getAccessToken()
        return if (accessToken.isBlank()) {
            null
        } else {
            "bearer $accessToken"
        }
    }

    fun saveProfile(profile: ProfileDto) {
        PrefsManager.get().save(PrefsManager.PREF_ACCESS_TOKEN, profile.accessToken ?: "")
        PrefsManager.get().save(PrefsManager.PREF_USER_PROFILE, profile)
        PrefsManager.get().save(PrefsManager.PREF_USER_ID, profile.id ?: "")
    }

    fun updateLocation(location: Location) {
        PrefsManager.get().save(PrefsManager.PREF_LATITUDE, location.latitude.toFloat())
        PrefsManager.get().save(PrefsManager.PREF_LONGITUDE, location.longitude.toFloat())
    }

    fun getLastLatitude(): Double? {
        val latitude = PrefsManager.get().getFloat(PrefsManager.PREF_LATITUDE, 0.0f)
        return if (latitude == 0.0f) {
            null
        } else {
            latitude.toDouble()
        }
    }

    fun getLastLongitude(): Double? {
        val longitude = PrefsManager.get().getFloat(PrefsManager.PREF_LONGITUDE, 0.0f)
        return if (longitude == 0.0f) {
            null
        } else {
            longitude.toDouble()
        }
    }

    fun isLoggedIn(): Boolean {
        return PrefsManager.get().contains(PrefsManager.PREF_USER_PROFILE)
    }

    fun getProfile(): ProfileDto {
        return PrefsManager.get().getObject(PrefsManager.PREF_USER_PROFILE, ProfileDto::class.java)
                ?: ProfileDto()
    }

    fun removeProfile() {
        PrefsManager.get().remove(PrefsManager.PREF_ACCESS_TOKEN)
        PrefsManager.get().remove(PrefsManager.PREF_USER_PROFILE)
        PrefsManager.get().remove(PrefsManager.PREF_USER_ID)
    }

    fun getUserId(): String {
        return PrefsManager.get().getString(PrefsManager.PREF_USER_ID, "")
    }

    fun getUniqueId(): String {
        val existingUniqueId = PrefsManager.get().getString(PrefsManager.PREF_UNIQUE_ID, "")

        // If unique id does not exist then generate a new one, save it locally and return the value.
        if (existingUniqueId.isBlank()) {
            val newUniqueId = UUID.randomUUID().toString()
            PrefsManager.get().save(PrefsManager.PREF_UNIQUE_ID, newUniqueId)
            return newUniqueId
        }

        return existingUniqueId
    }
}