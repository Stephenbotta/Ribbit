
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let ChatGroups = new Schema({
    conversationId: {type:Schema.ObjectId},
    name:{type:String, default: ""},
    adminId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    noChat: {type: Boolean, default: true}, 
    memberIds: [{type: Schema.ObjectId, ref: 'Users'}],
    memberCount: {type: Number, default: 0},
    createdOn: {type: Number, default: 0}, 
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('ChatGroups', ChatGroups);




