
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;


let Option = {
    optionId: { type: Schema.ObjectId, default: null, sparse: true },
    name: { type: String, trim: true, default: '' },
};

let Question = {
    questionId: { type: Schema.ObjectId, ref: 'Questions', default: null, sparse: true },
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
    }    
};

let UserSurveys = new Schema({
    userId: { type: Schema.ObjectId, ref: 'Users', default: null, index: true, sparse: true },
    surveyId: { type: Schema.ObjectId, ref: 'Surveys', default: null, sparse: true },
    questions: {
        type: [Question]
    },
    feedback: { type: String, default: '' },
    isCompleted: { type: Boolean, default: false },
    isBlocked: { type: Boolean, default: false },
    isDeleted: { type: Boolean, default: false },
}, { timestamps: true });

module.exports = mongoose.model('UserSurveys', UserSurveys);




// var json = {
//     "surveyId": "s1234",
//     "questions": [
//         {
//             "questionId": "Q1234",
//             "options":[
//                 {
//                     "optionId":"O1234"
//                 },
//                 {
//                     "optionId":"5678"
//                 }
//             ]
//         },
//         {
//             "questionId": "Q1234",
//             "options":[
//                 {
//                     "optionId":"O1234"
//                 },
//                 {
//                     "optionId":"5678"
//                 }
//             ]
//         },{
//             "questionId": "Q1234",
//             "options":[
//                 {
//                     "optionId":"O1234"
//                 },
//                 {
//                     "optionId":"5678"
//                 }
//             ]
//         }
//     ]
// }    