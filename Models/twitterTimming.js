
let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let TwitterTiming = new Schema({
    timeCount: { type: Number, default: 0 },
    totalEarnedPoints: { type: Number, default: 0 },
    totalTimeSpend: { type: Number, default: 0 },
    userId: { type: Schema.ObjectId, ref: "Users" }
}, { timestamps: true });


module.exports = mongoose.model('twitterTiming', TwitterTiming);




