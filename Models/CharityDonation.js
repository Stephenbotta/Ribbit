
const { string } = require('joi');
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let CharityDonationList = new Schema({
    givenPoint: { type: Number, default: 0 },
    amount: { type: Number, default: 0 },
    organizationId: { type: Schema.ObjectId, ref: 'CharityOrgList', index: true, require: true },
    userId: { type: Schema.ObjectId, ref: 'Users', index: true, require: true },
    isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('CharityDonationList', CharityDonationList);




