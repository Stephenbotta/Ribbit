const mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Configs = require('../Configs');

var notificationType = {
    type: String, enum: [
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_MEDIA,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.POST,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CHALLENGE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_COMMENT,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_REPLY,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_VENUE,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_GROUP,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
        Configs.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.SPEND_EARNED_POINT
    ]
}

const notifications = new Schema({
    toId: { type: Schema.ObjectId, ref: 'Users', index: true, require: true },
    byId: { type: Schema.ObjectId, ref: 'Users' },
    userId: { type: Schema.ObjectId, ref: 'Users' },
    groupId: { type: Schema.ObjectId, ref: 'PostGroups' },
    postId: { type: Schema.ObjectId, ref: 'Posts' },
    commentId: { type: Schema.ObjectId, ref: 'Comments' },
    replyId: { type: Schema.ObjectId, ref: 'Replies' },
    venueId: { type: Schema.ObjectId, ref: 'VenueGroups' },
    groupType: { type: String, enum: [Configs.APP_CONSTANTS.DATABASE.GROUP_TYPE.GROUP, Configs.APP_CONSTANTS.DATABASE.GROUP_TYPE.VENUE] },
    type: notificationType,
    text: { type: String },
    location: { type: [Number] },
    locationName: { type: String },
    locationAddress: { type: String },
    userName: { type: String },
    conversationId: { type: Schema.ObjectId },
    createdOn: { type: Number, index: true },
    isRead: { type: Boolean, default: false },
    isRejected: { type: Boolean, default: false },
    actionPerformed: { type: Boolean},
    isBlocked: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
}, { timestamp: true });

module.exports = mongoose.model('Notifications', notifications);
