'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let Challenge = new Schema({
    title:{type:String},
    type: {
        type: String,
        enum: ['Questionnaire survey','Task'],
        select: false
    },            
    isDeleted:{type:Boolean, default:false},
}, {timestamps: true});

module.exports = mongoose.model('Challenge', Challenge);