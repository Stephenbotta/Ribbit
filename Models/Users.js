
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let followers = { type: Schema.ObjectId, ref: "Users", default: [] }

let following = { type: Schema.ObjectId, ref: "Users", default: [] };

let blockedBy = { type: Schema.ObjectId, ref: "Users" }


let blockedWhom = { type: Schema.ObjectId, ref: "Users" }

let hidePersonalInfoFromUser = {
    type: Schema.ObjectId, ref: "Users"
};

let gender = {
    type: String,
    default: "",
    enum: [
        Configs.APP_CONSTANTS.DATABASE.GENDER.MALE,
        Configs.APP_CONSTANTS.DATABASE.GENDER.FEMALE,
        Configs.APP_CONSTANTS.DATABASE.GENDER.OTHERS,
        ""
    ],
};

let userType = {
    type: String,
    enum: [
        Configs.APP_CONSTANTS.DATABASE.NEW_USER_TYPE.STUDENT,
        Configs.APP_CONSTANTS.DATABASE.NEW_USER_TYPE.MENTOR,
    ],
};

let Users = new Schema({
    fullName: { type: String, trim: true, sparse: true },
    firstName: { type: String, trim: true, sparse: true },
    lastName: { type: String, trim: true, sparse: true },
    userName: { type: String, trim: true, sparse: true, unique: true },
    isSocialLogin: { type: Boolean, default: false },
    countryCode: { type: String, sparse: true },
    phoneNumber: { type: String, sparse: true }, //
    fullphoneNumber: { type: String, sparse: true },
    password: { type: String, sparse: true },
    email: { type: String, sparse: true }, //
    facebookId: { type: String, sparse: true },
    appleId: { type: String, sparse: true },
    googleId: { type: String, sparse: true },
    interestTags: [{ type: Schema.ObjectId, ref: 'Categories' }],
    currentLocation: { type: [Number], default: [0, 0] },
    locationTime: { type: Number, default: 0 },
    loginWith: { type: Number, default: 1 },
    locationName: { type: String, default: "" },
    locationAddress: { type: String, default: "" },
    dateOfBirth: { type: Number, default: 0 },  //
    designation: { type: String, default: '' },
    isPrivate: { type: Boolean, default: false },
    website: { type: String, default: '' },
    company: { type: String, default: '' },
    QRCode: { type: String, default: '' },
    age: { type: Number },
    gender: gender, //
    userType: userType,
    imageUrl: {
        original: { type: String, default: "" },
        thumbnail: { type: String, default: "" },
    },
    groupFollowed: [{ type: Schema.ObjectId, ref: 'Groups' }],
    bio: { type: String, default: null },
    registrationDate: { type: Number, default: 0 },
    lastLogin: { type: Number, default: 0 },
    followerCount: { type: Number, default: 0 },
    followers: [followers],
    followingCount: { type: Number, default: 0 },
    following: [following],
    blockedBy: [blockedBy],
    blockedWhom: [blockedWhom],
    // hidePersonalInfoFromUser:[hidePersonalInfoFromUser],
    OTPcode: { type: String, default: "" },
    isVerified: { type: Boolean, default: false },    // for otp 
    isProfileComplete: { type: Boolean, default: false },   //for profile completion
    isBlocked: { type: Boolean, default: false },
    isSocialVerified: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
    isInterestSelected: { type: Boolean, default: false },
    deviceToken: { type: String, trim: true, default: '' },
    deviceId: { type: String, trim: true, default: '' },
    platform: { type: String, enum: [Configs.APP_CONSTANTS.platform.ANDROID, Configs.APP_CONSTANTS.platform.IOS, Configs.APP_CONSTANTS.platform.WEB], default: Configs.APP_CONSTANTS.platform.WEB },
    apnsDeviceToken: { type: String, trim: true, default: '' },
    accessToken: { type: String, trim: true, sparse: true },
    isOnline: { type: Boolean, default: false },
    isPasswordExist: { type: Boolean, default: false },
    isPasswordReset: { type: Boolean, default: false },
    homeSearchTop: [{ type: Schema.ObjectId, ref: 'Users', default: [] }],
    tagsFollowed: [{ type: Schema.ObjectId, ref: 'Tags', default: [] }],
    isPhoneNumberVerified: { type: Boolean, default: false },
    isEmailVerified: { type: Boolean, default: false },
    isUploaded: { type: Boolean, default: false },
    passportDocUrl: { type: String, trim: true, default: '' },
    isPassportVerified: { type: Boolean, default: false },
    isAccountPrivate: { type: Boolean, default: false },
    imageVisibilityForFollowers: { type: Boolean, default: false },
    imageVisibilityForEveryone: { type: Boolean, default: true },
    imageVisibility: [{ type: Schema.ObjectId, ref: 'Users', default: [] }],
    locationVisibility: { type: Boolean, default: true },
    nameVisibilityForFollowers: { type: Boolean, default: false },
    nameVisibilityForEveryone: { type: Boolean, default: true },
    nameVisibility: [{ type: Schema.ObjectId, ref: 'Users', default: [] }],
    tagPermissionForFollowers: { type: Boolean, default: false },
    tagPermissionForEveryone: { type: Boolean, default: true },
    tagPermission: [{ type: Schema.ObjectId, ref: 'Users', default: [] }],
    personalInfoVisibilityForFollowers: { type: Boolean, default: false },
    personalInfoVisibility: [{ type: Schema.ObjectId, ref: 'Users', default: [] }],
    personalInfoVisibilityForEveryone: { type: Boolean, default: false },
    alertNotifications: { type: Boolean, default: true },
    appInviteEmails: [{ type: String }],
    isPromoted: { type: Boolean, default: false },
    /** Refferal code */
    referralCode: { type: String, default: '' },
    //spinWheel
    spinWheelTime: { type: String, default: "" },
    //getSpinWheel: { type: Boolean, default: false },
    /** Points earned and point redeemed */
    pointEarned: { type: Number, default: 0 },
    pointRedeemed: { type: Number, default: 0 },
    totalSurveyGiven: { type: Number, default: 0 },

    /** Take survey screen properties */
    isTakeSurvey: { type: Boolean, default: false },
    race: {
        type: String, default: '', enum: [
            Configs.APP_CONSTANTS.DATABASE.RACE.AMERICAN_INDIAN,
            Configs.APP_CONSTANTS.DATABASE.RACE.AFRICAN_AMERICAN,
            Configs.APP_CONSTANTS.DATABASE.RACE.ASIAN,
            Configs.APP_CONSTANTS.DATABASE.RACE.NATIVE_HAWAIIAN,
            Configs.APP_CONSTANTS.DATABASE.RACE.WHITE,
            '',
        ]
    },
    houseHoldIncome: { type: String, default: '' },
    homeOwnership: {
        type: String, default: '', enum: [
            Configs.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_MORTGAGE,
            Configs.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_OUTRIGHT,
            Configs.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.RENTED,
            ''
        ]
    },
    education: {
        type: String, default: '', enum: [
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.ELEMENTRY_SCHOOL,
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.MIDDLE_SCHOOL,
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.HIGH_SCHOOL,
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.BACHELORS,
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.MASTERS,
            Configs.APP_CONSTANTS.DATABASE.EDUCATION.DOCTORAL,
            ''
        ]
    },
    employementStatus: {
        type: String, default: '', enum: [
            Configs.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.EMPLOYED,
            Configs.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.UNEMPLOYED,
            ''
        ]
    },
    maritalStatus: {
        type: String, default: '', enum: ([
            Configs.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SINGLE,
            Configs.APP_CONSTANTS.DATABASE.MARITAL_STATUS.MARRIED,
            Configs.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SEPERATED,
            Configs.APP_CONSTANTS.DATABASE.MARITAL_STATUS.WIDOWED,
            ''
        ])
    },
    // check count for sms for challanges
    smsCountChallenge: { type: Number, default: 0 },
    isSmsChallengeRewarded: { type: Number, default: 0 }
}, {
    timestamps: true,
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
});

Users.index({ "currentLocation": "2dsphere" });

/**use vitual populate for get total surveys counts */
Users.virtual('totalSurveys', {
    ref: 'UserSurveys', // The model to use
    localField: '_id', // Find people where `localField`
    foreignField: 'userId', // is equal to `foreignField`
    count: true // And only get the number of docs
});

Users.pre('save', function (next) {
    let time = (new Date().getTime()).toString().substring(5, 13);
    this.referralCode = time;
    next();
});

// Users.post('save', function (doc, next) {
//     let time = (new Date().getTime()).toString().substring(5, 13);
//     let refferalData = doc.firstName.substring(0, 3) + time;
//     this.referralCode = refferalData;
//     this.set({ referralCode: refferalData })
//     next();
// });

module.exports = mongoose.model('Users', Users);