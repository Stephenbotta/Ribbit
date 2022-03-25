
let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let pointEarnedHistory = new Schema({
    source: { type: String, default: "" },
    //name: {type:String, default: "" },
    date: { type: String, default: "" },
    pointEarned: { type: Number, default: 0 },
    userId: { type: Schema.ObjectId, ref: 'Users', index: true, require: true },
}, { timestamps: true });

module.exports = mongoose.model('PointEarnedHistory', pointEarnedHistory);