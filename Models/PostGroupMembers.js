
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let PostGroupMembers = new Schema({
    groupId: {type: Schema.ObjectId, ref: 'PostGroups', index : true, require : true},
    userId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    joinedAt: {type:Number, default:0},
    isNotify: {type: Boolean, default:true},
    isAdmin: {type:Boolean, default: false},
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('PostGroupMembers', PostGroupMembers);




