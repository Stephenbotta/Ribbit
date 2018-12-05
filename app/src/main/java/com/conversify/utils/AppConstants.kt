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
    const val REQ_CODE_GALLERY_VIDEO = 104
    const val REQ_CODE_CAMERA_IMAGE = 105
    const val REQ_CODE_CAMERA_VIDEO = 106
    const val REQ_CODE_PLACE_PICKER = 107
    const val REQ_CODE_CREATE_VENUE = 108
    const val REQ_CODE_FILE_PICKER = 109
    const val REQ_CODE_VENUE_CHAT = 110
    const val REQ_CODE_VENUE_FILTERS = 111
    const val REQ_CODE_VENUE_DETAILS = 112
    const val REQ_CODE_CREATE_GROUP = 113
    const val REQ_CODE_GROUP_TOPIC = 114

    const val EXTRA_VENUE_FILTERS = "EXTRA_VENUE_FILTERS"
    const val EXTRA_VENUE = "EXTRA_VENUE"
    const val EXTRA_INTEREST = "EXTRA_INTEREST"
}