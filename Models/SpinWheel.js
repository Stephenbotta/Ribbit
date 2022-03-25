
let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let SpinWheel = new Schema({
    prize: [{
        color: { type: String, trim: true, sparse: true },
        value: { type: Number, default: 0 },
    }],
    isBlocked: { type: Boolean, default: false },
    isActive: { type: Boolean, default: true },
    isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('SpinWheel', SpinWheel);
