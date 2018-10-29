package com.conversify.utils

import android.support.v4.util.PatternsCompat

object ValidationUtils {
    fun isEmailValid(email: String) = PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()

    fun isPasswordLengthValid(password: String) = password.length in 6..20

    fun isPhoneNumberLengthValid(phoneNumber: String) = phoneNumber.length in 5..15
}