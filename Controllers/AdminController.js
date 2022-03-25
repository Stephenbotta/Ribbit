const Config = require('../Configs');
const Model = require('../Models')
const Service = require('../Services/queries')
const UniversalFunctions = require('../Utils/UniversalFunction')
const TokenManager = require('../Libs/TokenManager')
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
const { count } = require('../Models/Tags');


const moment = MomentRange.extendMoment(Moment);

let dashboardData = async (request) => {
    let startDate = request.payload.startDate;
    let endDate = request.payload.endDate;
    let criteria = { isDeleted: false };

    if (startDate != null && endDate != null) {
        criteria.createdAt = { "$gte": new Date(startDate), "$lte": new Date(endDate) };
    } else {
        startDate = moment().subtract(1, 'months').format('YYYY-MM-DD');
        endDate = moment().format('YYYY-MM-DD');
        criteria.createdAt = { "$gte": new Date(startDate), "$lte": new Date(endDate) };
    }

    const range = moment().range(startDate, endDate);
    const days = range.by('days');


    //let commentData = await Model.Comments.aggregate(pipeline);


    let postData = await getPostData(criteria, days);
    let commentData = await getCommentData(criteria, days);
    let userData = await getUserData(criteria, days);
    // let studentData = await getStudentData(criteria, days);

    return {
        postData,
        commentData,
        userData
    };

}

async function getPostData(criteria, days) {
    let response = {
        labels: [],
        data: [],
        totalCount: 0
    };

    let pipeline = [
        {
            $match: criteria
        },
        {
            $group: {
                _id: { month: { $month: "$createdAt" }, day: { $dayOfMonth: "$createdAt" }, year: { $year: "$createdAt" } },
                count: { $sum: 1 },
                date: { $first: "$createdAt" }
            }
        },
        {
            $sort: { postDate: 1 }
        },
    ];

    let postData = await Model.Posts.aggregate(pipeline);


    [...days].map(date => {
        let listDate = date.format('YYYY-MM-DD');
        let innerCount = 0;
        postData.map(data => {

            let dbDate = moment(data.date).format('YYYY-MM-DD');
            if (dbDate == listDate) {
                innerCount = data.count;
            }
        });
        response.labels.push(listDate);
        response.data.push(innerCount);
        response.totalCount += innerCount;

    });



    return new Promise(function (resolve, reject) {
        resolve(response);
    });
}

async function getCommentData(criteria, days) {
    let response = {
        labels: [],
        data: [],
        totalCount: 0
    };

    let pipeline = [
        {
            $match: criteria
        },
        {
            $group: {
                _id: { month: { $month: "$createdAt" }, day: { $dayOfMonth: "$createdAt" }, year: { $year: "$createdAt" } },
                count: { $sum: 1 },
                date: { $first: "$createdAt" }
            }
        },
        {
            $sort: { postDate: 1 }
        },
    ];

    let commentData = await Model.Comments.aggregate(pipeline);

    [...days].map(date => {
        let listDate = date.format('YYYY-MM-DD');
        let innerCount = 0;
        commentData.map(data => {

            let dbDate = moment(data.date).format('YYYY-MM-DD');
            if (dbDate == listDate) {
                innerCount = data.count;
            }
        });
        response.labels.push(listDate);
        response.data.push(innerCount);
        response.totalCount += innerCount;

    });

    return new Promise(async (resolve, reject) => {
        resolve(response);
    });
}

async function getUserData(criteria, days) {
    let response = {
        labels: [],
        data: [],
        totalCount: 0
    };

    let pipeline = [
        {
            $match: criteria
        },
        {
            $group: {
                _id: { month: { $month: "$createdAt" }, day: { $dayOfMonth: "$createdAt" }, year: { $year: "$createdAt" } },
                count: { $sum: 1 },
                date: { $first: "$createdAt" }
            }
        }
    ];

    let userData = await Model.Users.aggregate(pipeline);
    console.log(userData);
    [...days].map(date => {
        let listDate = date.format('YYYY-MM-DD');
        let innerCount = 0;
        userData.map(data => {

            let dbDate = moment(data.date).format('YYYY-MM-DD');
            if (dbDate == listDate) {
                innerCount = data.count;
            }
        });
        response.labels.push(listDate);
        response.data.push(innerCount);
        response.totalCount += innerCount;

    });

    return new Promise(function (resolve, reject) {
        resolve(response);
    });
}

