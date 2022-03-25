'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let Option = new Schema({
    challenge: {type: Schema.ObjectId, ref: 'Challenge'},
    question: {type: Schema.ObjectId, ref: 'Question'},
    option: {
        type: String
    },
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('Option', Option);