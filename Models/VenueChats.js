const mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Configs = require('../Configs');

var ImageVideoType = {
    type: String, enum: [
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT
    ]
}
    
const venueChats = new Schema({
    conversationId: {type:Schema.ObjectId},
    senderId : {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    groupId : {type :Schema.ObjectId, ref: 'VenueGroups'},
    chatDetails: {
        imageUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        videoUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        message: {type :String},
        userIdTags: [{type :Schema.ObjectId, ref: 'Users'}],
        type: ImageVideoType,
    },
    sentAt : {type : Number},
    // isRead : {type : Boolean, default : false},
    readAt: {type:Date, sparse : true},
    isDelivered : {type : Boolean, default : false},
    deliveredAt : {type:Date, sparse : true},
    createdDate: {type: Number},
    isBlocked:{type:Boolean,default:false},
    isDeleted: {type: Boolean, default: false},
    readBy: [{type: Schema.ObjectId, ref: 'Users'}]
}, {timestamp: true});

module.exports = mongoose.model('VenueChats', venueChats);