let adminLogin = async (payloadData) => {
    let criteria = {
        email: payloadData.email
    }
    let check = await Model.Admins.findOne(criteria, {}, { lean: true });

    if (check) {
        if (check.password !== UniversalFunctions.CryptData(payloadData.password)) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_PASSWORD)
        }
        try {
            var forwardData = await tokenUpdate(check, Config.APP_CONSTANTS.DATABASE.USER_TYPE.ADMIN);

            if (forwardData.length > 0)
                return forwardData[0];
        } catch (e) {
            console.log(e);
        }



    } else {
        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)
    }
};





let aggregateData = function (model, group) {
    return new Promise((resolve, reject) => {
        model.aggregate(group, function (err, data) {

            if (err) return reject(err);
            return resolve(data);
        });
    })
};

let getUsers = async (request) => {
    let userType = request.query.userType;
    let pageNo = request.query.pageNo;
    let limit = request.query.limit;
    let fullName = request.query.fullName;
    let criteria = {
        isDeleted: false
    }

    if (!pageNo) {
        pageNo = 1
    }
    if (!limit) {
        limit = 10
    }

    if (userType) {
        criteria.userType = userType;
    }
    if (fullName) {
        fullName = new RegExp(fullName, 'i');
        criteria.fullName = fullName;
    }

    let pipeline = [
        {
            $match: criteria
        },
        {
            "$lookup": {
                "from": "posts",
                "localField": "_id",
                "foreignField": "postBy",
                "as": "posts"
            }
        },
        {
            "$lookup": {
                "from": "postgroups",
                "localField": "_id",
                "foreignField": "isMember",
                "as": "groups"
            }
        },
        {
            "$project": {
                fullName: 1,
                firstName: 1,
                lastName: 1,
                email: 1,
                userType: 1,
                imageUrl: 1,
                postCount: { $size: "$posts" },
                groupCount: { $size: "$groups" },
                isBlocked: 1,
                createdAt: 1
            }
        },
        {
            $sort: {
                createdAt: -1
            }
        },
        {
            $skip: (pageNo - 1) * limit
        },
        {
            $limit: limit
        }
    ];



    let users = await Model.Users.aggregate(pipeline);

    const totalCount = await Model.Users.count(criteria);

    let response = {
        info: users,
        currentPage: pageNo,
        pages: Math.ceil(totalCount / limit),
        totalCount: totalCount
    }
    return response;
}

let updateUser = async (request) => {
    let userId = request.params.userId;
    let user = await Model.Users.findOneAndUpdate({ _id: userId }, request.payload, { new: true });

    if (user == null) {
        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
    } else {
        let response = {
            _id: user._id,
            isBlocked: user.isBlocked
        }

        return response;
    }
}

let getPosts = async (request) => {

    try {
        let pageNo = request.query.pageNo;
        let limit = request.query.limit;
        let userId = request.query.userId;
        let groupId = request.query.groupId;
        let interstId = request.query.interstId;
        let postId = request.query.postId;

        let criteria = { isDeleted: false };
        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }
        if (userId) {
            criteria.postBy = mongoose.Types.ObjectId(userId);
        }
        if (groupId) {
            criteria.groupId = mongoose.Types.ObjectId(groupId);
        }
        if (interstId) {
            criteria.selectInterests = { $in: [mongoose.Types.ObjectId(interstId)] };
        }
        if (postId) {
            criteria._id = mongoose.Types.ObjectId(postId);
        }

        let pipeline = [
            {
                $match: criteria
            },
            {
                $sort: {
                    createdAt: -1
                }
            },
            {
                $skip: (pageNo - 1) * limit
            },
            {
                $limit: limit
            }
        ]

        let posts = await Model.Posts.aggregate(pipeline);
        let postsWithpopulate = await Model.Users.populate(posts, [
            { path: 'postBy', select: 'fullName firstName lastName imageUrl' },
            { path: 'likes', select: 'fullName firstName lastName imageUrl' }
        ]);
        let interstWithpopulate = await Model.Categories.populate(postsWithpopulate, 'selectInterests');


        let response = {
            info: interstWithpopulate,
            totalCount: await Model.Posts.count(criteria)
        }
        return response;


        // let posts = await Model.Posts.aggregate(pipeline);
        // let postsWithpopulate = await Model.Posts.find().populate('postBy').populate({
        //     path: 'selectInterests',
        //     match: { isDeleted : { $eq : false } }
        // });
        // return postsWithpopulate;
    } catch (e) {
        console.log(e);
    }
}

