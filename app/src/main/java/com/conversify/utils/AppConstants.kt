package com.conversify.utils

object AppConstants {
    const val MODE_LOGIN = 0
    const val MODE_SIGN_UP = 1

    // Used within the app only
    const val REGISTERED_MODE_PHONE = 0
    const val REGISTERED_MODE_EMAIL = 1

    const val PRIVATE_TRUE = 1
    const val PRIVATE_FALSE = 2

    const val VENUE_VERIFICATION_FILE_SIZE_LIMIT = 5 * 1024 * 1024  // 5 MB

    const val REQ_CODE_GOOGLE_SIGN_IN = 100
    const val REQ_CODE_CHECK_LOCATION_SETTINGS = 101
    const val REQ_CODE_APP_SETTINGS = 102
    const val REQ_CODE_GALLERY_IMAGE = 103
    const val REQ_CODE_CAMERA_IMAGE = 104
    const val REQ_CODE_PLACE_PICKER = 105
    const val REQ_CODE_CREATE_VENUE = 106
    const val REQ_CODE_FILE_PICKER = 107
    const val REQ_CODE_VENUE_FILTERS = 108

    const val EXTRA_VENUE_FILTERS = "EXTRA_VENUE_FILTERS"
}