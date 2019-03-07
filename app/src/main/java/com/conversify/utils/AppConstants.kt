package com.conversify.utils

object AppConstants {
    const val MODE_LOGIN = 0
    const val MODE_SIGN_UP = 1

    // Used within the app only
    const val REGISTERED_MODE_PHONE = 0
    const val REGISTERED_MODE_EMAIL = 1

    const val PRIVATE_TRUE = 1
    const val PRIVATE_FALSE = 2

    const val VENUE_VERIFICATION_FILE_SIZE_LIMIT: Long = 5 * 1024 * 1024  // 5 MB
    const val MAXIMUM_VIDEO_SIZE: Long = 200 * 1024 * 1024    // 200 MB
    const val MINIMUM_FREE_SPACE: Long = 250 * 1024 * 1024    // 250 MB

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
    const val REQ_CODE_GROUP_TOPICS = 114
    const val REQ_CODE_TOPIC_GROUPS = 115
    const val REQ_CODE_CHOOSE_INTERESTS = 116
    const val REQ_CODE_ADD_PARTICIPANTS = 117
    const val REQ_CODE_NEW_POST = 118
    const val REQ_CODE_POST_DETAILS = 119
    const val REQ_CODE_JOIN_VENUE = 120
    const val REQ_CODE_INDIVIDUAL_CHAT = 121
    const val REQ_CODE_LISTING_INDIVIDUAL_CHAT = 122
    const val REQ_CODE_LISTING_GROUP_CHAT = 123
    const val REQ_CODE_LISTING_GROUP_DETAILS = 124
    const val REQ_CODE_GROUP_CHAT = 125
    const val REQ_CODE_GROUP_DETAILS = 126
    const val REQ_CODE_HOME_SEARCH = 127
    const val REQ_CODE_CREATE_NEW_POST = 128
    const val REQ_CODE_GROUP_DETAILS_MORE_OPTIONS = 129
    const val REQ_CODE_EXIT_GROUP = 130
    const val REQ_CODE_BLOCK_USER = 131
    const val REQ_CODE_PEOPLE = 132
    const val REQ_CODE_CONTACT_US = 133
    const val REQ_CODE_TERMS_AND_CONDITIONS = 134
    const val REQ_CODE_HIDE_INFO_STATUS = 135
    const val REQ_CODE_CONVERSE_NEARBY = 136
    const val REQ_CODE_CROSSED_PATH = 137
    const val REQ_CODE_EDIT_POST = 138
    const val REQ_CODE_PENDING_INTENT = 139

    const val REQ_CODE_POST_LIKE = 140


    const val EXTRA_VENUE_FILTERS = "EXTRA_VENUE_FILTERS"
    const val EXTRA_VENUE = "EXTRA_VENUE"
    const val EXTRA_INTEREST = "EXTRA_INTEREST"
    const val EXTRA_GROUP = "EXTRA_GROUP"
    const val EXTRA_PARTICIPANTS = "EXTRA_PARTICIPANTS"
    const val EXTRA_GROUP_POST = "EXTRA_GROUP_POST"
    const val EXTRA_PROFILE = "EXTRA_PROFILE"
    const val EXTRA_POST_DATA = "EXTRA_POST_DATA"
    const val EXTRA_FOLLOWERS = "EXTRA_FOLLOWERS"
    const val EXTRA_POST_ID = "EXTRA_POST_ID"

    const val POST_TYPE_REGULAR = "REGULAR"
    const val POST_TYPE_CONVERSE_NEARBY = "CONVERSE_NEARBY"
    const val POST_TYPE_LOOK_NEARBY = "LOOK_NEARBY"

    const val POST_IN_PUBLICILY = "PUBLICILY"
    const val POST_IN_FOLLOWERS = "FOLLOWERS"
    const val POST_IN_SELECTED_PEOPLE = "SELECTED_PEOPLE"

    const val ACTION_GROUP_POSTS_LOADED = "ACTION_GROUP_POSTS_LOADED"
    const val ACTION_GROUP_POST_UPDATED_POST_DETAILS = "ACTION_GROUP_POST_UPDATED_POST_DETAILS"
    const val ACTION_GROUP_POST_UPDATED_GROUP_POSTS_LISTING = "ACTION_GROUP_POST_UPDATED_GROUP_POSTS_LISTING"

    const val TITLE_SHARE_VIA = "Share via"
    const val PLAY_STORE_URL = "http://play.google.com/store/apps/details?id="

}