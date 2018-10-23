package com.conversify.data.local

import java.util.*

object UserManager {
    fun saveRememberedEmail(email: String) {
        PrefsManager.get().save(PrefsManager.PREF_REMEMBERED_EMAIL, email)
    }

    fun getRememberedEmail(): String {
        return PrefsManager.get().getString(PrefsManager.PREF_REMEMBERED_EMAIL, "")
    }

    fun removeRememberedEmail() {
        PrefsManager.get().remove(PrefsManager.PREF_REMEMBERED_EMAIL)
    }

    fun isFirstAppLaunch(): Boolean {
        return PrefsManager.get().getBoolean(PrefsManager.PREF_FIRST_APP_LAUNCH, true)
    }

    fun setFirstAppLaunched() {
        // Set first app launched to false so that we won't show walkthrough next time
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
            "Bearer $accessToken"
        }
    }

    fun getDeveloperAuthorization(): String {
        return "Bearer ${getAccessToken()}"
    }

    /*fun saveProfile(profile: UserProfileDto) {
        PrefsManager.get().save(PrefsManager.PREF_ACCESS_TOKEN, profile.token ?: "")
        PrefsManager.get().save(PrefsManager.PREF_USER_PROFILE, profile)
        PrefsManager.get().save(PrefsManager.PREF_USER_ID, profile.id ?: "")
    }*/

    fun isLoggedIn(): Boolean {
        return PrefsManager.get().contains(PrefsManager.PREF_USER_PROFILE)
    }

    /*fun getProfile(): UserProfileDto {
        return PrefsManager.get().getObject(PrefsManager.PREF_USER_PROFILE, UserProfileDto::class.java)
                ?: UserProfileDto()
    }*/

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