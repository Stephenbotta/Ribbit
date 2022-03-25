const Config = require('../Configs');
const Model = require('../Models')
const Service = require('../Services/queries')
const UniversalFunctions = require('../Utils/UniversalFunction')
const uploadFilesOnS3 = require('../Libs/UploadMultipart')
const codegenrate = require('../Libs/CodeGenerator')
const sendEmail = require('../Libs/email');
const pushNotification = require('../Libs/pushNotification');
const mongoose = require('mongoose');
const socketManager = require('../Libs/SocketManager');
const _ = require("lodash")
const emailTemplates = require("../Libs/emailTemplates");
var Moment = require('moment');
const aws = require('aws-sdk'); // import aws-sdk
const MomentRange = require('moment-range');

const moment = MomentRange.extendMoment(Moment);


let addEditQuestion = async (request) => {
    try {
        let id = request.payload.questionId;
        let dataToSave = request.payload;

        let response = {};

        if (id) {
            response = await Model.Questions.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
        } else {
            response = await new Model.Questions(dataToSave).save();
        }

        updateQuestionCouterSurvey(response);
        return response;
    } catch (e) {
        console.log(e)
    }
}

let updateQuestionCouterSurvey = async (payload) => {
    try {

        let getQuestionCount = await Model.Questions.countDocuments({ surveyId: payload.surveyId, isDeleted: { $ne: true } });
        console.log('getQuestionCount', getQuestionCount);

        await Model.Surveys.update({ _id: payload.surveyId }, {
            $set: {
                questionCount: getQuestionCount
            }
        })

        return {};
    } catch (e) {
        console.log(e)
    }
}

let getQuestions = async (request) => {
    console.log('getQuestions');

    let payload = request.query;
    let Models, query = { isDeleted: { $ne: true } }, populate = [], projection = {
        "updatedAt": 0,
        "__v": 0
    }, keyword;

    let options = { skip: (payload.pageNo - 1) * payload.limit, limit: payload.limit, sort: { 'createdAt': -1 } }

    if (payload.search) keyword = { $regex: new RegExp(payload.search, 'i') };
    if (payload.search) query.name = keyword;

    if (payload.surveyId) query.surveyId = mongoose.Types.ObjectId(payload.surveyId);

    Models = Model.Questions;


    let [data, count] = await Promise.all([
        Service.populateData(Models, query, projection, options, populate),
        Service.count(Models, query)]);
    console.log('data', data);
    console.log('count', count);

    return {
        info: data,
        currentPage: payload.pageNo,
        pages: Math.ceil(count / payload.limit),
        totalCount: count
    }
}

let blockedQuestion = async (request) => {
    let id = request.payload.questionId;
    let dataToSave = {};

    if (request.payload.action) {
        dataToSave.isBlocked = true;
    } else {
        dataToSave.isBlocked = false;
    }

    let response = {};

    response = await Model.Questions.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
    return response;
}

let deletedQuestion = async (request) => {
    let id = request.payload.questionId;
    let dataToSave = {};

    if (request.payload.action) {
        dataToSave.isDeleted = true;
    } else {
        dataToSave.isDeleted = false;
    }

    let response = {};

    response = await Model.Questions.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
    return response;
}

let deleteAllQuestions = async (query) => {
    await Model.Questions.updateMany(query, { isDeleted: true });
}

module.exports = {
    addEditQuestion,
    getQuestions,
    blockedQuestion,
    deletedQuestion,
    deleteAllQuestions,
}