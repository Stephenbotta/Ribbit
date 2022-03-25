
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let Surveys = new Schema({
    name: { type: String, trim: true, default: '' },
    description: { type: String, trim: true, default: '' },
    expiryDate: { type: Number, default: 0 },
    totalTime: { type: Number, default: 0 },
    questionCount: { type: Number, default: 0 },
    media: [{
        original: { type: String, default: '' },
        thumbnail: { type: String, default: '' },
        videoUrl: { type: String, default: '' },
        mediaType: {
            type: String, enum: [
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF,
            ]
        },
    }],
    categoryIds: { type: [Schema.ObjectId], ref: "Categories", default: null, sparse: true },
    rewardPoints: { type: Number, default: 0 },
    isBlocked: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
}, {
    timestamps: true,
    toJSON: { virtuals: true },
    toObject: { virtuals: true }
});

Surveys.virtual('questions', {
    ref: "Questions",
    localField: '_id', // Your local field, like a `FOREIGN KEY` in RDS
    foreignField: 'surveyId', // Your foreign field which `localField` linked to. Like `REFERENCES` in RDS
    // If `justOne` is true, 'members' will be a single doc as opposed to
    // an array. `justOne` is false by default.
    justOne: false,
    options: { sort: { "createdAt": 1 } }
});

module.exports = mongoose.model('Surveys', Surveys);




