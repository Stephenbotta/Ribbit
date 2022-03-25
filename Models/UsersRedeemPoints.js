
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let UsersRedeemPoints = new Schema({
    userId: { type: Schema.ObjectId, ref: 'Users', default: null, sparse: true },
    surveyId: { type: Schema.ObjectId, ref: 'Surveys', default: null, sparse: true },
    userSurveyId: { type: Schema.ObjectId, ref: 'UserSurveys', default: null, sparse: true },
    points: { type: Number, default: 0 },
    redeempPointsType: {
        type: String, enum: [
            Configs.APP_CONSTANTS.DATABASE.REDEEM_POINTS.CREDIT,
            Configs.APP_CONSTANTS.DATABASE.REDEEM_POINTS.DEBIT,
        ]
    }
}, {
    timestamps: true
});

module.exports = mongoose.model('UsersRedeemPoints', UsersRedeemPoints);