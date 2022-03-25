
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let UserChallengesSchema = new Schema({
    user_id: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    challenge_id: {type: Schema.ObjectId, ref: 'Challenge', index : true, require : true},
    status: {type:String, enum: Object.values(Configs.APP_CONSTANTS.userChallengeStatus), default:Configs.APP_CONSTANTS.userChallengeStatus.INPROGESS, required: true},
    userStartChallengeDateTime: {type:Date, default:  Date.now()},
    createdBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    modifiedBy: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
}, {timestamps: true});


module.exports = mongoose.model('User_Challenges', UserChallengesSchema);




