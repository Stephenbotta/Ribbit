package com.conversify.data.remote

object ApiConstants {
    const val FLAG_INTERESTS = 1
    const val FLAG_GET_VENUES = 2
    const val FLAG_GET_GROUPS = 3
    const val FLAG_GET_HOME_FEED = 4
    const val FLAG_GET_YOUR_GROUPS = 5

    const val FLAG_ALERT_NOTIFICATION = 8
    const val FLAG_PRIVATE_ACCOUNT = 2
    const val FLAG_LOCATION_VISIBILITY = 5

    //Setting hide personal info
    const val FLAG_PROFILE_PICTURE = 3
    const val FLAG_PRIVATE_INFO = 7
    const val FLAG_USERNAME = 4
    const val FLAG_MESSAGE = 6

    const val TYPE_VENUE = "VENUE"
    const val TYPE_GROUP = "GROUP"

    const val PRIVACY_PRIVATE = 1
    const val PRIVACY_PUBLIC = 2

    // Sent and received for api use
    const val FLAG_REGISTER_FACEBOOK = 1
    const val FLAG_REGISTER_GOOGLE = 2
    const val FLAG_REGISTER_PHONE_NUMBER = 3
    const val FLAG_REGISTER_EMAIL = 4

    const val FLAG_FOLLOWERS = 1
    const val FLAG_FOLLOWINGS = 2

    const val LIKED_TRUE = 1
    const val LIKED_FALSE = 2

    const val MESSAGE_TYPE_TEXT = "TEXT"
    const val MESSAGE_TYPE_IMAGE = "IMAGE"
    const val MESSAGE_TYPE_VIDEO = "VIDEO"

    const val GROUP_POST_TYPE_TEXT = "TEXT"
    const val GROUP_POST_TYPE_IMAGE = "IMAGE"
    const val GROUP_POST_TYPE_VIDEO = "VIDEO"

    const val NOTIFICATION_TYPE_REQUEST_JOIN_VENUE = "REQUEST_VENUE"
    const val NOTIFICATION_TYPE_REQUEST_JOIN_GROUP = "REQUEST_GROUP"
    const val NOTIFICATION_TYPE_INVITE_JOIN_VENUE = "INVITE_VENUE"
    const val NOTIFICATION_TYPE_INVITE_JOIN_GROUP = "INVITE_GROUP"

    const val ACCEPT_TYPE_INVITE = "INVITE"
    const val ACCEPT_TYPE_REQUEST = "REQUEST"

    const val REQUEST_STATUS_NONE = "NONE"
    const val REQUEST_STATUS_PENDING = "PENDING"
    const val REQUEST_STATUS_REJECTED = "REJECTED"

    const val PARTICIPATION_ROLE_MEMBER = "MEMBER"
    const val PARTICIPATION_ROLE_ADMIN = "ADMIN"
}