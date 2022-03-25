'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let Categories = new Schema({
    categoryName: { type: String, index: true },
    imageUrl: {
        original: { type: String, default: "" },
        thumbnail: { type: String, default: "" }
    },
    // createdOn: {type: Number,default:0},                   
    isDeleted: { type: Boolean, default: false },
    isBlocked: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('Categories', Categories);