let updatePost = async (request) => {
    let postId = request.params.postId;
    let post = await Model.Posts.findOneAndUpdate({ _id: postId }, request.payload, { new: true });

    if (post == null) {
        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
    } else {
        let response = {
            _id: post._id,
            isBlocked: post.isBlocked
        }
        return response;
    }
}

let addEditInterst = async (request) => {
    try {
        let id = request.payload.id;
        let query = {};
        query.imageUrl = {};
        let imageOriginal = request.payload.imageOriginal;
        let imageThumbnail = request.payload.imageThumbnail;


        if (request.payload.interstName)
            query.categoryName = request.payload.interstName;

        if (request.payload.isBlocked)
            query.isBlocked = request.payload.isBlocked;

        if (imageOriginal && imageThumbnail) {
            query.imageUrl.original = imageOriginal;
            query.imageUrl.thumbnail = imageThumbnail;
        }

        let response;

        if (id) {
            response = await Model.Categories.findOneAndUpdate({ _id: id }, query, { new: true });
        } else {
            response = await new Model.Categories(query).save();
        }
        return response;
    } catch (e) {
        console.log(e)
    }
}

let getIntersts = async (request) => {

    let pageNo = request.query.pageNo;
    let limit = request.query.limit;
    let interstName = request.query.interstName;

    if (!pageNo) {
        pageNo = 1;
    }
    if (!limit) {
        limit = 10;
    }
    let criteria = {
        isDeleted: false
    }
    if (interstName) {
        criteria.categoryName = new RegExp(interstName, 'i');
    }

    let pipeline = [
        {
            $match: criteria
        },
        {
            $skip: (pageNo - 1) * limit
        },
        {
            $limit: limit
        }
    ]

    let interst = await Model.Categories.aggregate(pipeline);


    let response = {
        info: interst,
        totalCount: await Model.Categories.count(criteria)
    }
    return response;
}

let getGroups = async (request) => {
    try {
        let pageNo = request.query.pageNo;
        let limit = request.query.limit;
        let groupName = request.query.groupName;
        let userId = request.query.userId;
        let groupId = request.query.groupId;

        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }
        let criteria = {
            isDeleted: false
        }
        if (userId) {
            criteria.isMember = { $in: [mongoose.Types.ObjectId(userId)] };
        }
        if (groupId) {
            criteria._id = mongoose.Types.ObjectId(groupId);
        }
        if (groupName) {
            groupName = new RegExp(groupName, 'i');
            criteria.groupName = groupName;
        }

        let pipeline = [
            {
                $match: criteria
            },
            {
                $lookup: {
                    from: 'posts',
                    localField: '_id',
                    foreignField: 'groupId',
                    as: 'posts'
                }
            },
            {
                $project: {
                    groupName: 1,
                    isBlocked: 1,
                    memberCounts: 1,
                    adminId: 1,
                    isMember: 1,
                    postCount: { $size: "$posts" },
                    createdAt: 1
                }
            },
            {
                $skip: (pageNo - 1) * limit
            },
            {
                $limit: limit
            }
        ]


        let groups = await Model.PostGroups.aggregate(pipeline);
        let groupsWithpopulate = await Model.Users.populate(groups, [
            { path: 'adminId', select: 'fullName firstName lastName imageUrl' },
            { path: 'isMember', select: 'fullName firstName lastName imageUrl' }
        ]);
        let response = {
            info: groupsWithpopulate,
            totalCount: await Model.PostGroups.count(criteria)
        }
        return response;
    } catch (e) {
        console.log(e);
    }
}

