let CronJob = require('cron').CronJob
const Service = require('./Services/queries');
const Model = require('./Models');
const sendEmail = require('./Libs/email');
const async = require('async')
const moment = require('moment')
const Config = require('./Configs');


exports.autoDeletePosts = new CronJob('* * * * * *', async function () {
    let time = Date.now()
    let criteria = {
        isBlocked: false,
        isDeleted: false,
        expirationTime: { $lte: time }
    };

    let a = await Service.update(Model.Posts, criteria, { $set: { isDeleted: true } }, { new: true, lean: true, multi: true })
    // console.log(a)
});

exports.dailyChallenge = async () => {

    let startDate = moment().startOf('day').valueOf()
    let endDate = moment().endOf('day').valueOf();

    console.log('startDate', startDate);
    console.log('endDate', endDate);

    let dailyChallenge = await Service.findOne(Model.DailyChallenge, { isActive: true }, {}, { lean: true });

    console.log(dailyChallenge);

    switch (dailyChallenge.type) {
        case Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.SMS_CHALLENGE: {

            let pipeLine = [
                {
                    $match: {
                        $and: [
                            { createdDate: { $lte: startDate } },
                            { createdDate: { $gte: endDate } },
                        ]
                    }
                },
                {
                    $project: {
                        senderId: 1
                    }
                },
                {
                    $group: {
                        _id: "$senderId",
                        count: { $sum: 1 }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userId: "$_id",
                        count: "$count",
                    }
                },
                {
                    $group: {
                        _id: null,
                        userIds: {
                            $push: {
                                "$cond": [{ "$gte": ["$count", 100] },
                                    "$userId", "$noval"]
                            }
                        }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userIds: "$userIds"
                    }
                }
            ];

            let res = await aggregateData(Model.Chats, pipeLine);

            console.log(res);

            if (res.length > 0) {
                await Service.update(Model.Users, { _id: { $in: res[0].userIds } }, { $inc: { pointEarned: dailyChallenge.rewardPoint } }, { multi: true });
            }

            break;
        }
        case Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.STORY_CHALLENGE: {

            let pipeLine = [
                {
                    $match: {
                        $and: [
                            { createdOn: { $lte: startDate } },
                            { createdOn: { $gte: endDate } },
                        ]
                    }
                },
                {
                    $project: {
                        postBy: 1
                    }
                },
                {
                    $group: {
                        _id: "$postBy",
                        count: { $sum: 1 }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userId: "$_id",
                        count: "$count",
                    }
                },
                {
                    $group: {
                        _id: null,
                        userIds: {
                            $push: {
                                "$cond": [{ "$gte": ["$count", 5] },
                                    "$userId", "$noval"]
                            }
                        }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userIds: "$userIds"
                    }
                }
            ];

            let res = await aggregateData(Model.Stories, pipeLine);

            if (res.length > 0) {
                await Service.update(Model.Users, { _id: { $in: res[0].userIds } }, { $inc: { pointEarned: dailyChallenge.rewardPoint } }, { multi: true });
            }

            break;
        }
        case Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.POST_CHALLENGE: {

            let pipeLine = [
                {
                    $match: {
                        $and: [
                            { createdOn: { $lte: startDate } },
                            { createdOn: { $gte: endDate } },
                        ]
                    }
                },
                {
                    $project: {
                        postBy: 1
                    }
                },
                {
                    $group: {
                        _id: "$postBy",
                        count: { $sum: 1 }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userId: "$_id",
                        count: "$count",
                    }
                },
                {
                    $group: {
                        _id: null,
                        userIds: {
                            $push: {
                                "$cond": [{ "$gte": ["$count", 10] },
                                    "$userId", "$noval"]
                            }
                        }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        userIds: "$userIds"
                    }
                }
            ];

            let res = await aggregateData(Model.Posts, pipeLine);

            if (res.length > 0) {
                await Service.update(Model.Users, { _id: { $in: res[0].userIds } }, { $inc: { pointEarned: dailyChallenge.rewardPoint } }, { multi: true });
            }

            break;
        }
    }
    return 'Success';

}

// smsCountChallenge();


/**
 * @description: helping function for aggregating data
 */
let aggregateData = function (model, group) {
    return new Promise((resolve, reject) => {
        model.aggregate(group, function (err, data) {

            if (err) return reject(err);
            return resolve(data);
        });
    })
};