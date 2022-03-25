
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let CrossedUsers = new Schema({
    crossedUserId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    userId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    conversationId: {type: Schema.ObjectId, default: null},
    time: {type:Number, default:0},
    locationName: {type:String, default: ''},
    locationAddress: {type:String, default: ''},
    location: {type:[Number], default: [0,0]},
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('CrossedUsers', CrossedUsers);




