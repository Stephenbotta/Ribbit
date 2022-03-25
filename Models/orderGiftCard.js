'use strict';
const { number } = require('joi');
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let GiftCard = new Schema({
    referenceOrderID: { type: String, sparse: true },
    rewardName: { type: String, default: "" },
    userId: { type: Schema.ObjectId, ref: "Users" },
    isDeleted: { type: Boolean, default: false },
    amount: { type: Number, default: 0 },
    image: { type: String, default: "" },
    claimCode: { type: String, default: "" }
}, { timestamps: true });

module.exports = mongoose.model('Order', GiftCard);