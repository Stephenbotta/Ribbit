const mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Configs = require('../Configs');

var ImageVideoType = {
    type: String, enum: [
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.AUDIO,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
        Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF
    ]
}
    
var chatType = {
    type: String, enum: [
        Configs.APP_CONSTANTS.DATABASE.MESSAGE_TYPE.INDIVIDUAL,
        Configs.APP_CONSTANTS.DATABASE.MESSAGE_TYPE.GROUP
    ]
}
const Chats = new Schema({
    conversationId:{type:Schema.ObjectId},
    senderId : {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    receiverId : {type: Schema.ObjectId, ref: 'Users'},
    chatType : chatType,
    groupId : {type: Schema.ObjectId, ref: 'ChatGroups'},
    chatDetails: {
        imageUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        videoUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        audioUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        gifUrl: {
            original: {type: String},
            thumbnail: {type: String},
        },
        audioDuration: {type :Number, default: 0},
        message: {type :String},
        type: ImageVideoType,
    }, 
    sentAt : {type : Number},
    noChat : {type : Boolean, default : true},
    // readAt: {type:Date, sparse : true},
    isDelivered : {type : Boolean, default : false},
    createdDate: {type: Number},
    isBlocked:{type:Boolean,default:false},
    isDeleted: {type: Boolean, default: false},
    readBy: [{type: Schema.ObjectId, ref: 'Users'}]
}, {timestamp: true});

module.exports = mongoose.model('Chats', Chats);
