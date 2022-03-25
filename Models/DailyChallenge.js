const mongoose = require("mongoose");
const Config = require('../Configs')

let Schema = mongoose.Schema;

let dailyChallengeSchema = new Schema({
    title: { type: String, default: "", required: true },
    description: { type: String, default: "" },
    rewardPoint: { type: Number, default: "" },
    type: { type: String, enum: [Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.SMS_CHALLENGE, Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.STORY_CHALLENGE, Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.POST_CHALLENGE], default: Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.SMS_CHALLENGE },
    isDeleted: { type: Boolean, default: false },
    isBlocked: { type: Boolean, default: false },
    isActive: { type: Boolean, default: false }
}, {
    timestamps: true
});


module.exports = mongoose.model('DailyChallenge', dailyChallengeSchema);