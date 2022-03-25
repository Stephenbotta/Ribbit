
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let ChatGroupMembers = new Schema({
    groupId: {type: Schema.ObjectId, ref: 'ChatGroups', index : true, require : true},
    userId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    isNotify: {type: Boolean, default:false},
    joinedAt: {type:Number, default: 0},
    isAdmin: {type:Boolean, default: false},
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('ChatGroupMembers', ChatGroupMembers);
