
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let PointStructures = new Schema({
    name: { type: String, default: '' },
    description: { type: String, default: '' },
    rewardPoint: { type: Number, default: 0 },
    quantity: { type: Number, default: 0 },
    parentId: { type: Schema.ObjectId, ref: "PointStructures", default: null },
    pointType: {
        type: String, enm: [
            Configs.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SURVEY,
            Configs.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.CHALLENGE,
            Configs.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SIGNUP_REWARDS,
            Configs.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.GAMIFICATION,
            Configs.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.THIRD_PARTY
        ]
    }
}, {
    timestamps: true,
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
});

/**use vitual populate for get total surveys counts */
PointStructures.virtual('subPoints', {
    ref: 'PointStructures', // The model to use
    localField: '_id', // Find people where `localField`
    foreignField: 'parentId', // is equal to `foreignField`
});

module.exports = mongoose.model('PointStructures', PointStructures);