let updateGroups = async (request) => {
    try {
        let groupId = request.params.groupId;

        let group = await Model.PostGroups.findOneAndUpdate({ _id: groupId }, request.payload, { new: true });

        if (group == null) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
        } else {
            let response = {
                _id: group._id,
                isBlocked: group.isBlocked
            }
            return response;
        }

    } catch (e) {
        console.log(e);
    }
}

let getCommentsOfPost = async (request) => {
    try {
        let postId = request.params.postId;
        let pageNo = request.query.pageNo;
        let limit = request.query.limit;

        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }

        let pipeline = [
            {
                $match: { 'postId': mongoose.Types.ObjectId(postId), isDeleted: false }
            },
            {
                $skip: (pageNo - 1) * limit
            },
            {
                $limit: limit
            }
        ]

        let comments = await Model.Comments.aggregate(pipeline);
        let commentsWithpopulate = await Model.Users.populate(comments, [
            { path: 'commentBy', select: 'fullName firstName lastName imageUrl' }
        ]);
        let response = {
            info: commentsWithpopulate,
            totalCount: await Model.Comments.count({ 'postId': mongoose.Types.ObjectId(postId), isDeleted: false })
        }
        return response;
    } catch (e) {
        console.log(e);
    }

}

let userContact = async (request) => {
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


        let pipeline = [
            {
                $sort: {
                    createdAt: -1
                }
            },
            {
                $lookup: {
                    from: "users",
                    let: { "id": "$user_id" },
                    pipeline: [
                        {
                            $match:
                            {
                                $expr:
                                {
                                    $and:
                                        [
                                            { $eq: ["$_id", "$$id"] }
                                        ]
                                }
                            }
                        },
                        { $project: { fullName: 1, email: 1 } }
                    ],
                    as: "user"
                }
            },
            {
                $unwind: "$user"
            }
        ];

        if (search) {
            pipeline.push({
                $match: {
                    "user.fullName": {
                        $regex: search, $options: 'i'
                    }
                }
            });
        }

        pipeline.push({
            $skip: (pageNo - 1) * limit
        });

        pipeline.push({
            $limit: limit
        });



        let result = await Model.UserContactUs.aggregate(pipeline);


        if (search) {
            totalCount = result.length;
        } else {
            totalCount = await Model.UserContactUs.count({});
        }

        let response = {
            list: result,
            currentPage: pageNo,
            pages: Math.ceil(totalCount / limit),
            totalCount: totalCount
        }
        return response;
    } catch (e) {
        console.log(e);
    }
}


let uploadFileToS3 = async (request) => {
    return new Promise((resolve, reject) => {
        //console.log(">>>>>>>>>>>>>>>>>>>>>>>>>", request);
        uploadFilesOnS3.uploadFilesOnS3(request.payload.image, (err, response) => {
            if (err) {
                //console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", err);
                reject(err);
            }

            else {
                //console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", response);
                resolve(response);
            }

        });
    });

}

/**
 * @description: helping function for getting data 
 */
var getRequired = (collection, condition, projection, option) => {
    return new Promise((resolve, reject) => {
        Service.getData(collection, condition, projection, option).then(result => {
            resolve(result)
        }).catch(reason => {
            reject(reason)
        })
    });
}

/**
 * @description: helping function for token updation 
 */
function tokenUpdate(data, type = Config.APP_CONSTANTS.DATABASE.USER_TYPE.USER) {
    let tokenData = {
        _id: data._id,
        type: type
    };

    return new Promise((resolve, reject) => {
        TokenManager.setToken(tokenData).then(result => {
            resolve(result)
        }).catch(reason => {
            reject(reason)
        })
    });
}

