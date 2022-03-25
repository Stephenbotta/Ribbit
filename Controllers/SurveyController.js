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
const QuestionController = require('./QuestionController');


let addEditSurvey = async (request) => {
    try {
        let id = request.payload.surveyId;
        let dataToSave = request.payload;

        let response = {};

        if (id) {
            response = await Model.Surveys.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
        } else {
            response = await new Model.Surveys(dataToSave).save();
        }
        return response;
    } catch (e) {
        console.log(e)
    }
}

let getSurvey = async (request) => {
    console.log('getSurvey');

    let payload = request.query;
    let Models, query = { isDeleted: { $ne: true } }, populate = [], projection = {
        "updatedAt": 0,
        "__v": 0,
        "media._id": 0
    }, keyword;

    let options = { skip: (payload.pageNo - 1) * payload.limit, limit: payload.limit, sort: { 'createdAt': -1 } }

    if (payload.search) keyword = { $regex: new RegExp(payload.search, 'i') };

    populate = [{
        path: 'categoryIds',
        match: {},
        select: 'categoryName imageUrl',
        model: Model.Categories,
        options: {}
    }]

    if (payload.search) query.name = keyword;

    Models = Model.Surveys;


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

let blockedSurvey = async (request) => {
    let id = request.payload.surveyId;
    let dataToSave = {};

    if (request.payload.action) {
        dataToSave.isBlocked = true;
    } else {
        dataToSave.isBlocked = false;
    }

    let response = {};

    response = await Model.Surveys.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
    return response;
}

let deletedSurvey = async (request) => {
    let id = request.payload.surveyId;
    let dataToSave = {};

    if (request.payload.action) {
        dataToSave.isDeleted = true;
    } else {
        dataToSave.isDeleted = false;
    }

    let response = {};

    response = await Model.Surveys.findOneAndUpdate({ _id: id }, dataToSave, { new: true });

    if (request.payload.action) {
        QuestionController.deleteAllQuestions({ surveyId: id });
    }
    return response;
};

let getOneSurveyAdmin = async (request, userData) => {

    try {

        let survey = await Service.getData(Model.Surveys, { _id: request.query.id }, {}, { lean: true });
        if (survey.length <= 0) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NO_SURVEY_FOUND);
        }

        return survey;

    } catch (err) {
        console.log(err);
    }
}



let getSurveyUser = async (payload, userData) => {
    let Models, query = { isDeleted: { $ne: true } }, populate = [], projection = {
        "updatedAt": 0,
        "__v": 0
    }, keyword;

    let options = { skip: (payload.pageNo - 1) * payload.limit, limit: payload.limit, sort: { 'createdAt': -1 } }

    if (payload.search) keyword = { $regex: new RegExp(payload.search, 'i') };

    // populate = [{
    //     path: 'categoryIds',
    //     match: {},
    //     select: 'categoryName imageUrl',
    //     model: Model.Categories,
    //     options: {}
    // }];

    query.isDeleted = { $ne: true };
    query.isBlocked = { $ne: true };
    query.expiryDate = { $gte: Date.now() };

    if (payload.search) query.name = keyword;

    // if (userData.interestTags && userData.interestTags.length) {
    //     query.categoryIds = { $in: userData.interestTags };
    // }
    let getAlreadySurveyUserId = (await Service.getData(Model.UserSurveys, { userId: userData._id }, { surveyId: 1 }, {})).map(item => item.surveyId);
    console.log('getAlreadySurveyUserId_______', getAlreadySurveyUserId);

    if (getAlreadySurveyUserId.length) {
        query._id = { $nin: getAlreadySurveyUserId };
    }
    Models = Model.Surveys;

    let [data, count] = await Promise.all([
        Service.populateData(Models, query, projection, options, populate),
        Service.count(Models, query)]);

    return {
        info: data,
        currentPage: payload.pageNo,
        pages: Math.ceil(count / payload.limit),
        totalCount: count
    }
}

let getSurveyQuestions = async (payload, userData) => {
    let Models, query = { isDeleted: { $ne: true } }, populate = [], projection = {
        "options.isDeleted": 0,
        "updatedAt": 0,
        "__v": 0
    };

    let options = { sort: { 'createdAt': 1 } }

    query.surveyId = mongoose.Types.ObjectId(payload.surveyId);
    query.isDeleted = { $ne: true };
    query.isBlocked = { $ne: true };

    Models = Model.Questions;
    // console.log('query', query);

    let [data, count] = await Promise.all([
        Service.populateData(Models, query, projection, options, populate),
        Service.count(Models, query)]);

    return {
        info: data,
        totalCount: count
    }
}

module.exports = {
    getSurvey,
    addEditSurvey,
    blockedSurvey,
    deletedSurvey,
    getSurveyUser,
    getSurveyQuestions,
    getOneSurveyAdmin
}