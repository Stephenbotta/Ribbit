'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let UserContactUs = new Schema({
    user_id: {type:Schema.ObjectId,ref:"Users"},
    message:{type:String, maxlength: 1000},    
    isDeleted:{type:Boolean, default:false}    
}, {timestamps: true});

module.exports = mongoose.model('User_Contact_Us', UserContactUs);