let showSpinWheelList = async (request, userData) => {
    try {
        let limit = request.query.limit;
        let pageNo = request.query.pageNo;
        //let organizationName = request.query.organizationName;

        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }

        console.log(">>>>>>>>>>>>>>>>>>>>>>>", request.query);

        let criteria = {
            isDeleted: false
        };

        // if (organizationName) {
        //     organizationName = new RegExp(organizationName, 'i');
        //     criteria.organizationName = organizationName;
        // }

        let pipeline = [
            {
                $match: criteria
            },
            {
                $sort: {
                    createdAt: -1
                }
            },
            {
                $skip: (pageNo - 1) * limit
            },
            {
                $limit: limit
            }
        ];

        let count = await Service.count(Model.SpinWheel, { isDeleted: false });

        console.log(">>>>>>>>>>>>>>>>>>>>", count)

        let data = await aggregateData(Model.SpinWheel, pipeline);
        console.log(data);

        return {
            pageNo: pageNo,
            limit: limit,
            count: count,
            data: data
        }

        let spinWheels = await Model.SpinWheel.aggregate(Model.SpinWheel, pipeline);
    } catch (err) {
        console.log(err);
    }
}

let addSpinWheel = async (request, userData) => {
    try {

        await Service.update(Model.SpinWheel, { isActive: true }, { $set: { isActive: false } }, { lean: true });

        let body = {};
        body = request.payload;
        let SpinWheel = await Service.saveData(Model.SpinWheel, body)
        return SpinWheel;
    } catch (e) {
        console.log(e);
    }
};


let addEditCharityOrgList = async (request, userData) => {
    try {
        if (request.payload.organizationId) {
            let criteria = {
                _id: request.payload.organizationId,
                isDeleted: false
            }
            let dataToUpdate = request.payload;

            let organization = await Service.update(Model.CharityOrgList, criteria, dataToUpdate, { lean: true });

            return Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.UPDATED
        }

        let body = {}

        body = request.payload;

        let OrganizationList = await Service.saveData(Model.CharityOrgList, body);

        return OrganizationList;

    } catch (e) {
        console.log(e);
    }
};

let showCharityOrgList = async (request, userData) => {
    try {
        console.log("????????M MMMMMMMMMMMMMMMMMMMM", JSON.stringify(request.query));
        let limit = request.query.limit;
        let pageNo = request.query.pageNo;
        let organizationName = request.query.organizationName;


        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }

        let criteria = {
            isDeleted: false
        };

        if (organizationName) {
            criteria.organizationName = {
                $regex: organizationName, $options: 'i'
            }
        };
        console.log("????????M MMMMMMMMMMMMMMMMMMMM 1111111111111111111");
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
                                organizationLink: "$organizationLink",
                                organizationName: "$organizationName",
                                createdAt: "$createdAt"
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


        console.log(">>>>>>>>>>>>>>><<<<<<<<<<<<<<<<<<<???????????????", criteria);



        let data = await Model.CharityOrgList.aggregate(pipeline);

        //let count;

        // if (organizationName) {
        //     count = data.length;
        // } else {
        //     count = await Service.count(Model.CharityOrgList, criteria);
        // }

        return {
            pageNo: pageNo,
            limit: limit,
            count: data[0].count,
            data: data[0].data
        }

    } catch (err) {
        console.log(err);
    }
};


