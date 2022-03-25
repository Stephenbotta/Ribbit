
const { string } = require('joi');
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let CharityOrgList = new Schema({
    organizationName: { type: String, default: '' },
    organizationLink: { type: String, default: '' },
    isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('CharityOrgList', CharityOrgList);




