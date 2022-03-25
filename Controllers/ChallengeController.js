const Config = require('../Configs');
const Model = require('../Models')
const Service = require('../Services/queries')
const UniversalFunctions = require('../Utils/UniversalFunction')
const TokenManager = require('../Libs/TokenManager')
const mongoose = require('mongoose');
const _ = require("lodash");
var Moment = require('moment');
const MomentRange = require('moment-range');
const { count } = require('../Models/Tags');
const moment = MomentRange.extendMoment(Moment);


let readChallenge = async (role, request, userData) => {
    try {
        let pageNo = request.query.pageNo;
        let limit = request.query.limit;
        let search = request.query.search;

        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }
        let criteria = {
            isDeleted: false
        }

        if (search) {
            criteria.title = {
                $regex: search, $options: 'i'

            }
        }




        // if (userId) {
        //     criteria.isMember = { $in: [mongoose.Types.ObjectId(userId)] };
        // }
        // if (groupId) {
        //     criteria._id = mongoose.Types.ObjectId(groupId);
        // }
        // if (groupName) {
        //     groupName = new RegExp(groupName, 'i');
        //     criteria.groupName = groupName;
        // }
        /*
        db.getCollection('challenges').aggregate([
         {
             $match:{
                  
                 }
             
             },
             {
                 $lookup: {
                   from: "user_challenges",
                   let: { challenge_id: "$_id" },
                   pipeline: [
                      { $match:
                         { $expr:
                            { $and:
                               [
                                 { $eq: [ "$challenge_id",  "$$challenge_id" ] },
                                 { $eq: [ "$user_id", ObjectId("5d95e47e13ba822ddf6fbfed") ] }
                               ]
                            }
                         }
                      },
                   ],
                   as: "user_challenge_data"
                 }
                 
               },
               { $unwind:{ path:"$user_challenge_data", preserveNullAndEmptyArrays:true } },
               {
                $addFields:{
                    status:"$user_challenge_data.status",
                    userStartChallengeDateTime:"$user_challenge_data.userStartChallengeDateTime"
                    }   
                 },
                 
                 {
                     $project:{
                         user_challenge_data:0
                         }
                     }
        
        
        ])
        */
        let pipeline = [
            {
                $match: criteria
            },
            {
                $facet: {
                    data: [
                        { $sort: { createdAt: -1 } },
                        { $skip: (pageNo - 1) * limit },
                        { $limit: limit },
                        {
                            $project: {
                                imageUrl: "$imageUrl",
                                title: "$title",
                                createdAt: "$createdAt",
                                description: "$description",
                                isDeleted: "$isDeleted",
                                isBlocked: "$isBlocked",
                                startDate: "$startDate",
                                endDate: "$endDate",
                                rewardPoint: "$rewardPoint",
                                challengeType: "$challengeType",
                                quantity: "$quantity",
                                createdBy: "$createdBy",
                                updatedAt: "$updatedAt"
                            }
                        }
                    ],
                    count: [{ $count: 'count' }]
                }
            }, {
                $project: {
                    count: { $arrayElemAt: ["$count.count", 0] },
                    data: "$data"
                }

            }
        ];


        // "_id": "6036243689efa753716e030d",
        // "imageUrl": {
        //   "original": "",
        //   "thumbnail": ""
        // },
        // "title": "test",
        // "description": "test",
        // "isDeleted": false,
        // "isBlocked": false,
        // "startDate": "2021-03-11T00:00:00.000Z",
        // "endDate": "2021-05-13T00:00:00.000Z",
        // "rewardPoint": 12,
        // "challengeType": "task",
        // "quantity": 12,
        // "createdBy": "5d95dad5662ab22dc12ae8c2",
        // "createdAt": "2021-02-24T10:02:30.302Z",
        // "updatedAt": "2021-02-24T10:02:30.302Z",
        // "__v": 0


        if (role === "student") {
            const userId = userData._id;
            criteria.isBlocked = false;
            pipeline = [
                {
                    $match: criteria
                },
                {
                    $lookup: {
                        from: "user_challenges",
                        let: { challenge_id: "$_id" },
                        pipeline: [
                            {
                                $match:
                                {
                                    $expr:
                                    {
                                        $and:
                                            [
                                                { $eq: ["$challenge_id", "$$challenge_id"] },
                                                { $eq: ["$user_id", userId] }
                                            ]
                                    }
                                }
                            },
                        ],
                        as: "user_challenge_data"
                    }

                },
                { $unwind: { path: "$user_challenge_data", preserveNullAndEmptyArrays: true } },
                {
                    $addFields: {
                        status: { $ifNull: ["$user_challenge_data.status", Config.APP_CONSTANTS.userChallengeStatus.NOTSTARTED] },
                        userStartChallengeDateTime: "$user_challenge_data.userStartChallengeDateTime"
                    }
                },

                {
                    $project: {
                        user_challenge_data: 0
                    }
                },
                {
                    $skip: (pageNo - 1) * limit
                },
                {
                    $limit: limit
                }
            ];
        }


        let result = await Model.Challenges.aggregate(pipeline);

        const totalCount = await Model.Challenges.count(criteria);

        let response;


        if (role === "student") {
            response = {
                challenges: result,
                currentPage: pageNo,
                pages: Math.ceil(totalCount / limit),
                totalCount: totalCount
            }

        } else {
            response = {
                challenges: result[0].data,
                currentPage: pageNo,
                pages: Math.ceil(result[0].count / limit),
                totalCount: result[0].count
            }

        }


        return response;
    } catch (e) {
        console.log(e);
    }
};


