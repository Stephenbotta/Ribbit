
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
// let Configs = require('../Configs');

// let likes={
//     userId:{type:Schema.ObjectId,ref:"Users"},
//     time:{type:Number,default:0},
// };

let likes= {type:Schema.ObjectId,ref:"Users"}

let MediaReplies = new Schema({
    commentId :{type: Schema.ObjectId, ref: 'MediaComments'},
    postId :{type: Schema.ObjectId, ref: 'Posts'},
    mediaId :{type: Schema.ObjectId},
    replyBy :{type: Schema.ObjectId,ref:'Users'},
    reply :{type: String,default:''},
    // userIdTag: [{type: Schema.ObjectId, ref: 'Users'}],
    createdOn :{type: Number,default:0},
    editAt :{type: Number,default:0},
    likes: [likes],
    likeCount:{type:Number,default:0},
    isBlocked:{type:Boolean,default:false},
    isDeleted:{type:Boolean,default:false},
}, {timestamps: true});

module.exports = mongoose.model('MediaReplies', MediaReplies);
