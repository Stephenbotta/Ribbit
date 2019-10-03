package com.ribbit.utils

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.ribbit.data.remote.ApiConstants
import com.ribbit.data.remote.models.social.FacebookProfile
import com.ribbit.data.remote.models.social.SocialProfile

object SocialUtils {
    fun getFacebookProfileImage(facebookId: String): String {
        return "http://graph.facebook.com/$facebookId/picture?type=large"
    }

    fun getFacebookSocialProfile(facebookProfile: FacebookProfile): SocialProfile {
        return SocialProfile(ApiConstants.FLAG_REGISTER_FACEBOOK,
                facebookProfile.facebookId,
                facebookProfile.fullName,
                facebookProfile.firstName,
                facebookProfile.lastName,
                facebookProfile.profileImage,
                facebookProfile.email)
    }

    fun getGoogleSocialProfile(googleAccount: GoogleSignInAccount): SocialProfile {
        return SocialProfile(ApiConstants.FLAG_REGISTER_GOOGLE,
                googleAccount.id ?: "",
                googleAccount.displayName ?: "",
                googleAccount.givenName ?: "",
                googleAccount.familyName ?: "",
                googleAccount.photoUrl?.toString() ?: "",
                googleAccount.email ?: "")
    }
}