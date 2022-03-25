
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let Option = {
    name: { type: String, trim: true, default: '' },
    isDeleted: { type: Boolean, default: false },
};

let Questions = new Schema({
    surveyId: { type: Schema.ObjectId, ref: 'Surveys', default: null, sparse: true },
    name: { type: String, trim: true, default: '' },
    questionType: {
        type: Number,
        default: Configs.APP_CONSTANTS.DATABASE.QUESTION_TYPES.SINGLE_VALUE,
        enum: [
            Configs.APP_CONSTANTS.DATABASE.QUESTION_TYPES.SINGLE_VALUE,
            Configs.APP_CONSTANTS.DATABASE.QUESTION_TYPES.MULTI_VALUE,
        ]
    },
    options: {
        type: [Option],
        default: []
    },
    media: {
        original: { type: String, default: '' },
        thumbnail: { type: String, default: '' },
        videoUrl: { type: String, default: '' },
        mediaType: {
            type: String, enum: [
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF,
            ],
            default: Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT
        },
    },
    isBlocked: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('Questions', Questions);