let showCharityDonationList = async (request) => {
    try {

        let limit = request.query.limit;
        let pageNo = request.query.pageNo;
        let search = request.query.search;

        let criteria = {
            isDeleted: false
        };

        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }


        let pipeline = [
            {
                $match: criteria
            },
            {
                $lookup: {
                    from: "charityorglists",
                    localField: "organizationId",
                    foreignField: "_id",
                    as: "organizationDetails"
                }

            },
            {
                $unwind: "$organizationDetails"

            },
            {
                $lookup: {
                    from: "users",
                    localField: "userId",
                    foreignField: "_id",
                    as: "userDetails"
                }
            },

            {
                $unwind: "$userDetails"
            },
            {
                $project: {
                    "userDetails.fullName": "$userDetails.fullName",
                    "userDetails._id": "$userDetails._id",
                    "userDetails.imageUrl": "$userDetails.imageUrl",
                    "organizationDetails.organizationName": "$organizationDetails.organizationName",
                    givenPoint: "$givenPoint",
                    amount: "$amount",
                    createdAt: "$createdAt"
                }
            }
        ];


        if (search) {
            pipeline.push({
                $match: {
                    $or: [
                        {
                            "organizationDetails.organizationName": {
                                $regex: search, $options: 'i'
                            }
                        },
                        {
                            "userDetails.fullName": {
                                $regex: search, $options: 'i'
                            }

                        }
                    ]
                }
            });
        }


        pipeline.push({
            $facet: {
                data: [
                    { $sort: { createdAt: -1 } },
                    { $skip: (pageNo - 1) * limit },
                    { $limit: limit },
                    {
                        $project: {
                            "userDetails.fullName": "$userDetails.fullName",
                            "userDetails._id": "$userDetails._id",
                            "userDetails.imageUrl": "$userDetails.imageUrl",
                            "organizationDetails.organizationName": "$organizationDetails.organizationName",
                            givenPoint: "$givenPoint",
                            amount: "$amount",
                            createdAt: "$createdAt"
                        }
                    }
                ],
                count: [{ $count: 'count' }]
            }
        });

        pipeline.push({
            $project: {
                count: { $arrayElemAt: ["$count.count", 0] },
                data: "$data"
            }
        });




        // pipeline.push({
        //     $sort: {
        //         createdAt: -1
        //     }
        // });

        // pipeline.push({
        //     $skip: (pageNo - 1) * limit
        // });



        // pipeline.push({
        //     $limit: limit
        // });



        let data = await Model.CharityDonationList.aggregate(pipeline);

        // let count;

        // if (search) {
        //     let res = await Model.CharityDonationList.aggregate(pipeline2);
        //     count = res.length;
        // } else {
        //     count = await Service.count(Model.CharityDonationList, { isDeleted: false });
        // }

        return response = {
            pageNo: request.query.pageNo,
            limit: request.query.limit,
            count: data[0].count,
            data: data[0].data
        }

    } catch (e) {
        console.log(e);
    }
};


let showRedeemCardsList = async (request) => {
    try {

        let limit = request.query.limit;
        let pageNo = request.query.pageNo;
        let search = request.query.search;

        let criteria = {
            isDeleted: false
        };


        if (!pageNo) {
            pageNo = 1;
        }
        if (!limit) {
            limit = 10;
        }

        let pipeline = [
            {
                $match: criteria
            },
            {
                $lookup: {
                    from: "users",
                    localField: "userId",
                    foreignField: "_id",
                    as: "userDetails"
                }
            },
            {
                $unwind: "$userDetails"
            }
        ];


        if (search) {
            pipeline.push({
                $match: {
                    $or: [
                        {
                            rewardName: {
                                $regex: search, $options: 'i'
                            }
                        },
                        {
                            "userDetails.fullName": {
                                $regex: search, $options: 'i'
                            }

                        }
                    ]
                }
            });
        }



        pipeline.push({
            $facet: {
                data: [
                    { $sort: { createdAt: -1 } },
                    { $skip: (pageNo - 1) * limit },
                    { $limit: limit },
                    {
                        $project: {
                            "userDetails.fullName": "$userDetails.fullName",
                            "userDetails._id": "$userDetails._id",
                            "userDetails.imageUrl": "$userDetails.imageUrl",
                            _id: "$_id",
                            rewardName: "$rewardName",
                            claimCode: "$claimCode",
                            referenceOrderID: "$referenceOrderID",
                            amount: "$amount",
                            createdAt: "$createdAt"
                        }
                    }
                ],
                count: [{ $count: 'count' }]
            }
        });

        pipeline.push({
            $project: {
                count: { $arrayElemAt: ["$count.count", 0] },
                data: "$data"
            }
        });



        let data = await Model.GiftOrder.aggregate(pipeline);

        console.log(data);


        return response = {
            pageNo: request.query.pageNo,
            limit: request.query.limit,
            count: data[0].count,
            data: data[0].data
        }

    } catch (e) {
        console.log(e);
    }
};

