'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let likes= {type:Schema.ObjectId,ref:"Users"}

let Posts = new Schema({
    postBy:{type:Schema.ObjectId,ref:"Users",sparse:true},
    groupId:{type:Schema.ObjectId,ref:"PostGroups",sparse:true},
    postText:{type: String,trim: true,default:""},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""}
    },
    readBy: [{type: Schema.ObjectId, ref: 'Users'}],
    postCategoryId: {type: Schema.ObjectId, ref: 'Categories'},
    hashTags: [{type: String}],
    likes:[likes],
    type:{
        type: String,default:Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT, enum: [
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
        ]
    },
    media: [{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
        videoUrl: {type:String,default:""},
        mediaType: {type: String, enum: [
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF,
        ]},
        likes:[likes],
        likeCount:{type:Number,default:0},
    }], 
    createdOn: {type:Number,default:0},                   
    isDeleted:{type:Boolean, default:false},
    isBlocked:{type:Boolean, default:false},
    commentCount:{type:Number,default:0},
    likeCount:{type:Number,default:0},
    postType: {
        type: String, default:Configs.APP_CONSTANTS.DATABASE.POST_TYPE.REGULAR, enum: [
            Configs.APP_CONSTANTS.DATABASE.POST_TYPE.REGULAR,
            Configs.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY,
            Configs.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY,
        ]
    },
    postingIn: {
        type: String, default:Configs.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS, enum: [
            Configs.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY,
            Configs.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS,
            Configs.APP_CONSTANTS.DATABASE.POSTING_IN.SELECTED_PEOPLE,
        ]
    },
    selectedPeople: [{type:Schema.ObjectId,ref:"Users",sparse:true}],
    //selectInterests: [{type: Schema.ObjectId, ref: 'Categories'}],
    location: { type: [Number], default: [0,0]},
    locationName: {type: String, default: ""},
    locationAddress: {type: String, default: ""},
    meetingTime: {type: Number},
    expirationTime: {type: Number},
}, {timestamps: true});

Posts.index({ "location": "2dsphere" });

module.exports = mongoose.model('Posts', Posts);