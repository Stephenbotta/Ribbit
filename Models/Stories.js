'use strict';
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');

let Stories = new Schema({
    postBy: { type: Schema.ObjectId, ref: "Users", sparse: true },
    media: {
        original: { type: String, default: "" },
        thumbnail: { type: String, default: "" },
        videoUrl: { type: String, default: "" },
        mediaType: {
            type: String, enum: [
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE
            ]
        }
    },
    createdOn: { type: Number, default: 0 },
    isDeleted: { type: Boolean, default: false },
    viewBy: [{ type: Schema.ObjectId, ref: 'Users' }],
    isBlocked: { type: Boolean, default: false },
    expirationTime: { type: Number }
}, { timestamps: true });


module.exports = mongoose.model('Stories', Stories);