
const { string } = require('joi');
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let redeemHistory = new Schema({
    redeemType: {type:String, default: "" },
    name: {type:String, default: "" },
    point: { type: Number, default: 0 },
    userId: { type: Schema.ObjectId, ref: 'Users', index: true, require: true },
}, { timestamps: true });

module.exports = mongoose.model('redeemHistory', redeemHistory);