let challengeDetail = async (request, userData) => {
    try {

        let criteria = {
            isDeleted: false,
            _id: mongoose.Types.ObjectId(request.params.id)
        };

        // let result = await Model.Challenges.findOne(criteria,{},{lean: true});
        const userId = userData._id;
        const pipeline = [
            {
                $match: criteria
            },
            {
                $lookup: {
                    from: "user_challenges",
                    let: { challenge_id: "$_id" },
                    pipeline: [
                        {
                            $match:
                            {
                                $expr:
                                {
                                    $and:
                                        [
                                            { $eq: ["$challenge_id", "$$challenge_id"] },
                                            { $eq: ["$user_id", userId] }
                                        ]
                                }
                            }
                        },
                    ],
                    as: "user_challenge_data"
                }

            },
            { $unwind: { path: "$user_challenge_data", preserveNullAndEmptyArrays: true } },
            {
                $addFields: {
                    status: "$user_challenge_data.status",
                    userStartChallengeDateTime: "$user_challenge_data.userStartChallengeDateTime"
                }
            },

            {
                $project: {
                    user_challenge_data: 0
                }
            }
        ];



        let [result] = await Model.Challenges.aggregate(pipeline);


        const anychangeInProgress = await Model.UserChallenges.findOne({ challenge_id: request.params.id, "user_id": userId, "status": Config.APP_CONSTANTS.userChallengeStatus.INPROGESS });

        result.otherChallengeInProgress = anychangeInProgress;


        console.log(result);

        return result;
    } catch (e) {
        console.log(e);
    }
};

let createChallenge = async (request, userData) => {
    try {

        // console.log(request, userData);

        console.log(userData._id);
        console.log(request.payload);

        const { payload: body } = request;
        body.createdBy = userData._id;


        // { challengeType: 'photo',
        // quantity: 1,
        // description: 'sdfasfd',
        // rewardPoint: 12,
        // startDate: '2018-02-10',
        // endDate: '2018-02-13' },


        let groups = await Model.Challenges.create(body);
        return groups;
    } catch (e) {
        console.log(e);
    }
};

let editChallenge = async (request) => {
    try {
        let id = request.payload.challengeId;
        let dataToSave = request.payload;

        let response = await Model.Challenges.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
        return response;
    } catch (e) {
        console.log(e)
    }
}

let deleteChallenge = async (request, userData) => {
    try {
        const { params: { id: challengeId } } = request;
        const response = {
            modifiedBy: userData._id,
            isDeleted: true
        };
        return await Model.Challenges.findOneAndUpdate({ _id: challengeId }, { $set: response }, { new: true });
    } catch (e) {
        console.log(e);
    }
};

module.exports = {
    createChallenge,
    readChallenge,
    deleteChallenge,
    challengeDetail,
    editChallenge
}