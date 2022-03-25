const mongoose = require('mongoose'),
    Schema = mongoose.Schema,
    Configs = require('../Configs');

  
const PostGroupChats = new Schema({
    // conversationId:{type:String,required: true},
    senderId : {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    postId : {type :Schema.ObjectId, ref: 'Posts'},
    groupId : {type :Schema.ObjectId, ref: 'Groups'},
    sentAt : {type : Number},
    isDelivered : {type : Boolean, default : false},
    deliveredAt : {type:Date, sparse : true},
    isBlocked:{type:Boolean,default:false},
    isDeleted: {type: Boolean, default: false},
    readBy: [{type: Schema.ObjectId, ref: 'Users'}]
}, {timestamp: true});

module.exports = mongoose.model('PostGroupChats', PostGroupChats);
