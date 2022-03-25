
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let VenueGroups = new Schema({
    conversationId: {type:Schema.ObjectId},
    venueTitle:{type:String, default: ""},
    adminId: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    createdBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    categoryId: {type: Schema.ObjectId, ref: 'Categories'},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    venueLocation: { type: [Number], default: [0,0]},
    venueLocationName: { type: String, default: ""},
    venueLocationAddress: { type: String, default: ""},
    venueTime: {type: Number, default: 0},
    memberIds: [{type: Schema.ObjectId, ref: 'Users'}],
    memberCount: {type: Number, default: 0},
    venueTags: [{type: String}],
    venueAdditionalDetailsName: {type: String, default: ""},
    venueAdditionalDetailsDocs: {type: String, default: ""},
    isPrivate: {type: Boolean, default: false}, 
    createdOn: {type: Number, default: 0}, 
    infoUpdated: {type: Number, default: 0}, 
    isBlocked:{type:Boolean, default:false},
    isDeleted:{type:Boolean, default:false},
    isArchive:[{type: Schema.ObjectId, ref: 'Users', index : true, require : true}],
}, {timestamps: true});

VenueGroups.index({ "venueLocation": "2dsphere" });

module.exports = mongoose.model('VenueGroups', VenueGroups);




