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

let sumitUserSurvey = async (payload, userData) => {
    // let surveyData = await Service.populateData(Model.Surveys, { _id: payload.surveyId }, {}, {}, [{
    let surveyData = await Model.Surveys.findOne({
        _id: payload.surveyId,
        isBlocked: false,
        isDeleted: false
    }, {
        "updatedAt": 0,
        "__v": 0,
    }).populate([
        {
            path: "questions",
            model: Model.Questions,
            match: { isBlocked: false, isDeleted: false },
            options: { sort: { "createdAt": 1 } }
        }
    ]);

    console.log('surveyData', JSON.stringify(surveyData));
    let questionsList = surveyData.questions;
    let dataToSave = {};

    dataToSave.surveyId = payload.surveyId;
    dataToSave.userId = userData._id;
    dataToSave.feedback = payload.feedback;
    let questions = [];
    if (payload.questions.length) {
        for (let i = 0; i < payload.questions.length; i++) {
            let questionPayload = payload.questions[i];
            // console.log('questionId', questionPayload.questionId);

            let index = questionsList.findIndex(x => x._id.toString() == questionPayload.questionId.toString());
            // console.log('index', index);
            if (index < 0) {
                continue;
            }
            let questionsOptionsInDb = questionsList[index].options;
            let selectedOption = [];
            // console.log('questionsOptionsInDb', questionsOptionsInDb);
            for (let j = 0; j < questionPayload.options.length; j++) {
                let optionPayload = questionPayload.options[j];
                // console.log('optionPayload', optionPayload);

                let indexOption = questionsOptionsInDb.findIndex(y => y._id.toString() == optionPayload.optionId.toString());
                // console.log('indexOption', indexOption);
                if (indexOption < 0) {
                    continue;
                }
                selectedOption.push({
                    optionId: questionsOptionsInDb[indexOption]._id,
                    name: questionsOptionsInDb[indexOption].name,
                });
            }

            // console.log('selectedOption', selectedOption);

            questions.push({
                questionId: questionsList[index]._id,
                name: questionsList[index].name,
                questionType: questionsList[index].questionType,
                options: selectedOption,
            })
            // questions.push({
            //     questionId: 
            // })

        }

        console.log('questions', JSON.stringify(questions));

        dataToSave.questions = questions;
        if (questions.length == surveyData.questionCount) {
            dataToSave.isCompleted = true;
        } else {
            dataToSave.isCompleted = false;
        }

        // console.log('dataToSave', JSON.stringify(dataToSave));

    }

    let userSurveysData = await Service.saveData(Model.UserSurveys, dataToSave);

    if (dataToSave.isCompleted) {
        console.log("survey data---", surveyData, userSurveysData);
        await saveRedeemPoints(surveyData, userSurveysData, userData);
        receivedRewardsEmail({
            name: userData.firstName,
            email: userData.email,
            pointEarned: surveyData.rewardPoints,
        }, userData)
            .catch(e => console.log("erro rin received reward email----", e));
        receivedRewardsNotification(userData)
            .catch(e => console.log("erro rin received reward notification----", e));
    }

    return {};

}

let saveRedeemPoints = async (surveyData, userSurveysData, userData) => {
    await Service.saveData(Model.UsersRedeemPoints, {
        userId: userData._id,
        surveyId: surveyData._id,
        userSurveyId: userSurveysData._id,
        points: surveyData.rewardPoints,
        redeempPointsType: Config.APP_CONSTANTS.DATABASE.REDEEM_POINTS.CREDIT
    });

    let saveData = {
        userId: userData._id,
        pointEarned: surveyData.rewardPoints,
        source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.SURVEY,
        date: moment().format('MM/DD/YYYY')
    }

    let user = await Service.update(Model.Users, { _id: userData._id }, { $inc: { pointEarned: surveyData.rewardPoints } }, { new: true });

    var res = await Service.saveData(Model.PointEarnedHistory, saveData);

    console.log("user after survey reward ponts--", user);

    return {};
}

let receivedRewardsEmail = async (data, userData) => {
    let receivedRewardsEmailTemplate = await emailTemplates.reciveRewardsPointTemplate(data);
    sendEmail.sendEmail(userData.email, "Survey Received Rewards", receivedRewardsEmailTemplate);
}

let receivedRewardsNotification = async (userData) => {
    let pushData = {
        byId: userData._id,
        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
        msg: 'You received reward points for complete our survey'
    };
    pushNotification.sendPush(userData.deviceToken, pushData, (err, res) => {
    });
}

module.exports = {
    sumitUserSurvey
}