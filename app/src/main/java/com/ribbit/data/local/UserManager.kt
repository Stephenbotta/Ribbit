package com.ribbit.data.local

import android.location.Location
import com.ribbit.data.remote.models.loginsignup.ProfileDto
import java.util.*
import java.util.concurrent.TimeUnit

object UserManager {
    // Minimum difference between current millis and last location millis for it to be considered as valid
    private val lastLocationDifferenceMillis = TimeUnit.MINUTES.toMillis(5)

    fun isFirstAppLaunch(): Boolean {
        return PrefsManager.get().getBoolean(PrefsManager.PREF_FIRST_APP_LAUNCH, true)
    }

    fun setFirstAppLaunched() {
        PrefsManager.get().save(PrefsManager.PREF_FIRST_APP_LAUNCH, false)
    }

    fun getAccessToken(): String {
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
        PrefsManager.get().save(PrefsManager.PREF_GROUP_COUNT, profile.groupCount ?: 0)
    }

    fun saveDemographicClick(isClicked:Boolean){
        PrefsManager.get().save(PrefsManager.DEMOGRAPHIC_CLICK,isClicked)
    }

    fun getDemographicCLick():Boolean{
        return PrefsManager.get().contains(PrefsManager.DEMOGRAPHIC_CLICK)
    }

    fun updateLocation(location: Location) {
        PrefsManager.get().save(PrefsManager.PREF_LOCATION_UPDATE_MILLIS, System.currentTimeMillis())
        PrefsManager.get().save(PrefsManager.PREF_LATITUDE, location.latitude.toFloat())
        PrefsManager.get().save(PrefsManager.PREF_LONGITUDE, location.longitude.toFloat())
    }

    fun isLastLocationUpdated(): Boolean {
        return if (!PrefsManager.get().contains(PrefsManager.PREF_LOCATION_UPDATE_MILLIS)) {
            false
        } else {
            val lastLocationMillis = PrefsManager.get().getLong(PrefsManager.PREF_LOCATION_UPDATE_MILLIS, 0)
            System.currentTimeMillis() - lastLocationMillis < lastLocationDifferenceMillis
        }
    }

    fun getLastLatitude(): Double? {
        if (!isLastLocationUpdated()) {
            return null
        }

        val latitude = PrefsManager.get().getFloat(PrefsManager.PREF_LATITUDE, 0.0f)
        return if (latitude == 0.0f) {
            null
        } else {
            latitude.toDouble()
        }
    }

    fun getLastLongitude(): Double? {
        if (!isLastLocationUpdated()) {
            return null
        }

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
        PrefsManager.get().remove(PrefsManager.PREF_GROUP_COUNT)
    }

    fun getUserId(): String {
        return PrefsManager.get().getString(PrefsManager.PREF_USER_ID, "")
    }

    fun getGroupCount(): Int {
        return PrefsManager.get().getInt(PrefsManager.PREF_GROUP_COUNT, 0)
    }

    fun saveDeviceToken(token: String) {
        PrefsManager.get().save(PrefsManager.PREF_DEVICE_TOKEN, token)
    }

    fun getDeviceToken(): String {
        return PrefsManager.get().getString(PrefsManager.PREF_DEVICE_TOKEN, "")
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

    fun saveConversationId(conversationId: String) {
        PrefsManager.get().save(PrefsManager.PREF_CONVERSATION_ID, conversationId)
    }

    fun removeConversationId() {
        PrefsManager.get().remove(PrefsManager.PREF_CONVERSATION_ID)
    }

    fun getConversationId(): String? {
        return PrefsManager.get().getString(PrefsManager.PREF_CONVERSATION_ID, "")
    }
}