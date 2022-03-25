const Config             = require('../../Configs');
const Model              = require('../../Models')
const Service            = require('../../Services/queries')
const UniversalFunctions = require('../../Utils/UniversalFunction')
const TokenManager       = require('../../Libs/TokenManager')
const uploadFilesOnS3    = require('../../Libs/UploadMultipart')
const codegenrate        = require('../../Libs/CodeGenerator')
const sendEmail          = require('../../Libs/email');
const pushNotification   = require('../../Libs/pushNotification');
const mongoose           = require('mongoose');
const socketManager      = require('../../Libs/SocketManager');
const _                  = require("lodash")
const emailTemplates     = require("../../Libs/emailTemplates");
var Moment = require('moment');
const aws = require('aws-sdk'); // import aws-sdk
const MomentRange = require('moment-range');

const moment = MomentRange.extendMoment(Moment);

let addChallenge = async (request) => {
    try{
        let id = request.payload && request.payload.id ? request.payload.id : '';
        let query = request.payload;
        let response;
    
        if(id){b
            response = await Model.Challenge.findOneAndUpdate({ _id: id }, query, { new: true });
        }else{
            response = await new Model.Challenge(query).save();
            let users = await getRequired(Model.Users, {}, {}, {lean: true})
            users.map(async data => {
                 await createData(Model.Notifications, {
                    toId: data._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CHALLENGE,
                    postId : response._id,
                    createdOn: +new Date
                })

                let pushData ={
                    id: response._id,
                    TYPE : Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CHALLENGE,
                    msg : 'New challenge',
                    body: response.title
                };
                pushNotification.sendPushToUser(data.deviceToken, pushData, (err, res)=>{console.log(err, res)})
            });
           

        }
         return response;
    }catch(e){
    console.log(e)
    }  
}

let getChallenge = async (request) => {
    try{
        let pageNo = request.query.pageNo;
        let limit = request.query.limit;
        
        if(!pageNo){
            pageNo = 1;
        }
        if(!limit){
            limit = 10;
        }
    
        let pipeline = [
            {
                $match: { isDeleted : false }
            },
            {
                $skip : (pageNo - 1) * limit
            },
            {
                $limit: limit
            },
            // {
            //     $lookup: {
            //         "from": "questions",
            //         "localField": "_id",
            //         "foreignField": "challenge",
            //         "as": "questions"
            //     }
            // },
            {
                $lookup: {
                from: "questions",
                let: {"challengeId": "$_id"},
                pipeline: [
                    {
                        $match: {$expr: {$eq:["$challenge", "$$challengeId"]}}
                    },
                    {
                        $lookup: {
                                from: "options",
                                localField: "_id",
                                foreignField: "question",
                                as: "options",
                            },
                    }
                ],
                as: "questions",
                },
            },   
        ]
    
        console.log(pipeline)
        let Challenge = await Model.Challenge.aggregate(pipeline);
        
        let response = {
            info : Challenge,
            totalCount: await Model.Challenge.count({ isDeleted : false } )
        }
        return response;
    }catch(e){
        console.log(e);
    }
  
}

let addQuestion = async (request) => {
    try{
        let id = request.payload && request.payload.id ? request.payload.id : '';
        let query = request.payload;
        let response;
    
        if(id){
            response = await Model.ChallengeQuestions.findOneAndUpdate({ _id: id }, query, { new: true });
        }else{
            response = await new Model.ChallengeQuestions(query).save();
        }
        
         return response;
    }catch(e){
    console.log(e)
    }  
}

let getQuestion = async (request) => {
    try{
        let challengeId = request.params.challengeId;
        
    
        let pipeline = [
            {
                $match: { 'challenge': mongoose.Types.ObjectId(challengeId), isDeleted : false }
            },
            {
                $lookup: {
                        from: "options",
                        localField: "_id",
                        foreignField: "question",
                        as: "options",
                    },
            } 
        ]
    
        let questions = await Model.ChallengeQuestions.aggregate(pipeline);
        // let commentsWithpopulate = await Model.Users.populate(comments, [
        //     { path: 'commentBy', select: 'fullName firstName lastName imageUrl' }
        // ]);
        return questions;
    }catch(e){
        console.log(e);
    }
  
}

let addOption = async (request) => {
    try{
        let id = request.payload && request.payload.id ? request.payload.id : '';
        let query = request.payload;
        let response;
    
        if(id){
            response = await Model.ChallengeQuestionOption.findOneAndUpdate({ _id: id }, query, { new: true });
        }else{
            response = await new Model.ChallengeQuestionOption(query).save();
        }
        
         return response;
    }catch(e){
    console.log(e)
    }  
}

/**
 * @description: helping function for creating data 
 */
var createData = (collection, condition)=>{
    return new Promise((resolve, reject) => {
        Service.saveData(collection, condition).then(result=>{
            resolve(result)
        }).catch(reason=>{
            reject(reason)
        })
    });
}
/**
 * @description: helping function for getting data 
 */
var getRequired = (collection, condition, projection, option)=>{
    return new Promise((resolve, reject) => {
        Service.getData(collection, condition, projection, option).then(result=>{
            resolve(result)
        }).catch(reason=>{
            reject(reason)
        })
    });
}

module.exports = {
    addChallenge,
    getChallenge,
    addQuestion,
    getQuestion,
    addOption
}