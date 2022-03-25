
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let ChallengeSchema = new Schema({
    challengeType:{type:String, enum: Object.values(Configs.APP_CONSTANTS.challengeType), required:true},
    title:{type:String, default: "", requried:true},
    description:{type:String, default: ""},
    quantity: {type: Number, required:true},
    rewardPoint: {type: Number, required:true},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    startDate: {type: Date},
    endDate: {type: Date},
    isDeleted:{type:Boolean, default:false},
    createdBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    modifiedBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    isBlocked: { type: Boolean, default: false },
}, {timestamps: true});


module.exports = mongoose.model('Challenge', ChallengeSchema);




