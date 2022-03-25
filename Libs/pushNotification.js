
'use strict';
var Config = require('../Configs');
const FCM = require('fcm-node');
const serverKey = 'AAAAGkDkkTU:APA91bF1pc9ZRnzYR3ZL6IEfmpJVkWGePOS5Hb0SXJh8F37EOESKjhMytm-1XInNqYN5OmTOi0jD-rj1K1lcDsWyWbojmwranMxhFYH2Kyjbj4m0hLhdpdtiumEr5y4Qh_-HwrSWOxG1';
const fcm = new FCM(serverKey);

var client = require('twilio')(Config.smsConfig.twilioCredentials.accountSid, Config.smsConfig.twilioCredentials.authToken);


let sendPush = function (deviceToken, data) {

    return new Promise((resolve, reject) => {

        let message;

        if (data.msg === 'Calling') {
            message = {
                // registration_ids: deviceToken,
                to: deviceToken,
                data: data,
                priority: 'high'
            };
        } else {
            message = {
                // registration_ids: deviceToken,
                to: deviceToken,
                notification: {
                    title: 'Photo Drip',
                    body: data.msg,
                    sound: "default",
                    badge: 0
                },
                data: data,
                priority: 'high'
            };
        }


        if (data.imageUrl)
            message.notification.imageUrl = data.imageUrl;
        if (data.userName)
            message.notification.userName = data.userName;
        if (data.fullName)
            message.notification.fullName = data.fullName;
        if (data.userId)
            message.notification.userId = data.userId;

        fcm.send(message, function (err, result) {
            if (err) {
                console.log("Something has gone wrong!", err);
                resolve(null);
            } else {
                console.log("Successfully sent with response: ", result);
                resolve(null, result);
            }
        });
    })
};



let sendMultiUser = function (deviceToken, data) {

    return new Promise((resolve, reject) => {
        let message = {
            registration_ids: deviceToken,
            notification: {
                title: 'Photo Drip',
                body: data.msg,
                sound: "default",
                badge: 0
            },
            data: data,
            priority: 'high'
        };

        if (data.venueTitle) message.notification.venueTitle = data.venueTitle;
        if (data.groupName) message.notification.groupName = data.groupName;
        if (data.imageUrl) message.notification.imageUrl = data.imageUrl;
        if (data.groupId) message.notification.groupId = data.groupId;
        if (data.venueId) message.notification.venueId = data.venueId;

        fcm.send(message, function (err, result) {
            if (err) {
                console.log("Something has gone wrong!", err);
                resolve(null);
            } else {
                console.log("Successfully sent with response: ", result);
                resolve(null, result);
            }
        });

    })

};

let sendPushToUser = function (deviceToken, data) {
    return new Promise((resolve, reject) => {
        console.log(data);
        let message = {
            // registration_ids: deviceToken,
            to: deviceToken,
            notification: {
                title: data.title,
                body: data.body,
                sound: "default",
                badge: 0
            },
            data: data,
            priority: 'high'
        };

        fcm.send(message, function (err, result) {
            if (err) {
                console.log("Something has gone wrong!", err);
                resolve(null);
            } else {
                console.log("Successfully sent with response: ", result);
                resolve(null, result);
            }
        });
    })
};
/*
 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 @ sendSMS Function
 @ This function will initiate sending sms as per the smsOptions are set
 @ Requires following parameters in smsOptions
 @ from:  // sender address
 @ to:  // list of receivers
 @ Body:  // SMS text message
 @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
 */

function sendSMS(smsOptions) {
    return new Promise((resolve, reject) => {
        console.log(smsOptions)
        client.messages.create(smsOptions, function (err, message) {
            console.log('SMS RES', err, message);
            if (err) {
                reject(err)
            }
            else {
                resolve(null, message.sid);
            }
        });
    })

    // cb(null, null); // Callback is outside as sms sending confirmation can get delayed by a lot of time
}

module.exports = {
    sendPush: sendPush,
    sendMultiUser: sendMultiUser,
    sendSMS: sendSMS,
    sendPushToUser: sendPushToUser
};