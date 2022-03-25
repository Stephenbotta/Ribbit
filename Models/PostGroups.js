
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let PostGroups = new Schema({
    conversationId: {type:Schema.ObjectId},
    groupName:{type:String, default: ""},
    adminId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    createdBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    categoryId: {type: Schema.ObjectId, ref: 'Categories'},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    noChat: {type: Boolean, default: true}, 
    createdOn: {type: Number, default: 0},
    isMember: [{type: Schema.ObjectId, ref: 'Users'}],
    description: {type: String, default: ""},
    memberCounts: {type: Number, default:0},
    noPost: {type: Boolean, default: true},
    isPrivate: {type: Boolean, default: false}, 
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
    isArchive:[{type: Schema.ObjectId, ref: 'Users', index : true, require : true}],
}, {timestamps: true});

module.exports = mongoose.model('PostGroups', PostGroups);




