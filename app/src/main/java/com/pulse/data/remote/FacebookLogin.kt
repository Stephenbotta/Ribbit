package com.pulse.data.remote

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.pulse.data.remote.models.social.FacebookProfile
import com.pulse.utils.SocialUtils
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import org.json.JSONObject
import java.util.*

class FacebookLogin(private val listener: FacebookLoginListener) {
    companion object {
        private const val FACEBOOK_PROFILE_ID = "id"
        private const val FACEBOOK_FULL_NAME = "name"
        private const val FACEBOOK_GENDER = "gender"
        private const val FACEBOOK_PROFILE_PICTURE = "picture.height(500).width(500)"
        private const val FACEBOOK_FIRST_NAME = "first_name"
        private const val FACEBOOK_LAST_NAME = "last_name"
        private const val FACEBOOK_EMAIL = "email"
        private const val FACEBOOK_FIELDS = "fields"
    }

    private val callbackManager by lazy { CallbackManager.Factory.create() }

    init {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                listener.onFacebookLoginSuccess()
            }

            override fun onCancel() {
                listener.onFacebookLoginCancel()
            }

            override fun onError(exception: FacebookException) {
                listener.onFacebookLoginError(exception)
                AccessToken.refreshCurrentAccessTokenAsync()
            }
        })
    }

    fun performLogin(activity: AppCompatActivity) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"))
    }

    fun performLogin(fragment: Fragment) {
        LoginManager.getInstance().logInWithReadPermissions(fragment, Arrays.asList("email", "public_profile"))
    }

    fun getUserProfile() {
        val request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken()) { jsonObject, _ ->
            if (jsonObject == null) {
                listener.onFacebookLoginError(FacebookException("Response is null"))
            } else {
                val facebookId = getKeyOrEmpty(jsonObject, FACEBOOK_PROFILE_ID)
                val profile = FacebookProfile(
                        facebookId,
                        getKeyOrEmpty(jsonObject, FACEBOOK_EMAIL),
                        SocialUtils.getFacebookProfileImage(facebookId),
                        getKeyOrEmpty(jsonObject, FACEBOOK_FIRST_NAME),
                        getKeyOrEmpty(jsonObject, FACEBOOK_LAST_NAME),
                        getKeyOrEmpty(jsonObject, FACEBOOK_FULL_NAME))
                listener.onFacebookProfileSuccess(profile)
            }
            LoginManager.getInstance().logOut()
        }

        val parameters = Bundle()
        parameters.putString(FACEBOOK_FIELDS, (FACEBOOK_PROFILE_ID + "," +
                FACEBOOK_EMAIL + "," + FACEBOOK_FIRST_NAME + "," +
                FACEBOOK_LAST_NAME + "," + FACEBOOK_FULL_NAME + "," +
                FACEBOOK_GENDER + "," + FACEBOOK_PROFILE_PICTURE))
        request.parameters = parameters
        request.executeAsync()
    }

    private fun getKeyOrEmpty(jsonObject: JSONObject, key: String): String {
        return jsonObject.optString(key, "")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    fun isAccessTokenValid(): Boolean {
        val token = AccessToken.getCurrentAccessToken()
        return token != null && !token.isExpired
    }

    fun getAccessToken(): String {
        return AccessToken.getCurrentAccessToken().token
    }

    fun unregisterCallback() {
        LoginManager.getInstance().unregisterCallback(callbackManager)
    }

    interface FacebookLoginListener {
        fun onFacebookLoginSuccess()
        fun onFacebookLoginCancel()
        fun onFacebookLoginError(exception: FacebookException)
        fun onFacebookProfileSuccess(profile: FacebookProfile)
    }
}