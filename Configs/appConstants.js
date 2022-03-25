/**
 * @description APP_CONSTANTS assist us to alter the messages for error and success cases
 */

/**
* @description Database messages
*/
const DATABASE = {

    POST_TYPE: {
        REGULAR: "REGULAR",
        CONVERSE_NEARBY: "CONVERSE_NEARBY",
        LOOK_NEARBY: "LOOK_NEARBY"
    },
    POSTING_IN: {
        PUBLICILY: "PUBLICILY",
        FOLLOWERS: "FOLLOWERS",
        SELECTED_PEOPLE: "SELECTED_PEOPLE"
    },
    FOLLOWING_STATUS: {
        PENDING: "PENDING",
        ACCEPTED: "ACCEPTED",
    },
    REDEEM_POINT_TYPE: {
        GIFTCARD: "GiftCard",
        DONATION: "Donation",
    },
    GENDER: {
        MALE: "MALE",
        FEMALE: "FEMALE",
        OTHERS: "OTHERS",
    },
    TWITTER_TIME_COUNT: {
        TIME: 5
    },
    TWITTER_FIX_TIME: {
        TIME: 15
    },
    POINT_PER_MINUTE: {
        POINT: 10
    },
    MEDIA_TYPE: {
        VIDEO: 'VIDEO',
        AUDIO: 'AUDIO',
        IMAGE: 'IMAGE',
        TEXT: 'TEXT',
        GIF: 'GIF'
    },
    MESSAGE_TYPE: {
        INDIVIDUAL: 'INDIVIDUAL',
        GROUP: 'GROUP',
        VENUE: 'VENUE'
    },
    GROUP_TYPE: {
        GROUP: 'GROUP',
        VENUE: 'VENUE'
    },
    USER_TYPE: {
        USER: 'USER',
        ADMIN: 'ADMIN'
    },
    PROFILE_PIC_PREFIX: {
        ORIGINAL: 'Pic_',
        THUMB: 'Thumb_'
    },
    DAILY_CHALLENGES: {
        SMS_CHALLENGE: "SMS_CHALLENGE",
        POST_CHALLENGE: "POST_CHALLENGE",
        STORY_CHALLENGE: "STORY_CHALLENGE"
    },
    NOTIFICATION_TYPE: {
        REQUEST_FOLLOW: "REQUEST_FOLLOW",
        FOLLOW: "FOLLOW",
        REQUEST_VENUE: 'REQUEST_VENUE',
        REQUEST_GROUP: 'REQUEST_GROUP',
        LIKE_POST: 'LIKE_POST',
        LIKE_COMMENT: 'LIKE_COMMENT',
        LIKE_REPLY: 'LIKE_REPLY',
        LIKE_MEDIA: 'LIKE_MEDIA',
        COMMENT: 'COMMENT',
        REPLY: 'REPLY',
        INVITE_VENUE: "INVITE_VENUE",
        INVITE_GROUP: "INVITE_GROUP",
        JOINING_PUBLIC_VENUE: "JOINING_PUBLIC_VENUE",
        POST: "POST",
        GROUP: "GROUP",
        VENUE: "VENUE",
        CHALLENGE: "CHALLENGE",
        ACCEPT_INVITE_VENUE: "ACCEPT_INVITE_VENUE",
        ACCEPT_INVITE_GROUP: "ACCEPT_INVITE_GROUP",
        ACCEPT_REQUEST_VENUE: "ACCEPT_REQUEST_VENUE",
        ACCEPT_REQUEST_GROUP: "ACCEPT_REQUEST_GROUP",
        ACCEPT_REQUEST_FOLLOW: "ACCEPT_REQUEST_FOLLOW",
        TAG_COMMENT: "TAG_COMMENT",
        TAG_REPLY: "TAG_REPLY",
        JOINED_VENUE: "JOINED_VENUE",
        JOINED_GROUP: "JOINED_GROUP",
        ALERT_LOOK_NEARBY_PUSH: "ALERT_LOOK_NEARBY_PUSH",
        ALERT_CONVERSE_NEARBY_PUSH: "ALERT_CONVERSE_NEARBY_PUSH",
        RECEIVED_REDEEM_POINTS: "RECEIVED_REDEEM_POINTS",
        SPEND_EARNED_POINT: "SPEND_EARNED_POINT",
        CALLING: "CALLING",
        CALL_DISCONNECT: "Call_Disconnect"
    },
    NEW_USER_TYPE: {
        STUDENT: 'STUDENT',
        MENTOR: 'MENTOR'
    },
    DEFAULT_IMAGE_URL: {
        PROFILE: {
            ORIGINAL: "https://s3-us-west-2.amazonaws.com/conversifybucket/Portrait_Placeholder.png",
            THUMBNAIL: "https://s3-us-west-2.amazonaws.com/conversifybucket/Portrait_Placeholder.png"
        },
        VENUE: {
            ORIGINAL: "https://s3-us-west-2.amazonaws.com/conversifybucket/venue_icon.jpg",
            THUMBNAIL: "https://s3-us-west-2.amazonaws.com/conversifybucket/venue_icon.jpg"
        },
        GROUP: {
            ORIGINAL: "https://s3-us-west-2.amazonaws.com/conversifybucket/multiple-users-silhouette%403x.png",
            THUMBNAIL: "https://s3-us-west-2.amazonaws.com/conversifybucket/multiple-users-silhouette%403x.png"
        }
    },
    QUESTION_TYPES: {
        SINGLE_VALUE: 1,
        MULTI_VALUE: 2
    },
    RACE: {
        AFRICAN_AMERICAN: "African American",
        AMERICAN_INDIAN: "American Indian",
        ASIAN: "Asian",
        NATIVE_HAWAIIAN: "Native Hawaiian",
        WHITE: "White",
    },
    HOME_ONWERSHIP: {
        OWN_HOUSE_OUTRIGHT: "Own House (outright)",
        OWN_HOUSE_MORTGAGE: "Own house (mortgage)",
        RENTED: "Rented",
    },
    EDUCATION: {
        ELEMENTRY_SCHOOL: "Elementry School",
        MIDDLE_SCHOOL: "Middle School",
        HIGH_SCHOOL: "High School",
        BACHELORS: "bachelor's",
        MASTERS: "master's",
        DOCTORAL: "doctoral",
    },
    EMPLOYMENT_STATUS: {
        EMPLOYED: "Employed",
        UNEMPLOYED: "Unemployed",
    },
    MARITAL_STATUS: {
        SINGLE: "Single",
        MARRIED: "Married",
        SEPERATED: "Seperated",
        WIDOWED: "Widowed",
    },
    HOUSE_HOLD_INCOME: {
        VALUE_1: "Less then $20,000",
        VALUE_2: "$20,000 - $44,999",
        VALUE_3: "$140,000 - $149,999",
        VALUE_4: "$150,000 - $199,999",
        VALUE_5: "$200,000+",
    },
    DOLLAR_PER_POINT: {
        USD: 0.008
    },
    POINT_PER_DOLLAR: {
        POINT: 125
    },
    SPIN_WHEEL_DURATION: {
        HOURS: 24
    },
    REDEEM_POINTS: {
        CREDIT: "Credit",
        DEBIT: "Debit",
    },
    POINT_STRUCTURE: {
        SURVEY: "Survey",
        CHALLENGE: "Challenge",
        SIGNUP_REWARDS: "Signup rewards",
        GAMIFICATION: "Gamification",
        THIRD_PARTY: "Third party",
    },
    POINT_EARNED_SOURCE: {
        TWITTER: "Twitter",
        SPIN_WHEEL: "Spin_Wheel",
        SURVEY: "Survey",
        CHALLENGE: "Challenge",
        INVITE_PEOPLE: "Invite_People"
    }
}

