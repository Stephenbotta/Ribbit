
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
// let Configs = require('../Configs');

// let likes={
//     userId:{type:Schema.ObjectId,ref:"Users"},
//     time:{type:Number,default:0},
// };

let likes= {type:Schema.ObjectId,ref:"Users"}

let MediaComments = new Schema({
    postId :{type: Schema.ObjectId, ref: 'Posts'},
    mediaId :{type: Schema.ObjectId},
    commentBy :{type: Schema.ObjectId,ref:'Users'},
    comment :{type: String,default:''},
    attachmentUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    // userIdTag: [{type: Schema.ObjectId, ref: 'Users'}],
    createdOn :{type: Number,default:0},
    likes: [likes],
    likeCount:{type:Number,default:0},
    editAt :{type: Number,default:0},
    // replyIds: [{type:Schema.ObjectId, ref: 'Replies'}],
    isBlocked:{type:Boolean,default:false},
    isDeleted:{type:Boolean,default:false},
}, {timestamps: true});

module.exports = mongoose.model('MediaComments', MediaComments);