let deleteSpinWheel = async (request, userData) => {

    try {
        let criteria = {
            _id: request.params.id
        };

        let wheel = await Service.findOne(Model.SpinWheel, criteria, {}, { lean: true });

        if (wheel.isActive) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.CANT_DELETE_SPIN_WHEEL);
        }
        let spinWheel = await Service.findAndUpdate(Model.SpinWheel, criteria, { $set: { isDeleted: true } }, { lean: true });

        if (spinWheel) {
            return Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.DELETED
        }

        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)

    } catch (err) {
        console.log(err)
    }
};

let deleteOrganization = async (request, userData) => {

    try {
        let criteria = {
            _id: request.params.id
        };

        let organization = await Service.findAndUpdate(Model.CharityOrgList, criteria, { $set: { isDeleted: true } }, { lean: true });

        if (organization) {
            return Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.DELETED
        }

        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)

    } catch (err) {
        console.log(err);
    }
};

let updateSpinWheel = async (request, userData) => {

    try {

        let criteria = {
            _id: request.payload.spinId,
            isDeleted: false
        }


        let pipeline = [
            {
                $match: {
                    _id: {
                        $ne: mongoose.Types.ObjectId(request.payload.spinId)
                    },
                    isDeleted: false
                }
            },
            {
                $group: {
                    _id: null,
                    Ids: { $push: "$_id" }
                }
            },
            {
                $project: {
                    _id: 0,
                    ids: "$Ids"
                }
            }
        ];



        if (request.payload.isActive) {

            // let spinWheelIds = Model.SpinWheel.aggregate(pipeline);
            let spinWheelIds = await Model.SpinWheel.aggregate(pipeline);


            console.log("spinWheelIds", spinWheelIds[0].ids);

            let spinIds = spinWheelIds[0].ids

            let criteria3 = {
                _id: {
                    $in: spinIds
                }
            };

            await Service.update(Model.SpinWheel, criteria3, { $set: { isActive: false } });

            await Service.update(Model.SpinWheel, criteria, { $set: { isActive: true } });

        }
        else {
            let spinwheel = Service.getData(Model.SpinWheel, { isActive: true, isDeleted: false }, {}, { lean: true });

            console.log("spinwheel.length > 0 && spinwheel[0]._id === request.payload.spinId", spinwheel.length > 0 && spinwheel[0]._id === request.payload.spinId);

            if (spinwheel.length > 0 && spinwheel[0]._id === request.payload.spinId) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.CANT_DEACTIVE_SPIN_WHEEL);
            }
            else {
                await Service.update(Model.SpinWheel, criteria, { $set: { isActive: request.payload.isActive } })
            }
        }

        return 'Success';

    } catch (err) {
        console.log(err);
        return err;
    }
}

let addDailyChallenge = async (request) => {

    try {

        let payload = request.payload;

        let dataToSet = {
            title: payload.title,
            description: payload.description,
            type: payload.challengeType,
            rewardPoint: payload.rewardPoints
        }

        if (payload.isActive) {
            isActive = payload.isActive
        }

        let res = await Service.saveData(Model.DailyChallenge, dataToSet);

        return res;

    } catch (err) {
        throw err;
    }

}

let getDailyChallenges = async () => {
    try {
        let res = await Service.getData(Model.DailyChallenge, { isDeleted: false }, {}, { lean: true });
        if (res.length) {
            return res
        }
        return 'No Daily Challenges Found'
    } catch (err) {
        throw err;
    }
}



module.exports = {
    adminLogin,
    getUsers,
    getPosts,
    updateUser,
    updatePost,
    addEditInterst,
    getIntersts,
    getGroups,
    updateGroups,
    getCommentsOfPost,
    dashboardData,
    uploadFileToS3,
    userContact,
    addSpinWheel,
    addEditCharityOrgList,
    showCharityDonationList,
    showCharityOrgList,
    showSpinWheelList,
    deleteSpinWheel,
    deleteOrganization,
    showRedeemCardsList,
    updateSpinWheel,
    addDailyChallenge,
    getDailyChallenges
}