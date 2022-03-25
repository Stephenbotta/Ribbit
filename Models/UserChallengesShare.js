
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
// let SchemaTypes = mongoose.Schema.Types;

let UserChallengesSchema = new Schema({
    userChallenge_id: {type: Schema.ObjectId, ref: 'User_Challenges', index : true, require : true},
    // user_id: {type: Schema.ObjectId, ref: 'Users', index : true, require : true},
    // challenge_id: {type: Schema.ObjectId, ref: 'Challenge', index : true, require : true},
    sharedPost: [
        new Schema({
            type:{type:String, default: "posts"},
            post_id: {type: Schema.ObjectId, ref: 'Posts', index : true, },
         })
           ],
}, {timestamps: true});


module.exports = mongoose.model('User_Challenge_Shares', UserChallengesSchema);