/**
 * @description JWT secret key
 */
const SERVER = {
    APP_NAME: 'Rabbit',
    JWT_SECRET_KEY: 'MaEHqzXzdWrCS6TS',
    APPLE_GATEWAY: "gateway.sandbox.push.apple.com"
}

/**
* @description status message 
*/
const STATUS_MSG = {
    ERROR: {
        INSUFFICIENT_AMOUNT: {
            statusCode: 400,
            customMessage: "You Have Low Points To Get This Reward",
            type: "INSUFFICIENT_AMOUNT"
        },
        CANT_DEACTIVE_SPIN_WHEEL: {
            statusCode: 400,
            customMessage: "You can't deactivate this Spin Wheel. Please activate any other spin wheel first.",
            type: "CANT_DEACTIVE_SPIN_WHEEL"
        },
        NO_SURVEY_FOUND: {
            statusCode: 400,
            customMessage: "No Survey Found",
            type: "NO_SURVEY_FOUND"
        },
        NO_USER_FOUND_FOR_THIS_REFFRAL_CODE: {
            statusCode: 400,
            customMessage: "No user Found for this reffrel Code please Enter Correct Code",
            type: "NO_USER_FOUND_FOR_THIS_REFFRAL_CODE"
        },
        INSUFFICIENT_POINT: {
            statusCode: 400,
            customMessage: "You Have Low Points To Make Charity",
            type: "INSUFFICIENT_AMOUNT"
        },
        POST_DELETED: {
            statusCode: 400,
            customMessage: "Post deleted by post owner",
            type: "POST_DELETED"
        },
        INVITE_REQUEST_ALREADY: {
            statusCode: 400,
            customMessage: "You have invite request pending for the same channel",
            type: "INVITE_REQUEST_ALREADY"
        },
        BLOCKED_BY_ADMIN: {
            statusCode: 400,
            customMessage: "Sorry you can not post in the group, you are blocked by the channel admin",
            type: "BLOCKED_BY_ADMIN"
        },
        ALREADY_A_MEMBER: {
            statusCode: 400,
            customMessage: "You are already member of the group.",
            type: "ALREADY_A_MEMBER"
        },
        ALREADY_SPIN_WHEEL: {
            statusCode: 400,
            customMessage: "Your Today Spin Wheel Limit Is Over",
            type: "ALREADY_SPIN_WHEEL"
        },
        REQUEST_SEND_ALREADY: {
            statusCode: 400,
            customMessage: 'Follow request has already been sent to this user.',
            type: 'REQUEST_SEND_ALREADY'
        },
        USERNAME_CONTAINS_SPACE: {
            statusCode: 400,
            customMessage: 'Username should not contain space',
            type: 'USERNAME_CONTAINS_SPACE'
        },
        USERNAME_ALREADY_REGISTERED: {
            statusCode: 400,
            customMessage: 'Username you have entered is already registered with us.',
            type: 'USERNAME_ALREADY_REGISTERED'
        },
        EMAIL_ALREADY_EXITS: {
            statusCode: 400,
            customMessage: 'Email already registered with other user',
            type: 'EMAIL_ALREADY_EXITS'
        },
        NOT_ADMIN: {
            statusCode: 400,
            customMessage: 'You are not admin to make these changes',
            type: 'NOT_ADMIN'
        },
        NOTIFICATION_SEND_ALREADY_FOR_VENUE: {
            statusCode: 400,
            customMessage: 'You have already requested to join this private venue.',
            type: 'NOTIFICATION_SEND_ALREADY_FOR_VENUE'
        },
        NOTIFICATION_SEND_ALREADY_FOR_GROUP: {
            statusCode: 400,
            customMessage: 'You have already requested to join this private group.',
            type: 'NOTIFICATION_SEND_ALREADY_FOR_GROUP'
        },
        ENTER_COUNTRY_CODE: {
            statusCode: 400,
            customMessage: 'Please enter the country code, for instance: +91',
            type: 'ENTER_COUNTRY_CODE'
        },
        TOKEN_EXPIRED: {
            statusCode: 401,
            customMessage: 'Sorry, your account has been logged in other device! Please login again to continue.',
            type: 'TOKEN_ALREADY_EXPIRED'
        },
        BLOCKED: {
            statusCode: 405,
            customMessage: 'This account is blocked by Admin. Please contact support team to activate your account.',
            type: 'BLOCKED'
        },
        DB_ERROR: {
            statusCode: 400,
            customMessage: 'DB Error : ',
            type: 'DB_ERROR'
        },
        INVALID_PASSWORD: {
            statusCode: 400,
            customMessage: 'Password you have entered does not match.',
            type: 'INVALID_PASSWORD'
        },
        INVALID_USERNAME: {
            statusCode: 400,
            customMessage: 'Username you have entered does not exists.',
            type: 'INVALID_USERNAME'
        },
        INVALID_INFO: {
            statusCode: 400,
            customMessage: 'Invalid info entered.',
            type: 'INVALID_INFO'
        },
        FACEBOOK_ALREADY_EXIST: {
            statusCode: 400,
            customMessage: 'Your account is already registered with us.',
            type: 'FACEBOOK_ALREADY_EXIST'
        },
        GOOGLE_ALREADY_EXIST: {
            statusCode: 400,
            customMessage: 'Your account is already registered with us.',
            type: 'GOOGLE_ALREADY_EXIST'
        },
        ALREADY_EXIST: {
            statusCode: 400,
            customMessage: 'Email address you have entered is already registered with us.',
            type: 'ALREADY_EXIST'
        },
        LINK_EXPIRE: {
            statusCode: 400,
            customMessage: 'This link is expired, Kindly resubmit your email to get new link.',
            type: 'ALREADY_EXIST'
        },
        USERNAME_EXIST: {
            statusCode: 400,
            customMessage: 'User name you have entered is already taken.',
            type: 'USERNAME_EXIST'
        },
        PHONE_ALREADY_EXIST: {
            statusCode: 400,
            customMessage: 'Phone number you have entered is already registered with us.',
            type: 'PHONE_ALREADY_EXIST'
        },
        IMP_ERROR: {
            statusCode: 500,
            customMessage: 'Implementation error',
            type: 'IMP_ERROR'
        },
        APP_ERROR: {
            statusCode: 400,
            customMessage: 'Application Error',
            type: 'APP_ERROR'
        },
        ID_MISSING: {
            statusCode: 400,
            customMessage: 'Id Missing',
            type: 'ID_MISSING'
        },
        DUPLICATE: {
            statusCode: 400,
            customMessage: 'Duplicate Entry',
            type: 'DUPLICATE'
        },
        USERNAME_INVALID: {
            statusCode: 400,
            customMessage: 'Username you have entered does not match.',
            type: 'USERNAME_INVALID'
        },
        INVALID_EMAIL: {
            statusCode: 400,
            customMessage: 'The email address you have entered does not match.',
            type: 'INVALID_EMAIL'
        },
        ALREADY_REPORT: {
            statusCode: 400,
            customMessage: 'This post has been already reported.',
            type: 'ALREADY_REPORT',
        },
        INVALID_TOKEN: {
            statusCode: 400,
            customMessage: 'The token you have entered does not match.',
            type: 'INVALID_TOKEN'
        },
        SAME_PASSWORD: {
            statusCode: 400,
            customMessage: 'New password can\'t be same as Old password.',
            type: 'SAME_PASSWORD'
        },
        INCORRECT_OLD_PASSWORD: {
            statusCode: 400,
            customMessage: 'Old password you have entered does not match.',
            type: 'INCORRECT_OLD_PASSWORD'
        },
        INVALID_CODE: {
            statusCode: 400,
            customMessage: 'Otp you have entered does not match.',
            type: 'INVALID_CODE'
        },
        NOT_EXIST: {
            statusCode: 400,
            customMessage: 'You have not registered yet.',
            type: 'NOT_EXIST'
        },
        NOT_APPROVED: {
            statusCode: 400,
            customMessage: 'Your profile is not approved by admin.',
            type: 'NOT_APPROVED'
        },
        CATEGORY_NAME: {
            statusCode: 400,
            customMessage: 'Category name you have entered is already exist.',
            type: 'CATEGORY_NAME'
        },
        SUBCATEGORY_NAME: {
            statusCode: 400,
            customMessage: 'Sub category name you have entered is already exist.',
            type: 'SUBCATEGORY_NAME'
        },
        CITY_NAME: {
            statusCode: 400,
            customMessage: 'City name you have entered is already exist.',
            type: 'CITY_NAME'
        },
        ROOM_NAME: {
            statusCode: 400,
            customMessage: 'Room name you have entered is already exist.',
            type: 'ROOM_NAME'
        },
        STYLE_NAME: {
            statusCode: 400,
            customMessage: 'Style name you have entered is already exist.',
            type: 'STYLE_NAME'
        },
        BRAND_NAME: {
            statusCode: 400,
            customMessage: 'Brand name you have entered is already exist.',
            type: 'BRAND_NAME'
        },
        COLOR_NAME: {
            statusCode: 400,
            customMessage: 'Color name you have entered is already exist.',
            type: 'COLOR_NAME'
        },
        SIZE_NAME: {
            statusCode: 400,
            customMessage: 'Size you have entered is already exist.',
            type: 'SIZE_NAME'
        },
        COUNTRY_NAME: {
            statusCode: 400,
            customMessage: 'Country name you have entered is already exist.',
            type: 'COUNTRY_NAME'
        },
        CANT_DELETE_SPIN_WHEEL: {
            statusCode: 400,
            customMessage: 'You Cannot delete this Spin Wheel',
            type: 'CANT_DELETE_SPIN_WHEEL'
        },
        DUPLICATE_EMAIL: {
            statusCode: 400,
            customMessage: 'Duplicate email entered',
            type: 'DUPLICATE_EMAIL'
        },
        INVALID_PHONE_NUMBER: {
            statusCode: 400,
            customMessage: 'Invalid phone number entered',
            type: 'INVALID_PHONE_NUMBER'
        },
        NOT_ABLE_TO_SAVE: {
            statusCode: 400,
            customMessage: 'DB Error: Not able to Save Information. Try again',
            type: 'NOT_ABLE_TO_SAVE'
        },
        NUMBER_NOT_REGISTERED: {
            statusCode: 400,
            customMessage: 'Entered number is not registered',
            type: 'NUMBER_NOR_REGISTERED'
        },
        EMAIL_NOT_REGISTERED: {
            statusCode: 400,
            customMessage: 'Entered email is not registered',
            type: 'EMAIL_NOT_REGISTERED'
        },
        NOT_REGISTERED: {
            statusCode: 400,
            customMessage: 'User info you have entered does not match.',
            type: 'NOT_REGISTERED'
        },
        ACCOUNT_VERIFIED_ALREADY: {
            statusCode: 400,
            customMessage: 'Your account is verified already',
            type: 'ACCOUNT_VERIFIED_ALREADY'
        },
        INVALID_ID: {
            statusCode: 400,
            customMessage: 'Invalid Id Provided : ',
            type: 'INVALID_ID'
        },
        SOMETHING_WENT_WRONG: {
            statusCode: 400,
            customMessage: 'Something went Wrong',
            type: 'SOMETHING_WENT_WRONG'
        },
        GROUP_NOT_EXIST: {
            statusCode: 400,
            customMessage: "This venue does not exists any more. Please refresh the listing and try again.",
            type: 'GROUP_NOT_EXIST'
        },
        GROUP_DELETED: {
            statusCode: 400,
            customMessage: "This channel does not exists any more. Please refresh the listing and try again.",
            type: 'GROUP_DELETED'
        },
        LAT_LONG_MISSING: {
            statusCode: 400,
            customMessage: "Latitude and Longitude are missing",
            type: 'LAT_LONG_MISSING'
        },
        OTP_NOT_MATCH: {
            statusCode: 400,
            customMessage: "OTP don't match",
            type: 'OTP_NOT_MATCH'
        },
        NOT_FOUND: {
            statusCode: 400,
            customMessage: "Not found",
            type: 'NOT_FOUND'
        },
        NO_GIFT_CARD_FOUND: {
            statusCode: 400,
            customMessage: "No Gift Cards Found",
            type: 'NOT_FOUND'
        }

    },
    SUCCESS: {
        RESET_PASSWORD_LINK: {
            statusCode: 200,
            customMessage: "Reset password link has been sent on your email successfully.",
            type: "RESET_PASSWORD_LINK"
        },
        USERNAME_AVAILABLE: {
            // statusCode:200,
            customMessage: 'Username available',
            type: true
        },
        CREATED: {
            statusCode: 200,
            customMessage: 'Created Successfully',
            type: 'CREATED'
        },
        DEFAULT: {
            statusCode: 200,
            customMessage: 'Success',
            type: 'DEFAULT'
        },
        UPDATED: {
            statusCode: 200,
            customMessage: 'Updated Successfully',
            type: 'UPDATED'
        },
        LOGOUT: {
            statusCode: 200,
            customMessage: 'Logged Out Successfully',
            type: 'LOGOUT'
        },
        DELETED: {
            statusCode: 200,
            customMessage: 'Deleted Successfully',
            type: 'DELETED'
        },
        REGISTER: {
            statusCode: 200,
            customMessage: 'Register Successfully',
            type: 'REGISTER'
        },
        USERNAME_NOT_AVAILABLE: {
            // statusCode:200,
            customMessage: "Username not available",
            type: false
        },
        NOTIFICATION_SEND_TO_ALL: {
            statusCode: 200,
            customMessage: "Notification is send to all the participants",
            type: false
        },
    }
};

const swaggerDefaultResponseMessages = {
    '200': { 'description': 'Success' },
    '400': { 'description': 'Bad Request' },
    '401': { 'description': 'Unauthorized' },
    '404': { 'description': 'Data Not Found' },
    '500': { 'description': 'Internal Server Error' }
};


const challengeType = {
    // PHOTO:"photo",
    // AUDIO:"audio",
    SURVEY: "survey",
    TASK: "task",
};

const platform = {
    ANDROID: "android",
    IOS: "ios",
    WEB: "web"
};

const userChallengeStatus = {
    NOTSTARTED: "notstarted",
    INPROGESS: "inprogress",
    COMPLETED: "completed"
};


let APP_CONSTANTS = {
    SERVER,
    DATABASE,
    STATUS_MSG,
    swaggerDefaultResponseMessages,
    challengeType,
    userChallengeStatus,
    platform
};



module.exports = APP_CONSTANTS;