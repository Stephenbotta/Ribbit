'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let Question = new Schema({
    challenge: {type: Schema.ObjectId, ref: 'Challenge'},
    question: {
        type: String
    },
    rightOption: {
        type: Schema.ObjectId,
        ref: 'Option',
        default: null
    },
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('Question', Question);