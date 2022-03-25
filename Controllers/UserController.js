const Config = require('../Configs');
const Model = require('../Models')
const Service = require('../Services/queries')
const UniversalFunctions = require('../Utils/UniversalFunction')
const TokenManager = require('../Libs/TokenManager')
const codegenrate = require('../Libs/CodeGenerator')
const sendEmail = require('../Libs/email').sendEmail;
const UploadMultipart = require('../Libs/UploadMultipart');
const pushNotification = require('../Libs/pushNotification');
const mongoose = require('mongoose');
const socketManager = require('../Libs/SocketManager');
const _ = require("lodash")
const emailTemplates = require("../Libs/emailTemplates")
const Tokbox = require("../Plugins/tokbox/tokbox")
const moment = require("moment")
const apn = require('apn');
const path = require("path");
const debug = require("debug")("userController");
const QRCode = require('qrcode');
const https = require("https");
const querystring = require("querystring");
const Request = require('request');
const { VariableInstance } = require('twilio/lib/rest/serverless/v1/service/environment/variable');
const Models = require('../Models');
const SpinWheel = require('../Models/SpinWheel');
const { CONFIG } = require('../Utils/UniversalFunction');
const Stories = require('../Models/Stories');
const { await } = require('asyncawait');
const TrustedComms = require('twilio/lib/rest/preview/TrustedComms');
const { exist } = require('joi');
const { ObjectId } = require('mongoose');

/*========================login and Signup Related Apis=====================*/
/*===================login and Signup Related Apis=================*/

/**
 * @description regsitering email or phone number at initial stage
 * @param {string} email
 * @param {string} countryCode
 * @param {string} phoneNumber
 * @returns user saved details
 */

let regEmailOrPhone = async (payloadData) => {
    try {
        let query = {}
        if (payloadData.email) {
            let criteria = {
                email: payloadData.email,
                isDeleted: false
            };
            let content;

            let check1 = await getRequired(Model.Users, criteria, {}, {});
            if (check1.length) {
                if (check1[0].isVerified) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ALREADY_EXIST)
                } else {
                    //let otp = await codegenrate.generateUniqueCustomerId(4)
                    query.OTPcode = '1234'
                    // content = `Your Ribbit Rewards Verification OTP is ${otp}`
                    // sendEmail(payloadData.email, "OTP Verification Code", content)
                   //sendEmail.sendEmail(payloadData.email, "OTP Verification Code", content);


                    let dataAgain = await updateData(Model.Users, {
                        _id: check1[0]._id,
                        isDeleted: false
                    }, { $set: { OTPcode: otp } }, { lean: true, new: true })
                    if (dataAgain) {
                        return dataAgain
                    }
                }
            } else {
                query.email = payloadData.email
            }

           // let otp = await codegenrate.generateUniqueCustomerId(4)
            query.OTPcode = '1234'
            // content = `Your Ribbit Rewards Verification OTP is ${otp}`
            // sendEmail(payloadData.email, "OTP Verification Code", content)
            // query.OTPcode = '4444'

            console.log(">>>>>>>>>>>>>>>>",query)

            let data = await Service.saveData(Model.Users, query)
            var QRCodeString = ''
            await QRCode.toDataURL(data._id, async (err, url) => {
                await updateData(Model.Users, {
                    _id: data._id,
                    isDeleted: false
                }, { $set: { QRCode: url } }, { lean: true, new: true })
                data.QRCode = url
            });

            if (data) {
                    return data;
            }
        }
        if (payloadData.phoneNumber && payloadData.countryCode) {
            let criteria = {
                $or: [{
                    phoneNumber: payloadData.phoneNumber
                }, {
                    phoneNumber: payloadData.phoneNumber,
                    countryCode: payloadData.countryCode,
                    isDeleted: false
                }]
            };
            let check1 = await getRequired(Model.Users, criteria, {}, {});
            if (check1.length) {
                if (check1[0].isVerified) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                } else {
                    let otp = await codegenrate.generateUniqueCustomerId(4)
                    let dataAgain = await updateData(Model.Users, {
                        _id: check1[0]._id,
                        isDeleted: false
                    }, { $set: { OTPcode: otp } }, { lean: true, new: true })
                    if (dataAgain) {

                        let otp = await codegenrate.generateUniqueCustomerId(4)
                        let smsData = {
                            to: payloadData.countryCode + payloadData.phoneNumber,
                            from: "+15045094820",
                            body: "Your Ribbit Rewards Verification OTP is : " + otp
                        }
                        console.log(smsData);
                        await pushNotification.sendSMS(smsData);
                        return dataAgain
                    }
                }
            } else {
                query.phoneNumber = payloadData.phoneNumber
                query.countryCode = payloadData.countryCode
                query.fullphoneNumber = payloadData.countryCode + payloadData.phoneNumber
            }

            let otp = await codegenrate.generateUniqueCustomerId(4)
            query.OTPcode = otp
            //query.OTPcode = '4444'


            let data = await Service.saveData(Model.Users, query)
            if (data) {

                let smsData = {
                    to: payloadData.countryCode + payloadData.phoneNumber,
                    from: "+15045094820",
                    body: "Your Ribbit Rewards Verification OTP is : " + otp
                }
                //console.log(smsData);
                await pushNotification.sendSMS(smsData);
                // console.log(smsResult)
                return data
            }
        }
    } catch (e) {
        console.log(e)
    }

}





/**
 * @description verifying otp by matching it to the database
 * @param {string} email
 * @param {string} countryCode
 * @param {string} phoneNumber
 * @param {string} otp
 * @returns either user is verified or approve user verification
 */

let verifyOtp = async (payloadData) => {
    try {
        let update = {};
        let data;
        let criteria = {
            OTPcode: payloadData.otp
        };
        if (payloadData.email) {
            let query = {
                isDeleted: false,
                email: payloadData.email
            }

            let check1 = await getRequired(Model.Users, query, {}, { lean: true });
            if (check1.length == 0)
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_EMAIL)
            else if (check1[0].isVerified) {
                return check1[0]
            } else {
                update.isEmailVerified = true
                data = check1[0]
                criteria.email = payloadData.email
            }

        }
        if (payloadData.phoneNumber || payloadData.countryCode) {

            let query = {
                isDeleted: false,
            }
            if (payloadData.phoneNumber) {
                query.phoneNumber = payloadData.phoneNumber
            } else if (payloadData.phoneNumber && payloadData.countryCode) {
                query.phoneNumber = payloadData.phoneNumber
                query.countryCode = payloadData.countryCode
            }
            let check1 = await getRequired(Model.Users, query, {}, { lean: true });

            if (check1.length == 0) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_PHONE_NUMBER)
            } else if (check1[0].isVerified) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ACCOUNT_VERIFIED_ALREADY)
            } else {
                data = check1[0]
                //update.isPhoneNumberVerified = true
                if (payloadData.phoneNumber) {
                    criteria.phoneNumber = payloadData.phoneNumber
                } else if (payloadData.phoneNumber && payloadData.countryCode) {
                    criteria.phoneNumber = payloadData.phoneNumber
                    criteria.countryCode = payloadData.countryCode
                }
            }
        }

        if (!data.password) {
            update.isVerified = true;
            //update.OTPcode = '';
            update.isPhoneNumberVerified = true
            let upData = await Service.findAndUpdate(Model.Users, criteria, update, { new: true })
            if (upData)
                return upData
            else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_CODE)
        } else return data
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description user signup through case 1. facebook signup, case 2. google signup, 3. signup through phone number, 4. signup through email
 * @param {string} email
 * @param {string} countryCode
 * @param {string} phoneNumber
 * @param {string} fullName
 * @param {string} userName
 * @param {string} facebookId
 * @param {string} googleId
 * @param {string} password
 * @returns signup details with accessToken
 */

let userSignUp = async (payloadData) => {
    try {
        let criteria = {
            isDeleted: false
        }
        let populate = [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
            path: "imageVisibility",
            select: "userName imageUrl fullName",
            model: "Users"
        }, {
            path: "nameVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "tagPermission",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "personalInfoVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }]
        let dataToSet = { platform: payloadData.platform };

        switch (payloadData.flag) {
            case 1: {

                if (payloadData.fullName) {
                    dataToSet.fullName = payloadData.fullName
                    let arrayFullName = payloadData.fullName.split(/(\s+)/).filter(function (e) {
                        return e.trim().length > 0;
                    });
                    dataToSet.firstName = arrayFullName[0]
                    dataToSet.lastName = arrayFullName[1]
                }
                if (payloadData.countryCode)
                    dataToSet.countryCode = payloadData.countryCode
                if (payloadData.phoneNumber)
                    dataToSet.phoneNumber = payloadData.phoneNumber
                if (payloadData.phoneNumber && payloadData.countryCode)
                    dataToSet.fullphoneNumber = payloadData.countryCode + payloadData.phoneNumber
                if (payloadData.facebookId)
                    dataToSet.facebookId = payloadData.facebookId
                if (payloadData.userName) {
                    let checkUserName = await getRequired(Model.Users, { userName: payloadData.userName }, {}, { lean: true })
                    if (checkUserName.length) {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_ALREADY_REGISTERED)
                    } else {
                        dataToSet.userName = payloadData.userName
                    }
                }
                if (payloadData.deviceId)
                    dataToSet.deviceId = payloadData.deviceId
                if (payloadData.email) {
                    dataToSet.email = payloadData.email
                    dataToSet.isEmailVerified = true
                }
                if (payloadData.deviceToken) {
                    dataToSet.deviceToken = payloadData.deviceToken;
                }
                if (payloadData.apnsDeviceToken) {
                    dataToSet.apnsDeviceToken = payloadData.apnsDeviceToken;
                }

                if (payloadData.facebookId) {

                    let query = {
                        facebookId: payloadData.facebookId,
                        isDeleted: false
                    };

                    let check1 = await getRequired(Model.Users, query, {}, { lean: true })


                    if (check1.length) {
                        if (payloadData.userName && payloadData.fullName && payloadData.countryCode && payloadData.phoneNumber) {
                            if (!check1[0].email && payloadData.email) {
                                let check3 = await getRequired(Model.Users, { email: payloadData.email }, {}, { lean: true });
                                if (check3.length) {
                                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.EMAIL_ALREADY_EXITS)
                                }
                            }
                            let check3 = await getRequired(Model.Users, {
                                phoneNumber: payloadData.phoneNumber,
                                countryCode: payloadData.countryCode
                            }, {}, { lean: true });
                            if (check3.length) {
                                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                            }
                            await updateData(Model.Users, query, { $set: dataToSet }, { new: true })
                        }
                        console.log("Inside Facebook Signup: " + JSON.stringify(check1));
                        let data = await tokenUpdate(check1[0])

                        if (data.length) {
                            // /* BOC Create customer at tango card*/
                            // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data[0]._id);
                            // console.log("Inside Facebook Signup with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                            let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                                lean: true,
                                new: true
                            }, populate)
                            let groupCount = await Service.count(Model.PostGroupMembers, {
                                userId: data[0]._id,
                                isDeleted: false
                            })
                            finalData[0].groupCount = groupCount
                            return finalData[0]
                        }
                    } else if (payloadData.email) {
                        let query = {
                            email: payloadData.email,
                            isDeleted: false
                        };
                        let check2 = await getRequired(Model.Users, query, {}, { lean: true });

                        if (check2.length) {
                            if (!check2[0].facebookId && payloadData.facebookId) {
                                let updateUserData = await updateData(Model.Users, {
                                    _id: check2[0]._id,
                                    isDeleted: false
                                }, {
                                    $set: {
                                        facebookId: payloadData.facebookId,
                                        deviceToken: payloadData.deviceToken
                                    }
                                }, { new: true, lean: true })
                                if (updateUserData) {
                                    console.log("Inside Facebook Signup using Email: " + JSON.stringify(check2[0]));
                                    await tokenUpdate(check2[0])
                                    //         /* BOC Create customer at tango card*/
                                    //         let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, check2[0]._id);
                                    //         console.log("Inside Facebook Signup using Email with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                                    //         /* EOC Create customer at tango card*/
                                    //         /* BOC Create customer Account at tango card*/
                                    //         let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, check2[0]._id);
                                    //         console.log("Inside Facebook Signup using Email with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                                    // /* EOC Create customer Account at tango card*/

                                    let finalData = await Service.populateData(Model.Users, { _id: check2[0]._id }, {}, {
                                        lean: true,
                                        new: true
                                    }, populate)
                                    let groupCount = await Service.count(Model.PostGroupMembers, {
                                        userId: check2[0]._id,
                                        isDeleted: false
                                    })
                                    finalData[0].groupCount = groupCount
                                    return finalData[0]
                                }
                            }
                        } else {
                            if (payloadData.phoneNumber && payloadData.countryCode) {
                                let check3 = await getRequired(Model.Users, {
                                    phoneNumber: payloadData.phoneNumber,
                                    countryCode: payloadData.countryCode,
                                    deviceToken: payloadData.deviceToken
                                }, {}, { lean: true });

                                if (check3.length) {
                                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                                }
                            }

                            // let otp = await codegenrate.generateUniqueCustomerId(4)
                            // let smsData = {
                            //     to: payloadData.countryCode + payloadData.phoneNumber,
                            //     from: "+15045094820",
                            //     body: "Your one time Password for Verification of Ribbit Rewards Plaform is : " + otp
                            // }
                            // //console.log(smsData);
                            // await pushNotification.sendSMS(smsData);


                            dataToSet.email = payloadData.email
                            dataToSet.isEmailVerified = true
                            dataToSet.isVerified = true
                            dataToSet.isPasswordExist = true
                            // dataToSet.OTPcode = otp
                            dataToSet.isSocialLogin = true
                            dataToSet.isProfileComplete = true

                            dataToSet.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                            console.log(dataToSet);
                            let data = await createData(Model.Users, dataToSet)
                            // if(data){
                            //     let url =`http://52.35.234.66:8000/user/emailVerification?id=${data._id}&timestamp=${+new Date()}`;
                            //     let emailTemplatesToSend = await emailTemplates.emailVerification({userName: data.userName, email: payloadData.email}, url)
                            //     sendEmail.sendEmail(payloadData.email, "Conversify Verify Email", emailTemplatesToSend)
                            // }

                            await QRCode.toDataURL(data.id, async (err, url) => {
                                await updateData(Model.Users, {
                                    _id: data._id,
                                    isDeleted: false
                                }, { $set: { QRCode: url } }, { lean: true, new: true })
                                data.QRCode = url
                            });

                            await tokenUpdate(data);
                            // /* BOC Create customer at tango card*/
                            // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data.id);
                            // console.log("Inside Facebook Signup Email 1st time with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                            // /* EOC Create customer at tango card*/

                            // /* BOC Create customer Account at tango card*/
                            // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data.id);
                            // console.log("Inside Facebook Signup Email 1st time with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                            // /* EOC Create customer Account at tango card*/

                            let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, {
                                lean: true,
                                new: true
                            }, populate)
                            let groupCount = await Service.count(Model.PostGroupMembers, {
                                userId: data._id,
                                isDeleted: false
                            })
                            finalData[0].groupCount = groupCount
                            let verificationData = await settingVerification({}, data)

                            if (verificationData) {
                                finalData[0].isUploaded = verificationData.isUploaded
                                finalData[0].isPhoneNumberVerified = verificationData.isPhoneNumberVerified
                                finalData[0].isEmailVerified = verificationData.isEmailVerified
                                finalData[0].isPassportVerified = verificationData.isPassportVerified
                            }
                            return finalData[0]
                        }
                    } else {
                        // dataToSet.email = payloadData.email
                        // dataToSet.isEmailVerified = true

                        if (payloadData.phoneNumber && payloadData.countryCode) {
                            let check3 = await getRequired(Model.Users, {
                                phoneNumber: payloadData.phoneNumber,
                                countryCode: payloadData.countryCode
                            }, {}, { lean: true });

                            if (check3.length) {
                                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                            }
                        }

                        // if (payloadData.referralCode) {
                        //     let checkForReffrel = await getRequired(Model.Users,
                        //         {
                        //             referralCode: payloadData.referralCode
                        //         }, {}, { lean: true });

                        //     if (checkForReffrel.length) {
                        //         await updateData(Model.Users, {
                        //             _id: checkForReffrel[0]._id
                        //         }, { $inc: { pointEarned: 1250 } });

                        //         let pushData = {
                        //             TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
                        //             id: checkForReffrel[0]._id,
                        //             msg: `${checkForReffrel[0].fullName} Congratulations you has won 1250 pts through Invite People`
                        //         };

                        //         pushNotification.sendPush(checkForReffrel[0].deviceToken, pushData, (err, res) => {
                        //             console.log(err, res)
                        //         });

                        //         let saveData = {
                        //             userId: checkForReffrel[0]._id,
                        //             pointEarned: 1250,
                        //             source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.INVITE_PEOPLE,
                        //             date: moment().format('MM/DD/YYYY')
                        //         }

                        //         await Service.saveData(Model.PointEarnedHistory, saveData);
                        //     }
                        //     else {
                        //         return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NO_USER_FOUND_FOR_THIS_REFFRAL_CODE);
                        //     }
                        // }

                        dataToSet.isVerified = true
                        dataToSet.isPasswordExist = true
                        dataToSet.isProfileComplete = true

                        // let otp = await codegenrate.generateUniqueCustomerId(4)
                        // let smsData = {
                        //     to: payloadData.countryCode + payloadData.phoneNumber,
                        //     from: "+15045094820",
                        //     body: "Your one time Password for Verification of Ribbit Rewards Plaform is : " + otp
                        // }
                        // //console.log(smsData);
                        // await pushNotification.sendSMS(smsData);

                        // dataToSet.OTPcode = otp

                        dataToSet.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }

                        if (payloadData.userType) {
                            dataToSet.userType = payloadData.userType
                        }
                        console.log(dataToSet);
                        let data = await createData(Model.Users, dataToSet)

                        await QRCode.toDataURL(data.id, async (err, url) => {
                            await updateData(Model.Users, {
                                _id: data._id,
                                isDeleted: false
                            }, { $set: { QRCode: url } }, { lean: true, new: true })
                            data.QRCode = url
                        });
                        // if(data){
                        //     let url =`http://52.35.234.66:8000/user/emailVerification?id=${data._id}&timestamp=${+new Date()}`;
                        //     let emailTemplatesToSend = await emailTemplates.emailVerification({userName: data.userName, email: payloadData.email}, url)
                        //     sendEmail.sendEmail(payloadData.email, "Conversify Verify Email", emailTemplatesToSend)
                        // }
                        await tokenUpdate(data);
                        // /* BOC Create customer at tango card*/
                        // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data.id);
                        // console.log("Inside Facebook Signup 1st time with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                        // /* EOC Create customer at tango card*/

                        // /* BOC Create customer Account at tango card*/
                        // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data._id);
                        // console.log("Inside Facebook Signup 1st time with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                        // /* EOC Create customer Account at tango card*/

                        let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, {
                            lean: true,
                            new: true
                        }, populate)
                        let groupCount = await Service.count(Model.PostGroupMembers, {
                            userId: data._id,
                            isDeleted: false
                        })
                        finalData[0].groupCount = groupCount
                        let verificationData = await settingVerification({}, data)

                        if (verificationData) {
                            finalData[0].isUploaded = verificationData.isUploaded
                            finalData[0].isPhoneNumberVerified = verificationData.isPhoneNumberVerified
                            finalData[0].isEmailVerified = verificationData.isEmailVerified
                            finalData[0].isPassportVerified = verificationData.isPassportVerified
                        }
                        return finalData[0]
                    }

                } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ID_MISSING)
            }
                break;

            case 2: {
                if (payloadData.fullName) {
                    dataToSet.fullName = payloadData.fullName
                    let arrayFullName = payloadData.fullName.split(/(\s+)/).filter(function (e) {
                        return e.trim().length > 0;
                    });
                    dataToSet.firstName = arrayFullName[0]
                    dataToSet.lastName = arrayFullName[1]
                }
                if (payloadData.countryCode)
                    dataToSet.countryCode = payloadData.countryCode
                if (payloadData.phoneNumber)
                    dataToSet.phoneNumber = payloadData.phoneNumber
                if (payloadData.phoneNumber && payloadData.countryCode)
                    dataToSet.fullphoneNumber = payloadData.countryCode + payloadData.phoneNumber
                if (payloadData.googleId)
                    dataToSet.googleId = payloadData.googleId
                if (payloadData.userName) {
                    let checkUserName = await getRequired(Model.Users, { userName: payloadData.userName }, {}, { lean: true })
                    if (checkUserName.length) {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_ALREADY_REGISTERED)
                    } else {
                        dataToSet.userName = payloadData.userName
                    }
                }
                if (payloadData.deviceId)
                    dataToSet.deviceId = payloadData.deviceId
                if (payloadData.email) {
                    dataToSet.email = payloadData.email
                    dataToSet.isEmailVerified = true
                }
                dataToSet.isVerified = true
                if (payloadData.googleId) {
                    let query = {
                        googleId: payloadData.googleId,
                        isDeleted: false
                    };
                    let check1 = await getRequired(Model.Users, query, {}, { lean: true })

                    if (check1.length) {
                        if (payloadData.userName && payloadData.fullName && payloadData.countryCode && payloadData.phoneNumber) {

                            let check3 = await getRequired(Model.Users, {
                                phoneNumber: payloadData.phoneNumber,
                                countryCode: payloadData.countryCode
                            }, {}, { lean: true });
                            if (check3.length) {
                                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                            }
                            await updateData(Model.Users, query, { $set: dataToSet }, { new: true })
                        }else{
                            await updateData(Model.Users, query, { $set: dataToSet }, { new: true })
                        }
                        
                        let data = await tokenUpdate(check1[0])
                        if (data.length) {
                            // /* BOC Create customer at tango card*/
                            // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data.id);
                            // console.log("Inside Google Signup with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                            // /* EOC Create customer at tango card*/

                            // /* BOC Create customer Account at tango card*/
                            // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data._id);
                            // console.log("Inside Google Signup with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                            // /* EOC Create customer Account at tango card*/

                            let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                                lean: true,
                                new: true
                            }, populate)
                            let groupCount = await Service.count(Model.PostGroupMembers, {
                                userId: data[0]._id,
                                isDeleted: false
                            })
                            finalData[0].groupCount = groupCount
                            console.log("1", finalData[0])
                            return finalData[0]
                        }

                    } else if (payloadData.email) {

                        let query = {
                            email: payloadData.email,
                            isDeleted: false
                        };
                        let check2 = await getRequired(Model.Users, query, {}, { lean: true });

                        if (check2.length) {
                            if (!check2[0].googleId && payloadData.googleId) {
                                let updateUserData = await updateData(Model.Users, {
                                    _id: check2[0]._id,
                                    isDeleted: false
                                }, {
                                    $set: {
                                        googleId: payloadData.googleId,
                                        deviceToken: payloadData.deviceToken
                                    }
                                }, { new: true, lean: true })
                                if (updateUserData) {
                                    console.log("Inside Email Signup: " + JSON.stringify(check2));
                                    await tokenUpdate(check2[0]);
                                    //         /* BOC Create customer at tango card*/
                                    //         let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, check2[0]._id);
                                    //         console.log("Inside Email Signup with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                                    //         /* EOC Create customer at tango card*/

                                    //         /* BOC Create customer Account at tango card*/
                                    // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, check2[0]._id);
                                    // console.log("Inside Email Signup with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                                    // /* EOC Create customer Account at tango card*/

                                    let finalData = await Service.populateData(Model.Users, { _id: check2[0]._id }, {}, {
                                        lean: true,
                                        new: true
                                    }, populate)
                                    let groupCount = await Service.count(Model.PostGroupMembers, {
                                        userId: check2[0]._id,
                                        isDeleted: false
                                    })
                                    finalData[0].groupCount = groupCount
                                    console.log("2", finalData[0])
                                    return finalData[0]
                                }
                            }
                        } else {
                            if (payloadData.phoneNumber && payloadData.countryCode) {
                                let check3 = await getRequired(Model.Users, {
                                    phoneNumber: payloadData.phoneNumber,
                                    countryCode: payloadData.countryCode
                                }, {}, { lean: true });

                                if (check3.length) {
                                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                                }
                            }

                            dataToSet.email = payloadData.email
                            dataToSet.isEmailVerified = true
                            dataToSet.isVerified = true
                            dataToSet.isPasswordExist = true
                            dataToSet.isSocialLogin = true
                            dataToSet.isProfileComplete = true
                            // let otp = await codegenrate.generateUniqueCustomerId(4)
                            // let smsData = {
                            //     to: payloadData.countryCode + payloadData.phoneNumber,
                            //     from: "+15045094820",
                            //     body: "Your one time Password for Verification of Ribbit Rewards Plaform is : " + otp
                            // }
                            // //console.log(smsData);
                            // await pushNotification.sendSMS(smsData);
                            // dataToSet.OTPcode = otp

                            dataToSet.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }

                            if (payloadData.userType) {
                                dataToSet.userType = payloadData.userType
                            }

                            let data = await createData(Model.Users, dataToSet)
                            await QRCode.toDataURL(data.id, async (err, url) => {
                                await updateData(Model.Users, {
                                    _id: data._id,
                                    isDeleted: false
                                }, { $set: { QRCode: url } }, { lean: true, new: true })
                                data.QRCode = url
                            });
                            // if(data){
                            //     let url =`http://52.35.234.66:8000/user/emailVerification?id=${data._id}&timestamp=${+new Date()}`;
                            //     let emailTemplatesToSend = await emailTemplates.emailVerification({userName: data.userName, email: payloadData.email}, url)
                            //     sendEmail.sendEmail(payloadData.email, "Conversify Verify Email", emailTemplatesToSend)
                            // }
                            await tokenUpdate(data)
                            // /* BOC Create customer at tango card*/
                            // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data._id);
                            // console.log("Inside Google Signup+Email 1st time with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                            // /* EOC Create customer at tango card*/

                            // /* BOC Create customer Account at tango card*/
                            // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data._id);
                            // console.log("Inside Google Signup+Email 1st time with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                            // /* EOC Create customer Account at tango card*/

                            let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, {
                                lean: true,
                                new: true
                            }, populate)
                            let groupCount = await Service.count(Model.PostGroupMembers, {
                                userId: data._id,
                                isDeleted: false
                            })
                            finalData[0].groupCount = groupCount
                            finalData[0].isPhoneNumberVerified = false
                            console.log("3", finalData[0])
                            return finalData[0]
                        }
                    } else {

                        if (payloadData.phoneNumber && payloadData.countryCode) {
                            let check3 = await getRequired(Model.Users, {
                                phoneNumber: payloadData.phoneNumber,
                                countryCode: payloadData.countryCode
                            }, {}, { lean: true });
                            if (check3.length) {
                                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                            }
                        }
                        dataToSet.isVerified = true
                        dataToSet.isPasswordExist = true
                        dataToSet.isProfileComplete = true

                        // let otp = await codegenrate.generateUniqueCustomerId(4)
                        // let smsData = {
                        //     to: payloadData.countryCode + payloadData.phoneNumber,
                        //     from: "+15045094820",
                        //     body: "Your one time Password for Verification of Ribbit Rewards Plaform is : " + otp
                        // }
                        // //console.log(smsData);
                        // await pushNotification.sendSMS(smsData);
                        // dataToSet.OTPcode = otp

                        dataToSet.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }

                        let data = await createData(Model.Users, dataToSet)
                        await QRCode.toDataURL(data.id, async (err, url) => {
                            await updateData(Model.Users, {
                                _id: data._id,
                                isDeleted: false
                            }, { $set: { QRCode: url } }, { lean: true, new: true })
                            data.QRCode = url
                        });
                        // if(data){
                        //     let url =`http://52.35.234.66:8000/user/emailVerification?id=${data._id}&timestamp=${+new Date()}`;
                        //     let emailTemplatesToSend = await emailTemplates.emailVerification({userName: data.userName, email: payloadData.email}, url)
                        //     sendEmail.sendEmail(payloadData.email, "Conversify Verify Email", emailTemplatesToSend)
                        // }
                        await tokenUpdate(data)
                        // /* BOC Create customer at tango card*/
                        // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data._id);
                        // console.log("Inside Google + PhoneNo Signup 1st time with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                        // /* EOC Create customer at tango card*/

                        // /* BOC Create customer Account at tango card*/
                        // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data._id);
                        // console.log("Inside Google + PhoneNo Signup Ist Time with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                        // /* EOC Create customer Account at tango card*/

                        let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, {
                            lean: true,
                            new: true
                        }, populate)
                        let groupCount = await Service.count(Model.PostGroupMembers, {
                            userId: data._id,
                            isDeleted: false
                        })
                        finalData[0].groupCount = groupCount
                        finalData[0].isPhoneNumberVerified = data.isPhoneNumberVerified
                        console.log("4", finalData[0])
                        return finalData[0]
                    }
                } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ID_MISSING)
            }
                break;

            case 3: {
                if (payloadData.email) {
                    let query = {
                        email: payloadData.email,
                        isDeleted: false
                    };
                    let check1 = await getRequired(Model.Users, query, {}, {});
                    if (check1.length)
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ALREADY_EXIST)
                    else
                        dataToSet.email = payloadData.email
                }

                if (payloadData.userName) {
                    let query = {
                        userName: payloadData.userName,
                        isDeleted: false
                    };
                    let check1 = await getRequired(Model.Users, query, {}, {})
                    if (check1.length)
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_EXIST)
                    else
                        dataToSet.userName = payloadData.userName
                }

                criteria.phoneNumber = payloadData.phoneNumber
                dataToSet.registrationDate = Date.now()
                if (payloadData.deviceId)
                    dataToSet.deviceId = payloadData.deviceId
                if (payloadData.fullName) {
                    dataToSet.fullName = payloadData.fullName
                    let arrayFullName = payloadData.fullName.split(/(\s+)/).filter(function (e) {
                        return e.trim().length > 0;
                    });
                    dataToSet.firstName = arrayFullName[0]
                    dataToSet.lastName = arrayFullName[1]
                }
                if (payloadData.password) {
                    dataToSet.password = UniversalFunctions.CryptData(payloadData.password)
                    dataToSet.isPasswordExist = true
                    dataToSet.isVerified = true
                    dataToSet.OTPcode = ""
                }

                dataToSet.imageUrl = {
                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                }

                if (payloadData.userType) {
                    dataToSet.userType = payloadData.userType
                }

                dataToSet.isProfileComplete = true


                if (payloadData.referralCode) {
                    let checkForReffrel = await getRequired(Model.Users,
                        {
                            referralCode: payloadData.referralCode
                        }, {}, { lean: true });

                    if (checkForReffrel.length) {
                        await updateData(Model.Users, {
                            _id: checkForReffrel[0]._id
                        }, { $inc: { pointEarned: 1250 } });

                        let pushData = {
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
                            id: checkForReffrel[0]._id,
                            msg: `${checkForReffrel[0].fullName} Congratulations you has won 1250 pts through Invite People`
                        };

                        pushNotification.sendPush(checkForReffrel[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        });

                        let saveData = {
                            userId: checkForReffrel[0]._id,
                            pointEarned: 1250,
                            source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.INVITE_PEOPLE,
                            date: moment().format('MM/DD/YYYY')
                        }

                        await Service.saveData(Model.PointEarnedHistory, saveData);
                    }
                    else {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NO_USER_FOUND_FOR_THIS_REFFRAL_CODE);
                    }
                }

                let result = await updateData(Model.Users, criteria, dataToSet, { new: true })
                await QRCode.toDataURL(result.id, async (err, url) => {
                    await updateData(Model.Users, {
                        _id: result._id,
                        isDeleted: false
                    }, { $set: { QRCode: url } }, { lean: true, new: true })
                    result.QRCode = url
                });
                // if(result){
                //     let url =`http://52.35.234.66:8000/user/emailVerification?id=${result._id}&timestamp=${+new Date()}`;
                //     let emailTemplatesToSend = await emailTemplates.emailVerification({userName: result.userName, email: payloadData.email}, url)
                //     sendEmail.sendEmail(payloadData.email, "Conversify Verify Email", emailTemplatesToSend)
                // }
                console.log("Inside Email Signup:" + JSON.stringify(result));
                let data = await tokenUpdate(result);

                // /* BOC Create customer at tango card*/
                // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data[0]._id);
                // console.log("Inside Email Signup with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                // /* EOC Create customer at tango card*/

                // /* BOC Create customer Account at tango card*/
                // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data[0]._id);
                // console.log("Inside Email Signup with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                /* EOC Create customer Account at tango card*/

                let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                finalData[0].groupCount = groupCount
                return finalData[0]
            }

            case 4: {
                if (payloadData.phoneNumber && payloadData.countryCode) {
                    let query = {
                        phoneNumber: payloadData.phoneNumber,
                        countryCode: payloadData.countryCode,
                        fullphoneNumber: payloadData.countryCode + payloadData.phoneNumber,
                        isDeleted: false
                    };
                    let check1 = await getRequired(Model.Users, query, {}, {});
                    if (check1.length)
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.PHONE_ALREADY_EXIST)
                    else {
                        dataToSet.phoneNumber = payloadData.phoneNumber
                        dataToSet.countryCode = payloadData.countryCode
                    }
                }

                if (payloadData.userName) {
                    let query = {
                        userName: payloadData.userName,
                        isDeleted: false
                    };
                    let check1 = await getRequired(Model.Users, query, {}, {})
                    if (check1.length)
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_EXIST)
                    else
                        dataToSet.userName = payloadData.userName
                }

                criteria.email = payloadData.email
                dataToSet.registrationDate = Date.now()
                dataToSet.fullphoneNumber = payloadData.countryCode + payloadData.phoneNumber
                if (payloadData.deviceId)
                    dataToSet.deviceId = payloadData.deviceId
                if (payloadData.fullName) {
                    dataToSet.fullName = payloadData.fullName
                    let arrayFullName = payloadData.fullName.split(/(\s+)/).filter(function (e) {
                        return e.trim().length > 0;
                    });
                    dataToSet.firstName = arrayFullName[0]
                    dataToSet.lastName = arrayFullName[1]
                }
                if (payloadData.password) {
                    dataToSet.password = UniversalFunctions.CryptData(payloadData.password)
                    dataToSet.isPasswordExist = true
                    dataToSet.isVerified = true
                    dataToSet.OTPcode = ""
                }

                dataToSet.imageUrl = {
                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                }

                if (payloadData.userType) {
                    dataToSet.userType = payloadData.userType
                }
                // console.log("criteria" + JSON.stringify(criteria));
                // console.log("dataToSet" + JSON.stringify(dataToSet));

                if (payloadData.referralCode) {
                    let checkForReffrel = await getRequired(Model.Users,
                        {
                            referralCode: payloadData.referralCode
                        }, {}, { lean: true });

                    if (checkForReffrel.length) {
                        await updateData(Model.Users, {
                            _id: checkForReffrel[0]._id
                        }, { $inc: { pointEarned: 1250 } });

                        let pushData = {
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
                            id: checkForReffrel[0]._id,
                            msg: `${checkForReffrel[0].fullName} Congratulations you has won 1250 pts through Invite People`
                        };

                        pushNotification.sendPush(checkForReffrel[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        });

                        let saveData = {
                            userId: checkForReffrel[0]._id,
                            pointEarned: 1250,
                            source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.INVITE_PEOPLE,
                            date: moment().format('MM/DD/YYYY')
                        }

                        await Service.saveData(Model.PointEarnedHistory, saveData);
                    }
                    else {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NO_USER_FOUND_FOR_THIS_REFFRAL_CODE);
                    }
                }

                // let otp = await codegenrate.generateUniqueCustomerId(4)
                // let smsData = {
                //     to: payloadData.countryCode + payloadData.phoneNumber,
                //     from: "+15045094820",
                //     body: "Your one time Password for Verification of Ribbit Rewards Plaform is : " + otp
                // }
                // //console.log(smsData);
                // await pushNotification.sendSMS(smsData);
                // dataToSet.OTPcode = otp
                let result = await updateData(Model.Users, criteria, dataToSet, { new: true })
                console.log(JSON.stringify(result));
                if (result) {
                    await QRCode.toDataURL(result._id, async (err, url) => {
                        await updateData(Model.Users, {
                            _id: result._id,
                            isDeleted: false
                        }, { $set: { QRCode: url } }, { lean: true, new: true })
                        result.QRCode = url
                    });

                    // let url = `http://52.35.234.66:8000/user/emailVerification?id=${result._id}&timestamp=${+new Date()}`;
                    // let emailTemplatesToSend = await emailTemplates.emailVerification({
                    //     userName: result.userName,
                    //     email: payloadData.email
                    // }, url)
                    // sendEmail.sendEmail(payloadData.email, "Check It Verify Email", emailTemplatesToSend)

                    let data = await tokenUpdate(result);
                    // /* BOC Create customer at tango card*/
                    // let tangoCreateCustomerResponse = await tangoCreateCustomer(payloadData, data[0]._id);
                    // console.log("Inside phoneNumber Signup with Tango: " + JSON.stringify(tangoCreateCustomerResponse));
                    // /* EOC Create customer at tango card*/

                    // /* BOC Create customer Account at tango card*/
                    // let tangoCreateAccountResponse = await tangoCreateAccount(payloadData, data[0]._id);
                    // console.log("Inside PhoneNumber Signup with Tango: " + JSON.stringify(tangoCreateAccountResponse));
                    // /* EOC Create customer Account at tango card*/

                    let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                        lean: true,
                        new: true
                    }, populate)
                    let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                    finalData[0].groupCount = groupCount
                    return finalData[0];
                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description user login through 1. userName, 2. email, 3. phone number, 4. facebookId/email, 5. googleId/email
 * @param {string} email
 * @param {string} countryCode
 * @param {string} phoneNumber
 * @param {string} fullName
 * @param {string} userName
 * @param {string} facebookId
 * @param {string} googleId
 * @param {string} password
 * @returns login details with accessToken
 */

let userLogIn = async (payloadData) => {
    try {
        let criteria = {}
        const basicDeviceInfo = {
            platform: payloadData.platform,
            deviceToken: payloadData.deviceToken,
            apnsDeviceToken: payloadData.apnsDeviceToken
        };
        let dataToSet = { ...basicDeviceInfo };
        let populate = [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
            path: "imageVisibility",
            select: "userName imageUrl fullName",
            model: "Users"
        }, {
            path: "nameVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "tagPermission",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "personalInfoVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }]

        if (payloadData.userCredentials) {
            criteria.$or = [
                { userName: payloadData.userCredentials },
                { email: payloadData.userCredentials },
                { fullphoneNumber: payloadData.userCredentials },
                { phoneNumber: payloadData.userCredentials },
            ]
        }

        if (payloadData.userCredentials && payloadData.password) {
            let check1 = await getRequired(Model.Users, criteria, {}, { lean: true })
            if (check1.length > 1) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ENTER_COUNTRY_CODE)
            }
            if (check1.length) {
                if (check1[0].password !== UniversalFunctions.CryptData(payloadData.password)) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_PASSWORD)
                }
                if (check1[0].isBlocked) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.BLOCKED)
                }

                dataToSet.lastLogin = Date.now()
                dataToSet.followerCount = check1[0].followers.length
                dataToSet.followingCount = check1[0].following.length
                let data = await updateData(Model.Users, criteria, { $set: dataToSet }, { new: true, lean: true })
                if (data) {
                    var forwardData = await tokenUpdate(data)
                    delete forwardData[0].password
                    let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, {
                        lean: true,
                        new: true
                    }, populate)
                    let groupCount = await Service.count(Model.PostGroupMembers, { userId: data._id, isDeleted: false })
                    await updateData(Model.Users, { _id: data._id }, { $set: { deviceToken: "" } }, { lean: true })
                    finalData[0].groupCount = groupCount
                    finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                    finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                    //console.log(finalData[0]);
                    return finalData[0]
                }
            } else {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_INFO)
            }
        } else if (payloadData.userCredentials) {
            let data = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
            if (data.length > 1) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ENTER_COUNTRY_CODE)
            }
            if (data.length) {
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                //console.log(data[0]);
                return {
                    userName: data[0].userName,
                    fullName: data[0].fullName,
                    email: data[0].email,
                    phoneNumber: data[0].phoneNumber,
                    isVerified: data[0].isVerified,
                    isProfileComplete: data[0].isProfileComplete,
                    isBlocked: data[0].isBlocked,
                    isDeleted: data[0].isDeleted,
                    isInterestSelected: data[0].isInterestSelected,
                    isPasswordExist: data[0].isPasswordExist,
                    fullphoneNumber: data[0].fullphoneNumber,
                    imageUrl: data[0].imageUrl,
                    groupCount: groupCount,
                    userType: data[0].userType
                }
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)
        } else if (payloadData.googleId && payloadData.email) {

            let data = await getRequiredPopulate(Model.Users, {
                email: payloadData.email,
                isDeleted: false
            }, {}, { lean: true }, populate)
            if (data.length) {
                await updateData(Model.Users, {
                    _id: data[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        googleId: payloadData.googleId,
                        followerCount: data[0].followers.length,
                        followingCount: data[0].following.length, ...basicDeviceInfo
                    }
                }, { lean: true })
                await tokenUpdate(data[0])
                let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                await updateData(Model.Users, { _id: data[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            }
            let googleData = await getRequiredPopulate(Model.Users, { googleId: payloadData.googleId }, {}, { lean: true }, populate)
            if (googleData.length) {
                await updateData(Model.Users, {
                    _id: googleData[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        followerCount: googleData[0].followers.length,
                        followingCount: googleData[0].following.length, ...basicDeviceInfo
                    }
                }, { lean: true })
                await tokenUpdate(googleData[0])
                let finalData = await Service.populateData(Model.Users, { _id: googleData[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, {
                    userId: googleData[0]._id,
                    isDeleted: false
                })
                await updateData(Model.Users, { _id: googleData[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)

        } else if (payloadData.facebookId && payloadData.email) {

            let data = await getRequiredPopulate(Model.Users, { email: payloadData.email }, {}, { lean: true }, populate)
            if (data.length) {
                await updateData(Model.Users, {
                    _id: data[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        facebookId: payloadData.facebookId,
                        followerCount: data[0].followers.length,
                        followingCount: data[0].following.length
                    }
                }, { lean: true })
                await tokenUpdate(data[0])
                let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                await updateData(Model.Users, { _id: data[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            }
            let facebookData = await getRequiredPopulate(Model.Users, { facebookId: payloadData.facebookId }, {}, { lean: true }, populate)
            if (facebookData.length) {
                await updateData(Model.Users, {
                    _id: facebookData[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        followerCount: facebookData[0].followers.length,
                        followingCount: facebookData[0].following.length
                    }
                }, { lean: true })
                await tokenUpdate(facebookData[0])
                let finalData = await Service.populateData(Model.Users, { _id: facebookData[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, {
                    userId: facebookData[0]._id,
                    isDeleted: false
                })
                await updateData(Model.Users, { _id: facebookData[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)
        } else if (payloadData.googleId) {

            let data = await getRequiredPopulate(Model.Users, { googleId: payloadData.googleId }, {}, { lean: true }, populate)
            if (data.length) {
                await updateData(Model.Users, {
                    _id: data[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        followerCount: data[0].followers.length,
                        followingCount: data[0].following.length
                    }
                }, { lean: true })
                await tokenUpdate(data[0])
                let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                await updateData(Model.Users, { _id: data[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)

        } else if (payloadData.facebookId) {

            let data = await getRequiredPopulate(Model.Users, { facebookId: payloadData.facebookId }, {}, { lean: true }, populate)
            if (data.length) {
                await updateData(Model.Users, {
                    _id: data[0]._id,
                    isDeleted: false
                }, {
                    $set: {
                        followerCount: data[0].followers.length,
                        followingCount: data[0].following.length
                    }
                }, { lean: true })
                await tokenUpdate(data[0])
                let finalData = await Service.populateData(Model.Users, { _id: data[0]._id }, {}, {
                    lean: true,
                    new: true
                }, populate)
                let groupCount = await Service.count(Model.PostGroupMembers, { userId: data[0]._id, isDeleted: false })
                await updateData(Model.Users, { _id: data[0]._id }, { $set: { ...basicDeviceInfo } }, { lean: true })
                finalData[0].groupCount = groupCount
                finalData[0].pointRedeemed = Math.round(finalData[0].pointRedeemed)
                finalData[0].pointEarned = Math.floor(finalData[0].pointEarned)
                return finalData[0]
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_REGISTERED)
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description user name check helps to check the entered username avaiilabilty
 * @param {string} userName
 * @returns true or not available
 */

let userNameCheck = async (payloadData) => {
    try {
        let criteria = {};

        let project = {
            userName: 1
        }

        if (payloadData.userName) {
            let userNameGiven = payloadData.userName
            var length = userNameGiven.split(" ").length - 1
            console.log(length)
            if (length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_CONTAINS_SPACE)
            }
            // criteria.$and = [{},{userName: {$regex: new RegExp(`${payloadData.userName}$`,'i', 'm')}}]
            criteria.userName = { $regex: new RegExp(`${payloadData.userName}$`, 'i', 'm') }
        }
        // criteria.userName = {$regex: /^payloadData.userName/i}
        // {userName : new RegExp(payloadData.search,'i')}

        let data = await getRequired(Model.Users, criteria, project, { lean: true })
        if (data.length) {
            return Promise.resolve(Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.USERNAME_NOT_AVAILABLE)
        } else {
            return Promise.resolve(Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.USERNAME_AVAILABLE)
        }
    } catch (e) {
        console.log(e)
    }
}

async function userNameCheckWithAuth(payloadData, userData) {
    let criteria = {};

    let project = {
        userName: 1
    }

    if (payloadData.userName) {
        criteria.userName = { $regex: new RegExp(`${payloadData.userName}$`, 'i', 'm') },
            criteria._id = { $ne: userData._id }
    }

    let data = await getRequired(Model.Users, criteria, project, { lean: true })
    if (data.length) {
        return Promise.resolve(Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.USERNAME_NOT_AVAILABLE)
    } else {
        return Promise.resolve(Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.USERNAME_AVAILABLE)
    }
}

/**
 * @description edit user profile details
 * @param {string} userName
 * @param {string} fullName
 * @param {string} bio
 * @param {string} website
 * @param {string} email
 * @param {string} gender
 * @param {number} dateOfBirth
 * @param {string} company
 * @param {string} designation
 */

let editProfile = async (payloadData, userData) => {
    try {
        let criteria = {}
        let populate = [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
            path: "imageVisibility",
            select: "userName imageUrl fullName",
            model: "Users"
        }, {
            path: "nameVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "tagPermission",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "personalInfoVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }]
        criteria._id = userData._id;
        criteria.isDeleted = false

        let dataToSet = {}
        dataToSet.$set = {};
        if (payloadData.userName && userData.userName != payloadData.userName) {
            let checkUserName = await getRequired(Model.Users, {
                userName: payloadData.userName,
                isDeleted: false
            }, {}, { lean: true })
            if (checkUserName.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.USERNAME_ALREADY_REGISTERED)
            } else {
                dataToSet.$set.userName = payloadData.userName
            }
        } else {
            dataToSet.$set.userName = payloadData.userName
        }
        if (payloadData.fullName) {
            dataToSet.$set.fullName = payloadData.fullName
            let arrayFullName = payloadData.fullName.split(/(\s+)/).filter(function (e) {
                return e.trim().length > 0;
            });
            dataToSet.$set.firstName = arrayFullName[0]
            dataToSet.$set.lastName = arrayFullName[1]
        }
        if (payloadData.bio === '""') {
            dataToSet.$set.bio = ""
        } else if (payloadData.bio && payloadData.bio != null) {
            dataToSet.$set.bio = payloadData.bio
        }

        if (payloadData.company || payloadData.company == "") {
            dataToSet.$set.company = payloadData.company
        }
        if (payloadData.website === '""')
            dataToSet.$set.website = ""
        else if (payloadData.website && payloadData.website != null)
            dataToSet.$set.website = payloadData.website

        if (payloadData.email && (payloadData.email != userData.email)) {
            let checkEmail = await getRequired(Model.Users, {
                email: payloadData.email,
                isDeleted: false
            }, {}, { lean: true })
            if (checkEmail.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.EMAIL_ALREADY_EXITS)
            } else {
                dataToSet.$set.email = payloadData.email
            }
        }
        if (payloadData.gender === "MALE")
            dataToSet.$set.gender = "MALE"
        if (payloadData.gender === "FEMALE")
            dataToSet.$set.gender = "FEMALE"
        if (payloadData.gender === "OTHERS")
            dataToSet.$set.gender = "OTHERS"
        if (payloadData.dateOfBirth) {
            dataToSet.$set.dateOfBirth = payloadData.dateOfBirth
            dataToSet.$set.age = calcAge(payloadData.dateOfBirth)
        }

        if (payloadData.isPrivate) {
            dataToSet.$set.isPrivate = true
        } else {
            dataToSet.$set.isPrivate = false
        }

        if (payloadData.designation === '')
            dataToSet.$set.designation = ""
        else if (payloadData.designation && payloadData.designation != null)
            dataToSet.$set.designation = payloadData.designation

        if (payloadData.imageOriginal && payloadData.imageThumbnail)
            dataToSet.$set.imageUrl = {
                original: payloadData.imageOriginal,
                thumbnail: payloadData.imageThumbnail
            }
        if (payloadData.company === '""')
            dataToSet.$set.company = ""
        else if (payloadData.company && payloadData.company != null)
            dataToSet.$set.company = payloadData.company


        let data = await updateData(Model.Users, criteria, dataToSet, { new: true, lean: true })
        let finalData = await Service.populateData(Model.Users, { _id: data._id }, {}, { lean: true, new: true }, populate)
        if (finalData)
            return finalData[0]
    } catch (e) {
        console.log(e)
    }

}
/**
 * @description getting different Pages Data  case 1: getting the interest category list,
 *                                            case 2: getting venue page data
 *                                            case 3: getting post group page data
 *                                            case 4: getting post page data
 *                                            case 5: list of groups
 * @param {string} authorization
 * @param {double} flag
 * @param {double} currentLong
 * @param {double} currentLat
 * @returns provides the data related to  case 1: interest category page
 *                                        case 2: venue page
 *                                        case 3: post group page
 *                                        case 4: post page
 *                                        case 5: list of groups
 */

let getData = async (payloadData, userData) => {
    try {
        switch (payloadData.flag) {
            case 1: {
                if (userData._id) {
                    let criteria = {
                        isDeleted: false,
                        isBlocked: false
                    }

                    let data = await getRequired(Model.Categories, criteria, { isDeleted: 0, isBlocked: 0 }, { lean: true })
                    if (data)
                        return data
                }
                break;
            }
            /* venue Page data */
            /* venue Page data */
            case 2: {
                let venueNearYou, venueAccordanceToInterest, idArray = [], notArrayPending = [], notArrayRejected = []
                // let populate = [{path: "membersList", select: "fullName imageUrl userName", model: "Users"} ]
                let userInterests = await getRequired(Model.Users, {
                    _id: userData._id,
                    isDeleted: false
                }, { interestTags: 1 }, { lean: true })
                let requestVenue = await getRequired(Model.Notifications, {
                    byId: userData._id,
                    groupType: "VENUE",
                    type: "REQUEST_VENUE"
                }, {}, { lean: true })

                if (requestVenue.length) {
                    for (let req of requestVenue) {
                        if (req.isRejected && req.actionPerformed) {
                            notArrayRejected.push(req.venueId)
                            continue
                        }
                        if (!req.actionPerformed && !req.isRejected) {
                            notArrayPending.push(req.venueId)
                        }
                    }
                }
                let yourVenueData = await aggregateData(Model.VenueGroups, [
                    {
                        $match: {
                            memberCount: { $ne: 0 },
                            isArchive: { $nin: [userData._id] },
                            adminId: { $nin: userData.blockedBy }
                        }
                    },
                    {
                        $lookup: {
                            "from": "venuegroupmembers",
                            "localField": "_id",
                            "foreignField": "groupId",
                            "as": "groupMembers"
                        }
                    }
                    // ,{
                    //     $addFields: {
                    //         memberCount: {$size: "$groupMembers"}
                    //     }
                    // }
                    , {
                        $unwind: "$groupMembers"
                    }, {
                        $match: {
                            "groupMembers.userId": userData._id,
                            "groupMembers.isDeleted": false,
                            isDeleted: false
                        }
                    }, {
                        $project: {
                            _id: 1,
                            groupId: "$_id",
                            conversationId: "$conversationId",
                            venueTitle: "$venueTitle",
                            adminId: "$adminId",
                            venueLocation: "$venueLocation",
                            venueTime: "$venueTime",
                            venueLocationName: "$venueLocationName",
                            venueLocationAddress: "$venueLocationAddress",
                            venueTags: "$venueTags",
                            // membersList:"$memberIds",
                            isPrivate: "$isPrivate",
                            createdOn: "$createdOn",
                            infoUpdated: "$infoUpdated",
                            imageUrl: "$imageUrl",
                            memberCount: "$memberCount",
                            createdBy: "$createdBy",
                            participationRole: {
                                $cond: {
                                    if: { $eq: ["$adminId", mongoose.Types.ObjectId(userData._id)] },
                                    then: "ADMIN",
                                    else: "MEMBER"
                                }
                            },
                            isMember: {
                                $cond: {
                                    if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                    then: true,
                                    else: false
                                }
                            }
                        }
                    }, {
                        $sort: {
                            infoUpdated: -1
                        }
                    }
                ]);

                for (let key of yourVenueData) {

                    idArray.push(mongoose.Types.ObjectId(key._id))
                }

                if (payloadData.currentLat && payloadData.currentLong) {

                    venueNearYou = await aggregateData(Model.VenueGroups, [{
                        $geoNear: {
                            near: { type: "Point", coordinates: [payloadData.currentLong, payloadData.currentLat] },
                            distanceField: "calculated",
                            maxDistance: 5000,
                            spherical: true,
                            distanceMultiplier: 0.000621371,
                            query: {
                                _id: { '$nin': idArray },
                                memberCount: { $ne: 0 },
                                adminId: { $nin: userData.blockedBy }
                            }
                        }
                    },
                    {
                        $lookup: {
                            "from": "venuegroupmembers",
                            "localField": "_id",
                            "foreignField": "groupId",
                            "as": "groupMembers"
                        }
                    }, {
                        $match: {
                            $or: [{ 'groupMembers.userId': { '$nin': [mongoose.Types.ObjectId(userData._id)] } },
                            {
                                'groupMembers.userId': { '$in': [mongoose.Types.ObjectId(userData._id)] },
                                "groupMembers.isDeleted": true
                            }],
                            isDeleted: false
                        }
                    },
                    {
                        $unwind: "$groupMembers"
                    },
                    {
                        $group: {
                            _id: "$_id",
                            distance: { $last: "$calculated" },
                            conversationId: { $last: "$conversationId" },
                            groupId: { $last: "$_id" },
                            adminId: { $last: "$adminId" },
                            createdBy: { $last: "$createdBy" },
                            venueTitle: { $last: "$venueTitle" },
                            venueTime: { $last: "$venueTime" },
                            venueTags: { $last: "$venueTags" },
                            //  membersList: {$last: "$memberIds"},
                            isPrivate: { $last: "$isPrivate" },
                            venueImageUrl: { $last: "$imageUrl" },
                            venueLocation: { $last: "$venueLocation" },
                            venueLocationName: { $last: "$venueLocationName" },
                            venueLocationAddress: { $last: "$venueLocationAddress" },
                            memberCount: { $last: "$memberCount" },
                            memberIds: { $last: "$memberIds" }
                        }
                    }, {
                        $project: {
                            distance: "$distance",
                            conversationId: "$conversationId",
                            adminId: "$adminId",
                            groupId: "$groupId",
                            venueTitle: "$venueTitle",
                            createdBy: "$createdBy",
                            venueTime: "$venueTime",
                            venueTags: "$venueTags",
                            //  membersList: "$membersList",
                            venueLocationName: "$venueLocationName",
                            venueLocationAddress: "$venueLocationAddress",
                            isPrivate: "$isPrivate",
                            imageUrl: "$venueImageUrl",
                            memberCount: "$memberCount",
                            venueLocation: "$venueLocation",
                            requestStatus: {
                                $cond: {
                                    if: { $in: ["$_id", notArrayPending] },
                                    then: "PENDING",
                                    else: {
                                        $cond: {
                                            if: { $in: ["$_id", notArrayRejected] },
                                            then: "REJECTED",
                                            else: "NONE"
                                        }
                                    }
                                }
                            },
                            isMember: {
                                $cond: {
                                    if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                    then: true,
                                    else: false
                                }
                            },
                        }
                    }, {
                        $sort: {
                            distance: 1
                        }
                    }
                        // ,{
                        //     $skip: (payloadData.pageNo - 1) * payloadData.limit
                        // },{
                        //     $limit: payloadData.limit
                        // }
                    ])
                    // console.log(venueAccordanceToInterest, venueNearYou)

                    if (!venueNearYou.length) {
                        venueAccordanceToInterest = await aggregateData(Model.VenueGroups, [{
                            $geoNear: {
                                near: { type: "Point", coordinates: [payloadData.currentLong, payloadData.currentLat] },
                                distanceField: "calculated",
                                maxDistance: 50000000,
                                spherical: true,
                                distanceMultiplier: 0.000621371,
                                query: {
                                    _id: { '$nin': idArray },
                                    memberCount: { $ne: 0 },
                                    "categoryId": { $in: userInterests[0].interestTags },
                                    adminId: { $nin: userData.blockedBy }
                                }
                            }
                        },
                        {
                            $lookup: {
                                "from": "venuegroupmembers",
                                "localField": "_id",
                                "foreignField": "groupId",
                                "as": "groupMembers"
                            }
                        }, {
                            $match: {
                                $or: [{ 'groupMembers.userId': { '$nin': [mongoose.Types.ObjectId(userData._id)] } },
                                {
                                    'groupMembers.userId': { '$in': [mongoose.Types.ObjectId(userData._id)] },
                                    "groupMembers.isDeleted": true
                                }],
                                isDeleted: false
                            }
                        },
                        {
                            $unwind: "$groupMembers"
                        },
                        {
                            $group: {
                                _id: "$_id",
                                distance: { $last: "$calculated" },
                                conversationId: { $last: "$conversationId" },
                                groupId: { $last: "$_id" },
                                adminId: { $last: "$adminId" },
                                createdBy: { $last: "$createdBy" },
                                venueTitle: { $last: "$venueTitle" },
                                venueTime: { $last: "$venueTime" },
                                venueTags: { $last: "$venueTags" },
                                //  membersList: {$last: "$memberIds"},
                                isPrivate: { $last: "$isPrivate" },
                                venueImageUrl: { $last: "$imageUrl" },
                                venueLocation: { $last: "$venueLocation" },
                                venueLocationName: { $last: "$venueLocationName" },
                                venueLocationAddress: { $last: "$venueLocationAddress" },
                                memberCount: { $last: "$memberCount" },
                                memberIds: { $last: "$memberIds" }
                            }
                        }, {
                            $project: {
                                distance: "$distance",
                                conversationId: "$conversationId",
                                adminId: "$adminId",
                                groupId: "$groupId",
                                venueTitle: "$venueTitle",
                                createdBy: "$createdBy",
                                venueTime: "$venueTime",
                                venueTags: "$venueTags",
                                //  membersList: "$membersList",
                                venueLocationName: "$venueLocationName",
                                venueLocationAddress: "$venueLocationAddress",
                                isPrivate: "$isPrivate",
                                imageUrl: "$venueImageUrl",
                                memberCount: "$memberCount",
                                venueLocation: "$venueLocation",
                                requestStatus: {
                                    $cond: {
                                        if: { $in: ["$_id", notArrayPending] },
                                        then: "PENDING",
                                        else: {
                                            $cond: {
                                                if: { $in: ["$_id", notArrayRejected] },
                                                then: "REJECTED",
                                                else: "NONE"
                                            }
                                        }
                                    }
                                },
                                isMember: {
                                    $cond: {
                                        if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                        then: true,
                                        else: false
                                    }
                                },
                            }
                        }, {
                            $sort: {
                                distance: 1
                            }
                        }
                            // ,{
                            //     $skip: (payloadData.pageNo - 1) * payloadData.limit
                            // },{
                            //     $limit: payloadData.limit
                            // }
                        ])
                    }
                    var merged = _.merge(_.keyBy(venueAccordanceToInterest, '_id'), _.keyBy(venueNearYou, '_id'));
                    var values = _.values(merged);
                    // console.log(yourVenueData, values)
                    return { yourVenueData: yourVenueData || [], venueNearYou: values || [] }

                } else {
                    venueAccordanceToInterest = await aggregateData(Model.VenueGroups, [
                        {
                            $match: {
                                $and: [
                                    { "_id": { $nin: idArray } },
                                    { memberCount: { $ne: 0 } },
                                    { "memberIds": { $nin: [mongoose.Types.ObjectId(userData._id)] } },
                                    { "categoryId": { $in: userInterests[0].interestTags } },
                                    { adminId: { $nin: userData.blockedBy } }
                                ]
                            }
                        },
                        {
                            $project: {
                                groupId: "$_id",
                                venueTitle: "$venueTitle",
                                venueTime: "$venueTime",
                                adminId: "$adminId",
                                conversationId: "$conversationId",
                                venueLocationName: "$venueLocationName",
                                venueLocationAddress: "$venueLocationAddress",
                                isPrivate: "$isPrivate",
                                imageUrl: "$imageUrl",
                                venueTags: "$venueTags",
                                createdBy: "$createdBy",
                                memberCount: "$memberCount",
                                // membersList: "$memberIds",
                                venueLocation: "$venueLocation",
                                requestStatus: {
                                    $cond: {
                                        if: { $in: ["$_id", notArrayPending] },
                                        then: "PENDING",
                                        else: {
                                            $cond: {
                                                if: { $in: ["$_id", notArrayRejected] },
                                                then: "REJECTED",
                                                else: "NONE"
                                            }
                                        }
                                    }
                                },
                                isMember: {
                                    $cond: {
                                        if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                        then: true,
                                        else: false
                                    }
                                },
                            }
                        }])
                    return { yourVenueData: yourVenueData || [], venueNearYou: venueAccordanceToInterest || [] }
                }

            }
            /* venue Page data */
            /* venue Page data */

            /* postgroup Page data */
            /* postgroup Page data */
            case 3: {
                let notArrayPending = [], notArrayRejected = []
                // let populate = [{path: "membersList", select: "fullName imageUrl userName", model: "Users"}]
                let pipeline = [
                    {
                        $lookup: {
                            "from": "posts",
                            "localField": "groupId",
                            "foreignField": "groupId",
                            "as": "postInfo"
                        }
                    }, {
                        $lookup: {
                            "from": "postgroups",
                            "localField": "groupId",
                            "foreignField": "_id",
                            "as": "groupInfo"
                        }
                    }, {
                        $unwind: {
                            "path": "$groupInfo",
                            "preserveNullAndEmptyArrays": true,
                        }
                    }, {
                        $match: {
                            userId: mongoose.Types.ObjectId(userData._id),
                            "groupInfo.isArchive": { $nin: [mongoose.Types.ObjectId(userData._id)] },
                            isDeleted: false,
                            "groupInfo.adminId": { $nin: userData.blockedBy }
                        }
                    }, {
                        $unwind: {
                            "path": "$postInfo",
                            "preserveNullAndEmptyArrays": true,
                        }
                    }, {
                        $project: {
                            _id: 1,
                            groupId: 1,
                            groupInfo: 1,
                            isMember: { $ifNull: ["$isMember", true] },
                            participationRole: {
                                $cond: {
                                    if: { $eq: ["$groupInfo.adminId", mongoose.Types.ObjectId(userData._id)] },
                                    then: "ADMIN",
                                    else: "MEMBER"
                                }
                            },
                            createdOn: {
                                $cond: {
                                    if: { $gte: ["$groupInfo.createdOn", "$postInfo.createdOn"] },
                                    then: "$groupInfo.createdOn",
                                    else: "$postInfo.createdOn"
                                }
                            },
                            postInfoReadBy: { $ifNull: ["$postInfo.readBy", []] },
                            postInfo: { $ifNull: ["$postInfo", null] }
                        }
                    }, {
                        $group: {
                            _id: "$groupId",
                            imageUrl: { $last: "$groupInfo.imageUrl" },
                            groupName: { $last: "$groupInfo.groupName" },
                            adminId: { $last: "$groupInfo.adminId" },
                            createdBy: { $last: "$groupInfo.createdBy" },
                            conversationId: { $last: "$groupInfo.conversationId" },
                            createdOn: { $last: "$createdOn" },
                            isMember: { $last: "$isMember" },
                            isPrivate: { $last: "$groupInfo.isPrivate" },
                            memberCounts: { $last: "$groupInfo.memberCounts" },
                            description: { $last: "$groupInfo.description" },
                            // membersList: {$last: "$groupInfo.isMember"},
                            participationRole: { $last: "$participationRole" },
                            "unReadCounts": {
                                $sum: {
                                    $cond: [
                                        {
                                            $or: [
                                                { $eq: [true, "$groupInfo.noPost"] },
                                                {
                                                    $and: [
                                                        { $eq: [false, "$groupInfo.noPost"] },
                                                        { $in: [mongoose.Types.ObjectId(userData._id), "$postInfo.readBy"] }
                                                    ]
                                                }
                                            ]

                                        }, 0, 1]
                                }
                            }
                        }
                    }, {
                        $sort: { "createdOn": -1 }
                    }
                    // ,{
                    //     $skip: (payloadData.pageNo - 1) * payloadData.limit
                    // },{
                    //     $limit: payloadData.limit
                    // }
                ]
                let requestGroup = await getRequired(Model.Notifications, {
                    byId: userData._id,
                    groupType: "GROUP",
                    type: "REQUEST_GROUP"
                }, {}, { lean: true })

                if (requestGroup.length) {
                    for (let req of requestGroup) {
                        if (req.isRejected && req.actionPerformed) {
                            notArrayRejected.push(req.groupId)
                            continue
                        }
                        if (!req.actionPerformed && !req.isRejected) {
                            notArrayPending.push(req.groupId)
                        }
                    }
                }
                let data = await aggregateData(Model.PostGroupMembers, pipeline)
                // let userInterests = await getRequired(Model.Users, {_id: userData._id}, {interestTags:1}, {lean: true})
                let suggestedGroups = await aggregateData(Model.PostGroups, [{
                    $match: {
                        $and: [{ "isMember": { $nin: [userData._id] } }, { "categoryId": { $in: userData.interestTags } }],
                        isDeleted: false,
                        adminId: { $nin: userData.blockedBy }
                    }
                }, {
                    $project: {
                        imageUrl: 1,
                        groupName: 1,
                        isPrivate: 1,
                        adminId: 1,
                        createdBy: 1,
                        memberCounts: 1,
                        conversationId: 1,
                        // membersList: "$isMember",
                        requestStatus: {
                            $cond: {
                                if: { $in: ["$_id", notArrayPending] },
                                then: "PENDING",
                                else: {
                                    $cond: {
                                        if: {
                                            $in: ["$_id", notArrayRejected]
                                        },
                                        then: "REJECTED",
                                        else: "NONE"
                                    }
                                }
                            }
                        },
                        isMember: {
                            $cond: {
                                if: { $in: [mongoose.Types.ObjectId(userData._id), "$isMember"] },
                                then: true,
                                else: false
                            }
                        }
                    }
                }])
                return { suggestedGroups, yourGroups: data }
            }
            /* postgroup Page data */
            /* postgroup Page data */

            /* post Page data */
            /* post Page data */
            case 4: {
                if (!payloadData.pageNo) {
                    payloadData.pageNo = 1
                }
                if (!payloadData.limit) {
                    payloadData.limit = 10
                }

                let userGroups = await getRequired(Model.PostGroupMembers, {
                    userId: userData._id,
                    isDeleted: false
                }, { groupId: 1 }, { lean: true })
                let publicGroups = await getRequired(Model.PostGroups, {
                    isPrivate: false,
                    isDeleted: false
                }, { groupId: 1 }, { lean: true })
                let selectedPeoplePost = await getRequired(Model.Posts, {
                    selectedPeople: { $in: [mongoose.Types.ObjectId(userData._id)] },
                    isDeleted: false,
                    isBlocked: false
                }, { _id: 1 }, { lean: true })
                let userFollowedTags = await getRequiredPopulate(Model.Users, {
                    _id: userData._id,
                    isDeleted: false
                }, { tagsFollowed: 1 }, { lean: true }, [{ path: "tagsFollowed", select: "tagName", model: "Tags" }])
                let userGroupArray = [], finalTagArray = [], publicGroupArray = [], selectedPeoplePostArray = []
                //  console.log("+++++++++++++++++", selectedPeoplePost)

                if (userFollowedTags.length && userFollowedTags[0].tagsFollowed) {
                    for (let a of userFollowedTags[0].tagsFollowed) {
                        let b = a.tagName.slice(1)
                        finalTagArray.push(b)
                    }
                }

                publicGroups.map(obj => publicGroupArray.push(obj._id))
                userGroups.map(obj => userGroupArray.push(obj.groupId))
                selectedPeoplePost.map(obj => selectedPeoplePostArray.push(obj._id))

                //  console.log("+++++++++++++++++", userGroupArray, finalTagArray, userData.following, publicGroupArray, selectedPeoplePostArray)
                let matchJson = {

                    $or: [
                        {
                            $and: [{
                                _id: { $in: selectedPeoplePostArray }
                            }, {
                                postingIn: "SELECTED_PEOPLE"
                            }]
                        },
                        {
                            $and: [{
                                postBy: {
                                    $in: userData.following
                                }
                            }, {
                                postingIn: "PUBLICILY"
                            }]
                        },
                        {
                            postBy: mongoose.Types.ObjectId(userData._id)
                        },
                        {
                            hashTags: {
                                $in: finalTagArray
                            }
                        }
                    ],
                    postBy: {
                        $nin: userData.blockedBy
                    },
                    isDeleted: false

                }

                //   console.log(JSON.stringify(matchJson))
                let pipeline = [{
                    // $match: {
                    //     $or: [{groupId: {$in: userGroupArray}}, {$and: [{postBy: {$in: userData.following}}, {groupId: {$in: userGroupArray}}]}, {$and:[{postBy: mongoose.Types.ObjectId(userData._id)}, {groupId: {$in: userGroupArray}}]}, {$and: [{hashTags: {$in: finalTagArray}}, {groupId: {$in: publicGroupArray}}]}, {$and: [{hashTags: {$in: finalTagArray}}, {groupId: {$in: userGroupArray}}]}, {$and: [{hashTags: {$in: finalTagArray}}, {groupId: null}]}, {$and:[{postBy: {$in: userData.following}}, {groupId: null}]}, {$and:[{postBy: mongoose.Types.ObjectId(userData._id)}, {groupId: null}]}],
                    //     postBy: {$nin: userData.blockedBy},
                    //     isDeleted: false
                    // }
                    $match: matchJson
                },
                {
                    $project: {
                        _id: "$_id",
                        postBy: "$postBy",
                        imageUrl: "$imageUrl",
                        postText: "$postText",
                        media: "$media",
                        groupId: "$groupId",
                        type: "$type",
                        hashTags: "$hashTags",
                        createdOn: "$createdOn",
                        postCategoryId: "$postCategoryId",
                        likeCount: { $size: "$likes" },
                        // commentCount: "$commentCount",
                        postType: "$postType",
                        postingIn: "$postingIn",
                        selectInterests: "$selectInterests",
                        location: "$location",
                        locationName: "$locationName",
                        locationAddress: "$locationAddress",
                        meetingTime: "$meetingTime",
                        expirationTime: "$expirationTime",
                        comments: "$comments",
                        liked: {
                            $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                        }
                    }
                }, {
                    $sort: { "createdOn": -1 }
                }, {
                    $skip: (payloadData.pageNo - 1) * payloadData.limit
                }, {
                    $limit: payloadData.limit
                }]

                let populate = [{
                    path: "postBy",
                    select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers interestTags locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone tagPermission personalInfoVisibility",
                    model: "Users",
                    populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                        path: "imageVisibility",
                        select: "userName imageUrl fullName",
                        model: "Users"
                    }, {
                        path: "nameVisibility",
                        select: "fullName imageUrl userName",
                        model: "Users"
                    }, {
                        path: "tagPermission",
                        select: "fullName imageUrl userName",
                        model: "Users"
                    }, {
                        path: "personalInfoVisibility",
                        select: "fullName imageUrl userName",
                        model: "Users"
                    }]
                }, {
                    path: "postCategoryId",
                    select: "categoryName",
                    model: "Categories"
                }, {
                    path: "groupId",
                    select: "groupName",
                    model: "PostGroups"
                }, {
                    path: "selectInterests",
                    model: "Categories"
                }]
                let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
                // let groupCount = await Service.count(Model.PostGroupMembers, {isDeleted: false, userId: userData._id})
                if (data.length) {


                    for (let a of data) {
                        // sort media
                        a.media.sort((x, y) => {
                            return y.likeCount - x.likeCount
                        });

                        // add liked key in media
                        // loop all media
                        if (a.media.length > 0) {
                            for (let m = 0; m < a.media.length; m++) {
                                let changeType = a.media[m].likes.map(v => {
                                    return v.toString()
                                });
                                let likeIndex = changeType.indexOf(userData._id.toString());

                                if (likeIndex != -1)
                                    a.media[m].liked = true
                                else
                                    a.media[m].liked = false

                                // add new key
                                if (a.media[0].likeCount != 0)
                                    a.media[0].isMostLiked = true;
                            }
                        }

                        a.postBy = a.postBy.toObject()
                        let commentCount = await Service.count(Model.Comments, {
                            postId: a._id,
                            isDeleted: false,
                            commentBy: { "$nin": userData.blockedBy }
                        })
                        let repliesCount = await Service.count(Model.Replies, {
                            postId: a._id,
                            isDeleted: false,
                            replyBy: { "$nin": userData.blockedBy }
                        })
                        a.commentCount = commentCount + repliesCount
                        if (JSON.stringify(a.postBy._id) == JSON.stringify(userData._id)) {
                            continue
                        }
                        if (!a.postBy.imageVisibilityForEveryone) {
                            if (a.postBy.imageVisibilityForFollowers) {
                                a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                            } else if (a.postBy.imageVisibility && a.postBy.imageVisibility.length) {
                                a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                            }
                        }

                        if (!a.postBy.nameVisibilityForEveryone) {
                            if (a.postBy.nameVisibilityForFollowers) {
                                a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                            } else if (a.postBy.nameVisibility && a.postBy.nameVisibility.length) {
                                a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                            }
                        }

                        // if(!a.postBy.imageVisibilityForFollowers && !a.postBy.nameVisibilityForFollowers && !a.postBy.imageVisibility.length && !a.postBy.nameVisibility.length){
                        //     continue
                        // }
                        // a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                        // a.postBy = await nameVisibilityManipulation(a.postBy, userData)


                    }

                    return data
                } else {
                    return []
                }
            }
            /* post Page data */
            /* post Page data */

            /* user post group listing */
            case 5: {

                let pipeline = [{
                    $match: {
                        userId: userData._id,
                        isDeleted: false
                    }
                }, {
                    $project: {
                        _id: "$groupId"
                    }
                }]
                let populate = [
                    {
                        path: "_id",
                        select: "groupName imageUrl",
                        model: "PostGroups"
                    }
                ]
                let data = await aggregateWithPopulate(Model.PostGroupMembers, pipeline, populate)
                if (data.length) {
                    let finalRes = []
                    for (let group of data) {
                        finalRes.push(group._id)
                    }
                    return finalRes || []
                }
            }
            case 6: {

                let criteria = {
                    isDeleted: false,
                    isActive: true
                };
                let data = await Service.getData(Model.DailyChallenge, criteria, {}, { lean: true });

                if (data.length > 0) {
                    return data
                }

                return [];
            }
        }
    } catch (e) {
        console.log(e)
    }

}

/**
 * @description getting different filter list for front end
 * @param {string} authorization
 * @param {double} flag
 * @returns provides the attributes for filter
 */

let listOfFilters = async (payloadData, userData) => {
    try {
        switch (payloadData.flag) {
            case 1: {
                let filterData = { Date: "Date", categoryId: "categoryId", isPrivate: "isPrivate", location: "location" }
                return filterData
            }
        }
    } catch (e) {
        console.log(e)
    }

}

/**
 * @description updating the user details while selecting the interest categories
 * @param {string} authorization
 * @param {Array} categoryArray
 * @returns returns the user deatils when success
 */

let updateUserCategories = async (payloadData, userData) => {
    try {
        let criteria = {
            isDeleted: false,
            isVerified: true
        }
        let dataToSet = {}
        criteria._id = userData._id
        if (payloadData.categoryArray) {
            dataToSet.interestTags = payloadData.categoryArray
            dataToSet.isInterestSelected = true
            dataToSet.isProfileComplete = true
        }
        let populate = [{ path: "interestTags", select: "categoryName imageUrl", model: "Categories" }, {
            path: "imageVisibility",
            select: "userName imageUrl fullName",
            model: "Users"
        }, {
            path: "nameVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "tagPermission",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "personalInfoVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }]
        await updateData(Model.Users, criteria, dataToSet, { new: true, lean: true })
        console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
        let data = await Service.populateData(Model.Users, criteria, {}, { lean: true }, populate)
        console.log(data)
        if (data)
            return data[0]
    } catch (e) {
        console.log(e)
    }

}

/*=================login and Signup Related Apis==================*
/*=================login and Signup Related Apis=================*/


/*=================Venue Group Related Apis===================*/
/*=============Venue Group Related Apis=====================*/

/**
 * @description: function for leaving a prior venue group
 * @param {string} authorization
 * @param {string} groupId
 * @param {string} venueId
 * @returns: success
 */

let exitGroup = async (payloadData, userData) => {
    try {
        if (payloadData.groupId) {
            let data = await updateData(Model.PostGroupMembers, {
                isDeleted: false,
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, { isDeleted: true }, { new: true, lean: true })
            if (data) {
                await updateData(Model.Notifications, {
                    groupId: mongoose.Types.ObjectId(payloadData.groupId),
                    byId: userData._id,
                    type: "REQUEST_GROUP"
                }, { $set: { isDeleted: true, createdOn: +new Date } }, { lean: true, new: true })
                await updateData(Model.Notifications, {
                    groupId: mongoose.Types.ObjectId(payloadData.groupId),
                    toId: userData._id
                }, { $set: { isDeleted: true } }, { lean: true, new: true })

                await updateData(Model.PostGroups, { _id: mongoose.Types.ObjectId(payloadData.groupId) }, {
                    $inc: { memberCounts: -1 },
                    $pull: { isMember: mongoose.Types.ObjectId(userData._id) }
                })

                if (data.isAdmin) {

                    await updateData(Model.PostGroupMembers, {
                        isDeleted: true,
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        userId: mongoose.Types.ObjectId(userData._id)
                    }, { $set: { isAdmin: false } }, { new: true, lean: true })

                    await updateData(Model.Notifications, {
                        isDeleted: false,
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        toId: mongoose.Types.ObjectId(userData._id),
                        groupType: Config.APP_CONSTANTS.DATABASE.GROUP_TYPE.GROUP
                    }, { $set: { isDeleted: true, actionPerformed: true } }, { new: true, lean: true })

                    let newAdmin = await updateData(Model.PostGroupMembers, {
                        isDeleted: false,
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        joinedAt: { $gte: data.joinedAt }
                    }, { $set: { isAdmin: true, joinedAt: +new Date } }, { lean: true, new: true })

                    if (newAdmin) {
                        await updateData(Model.PostGroups, {
                            isDeleted: false,
                            _id: mongoose.Types.ObjectId(payloadData.groupId)
                        }, { $set: { adminId: mongoose.Types.ObjectId(newAdmin.userId) } }, { lean: true, new: true })
                    } else {
                        await updateData(Model.PostGroups, {
                            isDeleted: false,
                            _id: mongoose.Types.ObjectId(payloadData.groupId)
                        }, { $set: { isDeleted: true } }, { lean: true, new: true })

                        await Service.update(Model.Posts, { groupId: mongoose.Types.ObjectId(payloadData.groupId) }, { $set: { isDeleted: true } }, {
                            multi: true,
                            lean: true
                        })
                    }

                }
                return data
                /* notification to userId */
                /* notification to admin */
            }
        } else {
            let data = await updateData(Model.VenueGroupMembers, {
                isDeleted: false,
                groupId: mongoose.Types.ObjectId(payloadData.venueId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, { isDeleted: true }, { new: true, lean: true })

            if (data) {

                await updateData(Model.Notifications, {
                    venueId: mongoose.Types.ObjectId(payloadData.venueId),
                    byId: userData._id,
                    type: "REQUEST_VENUE"
                }, { $set: { isDeleted: true, createdOn: +new Date } }, { lean: true, new: true })

                await updateData(Model.VenueGroups, { _id: mongoose.Types.ObjectId(payloadData.venueId) }, {
                    $inc: { memberCount: -1 },
                    $pull: { memberIds: mongoose.Types.ObjectId(userData._id) }
                })

                if (data.isAdmin) {

                    await updateData(Model.VenueGroupMembers, {
                        isDeleted: true,
                        groupId: mongoose.Types.ObjectId(payloadData.venueId),
                        userId: mongoose.Types.ObjectId(userData._id)
                    }, { $set: { isAdmin: false } }, { new: true, lean: true })

                    await updateData(Model.Notifications, {
                        isDeleted: false,
                        venueId: mongoose.Types.ObjectId(payloadData.venueId),
                        toId: mongoose.Types.ObjectId(userData._id),
                        groupType: Config.APP_CONSTANTS.DATABASE.GROUP_TYPE.VENUE
                    }, { $set: { isDeleted: true, actionPerformed: true } }, { new: true, lean: true })

                    let newAdmin = await updateData(Model.VenueGroupMembers, {
                        isDeleted: false,
                        groupId: mongoose.Types.ObjectId(payloadData.venueId),
                        joinedAt: { $gte: data.joinedAt }
                    }, { $set: { isAdmin: true, joinedAt: +new Date } }, { lean: true, new: true })

                    if (newAdmin) {
                        await updateData(Model.VenueGroups, {
                            isDeleted: false,
                            _id: mongoose.Types.ObjectId(payloadData.venueId)
                        }, { $set: { adminId: mongoose.Types.ObjectId(newAdmin.userId) } }, { lean: true, new: true })
                    } else {
                        await updateData(Model.VenueGroups, {
                            isDeleted: false,
                            _id: mongoose.Types.ObjectId(payloadData.venueId)
                        }, { $set: { isDeleted: true } }, { lean: true, new: true })
                    }

                }
                return data
                /* notification to userId */
                /* notification to admin */
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let deleteGroup = async (payloadData, userData) => {
    let criteria = {}
    if (payloadData.groupId)
        criteria._id = payloadData.groupId
    criteria.adminId = userData._id
    criteria.memberCount = 1
    criteria.isDeleted = false


    if (payloadData.groupType === "VENUE") {
        let deleteData = await updateData(Model.VenueGroups, criteria, { $set: { isDeleted: true } }, {
            new: true,
            lean: true
        })
        if (deleteData) {
            await updateData(Model.VenueGroupMembers, {
                groupId: payloadData.groupId,
                userId: userData._id,
                isAdmin: true
            }, { $set: { isDeleted: true } }, { new: true, lean: true })
            return deleteData
        }
    } else {
        let deleteData = await updateData(Model.PostGroups, criteria, { $set: { isDeleted: true } }, {
            new: true,
            lean: true
        })
        if (deleteData) {
            await updateData(Model.PostGroupMembers, {
                groupId: payloadData.groupId,
                userId: userData._id,
                isAdmin: true
            }, { $set: { isDeleted: true } }, { new: true, lean: true })
            return deleteData
        }
    }

}

let archiveGroup = async (payloadData, userData) => {
    try {
        let criteria = {}
        if (payloadData.groupId)
            criteria._id = payloadData.groupId
        criteria.isDeleted = false


        if (payloadData.groupType === "VENUE") {
            let archiveData = await updateData(Model.VenueGroups, criteria, { $addToSet: { isArchive: userData._id } }, {
                new: true,
                lean: true
            })
            if (archiveData) {
                return archiveData
            }
        } else {
            let archiveData = await updateData(Model.PostGroups, criteria, { $addToSet: { isArchive: userData._id } }, {
                new: true,
                lean: true
            })
            if (archiveData) {
                return archiveData
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: joining venue or group
 * @param {string} authorization
 * @param {string} groupId
 * @param {string} userId
 * @param {string} adminId
 * @param {string} groupType
 * @param {boolean} isPrivate
 * @returns: success
 */

let joinGroup = async (payloadData, userData) => {
    try {
        if (payloadData.groupType === "VENUE") {
            let checkGroupExistance = await getRequired(Model.VenueGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, {}, { lean: true })
            if (!checkGroupExistance.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_NOT_EXIST)
            }
            if (payloadData.isPrivate) {
                let checkInvite = await getRequired(Model.Notifications, {
                    venueId: payloadData.groupId,
                    toId: userData._id,
                    type: "INVITE_VENUE",
                    actionPerformed: false
                }, {}, { lean: true })
                if (checkInvite.length) {
                    await socketManager.requestCount(userData._id)
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVITE_REQUEST_ALREADY)
                }
                let groupMemberExistance = await getRequired(Model.VenueGroupMembers, {
                    groupId: payloadData.groupId,
                    userId: userData._id,
                    isDeleted: false
                }, {}, { lean: true })

                if (groupMemberExistance.length) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ALREADY_A_MEMBER)
                }
                let query = {};

                query.byId = mongoose.Types.ObjectId(userData._id)
                query.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_VENUE;

                if (payloadData.groupId) {
                    query.groupType = "VENUE";
                    query.venueId = mongoose.Types.ObjectId(payloadData.groupId)
                }

                if (payloadData.adminId)
                    query.toId = mongoose.Types.ObjectId(payloadData.adminId);

                let check = await getRequired(Model.Notifications, query, {}, { new: true, lean: true });

                if (check.length) {
                    if (check[0].isDeleted) {
                        query.isDeleted = true
                        let data = await updateData(Model.Notifications, query, {
                            $set: {
                                isDeleted: false,
                                isRejected: false,
                                actionPerformed: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })

                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_VENUE,
                            msg: userData.fullName + ' requested to join your venue'
                        };
                        socketManager.requestCount(payloadData.adminId)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        return data
                    }

                    if (check[0].actionPerformed && check[0].isRejected) {
                        await updateData(Model.Notifications, { _id: check[0]._id }, {
                            $set: {
                                actionPerformed: false,
                                isRead: false,
                                isRejected: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })

                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_VENUE,
                            msg: userData.fullName + ' requested to join your venue'
                        };
                        socketManager.requestCount(payloadData.adminId)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        return
                    }

                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOTIFICATION_SEND_ALREADY_FOR_VENUE)
                }
                delete query.isDeleted
                query.text = userData.fullName + ' requested to join your venue';
                query.createdOn = +new Date()
                query.actionPerformed = false

                let data = await createData(Model.Notifications, query)
                if (data) {
                    let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })
                    let pushData = {
                        id: payloadData.groupId,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_VENUE,
                        msg: userData.fullName + ' requested to join your venue'
                    };
                    socketManager.requestCount(payloadData.adminId)
                    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    })
                    return data
                }
            }

            let check = await getRequired(Model.VenueGroups, {
                _id: mongoose.Types.ObjectId(payloadData.groupId),
                isDeleted: false
            }, { lean: true })

            if (!check.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_NOT_EXIST)
            }

            let checkInvite = await updateData(Model.Notifications, {
                venueId: payloadData.groupId,
                toId: userData._id,
                type: "INVITE_VENUE",
                actionPerformed: false
            }, { $set: { actionPerformed: true, isDeleted: true } }, { lean: true })


            let checkDelete = await updateData(Model.VenueGroupMembers, {
                isDeleted: true,
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, { isDeleted: false, joinedAt: +new Date }, { new: true, lean: true })
            if (checkDelete) {
                await socketManager.joinUserWhenGroupActive(userData._id, payloadData.groupId)
                await updateData(Model.VenueGroups, { _id: mongoose.Types.ObjectId(payloadData.groupId) }, {
                    $inc: { memberCount: 1 },
                    $push: { memberIds: mongoose.Types.ObjectId(userData._id) }
                })
                return checkDelete
            }

            let checkDelete2 = await getRequired(Model.VenueGroupMembers, {
                isDeleted: false,
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, {}, { lean: true })
            if (checkDelete2.length) {
                return checkDelete2[0]
            }

            let query = {}
            if (payloadData.groupId) {
                query.groupId = mongoose.Types.ObjectId(payloadData.groupId)
                query.userId = mongoose.Types.ObjectId(userData._id)
                query.joinedAt = +new Date()
            }

            let data = await Service.saveData(Model.VenueGroupMembers, query)

            if (data) {
                await socketManager.joinUserWhenGroupActive(userData._id, payloadData.groupId)
                let venueData = await updateData(Model.VenueGroups, { _id: mongoose.Types.ObjectId(payloadData.groupId) }, {
                    $inc: { memberCount: 1 },
                    $push: { memberIds: mongoose.Types.ObjectId(userData._id) }
                })
                await createData(Model.Notifications, {
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.VENUE,
                    conversationId: venueData.conversationId,
                    toId: payloadData.adminId,
                    byId: userData._id,
                    text: userData.fullName + ' has joined your venue',
                    createdOn: +new Date,
                    venueId: payloadData.groupId
                })
                let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })
                // let byIdData = await getRequired(Model.Users, {_id: userData._id}, {}, {lean: true})

                let pushData = {
                    id: payloadData.groupId,
                    conversationId: venueData.conversationId,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.VENUE,
                    msg: userData.fullName + ' has joined your venue'
                };
                await socketManager.requestCount(userData._id)
                await pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
                return data
            }
        } else {
            let checkGroupExistance = await getRequired(Model.PostGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, {}, { lean: true })
            if (!checkGroupExistance.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_DELETED)
            }
            if (payloadData.isPrivate) {
                let checkInvite = await getRequired(Model.Notifications, {
                    groupId: payloadData.groupId,
                    toId: userData._id,
                    type: "INVITE_GROUP",
                    actionPerformed: false
                }, {}, { lean: true })
                if (checkInvite.length) {
                    await socketManager.requestCount(userData._id)
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVITE_REQUEST_ALREADY)
                }
                let groupMemberExistance = await getRequired(Model.PostGroupMembers, {
                    groupId: payloadData.groupId,
                    userId: userData._id,
                    isDeleted: false
                }, {}, { lean: true })

                if (groupMemberExistance.length) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ALREADY_A_MEMBER)
                }

                let query = {}
                query.byId = userData._id
                query.type = "REQUEST_GROUP";

                if (payloadData.groupId) {
                    query.groupType = "GROUP";
                    query.groupId = payloadData.groupId
                }

                if (payloadData.adminId)
                    query.toId = payloadData.adminId;


                let check = await getRequired(Model.Notifications, query, {}, { new: true, lean: true })
                if (check.length) {

                    if (check[0].isDeleted) {
                        query.isDeleted = true;
                        let data = await updateData(Model.Notifications, query, {
                            $set: {
                                isDeleted: false,
                                isRejected: false,
                                actionPerformed: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_GROUP,
                            msg: userData.fullName + ' requested to join your channel'
                        };
                        socketManager.requestCount(payloadData.adminId)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        return data
                    }

                    if (check[0].actionPerformed && check[0].isRejected) {
                        await updateData(Model.Notifications, { _id: check[0]._id }, {
                            $set: {
                                actionPerformed: false,
                                isRejected: false,
                                isRead: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_GROUP,
                            msg: userData.fullName + ' requested to join your channel'
                        };
                        socketManager.requestCount(payloadData.adminId)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        return
                    }

                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOTIFICATION_SEND_ALREADY_FOR_GROUP)
                }

                delete query.isDeleted;
                query.text = userData.fullName + ' requested to join your channel';
                query.createdOn = +new Date();
                query.actionPerformed = false

                let data = await createData(Model.Notifications, query);

                if (data) {

                    let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true });
                    let pushData = {
                        id: payloadData.groupId,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_GROUP,
                        msg: userData.fullName + ' requested to join your channel'
                    };
                    socketManager.requestCount(payloadData.adminId)
                    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    });
                    return data
                }
            }

            let check = await getRequired(Model.PostGroups, {
                _id: mongoose.Types.ObjectId(payloadData.groupId),
                isDeleted: false
            }, { lean: true })
            if (!check.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_NOT_EXIST)
            }

            let checkInvite = await updateData(Model.Notifications, {
                groupId: payloadData.groupId,
                toId: userData._id,
                type: "INVITE_GROUP",
                actionPerformed: false
            }, { $set: { actionPerformed: true, isDeleted: true } }, { lean: true })

            let checkDelete = await updateData(Model.PostGroupMembers, {
                isDeleted: true,
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, { isDeleted: false }, { new: true, lean: true })
            if (checkDelete) {
                await socketManager.joinUserWhenGroupActive(userData._id, payloadData.groupId)
                await updateData(Model.PostGroups, { _id: mongoose.Types.ObjectId(payloadData.groupId) }, {
                    $inc: { memberCounts: 1 },
                    $push: { isMember: mongoose.Types.ObjectId(userData._id) }
                })
                return checkDelete
            }

            let checkDelete2 = await getRequired(Model.PostGroupMembers, {
                isDeleted: false,
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                userId: mongoose.Types.ObjectId(userData._id)
            }, {}, { lean: true })
            if (checkDelete2.length) {
                return checkDelete2[0]
            }

            let query = {}
            if (payloadData.groupId) {
                query.groupId = mongoose.Types.ObjectId(payloadData.groupId)
                query.userId = mongoose.Types.ObjectId(userData._id)
                query.joinedAt = +new Date()
            }

            let data = await Service.saveData(Model.PostGroupMembers, query)
            if (data) {
                Service.update(Model.Posts, {
                    groupId: payloadData.groupId,
                    readBy: { $nin: [mongoose.Types.ObjectId(userData._id)] }
                }, { $addToSet: { readBy: userData._id } }, { new: true, lean: true, multi: true })

                await socketManager.joinUserWhenGroupActive(userData._id, payloadData.groupId)
                let groupData = await updateData(Model.PostGroups, { _id: mongoose.Types.ObjectId(payloadData.groupId) }, {
                    $inc: { memberCounts: 1 },
                    $push: { isMember: mongoose.Types.ObjectId(userData._id) }
                })
                await createData(Model.Notifications, {
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.GROUP,
                    conversationId: groupData.conversationId,
                    toId: payloadData.adminId,
                    text: userData.fullName + ' has joined your channel',
                    byId: userData._id,
                    createdOn: +new Date,
                    groupId: payloadData.groupId
                })
                let toIdData = await getRequired(Model.Users, { _id: payloadData.adminId }, {}, { lean: true })
                // let byIdData = await getRequired(Model.Users, {_id: userData._id}, {}, {lean: true})
                let pushData = {
                    id: payloadData.groupId,
                    conversationId: groupData.conversationId,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.GROUP,
                    msg: userData.fullName + ' has joined your channel'
                };
                await socketManager.requestCount(userData._id)
                await pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
                return data
            }
        }
    } catch (e) {
        console.log(e)
    }

}

let groupDetails = async (payloadData, userData) => {
    try {
        if (payloadData.venueId) {
            let checkGroupExistance = await getRequired(Model.VenueGroups, {
                _id: payloadData.venueId,
                isDeleted: false
            }, {}, { lean: true })
            if (!checkGroupExistance.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_NOT_EXIST)
            }
            let membersList = [], notArrayRejected = [], notArrayPending = [], requestStatus
            let populate = [{
                path: "userId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]
            let groupInfo = await getRequired(Model.VenueGroups, {
                _id: payloadData.venueId,
                isDeleted: false
            }, {}, { lean: true })
            let groupMembersInfo = await getRequiredPopulate(Model.VenueGroupMembers, {
                groupId: payloadData.venueId,
                isDeleted: false
            }, { _id: 0, userId: 1, isAdmin: 1, isNotify: 1 }, { lean: true }, populate)
            let requestVenue = await getRequired(Model.Notifications, {
                byId: userData._id,
                actionPerformed: false,
                groupType: "VENUE",
                type: "REQUEST_VENUE"
            }, {}, { lean: true })

            if (requestVenue.length) {
                for (let req of requestVenue) {
                    if (req.isRejected && req.actionPerformed) {
                        notArrayRejected.push(req.venueId)
                        continue
                    }
                    if (!req.actionPerformed && !req.isRejected) {
                        notArrayPending.push(req.venueId)
                    }
                }
            }
            let userNotification
            if (groupMembersInfo.length) {
                for (let a of groupMembersInfo) {
                    membersList.push(a)
                    if (JSON.stringify(a.userId._id) == JSON.stringify(userData._id)) {
                        userNotification = a.isNotify
                        continue
                    }
                    if (!a.userId.imageVisibilityForEveryone) {
                        if (a.userId.imageVisibilityForFollowers) {
                            a.userId = await imageVisibilityManipulation(a.userId, userData)
                        } else if (a.userId.imageVisibility && a.userId.imageVisibility.length) {
                            a.userId = await imageVisibilityManipulation(a.userId, userData)
                        }
                    }

                    if (!a.userId.nameVisibilityForEveryone) {
                        if (a.userId.nameVisibilityForFollowers) {
                            a.userId = await nameVisibilityManipulation(a.userId, userData)
                        } else if (a.userId.nameVisibility && a.userId.nameVisibility.length) {
                            a.userId = await nameVisibilityManipulation(a.userId, userData)
                        }
                    }
                    // if(!a.userId.imageVisibilityForFollowers && !a.userId.nameVisibilityForFollowers && !a.userId.imageVisibility.length && !a.userId.nameVisibility.length){
                    //     continue
                    // }
                    // a.userId = await imageVisibilityManipulation(a.userId, userData)
                    // a.userId = await nameVisibilityManipulation(a.userId, userData)
                }
            }
            if (notArrayPending.length && JSON.stringify(notArrayPending).includes(payloadData.venueId)) {
                requestStatus = "PENDING"
            } else if (notArrayRejected.length && JSON.stringify(notArrayRejected).includes(payloadData.venueId)) {
                requestStatus = "REJECTED"
            } else {
                requestStatus = "NONE"
            }

            return {
                _id: groupInfo[0]._id,
                groupId: groupInfo[0]._id,
                venueTitle: groupInfo[0].venueTitle,
                imageUrl: groupInfo[0].imageUrl,
                adminId: groupInfo[0].adminId,
                venueTags: groupInfo[0].venueTags,
                createdBy: groupInfo[0].createdBy,
                conversationId: groupInfo[0].conversationId,
                isPrivate: groupInfo[0].isPrivate,
                memberCount: groupInfo[0].memberCount,
                venueTime: groupInfo[0].venueTime,
                requestStatus: requestStatus,
                venueLocation: groupInfo[0].venueLocation,
                venueLocationAddress: groupInfo[0].venueLocationAddress,
                venueLocationName: groupInfo[0].venueLocationName,
                membersList: membersList || [],
                notification: userNotification || false
            }
        } else {

            let checkGroupExistance = await getRequired(Model.PostGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, {}, { lean: true })
            if (!checkGroupExistance.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_DELETED)
            }

            let membersList = []
            let populate = [{
                path: "userId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]
            let groupInfo = await getRequired(Model.PostGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, {}, { lean: true })
            let groupMembersInfo = await getRequiredPopulate(Model.PostGroupMembers, {
                groupId: payloadData.groupId,
                isDeleted: false
            }, { _id: 0, userId: 1, isAdmin: 1, isNotify: 1 }, { lean: true }, populate)
            let userNotification
            if (groupMembersInfo.length) {
                for (let a of groupMembersInfo) {
                    membersList.push(a)
                    if (JSON.stringify(a.userId._id) == JSON.stringify(userData._id)) {
                        userNotification = a.isNotify
                        continue
                    }
                    if (!a.userId.imageVisibilityForEveryone) {
                        if (a.userId.imageVisibilityForFollowers) {
                            a.userId = await imageVisibilityManipulation(a.userId, userData)
                        } else if (a.userId.imageVisibility && a.userId.imageVisibility.length) {
                            a.userId = await imageVisibilityManipulation(a.userId, userData)
                        }
                    }

                    if (!a.userId.nameVisibilityForEveryone) {
                        if (a.userId.nameVisibilityForFollowers) {
                            a.userId = await nameVisibilityManipulation(a.userId, userData)
                        } else if (a.userId.nameVisibility && a.userId.nameVisibility.length) {
                            a.userId = await nameVisibilityManipulation(a.userId, userData)
                        }
                    }
                    // if(!a.userId.imageVisibilityForFollowers && !a.userId.nameVisibilityForFollowers && !a.userId.imageVisibility.length && !a.userId.nameVisibility.length){
                    //     continue
                    // }
                    // a.userId = await imageVisibilityManipulation(a.userId, userData)
                    // a.userId = await nameVisibilityManipulation(a.userId, userData)
                }
            }

            return {
                _id: groupInfo[0]._id,
                groupId: groupInfo[0]._id,
                groupName: groupInfo[0].groupName,
                imageUrl: groupInfo[0].imageUrl,
                adminId: groupInfo[0].adminId,
                createdBy: groupInfo[0].createdBy,
                conversationId: groupInfo[0].conversationId,
                isPrivate: groupInfo[0].isPrivate,
                memberCount: groupInfo[0].memberCounts,
                description: groupInfo[0].description,
                membersList: membersList || [],
                notification: userNotification || false
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: config notification facilities
 * @param {string} authorization
 * @param {string} venueId
 * @param {boolean} action
 * @returns: success
 */

let configNotification = async (payloadData, userData) => {
    try {
        if (payloadData.venueId) {
            let criteria = {}
            let dataToSet = {}
            if (payloadData.venueId)
                criteria.groupId = payloadData.venueId
            criteria.userId = userData._id
            criteria.isDeleted = false

            if (payloadData.action)
                dataToSet.isNotify = true
            else
                dataToSet.isNotify = false
            let data = await updateData(Model.VenueGroupMembers, criteria, { $set: dataToSet }, { lean: true, new: true })
            if (data)
                return data
        } else {
            let criteria = {}
            let dataToSet = {}
            if (payloadData.groupId)
                criteria.groupId = mongoose.Types.ObjectId(payloadData.groupId)
            criteria.userId = mongoose.Types.ObjectId(userData._id)
            criteria.isDeleted = false

            if (payloadData.action)
                dataToSet.isNotify = true
            else
                dataToSet.isNotify = false
            let data = await updateData(Model.PostGroupMembers, criteria, { $set: dataToSet }, { lean: true, new: true })
            if (data)
                return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: getting notification list
 * @param {string} authorization
 * @param {number} pageNo
 * @returns: success
 */

let getNotifications = async (payloadData, userData) => {
    try {
        if (!payloadData.pageNo || payloadData.pageNo === 0)
            payloadData.pageNo = 1;

        let populate = [{
            path: "userId",
            select: "userName fullName imageUrl",
            model: "Users",
        }, {
            path: "byId",
            select: "_id userName imageUrl fullName isAccountPrivate imageVisibilityForEveryone nameVisibilityForEveryone imageVisibilityForFollowers imageVisibility nameVisibility nameVisibilityForFollowers followers firstName",
            model: "Users",
        }, {
            path: "venueId",
            select: "venueTitle imageUrl conversationId",
            model: "VenueGroups",
        }, {
            path: "groupId",
            select: "groupName imageUrl conversationId",
            model: "PostGroups",
        }, {
            path: "postId",
            select: "postCategoryId imageUrl postText",
            model: "Posts",
            populate: {
                path: "postCategoryId",
                select: "categoryName",
                model: "Categories"
            }
        }
            // ,{
            //     path: "commentId",
            //     select: "comment",
            //     model: "Comments",
            //     //match: { "commentId": { $nin: null } }
            // }
            , {
            path: "replyId",
            select: "reply",
            model: "Replies",
        }]
        // let getReqNoti = await getRequiredPopulate(Model.Notifications, {toId: userData._id, isDeleted: false, type: "REQUEST", actionPerformed: false}, {isRead:1, actionPerformed:1, byId:1, createdOn:1, venueId:1, groupId:1, toId:1, groupType:1 }, { sort: {createdOn: -1},lean: true}, populate)
        // let commentLikeNoti = await getRequiredPopulate(Model.Notifications, {toId: userData._id, isDeleted: false, $or: [{type: "COMMENT"}, {type: "LIKE"}, {type: "REPLY"}]}, {isRead:1,actionPerformed:1, byId:1, createdOn:1, venueId:1, toId:1, type: 1, postId: 1, commentId: 1}, {sort: {createdOn: -1}, lean: true}, populate)
        let data = await getRequiredPopulate(Model.Notifications, { toId: userData._id, isDeleted: false }, {
            isRead: 1,
            actionPerformed: 1,
            byId: 1,
            createdOn: 1,
            venueId: 1,
            toId: 1,
            type: 1,
            text: 1,
            postId: 1,
            commentId: 1,
            location: 1,
            locationName: 1,
            locationAddress: 1
        }, { sort: { createdOn: -1 }, lean: true, limit: 10, skip: ((payloadData.pageNo - 1) * 10) }, populate)
        if (data.length) {
            // await updateData(Model.Notifications, {
            //     toId: userData._id,
            //     isDeleted: false,
            //     actionPerformed: false
            // }, { $set: { isRead: true } }, { new: true, lean: true })
            let updates = await Service.update(Model.Notifications, {
                toId: userData._id,
                isDeleted: false
                //actionPerformed: false
            }, { $set: { isRead: true } }, { multi: true });
            console.log(">>>>>>>>>>>>>>>>>>>>>>", updates);
            let finalArray = []
            for (let user of data) {
                let tempObj = {}
                tempObj.byId = {};
                if (user.byId) {
                    if (!user.byId.imageVisibilityForEveryone) {
                        if (user.byId.imageVisibilityForFollowers && (JSON.stringify(user.byId.followers).includes(JSON.stringify(userData._id)))) {
                            tempObj.byId.imageUrl = user.byId.imageUrl
                        } else if (user.byId.imageVisibility && user.byId.imageVisibility.length && (JSON.stringify(user.byId.imageVisibility).includes(JSON.stringify(userData._id)))) {
                            tempObj.byId.imageUrl = user.byId.imageUrl
                        } else {
                            tempObj.byId.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                        }
                    } else {
                        tempObj.byId.imageUrl = user.byId.imageUrl
                    }

                    if (!user.byId.nameVisibilityForEveryone) {
                        if (user.byId.nameVisibilityForFollowers && (JSON.stringify(user.byId.followers).includes(JSON.stringify(userData._id)))) {
                            tempObj.byId.userName = user.byId.userName
                            tempObj.byId.fullName = user.byId.fullName
                        } else if (user.byId.nameVisibility && user.byId.nameVisibility.length && (JSON.stringify(user.byId.nameVisibility).includes(JSON.stringify(userData._id)))) {
                            tempObj.byId.userName = user.byId.userName
                            tempObj.byId.fullName = user.byId.fullName
                        } else {
                            tempObj.byId.userName = user.byId.userName.substr(0, 3)
                            tempObj.byId.fullName = user.byId.firstName.substr(0, 1) + ".... "
                        }
                    } else {
                        tempObj.byId.userName = user.byId.userName
                        tempObj.byId.fullName = user.byId.fullName
                    }
                }

                if (user.byId) {
                    if (user.byId._id) {
                        tempObj.byId._id = user.byId._id
                    }
                }
                tempObj._id = user._id
                tempObj.location = user.location
                tempObj.toId = user.toId
                tempObj.type = user.type
                tempObj.postId = user.postId
                tempObj.text = user.text
                tempObj.commentId = user.commentId
                tempObj.createdOn = user.createdOn
                tempObj.userId = user.userId
                tempObj.groupId = user.groupId
                tempObj.replyId = user.replyId
                tempObj.venueId = user.venueId
                tempObj.groupType = user.groupType
                tempObj.locationName = user.locationName
                tempObj.locationAddress = user.locationAddress
                tempObj.userName = user.userName
                tempObj.conversationId = user.conversationId
                tempObj.isRejected = user.isRejected
                tempObj.actionPerformed = user.actionPerformed
                tempObj.isBlocked = user.isBlocked
                tempObj.isDeleted = user.isDeleted
                // for comment and media comments
                if (tempObj.commentId) {
                    let comment = await Model.Comments.findById(tempObj.commentId, { 'comment': 1 });
                    if (comment)
                        tempObj.commentId = comment;
                    else {
                        let mediaComment = await Model.MediaComments.findById(tempObj.commentId, { 'comment': 1 });
                        if (mediaComment)
                            tempObj.commentId = mediaComment;
                    }
                }
                finalArray.push(tempObj)
            }
            //    console.log(finalArray);
            return finalArray
        } else
            return []
    } catch (e) {
        console.log(e)
    }
}

let clearNotification = async (payloadData, userData) => {
    if (!payloadData.pageNo || payloadData.pageNo === 0)
        payloadData.pageNo = 1;

    let populate = [{
        path: "byId",
        select: "userName fullName imageUrl",
        model: "Users",
    }, {
        path: "venueId",
        select: "venueTitle imageUrl conversationId",
        model: "VenueGroups",
    }, {
        path: "groupId",
        select: "groupName imageUrl conversationId",
        model: "PostGroups",
    }, {
        path: "postId",
        select: "postCategoryId imageUrl",
        model: "Posts",
        populate: {
            path: "postCategoryId",
            select: "categoryName",
            model: "Categories"
        }
    }, {
        path: "commentId",
        select: "comment",
        model: "Comments",
    }, {
        path: "replyId",
        select: "reply",
        model: "Replies",
    }]
    let updateNotification = await Service.update(Model.Notifications, {
        toId: userData._id,
        actionPerformed: { $ne: false }
    }, { $set: { isDeleted: true } }, { new: true, lean: true, multi: true })
    if (updateNotification) {
        let remaiiningNot = await getRequiredPopulate(Model.Notifications, {
            toId: userData._id,
            isDeleted: false
        }, {
            isRead: 1,
            actionPerformed: 1,
            byId: 1,
            createdOn: 1,
            venueId: 1,
            toId: 1,
            type: 1,
            postId: 1,
            commentId: 1
        }, { sort: { createdOn: -1 }, lean: true, limit: 10, skip: ((payloadData.pageNo - 1) * 10) }, populate)
        console.log(remaiiningNot)
        if (remaiiningNot.length) {
            return remaiiningNot
        } else {
            return []
        }
    }
}

/**
 * @description: creating and updating venue group
 * @param {string} authorization
 * @param {string} venueGroupId
 * @param {string} venueTitle
 * @param {double} venueLocationLong
 * @param {double} venueLocationLat
 * @param {string} venueLocationName
 * @param {string} venueTags
 * @param {string} categoryId
 * @param {string} isPrivate
 * @param {string} groupImageOriginal
 * @param {string} groupImageThumbnail
 * @param {string} venueTime
 * @param {string} venueAdditionalDetailsName
 * @param {string} venueAdditionalDetailsDocs
 * @returns: saved data
 */

let addEditVenueGroup = async (payloadData, userData) => {
    try {
        let query = {}
        if (payloadData.venueTitle)
            if (payloadData.venueTime)
                query.venueTime = payloadData.venueTime
        if (payloadData.venueLocationName)
            query.venueLocationName = payloadData.venueLocationName
        if (payloadData.venueLocationAddress)
            query.venueLocationAddress = payloadData.venueLocationAddress
        if (payloadData.categoryId)
            query.categoryId = payloadData.categoryId
        if (payloadData.venueTags)
            query.venueTags = payloadData.venueTags
        if (payloadData.isPrivate === 1)
            query.isPrivate = true
        if (payloadData.isPrivate === 2)
            query.isPrivate = false
        if (payloadData.venueAdditionalDetailsName)
            query.venueAdditionalDetailsName = payloadData.venueAdditionalDetailsName
        if (payloadData.members)
            query.members = payloadData.members
        if (payloadData.venueAdditionalDetailsDocs)
            query.venueAdditionalDetailsDocs = payloadData.venueAdditionalDetailsDocs

        if (payloadData.venueGroupId) {
            let criteria = {
                _id: payloadData.venueGroupId,
                isDeleted: false
            }
            if (payloadData.venueLocationLong && payloadData.venueLocationLat) {
                query.venueLocation = []
                query.venueLocation[0] = payloadData.venueLocationLong
                query.venueLocation[1] = payloadData.venueLocationLat
            }
            if (payloadData.groupImageOriginal && payloadData.groupImageThumbnail) {
                query.imageUrl = {
                    original: payloadData.groupImageOriginal,
                    thumbnail: payloadData.groupImageThumbnail
                }
            }
            let data = await updateData(Model.VenueGroups, criteria, { $set: query }, { new: true, lean: true })
            if (data) {
                return data
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        } else {
            if (payloadData.venueLocationLong && payloadData.venueLocationLat) {
                query.venueLocation = []
                query.venueLocation[0] = payloadData.venueLocationLong
                query.venueLocation[1] = payloadData.venueLocationLat
            } else {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.LAT_LONG_MISSING)
            }
            query.adminId = userData._id
            query.memberCount = 1
            query.memberIds = [userData._id]
            query.createdBy = userData._id
            query.conversationId = mongoose.Types.ObjectId()
            query.createdOn = +new Date
            query.infoUpdated = +new Date
            if (payloadData.groupImageOriginal && payloadData.groupImageThumbnail) {
                query.imageUrl = {
                    original: payloadData.groupImageOriginal,
                    thumbnail: payloadData.groupImageThumbnail
                }
            } else {
                query.imageUrl = {
                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.VENUE.ORIGINAL,
                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.VENUE.THUMBNAIL
                }
            }
            let data = await Service.saveData(Model.VenueGroups, query)
            if (data) {
                let query = {
                    groupId: data._id,
                    userId: userData._id,
                    isAdmin: true,
                    joinedAt: +new Date(),
                }
                await Service.saveData(Model.VenueGroupMembers, query)
                if (!payloadData.participantIds) {
                    payloadData.participantIds = []
                }
                if (payloadData.participantIds.length) {
                    let criteria = {}
                    for (let participant of payloadData.participantIds) {
                        criteria.toId = mongoose.Types.ObjectId(participant)
                        criteria.venueId = data._id
                        criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE
                        criteria.groupType = "VENUE"
                        let check = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                        if (check.length) {
                            await updateData(Model.Notifications, criteria, {
                                $set: {
                                    isDeleted: false,
                                    actionPerformed: false,
                                    isRejected: false,
                                    isRead: false,
                                    createdOn: +new Date
                                }
                            }, { new: true, lean: true })
                            let toIdData = await getRequired(Model.Users, {
                                _id: participant,
                                isDeleted: false
                            }, {}, { lean: true })
                            let groupData = await getRequired(Model.VenueGroups, { _id: payloadData.venueId }, {}, { lean: true })
                            let pushData = {
                                id: payloadData.venueId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                msg: userData.fullName + ' requested you to join ' + groupData[0].venueTitle + ' venue'
                            };
                            socketManager.requestCount(participant)
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                            continue
                        }
                        let query = {
                            venueId: data._id,
                            toId: participant,
                            text: userData.fullName + ' requested you to join ' + groupData[0].venueTitle + ' venue',
                            byId: userData._id,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                            groupType: "VENUE",
                            createdOn: +new Date,
                            actionPerformed: false
                        }
                        let notData = await createData(Model.Notifications, query)
                        if (notData) {
                            let toIdData = await getRequired(Model.Users, { _id: participant }, {}, { lean: true })
                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                msg: userData.fullName + ' requested you to join ' + payloadData.venueTitle + ' venue'
                            };
                            socketManager.requestCount(participant)
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        }
                    }
                }
                return data
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        }
    } catch (e) {
        console.log(e)
    }
}

let assignAdminAndExit = async (payloadData, userData) => {
    try {
        let criteria = {}
        if (payloadData.groupId && payloadData.userId) {
            criteria._id = payloadData.groupId
            criteria.isDeleted = false
            if (payloadData.groupType === "VENUE") {
                let data = await updateData(Model.VenueGroups, criteria, {
                    $set: { adminId: payloadData.userId },
                    $pull: { memberIds: userData._id },
                    $inc: { memberCount: -1 }
                }, { new: true, lean: true })
                if (data) {
                    await updateData(Model.VenueGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: userData._id,
                        isDeleted: false
                    }, { $set: { isAdmin: false, isDeleted: true } }, { new: true, lean: true })
                    await updateData(Model.VenueGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { isAdmin: true, joinedAt: +new Date() } }, { new: true, lean: true })
                    return data
                }
            } else {
                let data = await updateData(Model.PostGroups, criteria, {
                    $set: { adminId: payloadData.userId },
                    $pull: { isMember: userData._id },
                    $inc: { memberCounts: -1 }
                }, { new: true, lean: true })
                if (data) {
                    await updateData(Model.PostGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: userData._id,
                        isDeleted: false
                    }, { $set: { isAdmin: false, isDeleted: true } }, { new: true, lean: true })
                    await updateData(Model.PostGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { isAdmin: true, joinedAt: +new Date() } }, { new: true, lean: true })
                    return data
                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let assignAdmin = async (payloadData, userData) => {
    try {
        let criteria = {}
        if (payloadData.groupId && payloadData.userId) {
            criteria._id = payloadData.groupId
            criteria.isDeleted = false
            if (payloadData.groupType === "VENUE") {
                let data = await updateData(Model.VenueGroups, criteria, { $set: { adminId: payloadData.userId } }, {
                    new: true,
                    lean: true
                })
                if (data) {
                    await updateData(Model.VenueGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: userData._id,
                        isDeleted: false
                    }, { $set: { isAdmin: false } }, { new: true, lean: true })
                    await updateData(Model.VenueGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { isAdmin: true, joinedAt: +new Date() } }, { new: true, lean: true })
                    return data
                }
            } else {
                let data = await updateData(Model.PostGroups, criteria, { $set: { adminId: payloadData.userId } }, {
                    new: true,
                    lean: true
                })
                if (data) {
                    await updateData(Model.PostGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: userData._id,
                        isDeleted: false
                    }, { $set: { isAdmin: false } }, { new: true, lean: true })
                    await updateData(Model.PostGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { isAdmin: true, joinedAt: +new Date() } }, { new: true, lean: true })
                    return data
                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: fetching filtered data according to date, privacy, lat long, interest tags
 * @param {string} authorization
 * @param {string} date
 * @param {string} categoryId
 * @param {double} private
 * @param {double} locationLong
 * @param {double} locationLat
 * @returns: fetched data
 */

let getVenueFilter = async (payloadData, userData) => {
    try {
        let $match = {
            isDeleted: false,
        }

        let $geoNear = {}
        if (payloadData.date) {
            var date = new Date(payloadData.date + " 00:00 AM");
            $match.venueTime = {
                $gte: date.getTime(),
                $lt: (date.getTime() + (24 * 3600000))
            }
        }

        if (payloadData.private && payloadData.private.length === 1) {
            for (let cat of payloadData.private) {
                if (cat == "1") {
                    $match.isPrivate = true
                }
                if (cat == "2") {
                    $match.isPrivate = false
                }
            }
        }

        let arrayCategoryId = []
        if (payloadData.categoryId && payloadData.categoryId.length) {
            for (let cat of payloadData.categoryId) {
                arrayCategoryId.push(mongoose.Types.ObjectId(cat))
            }
        }
        if (payloadData.categoryId && payloadData.categoryId.length) {
            $match.categoryId = { $in: arrayCategoryId }
            // $match.categoryId = mongoose.Types.ObjectId(payloadData.categoryId)

        }

        if (payloadData.locationLong && payloadData.locationLat) {
            $geoNear.near = { type: "Point", coordinates: [payloadData.locationLong, payloadData.locationLat] },
                $geoNear.distanceField = "calculated",
                $geoNear.maxDistance = 5000,
                $geoNear.spherical = true,
                $geoNear.distanceMultiplier = 0.000621371
        }

        let $project = {
            groupId: "$_id",
            imageUrl: 1,
            venueTitle: 1,
            venueLocation: 1,
            venueLocationName: 1,
            venueLocationAddress: 1,
            venueTime: 1,
            venueTags: 1,
            venueAdditionalDetailsName: 1,
            venueAdditionalDetailsDocs: 1,
            isPrivate: 1,
            categoryId: 1,
            adminId: 1,
            memberCount: 1,
            isMember: {
                $cond: { if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] }, then: true, else: false }
            },
        }

        let pipeline = []
        if (Object.keys($geoNear).length) {
            pipeline.push({ $geoNear })
            pipeline.push({ $match })
            pipeline.push({ $project })
        } else {
            pipeline.push({ $match })
            pipeline.push({ $project })
        }
        let data = await aggregateData(Model.VenueGroups, pipeline)
        if (data) {
            return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: searching data according to the venue name
 * @param {string} authorization
 * @param {string} search
 * @returns: fetched data
 */

let searchVenue = async (payloadData, userData) => {
    try {
        let criteria = {
            isDeleted: false,
        };

        let project = {
            venueTitle: 1,
            imageUrl: 1,
            venueLocationName: 1,
            venueLocationAddress: 1,
            venueLocation: 1,
            memberCount: 1
        }

        if (payloadData.search) criteria.$or = [
            { venueTitle: new RegExp(payloadData.search, 'i') }
        ];

        let data = await getRequired(Model.VenueGroups, criteria, project, { lean: true })
        if (data.length) {
            return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: details chat conversation of a particular group
 * @param {string} authorization
 * @param {string} groupId
 * @param {double} pageNo
 * @returns: fetched chat data
 */

let venueConversationDetails = async (payloadData, userData) => {
    try {
        let data
        if (payloadData.chatId) {
            let chatDetails = await getRequired(Model.VenueChats, {
                _id: payloadData.chatId,
                isDeleted: false
            }, { createdDate: 1 }, { lean: true })
            let pipeline = [{
                $match: {
                    groupId: mongoose.Types.ObjectId(payloadData.groupId),
                    createdDate: { $lt: chatDetails[0].createdDate },
                    isDeleted: false
                }
            }, {
                $group: {
                    _id: "$groupId",
                    chat: {
                        $push: {
                            _id: "$_id",
                            chatDetails: "$chatDetails",
                            conversationId: "$conversationId",
                            isDelivered: "$isDelivered",
                            readBy: "$readBy",
                            senderId: "$senderId",
                            createdDate: "$createdDate",
                            groupId: "$groupId"
                        }
                    }
                }
            }, {
                $project: {
                    _id: 1,
                    chats: { $slice: ["$chat", -40, 40] }
                }
            }]
            let populate = [{
                path: "chats.senderId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]

            data = await aggregateWithPopulate(Model.VenueChats, pipeline, populate)
            if (data.length) {
                data = data[0].chats
            } else {
                data = []
            }
        } else {
            // let check = await getRequired(Model.VenueGroups, {_id: mongoose.Types.ObjectId(payloadData.groupId), isDeleted: false}, {lean: true})
            // if(!check.length){
            //     return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_NOT_EXIST)
            // }
            await socketManager.joinUserToGroupOnlogin([payloadData.groupId], userData._id)

            let pipeline = [{
                $match: {
                    groupId: mongoose.Types.ObjectId(payloadData.groupId),
                    isDeleted: false
                }
            }, {
                $project: {
                    _id: 1,
                    chatDetails: 1,
                    conversationId: 1,
                    isDelivered: 1,
                    readBy: 1,
                    senderId: 1,
                    createdDate: 1,
                    groupId: 1
                }
            }, { $sort: { createdDate: -1 } }, { $limit: 40 }, { $sort: { createdDate: 1 } }]
            let populate = [{
                path: "senderId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]

            data = await aggregateWithPopulate(Model.VenueChats, pipeline, populate)

        }
        // for(let a of data){
        //     a.senderId = a.senderId.toObject()
        //     if(JSON.stringify(a.senderId._id) == JSON.stringify(userData._id)){
        //         continue
        //     }
        //     if(!a.senderId.imageVisibilityForFollowers && !a.senderId.nameVisibilityForFollowers && !a.senderId.imageVisibility.length && !a.senderId.nameVisibility.length){
        //         continue
        //     }
        //     if(a.senderId.imageVisibilityForFollowers && JSON.stringify(a.senderId.followers).includes(userData._id)){
        //         a.senderId.imageUrl = a.senderId.imageUrl
        //     }else{
        //         if(JSON.stringify(a.senderId.imageVisibility).includes(userData._id)){
        //             a.senderId.imageUrl = a.senderId.imageUrl
        //         }else{
        //             a.senderId.imageUrl = {"original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
        //             "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL}
        //         }
        //     }

        //     if(a.senderId.nameVisibilityForFollowers && JSON.stringify(a.senderId.followers).includes(userData._id)){
        //         a.senderId.fullName = a.senderId.fullName
        //         a.senderId.userName = a.senderId.userName
        //     }else{
        //         if(JSON.stringify(a.senderId.nameVisibility).includes(userData._id)){
        //             a.senderId.fullName = a.senderId.fullName
        //             a.senderId.userName = a.senderId.userName
        //         }else{
        //             // a.senderId.fullName = a.senderId.firstName.substr(0, 1) + ".... " + a.senderId.lastName.substr(0, 1) + "...."
        //             a.senderId.fullName = a.senderId.firstName.substr(0, 1) + ".... "
        //             a.senderId.userName = a.senderId.userName.substr(0, 3)
        //         }
        //     }
        //     delete a.senderId.firstName
        //     delete a.senderId.lastName
        //     delete a.senderId.isAccountPrivate
        //     delete a.senderId.imageVisibility
        //     delete a.senderId.nameVisibility
        //     delete a.senderId.locationVisibility
        //     delete a.senderId.nameVisibilityForFollowers
        //     delete a.senderId.imageVisibilityForFollowers
        //     delete a.senderId.locationVisibility
        //     delete a.senderId.followers
        // }
        let groupDataCriteria = {
            groupId: payloadData.groupId,
            isDeleted: false
        }

        let groupDataProject = {
            _id: 0,
            userId: 1,
            isAdmin: 1
        }

        let groupDataPopulate = [{
            path: "userId",
            select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers",
            model: "Users",
            populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                path: "imageVisibility",
                select: "userName imageUrl fullName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
        }]
        let userNotify = await getRequired(Model.VenueGroupMembers, {
            groupId: payloadData.groupId,
            userId: userData._id
        }, { isNotify: 1 }, { lean: true })
        let groupData = await getRequiredPopulate(Model.VenueGroupMembers, groupDataCriteria, groupDataProject, {
            lean: true,
            sort: { joinedAt: 1 }
        }, groupDataPopulate)
        let groupDetails = await getRequired(Model.VenueGroups, { _id: payloadData.groupId }, {
            venueTitle: 1,
            imageUrl: 1,
            venueLocation: 1,
            venueLocationName: 1,
            venueLocationAddress: 1
        }, { lean: true })
        for (let a of groupData) {
            if (JSON.stringify(a.userId._id) == JSON.stringify(userData._id)) {
                continue
            }
            if (!a.userId.imageVisibilityForEveryone) {
                if (a.userId.imageVisibilityForFollowers) {
                    a.userId = await imageVisibilityManipulation(a.userId, userData)
                } else if (a.userId.imageVisibility && a.userId.imageVisibility.length) {
                    a.userId = await imageVisibilityManipulation(a.userId, userData)
                }
            }

            if (!a.userId.nameVisibilityForEveryone) {
                if (a.userId.nameVisibilityForFollowers) {
                    a.userId = await nameVisibilityManipulation(a.userId, userData)
                } else if (a.userId.nameVisibility && a.userId.nameVisibility.length) {
                    a.userId = await nameVisibilityManipulation(a.userId, userData)
                }
            }
            // if(!a.userId.imageVisibilityForFollowers && !a.userId.nameVisibilityForFollowers && !a.userId.imageVisibility.length && !a.userId.nameVisibility.length){
            //     continue
            // }
            // a.userId = await imageVisibilityManipulation(a.userId, userData)
            // a.userId = await nameVisibilityManipulation(a.userId, userData)
        }
        return {
            chatData: data || [],
            groupData,
            venueTitle: groupDetails[0].venueTitle,
            imageUrl: groupDetails[0].imageUrl,
            venueLocation: groupDetails[0].venueLocation,
            venueLocationName: groupDetails[0].venueLocationName,
            venueLocationAddress: groupDetails[0].venueLocationAddress,
            notification: userNotify[0].isNotify,
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: fetching data according differnt Ids
 * @param {string} authorization
 * @param {string} userId
 * @returns: fetched data
 */

let getProfileData = async (payloadData, userData) => {
    try {

        if (JSON.stringify(payloadData.userId) === JSON.stringify(userData._id)) {
            payloadData.userId = undefined
        }
        if (payloadData.userId || payloadData.userName) {
            let creden = {}, requestPending, isRequestPending = false
            if (payloadData.userId) {
                creden._id = mongoose.Types.ObjectId(payloadData.userId)
                requestPending = await getRequired(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    type: "REQUEST_FOLLOW",
                    actionPerformed: false
                }, {}, { lean: true })
                if (requestPending.length) {
                    isRequestPending = true
                }
            }
            if (payloadData.userName) {
                creden.userName = payloadData.userName
                let userNameData = await getRequired(Model.Users, { userName: payloadData.userName }, { _id: 1 }, { lean: true })
                requestPending = await getRequired(Model.Notifications, {
                    toId: mongoose.Types.ObjectId(userNameData[0]._id),
                    byId: userData._id,
                    type: "REQUEST_FOLLOW",
                    actionPerformed: false
                }, {}, { lean: true })
                if (requestPending.length) {
                    isRequestPending = true
                }
            }
            let pipeline = [
                {
                    $match: {
                        $or: [creden],
                        isDeleted: false,
                        isVerified: true,
                        _id: { $nin: userData.blockedBy }
                    }
                },
                {
                    $project: {
                        fullName: {
                            "$cond": [
                                {
                                    "$or": [
                                        {
                                            $cond: {
                                                if: { "$eq": ["$nameVisibilityForFollowers", true] }, then: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                        "then": true,
                                                        "else": false
                                                    }
                                                }, else: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$nameVisibility"] },
                                                        then: true,
                                                        else: false
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            "$cond": {
                                                "if": {
                                                    $and: [{
                                                        "$eq": ["$nameVisibilityForFollowers", false]
                                                    }, {
                                                        "$eq": [{ $size: "$nameVisibility" }, 0]
                                                    }]
                                                }, then: true, else: false
                                            }
                                        }
                                        // { "$in": [ mongoose.Types.ObjectId(userData._id), "$nameVisibility" ] }
                                    ]
                                },
                                // { $concat: [ { $substr: [ "$firstName", 0, 1 ] }, ".... ", { $substr: [ "$lastName", 0, 1 ] }, "...." ] },
                                "$firstName",
                                { $concat: [{ $substr: ["$firstName", 0, 1] }, ".... "] }
                            ]
                        },
                        // isBlocked: { $cond: { if: { "$in": [mongoose.Types.ObjectId(userData._id), "$blockedBy"] }, then: true, else: false } },
                        isOnline: "$isOnline",
                        interestTags: "$interestTags",
                        designation: "$designation",
                        userType: "$userType",
                        imageUrl: {
                            "$cond": [
                                {
                                    "$or": [
                                        {
                                            $cond: {
                                                if: { "$eq": ["$imageVisibilityForFollowers", false] }, then: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$imageVisibility"] },
                                                        then: true,
                                                        else: false
                                                    }
                                                }, else: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                        "then": true,
                                                        "else": false
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            "$cond": {
                                                "if": {
                                                    $and: [{
                                                        "$eq": ["$imageVisibilityForFollowers", false]
                                                    }, {
                                                        "$eq": [{ $size: "$imageVisibility" }, 0]
                                                    }]
                                                }, then: true, else: false
                                            }
                                        }
                                        // { "$in": [ mongoose.Types.ObjectId(userData._id), "$imageVisibility" ] }
                                    ]
                                },
                                "$imageUrl",
                                {
                                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                                }
                            ]
                        },
                        isPrivate: "$isPrivate",
                        bio: "$bio",
                        userName: {
                            "$cond": [
                                {
                                    "$or": [
                                        {
                                            $cond: {
                                                if: { "$eq": ["$nameVisibilityForFollowers", true] }, then: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                        "then": true,
                                                        "else": false
                                                    }
                                                }, else: {
                                                    "$cond": {
                                                        "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$nameVisibility"] },
                                                        then: true,
                                                        else: false
                                                    }
                                                }
                                            }
                                        },
                                        {
                                            "$cond": {
                                                "if": {
                                                    $and: [{
                                                        "$eq": ["$nameVisibilityForFollowers", false]
                                                    }, {
                                                        "$eq": [{ $size: "$nameVisibility" }, 0]
                                                    }]
                                                }, then: true, else: false
                                            }
                                        }
                                        // { "$in": [ mongoose.Types.ObjectId(userData._id), "$nameVisibility" ] }
                                    ]
                                },
                                "$userName",
                                { $substr: ["$userName", 0, 3] }
                            ]
                        },
                        email: "$email",
                        followingCount: { $size: "$following" },
                        followerCount: { $size: "$followers" },
                        isAccountPrivate: "$isAccountPrivate",
                        isEmailVerified: "$isEmailVerified",
                        isPhoneNumberVerified: "$isPhoneNumberVerified",
                        isUploaded: "$isUploaded",
                        isProfileComplete: "$isProfileComplete",
                        isFollowing: {
                            $cond: {
                                if: { $in: [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                then: true,
                                else: false
                            }
                        },
                        askForFollowBack: {
                            $cond: {
                                if: {
                                    $and: [
                                        // { $eq: ["$isFollowing", false] },
                                        { $eq: [{ $in: [mongoose.Types.ObjectId(userData._id), "$followers"] }, false] },
                                        { $in: [mongoose.Types.ObjectId(userData._id), "$following"] }
                                    ]
                                }, then: true, else: null
                            }
                        },
                        isBlocked: {
                            $cond: {
                                if: { $in: [mongoose.Types.ObjectId(userData._id), "$blockedBy"] },
                                then: true,
                                else: false
                            }
                        },
                        requestPending: {
                            $cond: { if: { $eq: [isRequestPending, true] }, then: true, else: false }
                        },
                        website: "$website",
                        phoneNumber: "$phoneNumber",
                        gender: "$gender",
                        countryCode: "$countryCode"
                    }
                }
            ]

            let populateData = [{
                path: "interestTags",
                select: "categoryName",
                model: "Categories"
            },
            {
                path: "totalSurveys"
            },
            ]

            let payloadUserData
            if (payloadData.userName) {
                payloadUserData = await getRequired(Model.Users, {
                    userName: payloadData.userName,
                    isDeleted: false
                }, { _id: 1 }, { lean: true })
                if (payloadUserData.length) {
                    payloadData.userId = payloadUserData[0]._id
                }
            }
            let checkConversation = await getRequired(Model.Chats, {
                $or: [{ $and: [{ senderId: userData._id }, { receiverId: payloadData.userId }] }, { $and: [{ senderId: payloadData.userId }, { receiverId: userData._id }] }],
                noChat: true
            }, { conversationId: 1 }, { lean: true })
            let data = await aggregateWithPopulate(Model.Users, pipeline, populateData)
            // console.log('data', data)
            if (data.length) {
                if (data[0].isProfileComplete) {
                    await maintainHistory(userData._id, payloadData.userId, payloadData.userName)
                }
                if (checkConversation.length) {
                    data[0].conversationId = checkConversation[0].conversationId
                } else {
                    data[0].conversationId = mongoose.Types.ObjectId()
                    await createData(Model.Chats, {
                        senderId: userData._id,
                        receiverId: payloadData.userId,
                        conversationId: data[0].conversationId,
                        noChat: true
                    })
                }
                return data[0]
            } else {
                return {}
            }
        } else {
            let pipeline = [
                {
                    $match: {
                        _id: userData._id,
                        isDeleted: false,
                        isVerified: true
                    }
                },
                {
                    $project: {
                        fullName: "$fullName",
                        isOnline: "$isOnline",
                        interestTags: "$interestTags",
                        designation: "$designation",
                        company: "$company",
                        imageUrl: "$imageUrl",
                        bio: "$bio",
                        userName: "$userName",
                        isEmailVerified: "$isEmailVerified",
                        isPhoneNumberVerified: "$isPhoneNumberVerified",
                        isUploaded: "$isUploaded",
                        email: "$email",
                        referralCode: "$referralCode",
                        followingCount: { $size: "$following" },
                        followerCount: { $size: "$followers" },
                        userType: "$userType",
                        pointEarned: { $ifNull: ["$pointEarned", 0] },
                        amount: { $multiply: ["$pointEarned", Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD] },
                        pointRedeemed: { $ifNull: ["$pointRedeemed", 0] },
                        website: "$website",
                        phoneNumber: "$phoneNumber",
                        gender: "$gender",
                        countryCode: "$countryCode"
                    }
                }
            ]

            let populateData = [{
                path: "interestTags",
                select: "categoryName",
                model: "Categories"
            }, {
                path: "imageVisibility",
                select: "userName imageUrl fullName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            },
            {
                path: "totalSurveys"
            }]

            let data = await aggregateWithPopulate(Model.Users, pipeline, populateData)
            if (data) {
                console.log(data);
                // return data[0]
                //  data[0].amount = data[0].pointEarned * Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT;

                data[0].pointEarned = Math.floor(data[0].pointEarned);
                data[0].pointRedeemed = Math.round(data[0].pointRedeemed);
                data[0].pointsPerUSD = 1 / Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD;

                return data[0];

            }
        }
    } catch (e) {
        console.log(e)
    }

}

/*=================Venue Group Related Apis==================*/
/*==============Venue Group Related Apis===================*/


/*=================Post Group Related Apis==================*/
/*==============Post Group Related Apis===================*/

/**
 * @description: fetching data according differnt Ids
 * @param {string} authorization
 * @param {string} search
 * @returns: fetched data
 */

let searchUser = async (payloadData, userData) => {
    try {
        let criteria = {
            isDeleted: false,
        };

        let project = {}
        if (payloadData.search) {
            criteria.userName = new RegExp(payloadData.search, 'i')
            criteria._id = { $ne: userData._id }
        }

        // if(userData.blockedWhom.length){
        //     criteria._id.$nin = userData.blockedWhom
        // }
        if (userData.blockedBy.length) {
            criteria._id.$nin = userData.blockedBy
        }

        let data = await getRequired(Model.Users, criteria, project, { lean: true })

        if (data.length) {
            let finalArrayToSend = []
            for (let user of data) {
                let tempObj = {}
                if (!user.tagPermissionForEveryone) {
                    if (user.tagPermissionForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                        tempObj.imageUrl = user.imageUrl
                    } else if (user.tagPermission.length && JSON.stringify(user.tagPermission).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                        tempObj.imageUrl = user.imageUrl
                    } else {
                        continue
                    }
                } else {
                    tempObj.userName = user.userName
                    tempObj.fullName = user.fullName
                    tempObj.imageUrl = user.imageUrl
                }

                if (!user.imageVisibilityForEveryone) {
                    if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = tempObj.imageUrl

                    } else if (user.imageVisibility && user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = tempObj.imageUrl
                    } else {

                        tempObj.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }
                    }
                } else {
                    tempObj.imageUrl = tempObj.imageUrl
                }

                if (!user.nameVisibilityForEveryone) {
                    if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = tempObj.userName
                        tempObj.fullName = tempObj.fullName

                    } else if (user.nameVisibility && user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = tempObj.userName
                        tempObj.fullName = tempObj.fullName
                    } else {
                        tempObj.userName = user.userName.substr(0, 3)
                        tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                    }
                } else {
                    tempObj.userName = tempObj.userName
                    tempObj.fullName = tempObj.fullName
                }
                finalArrayToSend.push(tempObj)
            }
            return finalArrayToSend
        } else {
            return []
        }
    } catch (e) {
        console.log(e)
    }

}

let searchTags = async (payloadData, userData) => {
    try {
        let criteria = {
            isDeleted: false,
        };

        let project = {
            tagName: 1,
            imageUrl: 1,
            // fullName:1
        }

        if (payloadData.search) criteria.$and = [
            { tagName: new RegExp(payloadData.search, 'i') },
            // {userName : {$ne: userData.userName}}
        ];

        let data = await getRequired(Model.Tags, criteria, project, { lean: true })
        if (data.length) {
            return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: creating and editinhg post group info
 * @param {string} authorization
 * @param {string} postGroupId
 * @param {string} groupName
 * @param {string} categoryId
 * @param {double} isPrivate
 * @param {string} groupImageOriginal
 * @param {string} groupImageThumbnail
 * @returns: saved data
 */

let addEditPostGroup = async (payloadData, userData) => {
    try {
        let query = {}
        if (payloadData.groupName)
            query.groupName = payloadData.groupName
        if (payloadData.categoryId)
            query.categoryId = payloadData.categoryId
        if (payloadData.isPrivate === 1)
            query.isPrivate = true
        if (payloadData.isPrivate === 2)
            query.isPrivate = false
        if (payloadData.members)
            query.members = payloadData.members
        if (payloadData.description)
            query.description = payloadData.description

        if (payloadData.postGroupId) {
            let criteria = {
                _id: payloadData.postGroupId,
                isDeleted: false
            }
            if (payloadData.groupImageOriginal && payloadData.groupImageThumbnail) {
                query.imageUrl = {
                    original: payloadData.groupImageOriginal,
                    thumbnail: payloadData.groupImageThumbnail
                }
            }
            let data = await updateData(Model.PostGroups, criteria, { $set: query }, { new: true, lean: true })
            if (data) {
                return {
                    _id: data._id,
                    imageUrl: data.imageUrl,
                    groupName: data.groupName,
                    createdOn: data.createdOn,
                    isMember: data.isMember,
                    memberCounts: data.memberCount,
                    isPrivate: data.isPrivate,
                    categoryId: data.categoryId,
                    adminId: data.adminId
                }
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        } else {
            query.adminId = userData._id
            query.createdOn = +new Date()
            query.isMember = [userData._id]
            query.createdBy = userData._id
            query.memberCounts = 1
            query.conversationId = mongoose.Types.ObjectId()
            if (payloadData.groupImageOriginal && payloadData.groupImageThumbnail) {
                query.imageUrl = {
                    original: payloadData.groupImageOriginal,
                    thumbnail: payloadData.groupImageThumbnail
                }
            } else {
                query.imageUrl = {
                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.GROUP.ORIGINAL,
                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.GROUP.THUMBNAIL
                }
            }

            let data = await Service.saveData(Model.PostGroups, query)
            if (data) {
                socketManager.joinUserToGroupOnlogin([data._id], userData._id)
                let query = {
                    groupId: data._id,
                    userId: userData._id,
                    isAdmin: true,
                    joinedAt: +new Date()
                }
                await Service.saveData(Model.PostGroupMembers, query)
                if (!payloadData.participantIds) {
                    payloadData.participantIds = []
                }
                if (payloadData.participantIds.length) {
                    let criteria = {}
                    if (payloadData.groupId) {
                        criteria.groupId = data._id
                    }
                    for (let participant of payloadData.participantIds) {
                        criteria.toId = mongoose.Types.ObjectId(participant)
                        criteria.groupId = data._id
                        criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP
                        criteria.groupType = "GROUP"
                        let check = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                        if (check.length) {
                            await updateData(Model.Notifications, criteria, {
                                $set: {
                                    isDeleted: false,
                                    actionPerformed: false,
                                    isRejected: false,
                                    isRead: false,
                                    createdOn: +new Date
                                }
                            }, { new: true, lean: true })
                            let toIdData = await getRequired(Model.Users, {
                                _id: participant,
                                isDeleted: false
                            }, {}, { lean: true })
                            let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId }, {}, { lean: true })
                            let pushData = {
                                id: payloadData.groupId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                                msg: userData.fullName + ' requested you to join ' + groupData[0].groupName + ' channel'
                            };
                            socketManager.requestCount(participant)
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                            continue
                        }
                        let query = {
                            groupId: data._id,
                            toId: participant,
                            text: userData.fullName + ' requested you to join ' + groupData[0].groupName + ' channel',
                            byId: userData._id,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                            groupType: "GROUP",
                            createdOn: +new Date,
                            actionPerformed: false
                        }
                        let notData = await createData(Model.Notifications, query)
                        if (notData) {
                            let toIdData = await getRequired(Model.Users, { _id: participant }, {}, { lean: true })
                            let byIdData = await getRequired(Model.Users, { _id: userData._id }, {}, { lean: true })
                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                                msg: byIdData[0].fullName + ' requested you to join ' + payloadData.groupName + ' channel'
                            };
                            socketManager.requestCount(participant)
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        }
                    }
                }

                return {
                    _id: data._id,
                    imageUrl: data.imageUrl,
                    groupName: data.groupName,
                    createdOn: data.createdOn,
                    isMember: data.isMember,
                    memberCounts: data.memberCount,
                    isPrivate: data.isPrivate,
                    categoryId: data.categoryId,
                    adminId: data.adminId
                }
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: searching post groups on the basis of their names
 * @param {string} authorization
 * @param {string} search
 * @returns: success
 */

let searchPostGroup = async (payloadData, userData) => {
    try {
        let criteria = {
            isDeleted: false,
        };

        let project = {
            groupName: 1,
            imageUrl: 1,
            memberCounts: 1
        }

        if (payloadData.search) criteria.$or = [
            { groupName: new RegExp(payloadData.search, 'i') }
        ];

        let data = await getRequired(Model.PostGroups, criteria, project, { lean: true })
        if (data.length) {
            return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: details chat conversation of a particular group
 * @param {string} authorization
 * @param {string} groupId
 * @param {double} pageNo
 * @param {double} limit
 * @returns: fetched chat data
 */

let postGroupConversation = async (payloadData, userData) => {
    try {
        let checkGroupExistance = await getRequired(Model.PostGroups, {
            _id: payloadData.groupId,
            isDeleted: false
        }, {}, { lean: true })
        if (!checkGroupExistance.length) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.GROUP_DELETED)
        }
        let notification, groupName, imageUrl, description
        let pipeline = [{
            $match: {
                groupId: mongoose.Types.ObjectId(payloadData.groupId),
                postBy: { "$nin": userData.blockedBy },
                isDeleted: false
            }
        }, {
            $project: {
                _id: 1,
                groupId: "$groupId",
                postBy: 1,
                postText: 1,
                media: 1,
                commentCount: 1,
                postCategoryId: 1,
                location: 1,
                locationAddress: 1,
                locationName: 1,
                type: 1,
                imageUrl: 1,
                likeCount: { $size: "$likes" },
                liked: {
                    $cond: { if: { $in: [mongoose.Types.ObjectId(userData._id), "$likes"] }, then: true, else: false }
                },
                createdOn: 1
            }
        }, {
            $sort: {
                createdOn: -1
            }
        }, {
            $skip: (payloadData.pageNo - 1) * payloadData.limit
        }, {
            $limit: payloadData.limit
        }]

        let populate = [{
            path: "postBy",
            select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
            model: "Users",
            populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                path: "imageVisibility",
                select: "userName imageUrl fullName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
        }, {
            path: "groupId",
            select: "groupName imageUrl",
            model: "PostGroups"
        }, { path: "postCategoryId", select: "categoryName", model: "Categories" }]
        let conversData = await aggregateWithPopulate(Model.Posts, pipeline, populate)

        let userNotify = await getRequired(Model.PostGroupMembers, {
            groupId: payloadData.groupId,
            userId: userData._id
        }, { isNotify: 1 }, { lean: true })
        if (userNotify.length) {
            notification = userNotify[0].isNotify
        }
        Service.update(Model.Posts, {
            groupId: payloadData.groupId,
            readBy: { $nin: [mongoose.Types.ObjectId(userData._id)] }
        }, { $addToSet: { readBy: userData._id } }, { new: true, lean: true, multi: true })

        let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId, isDeleted: false }, {
            groupName: 1,
            description: 1,
            imageUrl: 1
        }, { lean: true })
        if (groupData.length) {
            groupName = groupData[0].groupName
            imageUrl = groupData[0].imageUrl
            description = groupData[0].description
        }
        if (conversData.length) {
            for (let a of conversData) {
                a.postBy = a.postBy.toObject()
                if (JSON.stringify(a.postBy._id) == JSON.stringify(userData._id)) {
                    continue
                }
                if (!a.postBy.imageVisibilityForEveryone) {
                    if (a.postBy.imageVisibilityForFollowers) {
                        a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                    } else if (a.postBy.imageVisibility && a.postBy.imageVisibility.length) {
                        a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                    }
                }

                if (!a.postBy.nameVisibilityForEveryone) {
                    if (a.postBy.nameVisibilityForFollowers) {
                        a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                    } else if (a.postBy.nameVisibility && a.postBy.nameVisibility.length) {
                        a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                    }
                }
                // if(!a.postBy.imageVisibilityForFollowers && !a.postBy.nameVisibilityForFollowers && !a.postBy.imageVisibility.length && !a.postBy.nameVisibility.length){
                //     continue
                // }
                // a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                // a.postBy = await nameVisibilityManipulation(a.postBy, userData)
            }
        }

        return {
            conversData: conversData || [],
            groupName: groupName,
            imageUrl: imageUrl,
            description: description,
            isMember: true,
            notification: notification
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description:detailed particular post with comment an dreples details
 * @param {string} authorization
 * @param {string} postId
 * @returns: fetched post data
 */

async function getMediaComments(payloadData, userData) {
    try {
        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }
        if (!payloadData.limit) {
            payloadData.limit = 10
        }
        let postInfo = {};

        let pipeline = [
            {
                $match: { mediaId: mongoose.Types.ObjectId(payloadData.mediaId), isDeleted: false },
            },
            {
                $addFields: {
                    "liked": {
                        $cond: { if: { $in: [mongoose.Types.ObjectId(userData._id), "$likes"] }, then: true, else: false }
                    }
                }
            },
            {
                $lookup: {
                    "from": "mediareplies",
                    "let": { mediaIdWeHave: payloadData.mediaId },
                    "pipeline": [{
                        "$match": {
                            "$expr": {
                                $and: [
                                    { "$eq": ["$isDeleted", false] },
                                    { $eq: ["$mediaId", "$$mediaIdWeHave"] }
                                ]
                            }
                        }
                    }],
                    "as": "replyInfo"
                }
            },
            { $addFields: { "replyCount": { $size: '$replyInfo' } } }
        ];

        let commentInfo = await Model.MediaComments.aggregate(pipeline);
        // add like count

        let commentWithPopulate = await Model.MediaComments.populate(commentInfo, {
            "path": "commentBy",
            "select": "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone",
            "model": "Users",
            populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                path: "imageVisibility",
                select: "userName imageUrl fullName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
        });


        if (payloadData.postId) {
            postInfo = await Model.Posts.findById(payloadData.postId).populate({
                "path": "postBy",
                "select": "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone",
                "model": "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }).populate({
                "path": "selectInterests",
                "model": "Categories"
            }).lean();

            // delete like count
            delete postInfo.likeCount;
            delete postInfo.likes;

            // sort media
            postInfo.media.sort((x, y) => {
                return y.likeCount - x.likeCount
            });

            // add new key
            if (postInfo.media[0].likeCount != 0)
                postInfo.media[0].isMostLiked = true;

            // filter media
            let mediaFilter = postInfo.media.filter((val) => val._id == payloadData.mediaId);
            console.log(mediaFilter);

            postInfo.media = mediaFilter;

            // add likecount and likes
            if (mediaFilter.length > 0) {
                postInfo.likeCount = postInfo.media[0].likeCount;
                postInfo.likes = postInfo.media[0].likes;
            }

            const test = postInfo.likes.map(v => {
                return v.toString()
            });
            let likeIndex = test.indexOf(userData._id.toString());
            if (likeIndex == -1)
                postInfo.liked = false;
            else
                postInfo.liked = true;
        }

        return {
            ...postInfo,
            comment: commentWithPopulate,
            commentCount: commentWithPopulate.length
        };
    } catch (e) {
        console.log(e);
    }

}

let getPostWithComment = async (payloadData, userData) => {
    console.log("PAYLOAD ", payloadData);
    try {
        if (payloadData.mediaId) {
            return await getMediaComments(payloadData, userData);
        } else {
            if (!payloadData.pageNo) {
                payloadData.pageNo = 1
            }
            if (!payloadData.limit) {
                payloadData.limit = 10
            }

            let pipeline = [{
                $match: {
                    _id: mongoose.Types.ObjectId(payloadData.postId)
                }
            }, {
                $lookup: {
                    "from": "comments",
                    "localField": "_id",
                    "foreignField": "postId",
                    "as": "commentInfo"
                }
            }, {
                $unwind: {
                    "path": "$commentInfo",
                    "preserveNullAndEmptyArrays": true
                }
            }, {
                $match: {
                    "commentInfo.commentBy": {
                        $nin: userData.blockedBy
                    }
                }
            },
            {
                $lookup: {
                    "from": "replies",
                    "let": { commentIdWeHave: "$commentInfo._id" },
                    "pipeline": [{
                        "$match": {
                            "$expr": {
                                $and: [
                                    { "$eq": ["$isDeleted", false] },
                                    { $eq: ["$commentId", "$$commentIdWeHave"] }
                                ]
                            }
                        }
                    }],
                    "as": "replyInfo"
                }
            }
                // {
                //     $lookup: {
                //         "from" : "replies",
                //         "localField": "commentInfo._id",
                //         "foreignField": "commentId",
                //         "as": "replyInfo"
                //     }
                // }
                , {
                $project: {
                    imageUrl: 1,
                    postText: 1,
                    hashTags: 1,
                    createdOn: 1,
                    // commentCount:1,
                    likeCount: { $size: "$likes" },
                    type: 1,
                    media: 1,
                    groupId: 1,
                    postCategoryId: 1,
                    postBy: 1,
                    likes: 1,
                    // commentInfo: 1,
                    postType: 1,
                    postingIn: 1,
                    selectedPeople: 1,
                    selectInterests: 1,
                    location: 1,
                    locationName: 1,
                    locationAddress: 1,
                    meetingTime: 1,
                    expirationTime: 1,
                    liked: {
                        $cond: {
                            if: { $in: [mongoose.Types.ObjectId(userData._id), "$likes"] },
                            then: true,
                            else: false
                        }
                    },
                    replyCount: { $size: "$replyInfo" },
                    // replyCount: {
                    //     $sum: {$cond: [
                    //             {$and:
                    //                 [

                    //                 {$eq: [ "$replyInfo.isDeleted", true]}
                    //             ]}, 0, 1]}
                    //     },
                    commentInfo: { $ifNull: ["$commentInfo", { "likes": [], "nocomment": true }] }
                }
            }, {
                $addFields: {
                    "commentInfo.liked": {
                        $cond: {
                            if: { $in: [mongoose.Types.ObjectId(userData._id), "$commentInfo.likes"] },
                            then: true,
                            else: false
                        }
                    }
                }
            },
            // {
            //     $sort: {
            //         "commentInfo.createdOn": -1
            //     }
            // },
            // {
            //     $skip: (payloadData.pageNo - 1) * payloadData.limit
            // },{
            //     $limit: payloadData.limit
            // },{
            //     $sort: {
            //         "commentInfo.createdOn": 1
            //     }
            // },
            {
                $group: {
                    _id: "$_id",
                    imageUrl: { $last: "$imageUrl" },
                    postText: { $last: "$postText" },
                    hashTags: { $last: "$hashTags" },
                    createdOn: { $last: "$createdOn" },
                    // commentCount: {$last: "$commentCount"},
                    // commentInfoSize: {$sum:1},
                    likeCount: { $last: "$likeCount" },
                    type: { $last: "$type" },
                    media: { $last: "$media" },
                    // media: "$media",
                    groupId: { $last: "$groupId" },
                    liked: { $last: "$liked" },
                    postCategoryId: { $last: "$postCategoryId" },
                    postBy: { $last: "$postBy" },
                    postType: { $last: "$postType" },
                    postingIn: { $last: "$postingIn" },
                    selectedPeople: { $last: "$selectedPeople" },
                    selectInterests: { $last: "$selectInterests" },
                    location: { $last: "$location" },
                    locationName: { $last: "$locationName" },
                    locationAddress: { $last: "$locationAddress" },
                    meetingTime: { $last: "$meetingTime" },
                    expirationTime: { $last: "$expirationTime" },
                    comment: {
                        $push: {
                            "_id": "$commentInfo._id",
                            "comment": "$commentInfo.comment",
                            "userIdTag": "$commentInfo.userIdTag",
                            "createdOn": "$commentInfo.createdOn",
                            "likeCount": { $size: "$commentInfo.likes" },
                            // "likeCount": "$commentInfo.likeCount",
                            "commentBy": "$commentInfo.commentBy",
                            "liked": "$commentInfo.liked",
                            "isDeleted": "$commentInfo.isDeleted",
                            "replyCount": "$replyCount",
                            "attachmentUrl": "$commentInfo.attachmentUrl"
                        }
                    },
                }
            }, {
                $project: {
                    _id: "$_id",
                    imageUrl: "$imageUrl",
                    postText: "$postText",
                    hashTags: "$hashTags",
                    createdOn: "$createdOn",
                    // commentCount: "$commentCount",
                    // remainingComments: { $subtract: [ "$commentCount", { $multiply: [ payloadData.pageNo, payloadData.limit ] } ] },
                    likeCount: "$likeCount",
                    type: "$type",
                    groupId: "$groupId",
                    liked: "$liked",
                    postCategoryId: "$postCategoryId",
                    postBy: "$postBy",
                    postType: "$postType",
                    postingIn: "$postingIn",
                    selectedPeople: "$selectedPeople",
                    selectInterests: "$selectInterests",
                    location: "$location",
                    locationName: "$locationName",
                    locationAddress: "$locationAddress",
                    meetingTime: "$meetingTime",
                    expirationTime: "$expirationTime",
                    //media: "$media",
                    // comment: {$cond: {if: {$eq:[ 0, "$commentCount"]}, then: null, else: "$comment"}}
                    media: {
                        $cond: {
                            if: {
                                $eq: [null, "$media"]
                            }, then: [], else: "$media"
                        }
                    },
                    comment: {
                        $cond: {
                            if: {
                                $eq: [0, "$commentCount"]
                            }, then: null, else: {
                                $filter: {
                                    input: "$comment",
                                    as: "item",
                                    cond: {
                                        $eq: ["$$item.isDeleted", false]
                                    }
                                }
                            }
                        }
                    }
                }
            }]

            let populate = [{
                "path": "postBy",
                "select": "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone",
                "model": "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }, {
                "path": "comment.commentBy",
                "select": "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone",
                "model": "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }, {
                "path": "postCategoryId",
                "select": "categoryName",
                "model": "Categories",
            }, {
                "path": "groupId",
                "select": "groupName",
                "model": "PostGroups"
            }, {
                "path": "selectInterests",
                "model": "Categories"
            }]
            let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
            console.log(data[0].comment)
            if (data.length) {
                let commentCount = await Service.count(Model.Comments, {
                    postId: payloadData.postId,
                    isDeleted: false,
                    commentBy: { "$nin": userData.blockedBy }
                })
                let repliesCount = await Service.count(Model.Replies, { postId: payloadData.postId, isDeleted: false })
                data[0].commentCount = commentCount + repliesCount
                if (JSON.stringify(data[0].postBy._id) != JSON.stringify(userData._id)) {
                    if (!data[0].postBy.imageVisibilityForEveryone) {
                        if (data[0].postBy.imageVisibilityForFollowers) {
                            data[0].postBy = await imageVisibilityManipulation(data[0].postBy, userData)
                        } else if (data[0].postBy.imageVisibility && data[0].postBy.imageVisibility.length) {
                            data[0].postBy = await imageVisibilityManipulation(data[0].postBy, userData)
                        }
                    }

                    if (!data[0].postBy.nameVisibilityForEveryone) {
                        if (data[0].postBy.nameVisibilityForFollowers) {
                            data[0].postBy = await nameVisibilityManipulation(data[0].postBy, userData)
                        } else if (data[0].postBy.nameVisibility && data[0].postBy.nameVisibility.length) {
                            data[0].postBy = await nameVisibilityManipulation(data[0].postBy, userData)
                        }
                    }
                    // if(data[0].postBy.imageVisibilityForFollowers || data[0].postBy.nameVisibilityForFollowers || data[0].postBy.imageVisibility.length || data[0].postBy.nameVisibility.length){
                    //     data[0].postBy = await imageVisibilityManipulation(data[0].postBy, userData)
                    //     data[0].postBy = await nameVisibilityManipulation(data[0].postBy, userData)
                    // }

                }
                if (data[0].comment && data[0].comment.length) {
                    for (let a of data[0].comment) {
                        a.commentBy = a.commentBy.toObject()
                        if (JSON.stringify(a.commentBy._id) == JSON.stringify(userData._id)) {
                            continue
                        }
                        if (!a.commentBy.imageVisibilityForEveryone) {
                            if (a.commentBy.imageVisibilityForFollowers) {
                                a.commentBy = await imageVisibilityManipulation(a.commentBy, userData)
                            } else if (a.commentBy.imageVisibility && a.commentBy.imageVisibility.length) {
                                a.commentBy = await imageVisibilityManipulation(a.commentBy, userData)
                            }
                        }

                        if (!a.commentBy.nameVisibilityForEveryone) {
                            if (a.commentBy.nameVisibilityForFollowers) {
                                a.commentBy = await nameVisibilityManipulation(a.commentBy, userData)
                            } else if (a.commentBy.nameVisibility && a.commentBy.nameVisibility.length) {
                                a.commentBy = await nameVisibilityManipulation(a.commentBy, userData)
                            }
                        }
                        // if(!a.commentBy.imageVisibilityForFollowers && !a.commentBy.nameVisibilityForFollowers && !a.commentBy.imageVisibility.length && !a.commentBy.nameVisibility.length){
                        //     continue
                        // }
                        // a.commentBy = await imageVisibilityManipulation(a.commentBy, userData)
                        // a.commentBy = await nameVisibilityManipulation(a.commentBy, userData)
                    }
                }

                return data[0]
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let getMediaCommentReplies = async (payloadData, userData, skipLimit) => {
    try {
        var data;
        if (payloadData.replyId) {
            let replyData = await getRequired(Model.MediaReplies, { _id: payloadData.replyId }, { createdOn: 1 }, { lean: true })
            let pipeline = [{
                $match: {
                    "commentId": mongoose.Types.ObjectId(payloadData.commentId),
                    "createdOn": { $lt: replyData[0].createdOn },
                    isDeleted: false
                }
            }, {
                $group: {
                    _id: "$commentId",
                    replies: {
                        $push: {
                            _id: "$_id",
                            reply: "$reply",
                            userIdTag: "$userIdTag",
                            createdOn: "$createdOn",
                            likeCount: { $size: "$likes" },
                            // likeCount: "$likeCount",
                            replyBy: "$replyBy",
                            liked: {
                                $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                            }
                        }
                    }
                }
            }, {
                $project: {
                    _id: 1,
                    replies: { $slice: ["$replies", -10, 10] }
                }
            }]

            let populate = [{
                path: "replies.replyBy",
                select: "fullName userName imageUrl",
                model: "Users"
            }]


            data = await aggregateWithPopulate(Model.MediaReplies, pipeline, populate)
        } else {
            let pipeline = [{
                $match: {
                    "commentId": mongoose.Types.ObjectId(payloadData.commentId),
                    isDeleted: false
                }
            }, {
                $skip: skipLimit
            }, {
                $group: {
                    _id: "$commentId",
                    replies: {
                        $push: {
                            _id: "$_id",
                            reply: "$reply",
                            userIdTag: "$userIdTag",
                            createdOn: "$createdOn",
                            // likeCount: "$likeCount",
                            likeCount: { $size: "$likes" },
                            replyBy: "$replyBy",
                            liked: {
                                $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                            }
                        }
                    }
                }
            }]
            let populate = [{
                path: "replies.replyBy",
                select: "fullName userName imageUrl",
                model: "Users"
            }]

            data = await aggregateWithPopulate(Model.MediaReplies, pipeline, populate)
        }
        if (data.length)
            return data[0].replies
    } catch (err) {
        console.log(err);
    }
}

/**
 * @description replies under the comments
 * @param {string} authorization
 * @param {string} commentId
 * @param {string} replyId
 * @param {string} totalReply
 * @returns the array of the replies
 */
let getCommentReplies = async (payloadData, userData) => {
    try {
        var skipLimit;
        var data;
        if ((payloadData.totalReply - 10) < 0) {
            skipLimit = 0
        } else {
            skipLimit = (payloadData.totalReply - 10)
        }
        if (payloadData.mediaId) {
            return await getMediaCommentReplies(payloadData, userData, skipLimit);
        }

        if (payloadData.replyId) {
            let replyData = await getRequired(Model.Replies, { _id: payloadData.replyId }, { createdOn: 1 }, { lean: true })
            let pipeline = [{
                $match: {
                    "commentId": mongoose.Types.ObjectId(payloadData.commentId),
                    "createdOn": { $lt: replyData[0].createdOn },
                    isDeleted: false
                }
            }, {
                $group: {
                    _id: "$commentId",
                    replies: {
                        $push: {
                            _id: "$_id",
                            reply: "$reply",
                            userIdTag: "$userIdTag",
                            createdOn: "$createdOn",
                            likeCount: { $size: "$likes" },
                            // likeCount: "$likeCount",
                            replyBy: "$replyBy",
                            liked: {
                                $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                            }
                        }
                    }
                }
            }, {
                $project: {
                    _id: 1,
                    replies: { $slice: ["$replies", -10, 10] }
                }
            }]

            let populate = [{
                path: "replies.replyBy",
                select: "fullName userName imageUrl",
                model: "Users"
            }]


            data = await aggregateWithPopulate(Model.Replies, pipeline, populate)
        } else {
            let pipeline = [{
                $match: {
                    "commentId": mongoose.Types.ObjectId(payloadData.commentId),
                    isDeleted: false
                }
            }, {
                $skip: skipLimit
            }, {
                $group: {
                    _id: "$commentId",
                    replies: {
                        $push: {
                            _id: "$_id",
                            reply: "$reply",
                            userIdTag: "$userIdTag",
                            createdOn: "$createdOn",
                            // likeCount: "$likeCount",
                            likeCount: { $size: "$likes" },
                            replyBy: "$replyBy",
                            liked: {
                                $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                            }
                        }
                    }
                }
            }]
            let populate = [{
                path: "replies.replyBy",
                select: "fullName userName imageUrl",
                model: "Users"
            }]

            data = await aggregateWithPopulate(Model.Replies, pipeline, populate)
        }
        if (data.length)
            return data[0].replies
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description:detailed particular post with comment an dreples details
 * @param {string} authorization
 * @param {string} categoryId
 * @param {double} pageNo
 * @param {double} limit
 * @returns: fetched post data
 */

let getCatPostGroups = async (payloadData, userData) => {
    try {
        let notArrayRejected = [], notArrayPending = []
        let requestGroup = await getRequired(Model.Notifications, {
            byId: userData._id,
            groupType: "GROUP",
            type: "REQUEST_GROUP"
        }, {}, { lean: true })

        if (requestGroup.length) {
            for (let req of requestGroup) {
                if (req.isRejected && req.actionPerformed) {
                    notArrayRejected.push(req.groupId)
                    continue
                }
                if (!req.actionPerformed && !req.isRejected) {
                    notArrayPending.push(req.groupId)
                }
            }
        }
        let pipeline = [{
            $match: {
                categoryId: mongoose.Types.ObjectId(payloadData.categoryId),
                isDeleted: false
            }
        }, {
            $project: {
                imageUrl: 1,
                groupName: 1,
                adminId: 1,
                memberCounts: 1,
                isPrivate: 1,
                isMember: {
                    $cond: { if: { $in: [mongoose.Types.ObjectId(userData._id), "$isMember"] }, then: true, else: false }
                },
                requestStatus: {
                    $cond: {
                        if: { $in: ["$_id", notArrayPending] },
                        then: "PENDING",
                        else: {
                            $cond: {
                                if: {
                                    $in: ["$_id", notArrayRejected]
                                },
                                then: "REJECTED",
                                else: "NONE"
                            }
                        }
                    }
                }
            }
        }, {
            $sort: {
                createdOn: -1
            }
        }, {
            $skip: (payloadData.pageNo - 1) * payloadData.limit
        }, {
            $limit: payloadData.limit
        }]
        let data = await aggregateData(Model.PostGroups, pipeline)
        if (data)
            return data
    } catch (e) {
        console.log(e)
    }
}

let homeSearchTop = async (payloadData, userData) => {
    try {
        let criteria = {}
        criteria._id = {}
        criteria._id.$ne = userData._id
        criteria.isDeleted = false
        // if(userData.blockedWhom.length){
        //     criteria._id.$nin = userData.blockedWhom
        // }

        if (userData.blockedBy.length) {
            criteria._id.$nin = userData.blockedBy
        }

        if (!payloadData.search) {
            criteria._id = mongoose.Types.ObjectId(userData._id)
            let data = await getRequiredPopulate(Model.Users, criteria, { homeSearchTop: 1 }, { lean: true }, [{
                path: "homeSearchTop",
                select: "userName imageUrl fullName isAccountPrivate imageVisibilityForEveryone nameVisibilityForEveryone imageVisibilityForFollowers imageVisibility nameVisibility nameVisibilityForFollowers followers firstName",
                model: "Users"
            }])
            if (data && data.length && data[0].homeSearchTop.length && payloadData.pageNo === 1) {
                var finalArray = []
                for (let user of data[0].homeSearchTop) {
                    if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(user._id))) {
                        continue
                    }
                    let tempObj = {}
                    if (!user.imageVisibilityForEveryone) {
                        if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl

                        } else if (user.imageVisibility && user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else {

                            tempObj.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                        }
                    } else {
                        tempObj.imageUrl = user.imageUrl
                    }

                    if (!user.nameVisibilityForEveryone) {
                        if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName

                        } else if (user.nameVisibility && user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else {
                            tempObj.userName = user.userName.substr(0, 3)
                            tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                        }
                    } else {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    }
                    tempObj.isAccountPrivate = user.isAccountPrivate
                    tempObj._id = user._id
                    finalArray.push(tempObj)
                }
                if (!finalArray.length) {
                    let criteriaForNew = {}
                    criteriaForNew._id = {}
                    // if(userData.blockedWhom.length){
                    //     criteriaForNew._id.$nin = userData.blockedWhom
                    // }

                    if (userData.blockedBy.length) {
                        criteriaForNew._id.$nin = userData.blockedBy
                    }
                    if (userData._id) {
                        criteriaForNew._id.$ne = userData._id
                    }
                    criteriaForNew.isProfileComplete = true
                    let newUserShowData = await getRequired(Model.Users, criteriaForNew, {}, {
                        lean: true,
                        skip: (payloadData.pageNo - 1) * 10,
                        limit: 10,
                        sort: { _id: -1 }
                    })
                    if (newUserShowData.length) {
                        let finalArray = []
                        for (let user of newUserShowData) {
                            let tempObj = {}
                            if (!user.imageVisibilityForEveryone) {
                                if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                                    tempObj.imageUrl = user.imageUrl

                                } else if (user.imageVisibility && user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                                    tempObj.imageUrl = user.imageUrl
                                } else {

                                    tempObj.imageUrl = {
                                        "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                        "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                                    }
                                }
                            } else {
                                tempObj.imageUrl = user.imageUrl
                            }

                            if (!user.nameVisibilityForEveryone) {
                                if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                                    tempObj.userName = user.userName
                                    tempObj.fullName = user.fullName

                                } else if (user.nameVisibility && user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                                    tempObj.userName = user.userName
                                    tempObj.fullName = user.fullName
                                } else {
                                    tempObj.userName = user.userName.substr(0, 3)
                                    tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                                }
                            } else {
                                tempObj.userName = user.userName
                                tempObj.fullName = user.fullName
                            }
                            tempObj.isAccountPrivate = user.isAccountPrivate
                            tempObj._id = user._id

                            finalArray.push(tempObj)
                        }
                        return finalArray
                    }
                }
                return finalArray
            } else if (data && data.length && data[0].homeSearchTop.length && payloadData.pageNo > 1) {
                return []
            } else {
                let criteriaForNew = {}
                criteriaForNew._id = {}
                // if(userData.blockedWhom.length){
                //     criteriaForNew._id.$nin = userData.blockedWhom
                // }

                if (userData.blockedBy.length) {
                    criteriaForNew._id.$nin = userData.blockedBy
                }
                if (userData._id) {
                    criteriaForNew._id.$ne = userData._id
                }
                criteriaForNew.isProfileComplete = true
                let newUserShowData = await getRequired(Model.Users, criteriaForNew, {}, {
                    lean: true,
                    skip: (payloadData.pageNo - 1) * 10,
                    limit: 10,
                    sort: { _id: -1 }
                })
                if (newUserShowData.length) {
                    let finalArray = []
                    for (let user of newUserShowData) {
                        let tempObj = {}
                        if (!user.imageVisibilityForEveryone) {
                            if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                                tempObj.imageUrl = user.imageUrl

                            } else if (user.imageVisibility && user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                                tempObj.imageUrl = user.imageUrl
                            } else {

                                tempObj.imageUrl = {
                                    "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                    "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                                }
                            }
                        } else {
                            tempObj.imageUrl = user.imageUrl
                        }

                        if (!user.nameVisibilityForEveryone) {
                            if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                                tempObj.userName = user.userName
                                tempObj.fullName = user.fullName

                            } else if (user.nameVisibility && user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                                tempObj.userName = user.userName
                                tempObj.fullName = user.fullName
                            } else {
                                tempObj.userName = user.userName.substr(0, 3)
                                tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                            }
                        } else {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        }
                        tempObj.isAccountPrivate = user.isAccountPrivate
                        tempObj._id = user._id

                        finalArray.push(tempObj)
                    }
                    return finalArray
                }
            }
        } else {
            if (!payloadData.pageNo) {
                payloadData.pageNo = 1
            }

            if (payloadData.search) {
                criteria.$and = [
                    { userName: new RegExp(payloadData.search, 'i') },
                    { userName: { $ne: userData.userName } }
                ]
            }
            console.log(criteria)
            let data = await getRequired(Model.Users, criteria, {}, {
                lean: true,
                skip: (payloadData.pageNo - 1) * 10,
                limit: 10
            })
            if (data) {
                let finalArray = []
                for (let user of data) {
                    let tempObj = {}
                    if (!user.imageVisibilityForEveryone) {
                        if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl

                        } else if (user.imageVisibility && user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else {

                            tempObj.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                        }
                    } else {
                        tempObj.imageUrl = user.imageUrl
                    }

                    if (!user.nameVisibilityForEveryone) {
                        if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName

                        } else if (user.nameVisibility && user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else {
                            tempObj.userName = user.userName.substr(0, 3)
                            tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                        }
                    } else {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    }
                    tempObj.isAccountPrivate = user.isAccountPrivate
                    tempObj._id = user._id

                    finalArray.push(tempObj)
                }
                return finalArray
            } else {
                return []
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let homeSearchTag = async (payloadData, userData) => {
    try {
        let criteria = {}
        criteria.isDeleted = false
        criteria._id = userData._id
        let tagData = await getRequired(Model.Users, criteria, { tagsFollowed: 1 }, { lean: true })
        if (!payloadData.search || payloadData.search == "#") {

            let pipeline = [{
                $project: {
                    _id: 1,
                    tagName: 1,
                    imageUrl: 1,
                    isFollowing: { $in: ["$_id", tagData[0].tagsFollowed] }
                }
            }, { $sort: { _id: -1 } }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }]

            let tagSearchedData = await aggregateData(Model.Tags, pipeline)
            if (tagSearchedData.length) {
                return tagSearchedData
            } else {
                return []
            }

        } else {
            if (!payloadData.pageNo) {
                payloadData.pageNo = 1
            }
            let pipeline = [{
                $match: {
                    tagName: new RegExp(payloadData.search, 'i')
                }
            }, {
                $project: {
                    tagName: 1,
                    imageUrl: 1,
                    isFollowing: { $in: ["$_id", tagData[0].tagsFollowed] }
                }
            }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }]
            // if(payloadData.search){
            //     criteria.$and = [
            //         {tagName : new RegExp(payloadData.search,'i')}
            //         ]
            // }
            // let data = await getRequired(Model.Tags, criteria, {tagName:1, imageUrl:1}, {lean:true})
            let data = await aggregateData(Model.Tags, pipeline)
            if (data) {
                return data
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let followUnfollowTag = async (payloadData, userData) => {
    try {
        let criteria = {}, dataToSet = {}
        criteria._id = userData._id
        criteria.isDeleted = false

        if (payloadData.follow) {
            if (payloadData.tagId) {
                dataToSet.$addToSet = {
                    tagsFollowed: mongoose.Types.ObjectId(payloadData.tagId)
                }
            }
            let dataAfterUpdate = await updateData(Model.Users, criteria, dataToSet, { new: true, lean: true })
            if (dataAfterUpdate) {
                return dataAfterUpdate
            }


        } else {
            if (payloadData.tagId) {
                dataToSet.$pull = {
                    tagsFollowed: payloadData.tagId
                }
            }
            let dataAfterUpdate = await updateData(Model.Users, criteria, dataToSet, { new: true, lean: true })
            if (dataAfterUpdate) {
                return dataAfterUpdate
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let homeSearchPost = async (payloadData, userData) => {
    try {
        let criteria = {}, publicGroupArray = [], userGroupArray = [], groupArray = []
        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }
        let userGroups = await getRequired(Model.PostGroupMembers, {
            userId: userData._id,
            isDeleted: false
        }, { groupId: 1 }, { lean: true })
        let publicGroups = await getRequired(Model.PostGroups, {
            isPrivate: false,
            isDeleted: false
        }, { groupId: 1 }, { lean: true })
        publicGroups.map(obj => publicGroupArray.push(obj._id))
        userGroups.map(obj => userGroupArray.push(obj.groupId))
        let populate = [{
            path: "postBy",
            select: "fullName userName bio imageUrl",
            model: "Users"
        }, {
            path: "postCategoryId",
            select: "categoryName",
            model: "Categories"
        }, {
            path: "groupId",
            select: "groupName isPrivate",
            model: "PostGroups"
        }]
        groupArray = _.merge(publicGroupArray, userGroupArray)

        if (!payloadData.search) {

            let pipeline = [{
                $match: {
                    type: Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                    $or: [{ $and: [{ postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.REGULAR }, { groupId: null }] }, { groupId: { $in: groupArray } }],
                    isDeleted: false,
                    postBy: { $nin: userData.blockedBy }
                }
            }, {
                $project: {
                    _id: "$_id",
                    postBy: "$postBy",
                    imageUrl: "$imageUrl",
                    postText: "$postText",
                    media: "$media",
                    groupId: "$groupId",
                    type: "$type",
                    hashTags: "$hashTags",
                    createdOn: "$createdOn",
                    postCategoryId: "$postCategoryId",
                    likeCount: { $size: "$likes" },
                    commentCount: "$commentCount",
                    liked: {
                        $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                    }
                }
            }
                , {
                $sort: { "createdOn": -1 }
            }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }]

            let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
            if (data) {
                return data
            }
        } else {
            let pipeline = [{
                $match: {
                    $or: [{ postText: new RegExp(payloadData.search, 'i') }, { hashTags: { $in: [new RegExp(payloadData.search, 'i')] } }],
                    //type: Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                    //postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY,
                    //groupId: {$in: groupArray},
                    isDeleted: false,
                    postBy: { $ne: userData.blockedBy },
                    'media.0': { $exists: true }
                }
            }, {
                $project: {
                    _id: "$_id",
                    postBy: "$postBy",
                    imageUrl: "$imageUrl",
                    postText: "$postText",
                    media: "$media",
                    groupId: "$groupId",
                    type: "$type",
                    hashTags: "$hashTags",
                    createdOn: "$createdOn",
                    postCategoryId: "$postCategoryId",
                    likeCount: { $size: "$likes" },
                    commentCount: "$commentCount",
                    liked: {
                        $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                    }
                }
            }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }]

            let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
            if (data) {
                return data
            }
        }
    } catch (e) {
        console.log(e)
    }
}


let getUserPosts = async (payloadData, userData) => {

    try {
        let criteria = {
            isDeleted: false,
            postBy: mongoose.Types.ObjectId(payloadData.id)
        }

        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }

        // let populate = [{
        //     path: "postCategoryId",
        //     select: "categoryName",
        //     model: "Categories"
        // }
        // {
        //     path: "groupId",
        //     select: "groupName isPrivate",
        //     model: "PostGroups"
        // }
        // ]
        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }
        if (!payloadData.limit) {
            payloadData.limit = 10
        }

        let pipeline = [{
            $match: criteria
        },
        {
            $project: {
                _id: "$_id",
                postBy: "$postBy",
                imageUrl: "$imageUrl",
                postText: "$postText",
                media: "$media",
                groupId: "$groupId",
                type: "$type",
                hashTags: "$hashTags",
                createdOn: "$createdOn",
                postCategoryId: "$postCategoryId",
                likeCount: { $size: "$likes" },
                // commentCount: "$commentCount",
                postType: "$postType",
                postingIn: "$postingIn",
                selectInterests: "$selectInterests",
                location: "$location",
                locationName: "$locationName",
                locationAddress: "$locationAddress",
                meetingTime: "$meetingTime",
                expirationTime: "$expirationTime",
                comments: "$comments",
                liked: {
                    $cond: { if: { $in: [userData._id, "$likes"] }, then: true, else: false }
                }
            }
        }, {
            $sort: { "createdOn": -1 }
        }, {
            $skip: (payloadData.pageNo - 1) * payloadData.limit
        }, {
            $limit: payloadData.limit
        }]

        let populate = [{
            path: "postBy",
            select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers interestTags locationVisibility nameVisibility followers nameVisibilityForEveryone imageVisibilityForEveryone tagPermission personalInfoVisibility",
            model: "Users",
            populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                path: "imageVisibility",
                select: "userName imageUrl fullName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
        }, {
            path: "postCategoryId",
            select: "categoryName",
            model: "Categories"
        }, {
            path: "groupId",
            select: "groupName",
            model: "PostGroups"
        }, {
            path: "selectInterests",
            model: "Categories"
        }]
        let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
        // let groupCount = await Service.count(Model.PostGroupMembers, {isDeleted: false, userId: userData._id})
        if (data.length) {


            for (let a of data) {
                // sort media
                a.media.sort((x, y) => {
                    return y.likeCount - x.likeCount
                });

                // add liked key in media
                // loop all media
                if (a.media.length > 0) {
                    for (let m = 0; m < a.media.length; m++) {
                        let changeType = a.media[m].likes.map(v => {
                            return v.toString()
                        });
                        let likeIndex = changeType.indexOf(userData._id.toString());

                        if (likeIndex != -1)
                            a.media[m].liked = true
                        else
                            a.media[m].liked = false

                        // add new key
                        if (a.media[0].likeCount != 0)
                            a.media[0].isMostLiked = true;
                    }
                }

                a.postBy = a.postBy.toObject()
                let commentCount = await Service.count(Model.Comments, {
                    postId: a._id,
                    isDeleted: false,
                    commentBy: { "$nin": userData.blockedBy }
                })
                let repliesCount = await Service.count(Model.Replies, {
                    postId: a._id,
                    isDeleted: false,
                    replyBy: { "$nin": userData.blockedBy }
                })
                a.commentCount = commentCount + repliesCount
                if (JSON.stringify(a.postBy._id) == JSON.stringify(userData._id)) {
                    continue
                }
                if (!a.postBy.imageVisibilityForEveryone) {
                    if (a.postBy.imageVisibilityForFollowers) {
                        a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                    } else if (a.postBy.imageVisibility && a.postBy.imageVisibility.length) {
                        a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                    }
                }

                if (!a.postBy.nameVisibilityForEveryone) {
                    if (a.postBy.nameVisibilityForFollowers) {
                        a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                    } else if (a.postBy.nameVisibility && a.postBy.nameVisibility.length) {
                        a.postBy = await nameVisibilityManipulation(a.postBy, userData)
                    }
                }

                // if(!a.postBy.imageVisibilityForFollowers && !a.postBy.nameVisibilityForFollowers && !a.postBy.imageVisibility.length && !a.postBy.nameVisibility.length){
                //     continue
                // }
                // a.postBy = await imageVisibilityManipulation(a.postBy, userData)
                // a.postBy = await nameVisibilityManipulation(a.postBy, userData)


            }

            return data
        } else {
            return []
        }

        // if (!payloadData.search) {

        //     let pipeline = [{
        //         $match:criteria
        //         //  {
        //             // type: Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
        //             // $or: [{$and: [{postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.REGULAR}]}],
        //         // }
        //     }, {
        //         $project: {
        //             _id: "$_id",
        //             postBy: "$postBy",
        //             imageUrl: "$imageUrl",
        //             postText: "$postText",
        //             media: "$media",
        //             groupId: "$groupId",
        //             type: "$type",
        //             hashTags: "$hashTags",
        //             createdOn: "$createdOn",
        //             postCategoryId: "$postCategoryId",
        //             likeCount: {$size: "$likes"},
        //             commentCount: "$commentCount",
        //             liked: {
        //                 $cond: {if: {$in: [userData._id, "$likes"]}, then: true, else: false}
        //             }
        //         }
        //     }
        //         , {
        //             $sort: {"createdOn": -1}
        //         }, {
        //             $skip: (payloadData.pageNo - 1) * 10
        //         }, {
        //             $limit: 10
        //         }]

        //     let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
        //     if (data) {
        //         return data
        //     }
        // } else {
        //     let pipeline = [
        //         {
        //             $match:criteria

        //         },
        //         {
        //         $match: {
        //             $or: [{postText: new RegExp(payloadData.search, 'i')}, {hashTags: {$in: [new RegExp(payloadData.search, 'i')]}}],
        //         }
        //     }, {
        //         $project: {
        //             _id: "$_id",
        //             postBy: "$postBy",
        //             imageUrl: "$imageUrl",
        //             postText: "$postText",
        //             media: "$media",
        //             groupId: "$groupId",
        //             type: "$type",
        //             hashTags: "$hashTags",
        //             createdOn: "$createdOn",
        //             postCategoryId: "$postCategoryId",
        //             likeCount: {$size: "$likes"},
        //             commentCount: "$commentCount",
        //             liked: {
        //                 $cond: {if: {$in: [userData._id, "$likes"]}, then: true, else: false}
        //             }
        //         }
        //     }, {
        //         $skip: (payloadData.pageNo - 1) * 10
        //     }, {
        //         $limit: 10
        //     }]

        //     let data = await aggregateWithPopulate(Model.Posts, pipeline, populate)
        //     if (data) {
        //         return data
        //     }
        // }
    } catch (e) {
        console.log(e)
    }
}

let homeSearchGroup = async (payloadData, userData) => {
    try {
        let notArrayPending = [], notArrayRejected = []
        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }

        let requestGroup = await getRequired(Model.Notifications, {
            byId: userData._id,
            groupType: "GROUP",
            type: "REQUEST_GROUP"
        }, {}, { lean: true })

        if (requestGroup.length) {
            for (let req of requestGroup) {
                if (req.isRejected && req.actionPerformed) {
                    notArrayRejected.push(req.groupId)
                    continue
                }
                if (!req.actionPerformed && !req.isRejected) {
                    notArrayPending.push(req.groupId)
                }
            }
        }
        if (!payloadData.search) {
            let userInterests = await getRequired(Model.Users, { _id: userData._id }, { interestTags: 1 }, { lean: true })

            let allData = await aggregateData(Model.PostGroups, [{
                $match: {
                    $and: [{ "isMember": { $nin: [userData._id] } }, { "categoryId": { $in: userInterests[0].interestTags } }],
                    isDeleted: false,
                    adminId: { $nin: userData.blockedBy }
                }
            }, {
                $project: {
                    imageUrl: 1,
                    groupName: 1,
                    isPrivate: 1,
                    adminId: 1,
                    createdBy: 1,
                    memberCounts: 1,
                    conversationId: 1,
                    // membersList: "$isMember",
                    requestStatus: {
                        $cond: {
                            if: { $in: ["$_id", notArrayPending] },
                            then: "PENDING",
                            else: {
                                $cond: {
                                    if: {
                                        $in: ["$_id", notArrayRejected]
                                    },
                                    then: "REJECTED",
                                    else: "NONE"
                                }
                            }
                        }
                    },
                    isMember: {
                        $cond: {
                            if: { $in: [mongoose.Types.ObjectId(userData._id), "$isMember"] },
                            then: true,
                            else: false
                        }
                    }
                }
            }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }])
            if (allData) {
                return allData
            } else {
                return []
            }
        } else {

            let pipeline = [{
                $match: {
                    groupName: new RegExp(payloadData.search, 'i'),
                    isDeleted: false,
                    adminId: { $nin: userData.blockedBy }
                }
            }, {
                $project: {
                    imageUrl: 1,
                    groupName: 1,
                    isPrivate: 1,
                    adminId: 1,
                    createdBy: 1,
                    memberCounts: 1,
                    conversationId: 1,
                    // membersList: "$isMember",
                    requestStatus: {
                        $cond: {
                            if: { $in: ["$_id", notArrayPending] },
                            then: "PENDING",
                            else: {
                                $cond: {
                                    if: {
                                        $in: ["$_id", notArrayRejected]
                                    },
                                    then: "REJECTED",
                                    else: "NONE"
                                }
                            }
                        }
                    },
                    isMember: {
                        $cond: {
                            if: { $in: [mongoose.Types.ObjectId(userData._id), "$isMember"] },
                            then: true,
                            else: false
                        }
                    }
                }
            }, {
                $skip: (payloadData.pageNo - 1) * 10
            }, {
                $limit: 10
            }]
            let suggestedGroups = await aggregateData(Model.PostGroups, pipeline)
            if (suggestedGroups) {
                return suggestedGroups
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let homeSearchVenue = async (payloadData, userData) => {
    try {
        let notArrayRejected = [], notArrayPending = [], pipeline
        if (!payloadData.pageNo) {
            payloadData.pageNo = 1
        }
        let userInterests = await getRequired(Model.Users, {
            _id: userData._id,
            isDeleted: false
        }, { interestTags: 1 }, { lean: true })
        let requestVenue = await getRequired(Model.Notifications, {
            byId: userData._id,
            groupType: "VENUE",
            type: "REQUEST_VENUE"
        }, {}, { lean: true })

        if (requestVenue.length) {
            for (let req of requestVenue) {
                if (req.isRejected && req.actionPerformed) {
                    notArrayRejected.push(req.venueId)
                    continue
                }
                if (!req.actionPerformed && !req.isRejected) {
                    notArrayPending.push(req.venueId)
                }
            }
        }
        if (!payloadData.search) {
            let result
            if (payloadData.currentLat && payloadData.currentLong) {
                let result1 = await aggregateData(Model.VenueGroups, [{
                    $geoNear: {
                        near: { type: "Point", coordinates: [payloadData.currentLong, payloadData.currentLat] },
                        distanceField: "calculated",
                        maxDistance: 20000,
                        spherical: true,
                        distanceMultiplier: 0.000621371,
                        query: {
                            // _id: {'$nin': idArray},
                            memberCount: { $ne: 0 },
                            adminId: { $nin: userData.blockedBy }
                        }
                    }
                },
                {
                    $lookup: {
                        "from": "venuegroupmembers",
                        "localField": "_id",
                        "foreignField": "groupId",
                        "as": "groupMembers"
                    }
                }, {
                    $match: {
                        $or: [{ 'groupMembers.userId': { '$nin': [mongoose.Types.ObjectId(userData._id)] } },
                        {
                            'groupMembers.userId': { '$in': [mongoose.Types.ObjectId(userData._id)] },
                            "groupMembers.isDeleted": true
                        }],
                        isDeleted: false
                    }
                },
                {
                    $unwind: "$groupMembers"
                },
                {
                    $group: {
                        _id: "$_id",
                        distance: { $last: "$calculated" },
                        conversationId: { $last: "$conversationId" },
                        groupId: { $last: "$_id" },
                        adminId: { $last: "$adminId" },
                        createdBy: { $last: "$createdBy" },
                        venueTitle: { $last: "$venueTitle" },
                        venueTime: { $last: "$venueTime" },
                        venueTags: { $last: "$venueTags" },
                        //  membersList: {$last: "$memberIds"},
                        isPrivate: { $last: "$isPrivate" },
                        venueImageUrl: { $last: "$imageUrl" },
                        venueLocation: { $last: "$venueLocation" },
                        venueLocationName: { $last: "$venueLocationName" },
                        venueLocationAddress: { $last: "$venueLocationAddress" },
                        memberCount: { $last: "$memberCount" },
                        memberIds: { $last: "$memberIds" }
                    }
                }, {
                    $project: {
                        distance: "$distance",
                        conversationId: "$conversationId",
                        adminId: "$adminId",
                        groupId: "$groupId",
                        venueTitle: "$venueTitle",
                        createdBy: "$createdBy",
                        venueTime: "$venueTime",
                        venueTags: "$venueTags",
                        //  membersList: "$membersList",
                        venueLocationName: "$venueLocationName",
                        venueLocationAddress: "$venueLocationAddress",
                        isPrivate: "$isPrivate",
                        imageUrl: "$venueImageUrl",
                        memberCount: "$memberCount",
                        venueLocation: "$venueLocation",
                        requestStatus: {
                            $cond: {
                                if: { $in: ["$_id", notArrayPending] },
                                then: "PENDING",
                                else: {
                                    $cond: {
                                        if: { $in: ["$_id", notArrayRejected] },
                                        then: "REJECTED",
                                        else: "NONE"
                                    }
                                }
                            }
                        },
                        isMember: {
                            $cond: {
                                if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                then: true,
                                else: false
                            }
                        },
                    }
                }, {
                    $sort: {
                        distance: 1
                    }
                }, {
                    $skip: (payloadData.pageNo - 1) * 10
                }, {
                    $limit: 10
                }])
                if (!result1.length) {
                    let result2 = await aggregateData(Model.VenueGroups, [{
                        $geoNear: {
                            near: { type: "Point", coordinates: [payloadData.currentLong, payloadData.currentLat] },
                            distanceField: "calculated",
                            maxDistance: 50000000,
                            spherical: true,
                            distanceMultiplier: 0.000621371,
                            query: {
                                // _id: {'$nin': idArray},
                                memberCount: { $ne: 0 },
                                "categoryId": { $in: userInterests[0].interestTags },
                            }
                        }
                    },
                    {
                        $lookup: {
                            "from": "venuegroupmembers",
                            "localField": "_id",
                            "foreignField": "groupId",
                            "as": "groupMembers"
                        }
                    }, {
                        $match: {
                            $or: [{ 'groupMembers.userId': { '$nin': [mongoose.Types.ObjectId(userData._id)] } },
                            {
                                'groupMembers.userId': { '$in': [mongoose.Types.ObjectId(userData._id)] },
                                "groupMembers.isDeleted": true
                            }],
                            isDeleted: false
                        }
                    },
                    {
                        $unwind: "$groupMembers"
                    },
                    {
                        $group: {
                            _id: "$_id",
                            distance: { $last: "$calculated" },
                            conversationId: { $last: "$conversationId" },
                            groupId: { $last: "$_id" },
                            adminId: { $last: "$adminId" },
                            createdBy: { $last: "$createdBy" },
                            venueTitle: { $last: "$venueTitle" },
                            venueTime: { $last: "$venueTime" },
                            venueTags: { $last: "$venueTags" },
                            //  membersList: {$last: "$memberIds"},
                            isPrivate: { $last: "$isPrivate" },
                            venueImageUrl: { $last: "$imageUrl" },
                            venueLocation: { $last: "$venueLocation" },
                            venueLocationName: { $last: "$venueLocationName" },
                            venueLocationAddress: { $last: "$venueLocationAddress" },
                            memberCount: { $last: "$memberCount" },
                            memberIds: { $last: "$memberIds" }
                        }
                    }, {
                        $project: {
                            distance: "$distance",
                            conversationId: "$conversationId",
                            adminId: "$adminId",
                            groupId: "$groupId",
                            venueTitle: "$venueTitle",
                            createdBy: "$createdBy",
                            venueTime: "$venueTime",
                            venueTags: "$venueTags",
                            //  membersList: "$membersList",
                            venueLocationName: "$venueLocationName",
                            venueLocationAddress: "$venueLocationAddress",
                            isPrivate: "$isPrivate",
                            imageUrl: "$venueImageUrl",
                            memberCount: "$memberCount",
                            venueLocation: "$venueLocation",
                            requestStatus: {
                                $cond: {
                                    if: { $in: ["$_id", notArrayPending] },
                                    then: "PENDING",
                                    else: {
                                        $cond: {
                                            if: { $in: ["$_id", notArrayRejected] },
                                            then: "REJECTED",
                                            else: "NONE"
                                        }
                                    }
                                }
                            },
                            isMember: {
                                $cond: {
                                    if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                    then: true,
                                    else: false
                                }
                            },
                        }
                    }, {
                        $sort: {
                            distance: 1
                        }
                    }
                        , {
                        $skip: (payloadData.pageNo - 1) * 10
                    }, {
                        $limit: 10
                    }
                    ])
                    var merged = _.merge(_.keyBy(result2, '_id'), _.keyBy(result1, '_id'));
                    result = _.values(merged);
                } else {
                    result = result1
                }
            } else {
                result = await aggregateData(Model.VenueGroups, [
                    {
                        $match: {
                            $and: [
                                // {"_id": {$nin: idArray}},
                                { memberCount: { $ne: 0 } },
                                { "memberIds": { $nin: [mongoose.Types.ObjectId(userData._id)] } },
                                { "categoryId": { $in: userInterests[0].interestTags } },
                                { adminId: { $nin: userData.blockedBy } }
                            ]
                        }
                    },
                    {
                        $project: {
                            groupId: "$_id",
                            venueTitle: "$venueTitle",
                            venueTime: "$venueTime",
                            adminId: "$adminId",
                            conversationId: "$conversationId",
                            venueLocationName: "$venueLocationName",
                            venueLocationAddress: "$venueLocationAddress",
                            isPrivate: "$isPrivate",
                            imageUrl: "$imageUrl",
                            venueTags: "$venueTags",
                            createdBy: "$createdBy",
                            memberCount: "$memberCount",
                            // membersList: "$memberIds",
                            venueLocation: "$venueLocation",
                            requestStatus: {
                                $cond: {
                                    if: { $in: ["$_id", notArrayPending] },
                                    then: "PENDING",
                                    else: {
                                        $cond: {
                                            if: { $in: ["$_id", notArrayRejected] },
                                            then: "REJECTED",
                                            else: "NONE"
                                        }
                                    }
                                }
                            },
                            isMember: {
                                $cond: {
                                    if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                    then: true,
                                    else: false
                                }
                            },
                        }
                    }, {
                        $skip: (payloadData.pageNo - 1) * 10
                    }, {
                        $limit: 10
                    }])
            }
            if (result) {
                return result
            }
        } else {
            if (payloadData.currentLat && payloadData.currentLong) {
                pipeline = [{
                    $geoNear: {
                        near: { type: "Point", coordinates: [payloadData.currentLong, payloadData.currentLat] },
                        distanceField: "calculated",
                        maxDistance: 5000000,
                        spherical: true,
                        distanceMultiplier: 0.000621371,
                        query: {
                            venueTitle: new RegExp(payloadData.search, 'i'),
                            adminId: { $nin: userData.blockedBy }
                            // memberCount : {$ne : 0},
                        }
                    }
                }, {
                    $lookup: {
                        "from": "venuegroupmembers",
                        "localField": "_id",
                        "foreignField": "groupId",
                        "as": "groupMembers"
                    }
                }, {
                    $match: {
                        // $or: [
                        //     {'groupMembers.userId': {'$nin': [mongoose.Types.ObjectId(userData._id)]}},
                        //     {
                        //         'groupMembers.userId': {'$in': [mongoose.Types.ObjectId(userData._id)]},
                        //         "groupMembers.isDeleted": true
                        //     }],
                        isDeleted: false
                    }
                }, {
                    $unwind: "$groupMembers"
                }, {
                    $group: {
                        _id: "$_id",
                        distance: { $last: "$calculated" },
                        conversationId: { $last: "$conversationId" },
                        groupId: { $last: "$_id" },
                        adminId: { $last: "$adminId" },
                        createdBy: { $last: "$createdBy" },
                        venueTitle: { $last: "$venueTitle" },
                        venueTime: { $last: "$venueTime" },
                        venueTags: { $last: "$venueTags" },
                        //  membersList: {$last: "$memberIds"},
                        isPrivate: { $last: "$isPrivate" },
                        venueImageUrl: { $last: "$imageUrl" },
                        venueLocation: { $last: "$venueLocation" },
                        venueLocationName: { $last: "$venueLocationName" },
                        venueLocationAddress: { $last: "$venueLocationAddress" },
                        memberCount: { $last: "$memberCount" },
                        memberIds: { $last: "$memberIds" }
                    }
                }, {
                    $project: {
                        distance: "$distance",
                        conversationId: "$conversationId",
                        adminId: "$adminId",
                        groupId: "$groupId",
                        venueTitle: "$venueTitle",
                        createdBy: "$createdBy",
                        venueTime: "$venueTime",
                        venueTags: "$venueTags",
                        //  membersList: "$membersList",
                        venueLocationName: "$venueLocationName",
                        venueLocationAddress: "$venueLocationAddress",
                        isPrivate: "$isPrivate",
                        imageUrl: "$venueImageUrl",
                        memberCount: "$memberCount",
                        venueLocation: "$venueLocation",
                        requestStatus: {
                            $cond: {
                                if: { $in: ["$_id", notArrayPending] },
                                then: "PENDING",
                                else: {
                                    $cond: {
                                        if: { $in: ["$_id", notArrayRejected] },
                                        then: "REJECTED",
                                        else: "NONE"
                                    }
                                }
                            }
                        },
                        isMember: {
                            $cond: {
                                if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                then: true,
                                else: false
                            }
                        },
                    }
                }, {
                    $sort: {
                        distance: 1
                    }
                }, {
                    $skip: (payloadData.pageNo - 1) * 10
                }, {
                    $limit: 10
                }]
            } else {

                pipeline = [
                    {
                        $match: {
                            $and: [
                                // {"_id": {$nin: idArray}},
                                { memberCount: { $ne: 0 } },
                                { venueTitle: new RegExp(payloadData.search, 'i') },
                                { adminId: { $nin: userData.blockedBy } }
                                // {"memberIds": {$nin:[mongoose.Types.ObjectId(userData._id)]}},
                                // {"categoryId": {$in: userInterests[0].interestTags}},
                            ]
                        }
                    },
                    {
                        $project: {
                            groupId: "$_id",
                            venueTitle: "$venueTitle",
                            venueTime: "$venueTime",
                            adminId: "$adminId",
                            conversationId: "$conversationId",
                            venueLocationName: "$venueLocationName",
                            venueLocationAddress: "$venueLocationAddress",
                            isPrivate: "$isPrivate",
                            imageUrl: "$imageUrl",
                            venueTags: "$venueTags",
                            createdBy: "$createdBy",
                            memberCount: "$memberCount",
                            // membersList: "$memberIds",
                            venueLocation: "$venueLocation",
                            requestStatus: {
                                $cond: {
                                    if: { $in: ["$_id", notArrayPending] },
                                    then: "PENDING",
                                    else: {
                                        $cond: {
                                            if: { $in: ["$_id", notArrayRejected] },
                                            then: "REJECTED",
                                            else: "NONE"
                                        }
                                    }
                                }
                            },
                            isMember: {
                                $cond: {
                                    if: { $in: [mongoose.Types.ObjectId(userData._id), "$memberIds"] },
                                    then: true,
                                    else: false
                                }
                            },
                        }
                    }, {
                        $skip: (payloadData.pageNo - 1) * 10
                    }, {
                        $limit: 10
                    }]
            }

            let venueNearYou = await aggregateData(Model.VenueGroups, pipeline)
            if (venueNearYou) {
                return venueNearYou
            } else {
                return []
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let settingInvitePeople = async (payloadData, userData) => {
    if (payloadData.emailArray) {
        for (let email of payloadData.emailArray) {
            let check = await getRequired(Model.Users, { email: email, isDeleted: false }, {}, { lean: true })
            if (!check.length) {
                // let url =`http://52.35.234.66:8000/user/emailVerification?id=${userData._id}&timestamp=${+new Date()}`;
                // let inviteTemplate = await emailTemplates.inviteTemplate({email: email, senderName: userData.fullName}, url)
                // sendEmail.sendEmail(email, "Conversify Invite", inviteTemplate)
                await updateData(Model.Users, { _id: userData._id }, { $addToSet: { appInviteEmails: email } }, {
                    new: true,
                    lean: true
                })

            }
        }
        return
    }

    if (payloadData.phoneNumberArray) {
        let data = {
            from: userData.userName,
            to: payloadData.phoneNumberArray,
            body: ""
        }
        // pushNotification.sendSMS(data)
    }
}

let settingVerification = async (payloadData, userData) => {
    let criteria = {}, dataToSet = {}
    if (payloadData.email) {
        // let url =`http://52.35.234.66:8000/user/emailVerification?id=${userData._id}&timestamp=${+new Date()}`;
        let url = `http://52.35.234.66:8001/user/emailVerification?id=${userData._id}&timestamp=${+new Date()}`;
        let emailTemplatesToSend = await emailTemplates.emailVerification({
            userName: userData.userName,
            email: payloadData.email
        }, url)
        sendEmail.sendEmail(payloadData.email, "Check It Verify Email", emailTemplatesToSend)
    } else if (payloadData.phoneNumber) {
        criteria._id = userData._id
        criteria.isDeleted = false
        dataToSet.OTPcode = "4444"
        let data = await updateData(Model.Users, criteria, { $set: dataToSet }, { new: true, lean: true })
        if (data) {
            let smsData = {
                To: "+" + payloadData.phoneNumber,
                From: "+" + 12108797398,
                Body: "OTP: 4444"
            }
            await pushNotification.sendSMS(smsData, (err, res) => {
                console.log(err, res)
            })
            return data
        }
    } else if (payloadData.passportDocUrl) {
        criteria._id = userData._id
        criteria.isDeleted = false
        dataToSet.passportDocUrl = payloadData.passportDocUrl
        dataToSet.isUploaded = true
        let data = await updateData(Model.Users, criteria, { $set: dataToSet }, { new: true, lean: true })
        if (data) {
            return data
        }
    } else {
        criteria._id = userData._id
        criteria.isDeleted = false
        let project = {}
        let userVerifiedData = await getRequired(Model.Users, criteria, project, { lean: true })
        if (userVerifiedData.length) {
            return userVerifiedData[0]
        }
    }
}

let emailVerification = async (queryData) => {

    if (queryData.id && queryData.timestamp) {
        let criteria = {
            _id: queryData.id,
            isDeleted: false
        };
        let check = await getRequired(Model.Users, criteria, {}, { lean: true });
        if (check[0].isEmailVerified) {
            let templateToSend = await emailTemplates.successTemplate({ data: "This link is expired, Kindly resubmit your email to get new link." }, "#d93025")
            return templateToSend
        }
        if (queryData.timestamp < (Date.now() - 10 * 60000)) {
            // return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.LINK_EXPIRE)
            let templateToSend = await emailTemplates.successTemplate({ data: "This link is expired, Kindly resubmit your email to get new link." }, "#d93025")
            return templateToSend
        } else {
            let updateIsEmailVarified = await updateData(Model.Users, {
                _id: queryData.id,
                isDeleted: false
            }, { $set: { isEmailVerified: true } }, { new: true, lean: true })
            if (updateIsEmailVarified) {
                let templateToSend = await emailTemplates.successTemplate({ data: "Your email is now successfully registered with us." }, "#0072ff")
                socketManager.emailVerificationSocket(queryData.id)
                return templateToSend
            } else {
                let templateToSend = await emailTemplates.successTemplate({ data: "Something went wrong!!" }, "#d93025")
                return templateToSend
            }
        }
    }
}

let getOTPForVerificiation = async (payloadData, userData) => {
    let updateInfo = await updateData(Model.Users, { _id: userData._id, isDeleted: false }, {
        OTPcode: "4444",
        isPhoneNumberVerified: false
    }, { new: true, lean: true })
    if (updateInfo) {
        return updateInfo
    }
}


let phoneVerification = async (payloadData, userData) => {

    if (payloadData.OTPcode) {
        if (userData.OTPcode == payloadData.OTPcode) {
            let updateInfo = await updateData(Model.Users, { _id: userData._id, isDeleted: false }, {
                $set: {
                    OTPcode: "",
                    isPhoneNumberVerified: true
                }
            }, { new: true, lean: true })
            if (updateInfo) {
                return updateInfo
            }
        } else {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.OTP_NOT_MATCH)
        }
    }
}

let blockUser = async (payloadData, userData) => {
    try {
        let update = {}, updateOther = {}, ownCriteria = {}, otherCriteria = {};

        if (payloadData.action === 1) {
            ownCriteria._id = userData._id;

            update.$addToSet = {
                blockedWhom: payloadData.userId
            };
            otherCriteria._id = payloadData.userId;

            updateOther.$addToSet = {
                blockedBy: userData._id
            };
            await updateData(Model.Users, ownCriteria, update, { new: true });
            let updateOtherData = await updateData(Model.Users, otherCriteria, updateOther, { new: true, lean: true });
            if (updateOtherData) {
                return
            }


        } else {
            update.$pull = {
                blockedWhom: payloadData.userId
            }
            updateOther.$pull = {
                blockedBy: userData._id
            }

            await updateData(Model.Users, { _id: userData._id }, update, { new: true });

            let updateOtherData = await updateData(Model.Users, { _id: payloadData.userId }, updateOther, { new: true });

            if (updateOtherData) {
                return
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let listBlockedUsers = async (payloadData, userData) => {
    try {
        let criteria = {}
        criteria._id = userData._id
        criteria.isDeleted = false

        let populate = [{
            path: "blockedWhom",
            select: "fullName userName imageUrl bio firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
            model: "Users"
        }]
        let desireData = await getRequiredPopulate(Model.Users, criteria, { blockedWhom: 1 }, { lean: true }, populate)
        if (desireData.length) {
            let finalData = []
            for (let user of desireData[0].blockedWhom) {
                let tempObj = {}
                // if(JSON.stringify(venueMemberPreList).includes(JSON.stringify(user._id))){
                //     continue
                // }
                if (!user.imageVisibilityForEveryone) {
                    if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else if (user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else {
                        tempObj.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }
                    }
                } else {
                    tempObj.imageUrl = user.imageUrl
                }

                if (!user.nameVisibilityForEveryone) {
                    if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else if (user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else {
                        tempObj.userName = user.userName.substr(0, 3)
                        tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                    }
                } else {
                    tempObj.userName = user.userName
                    tempObj.fullName = user.fullName
                }
                tempObj._id = user._id
                tempObj.interestTags = user.interestTags

                finalData.push(tempObj)
            }
            return finalData
        } else {
            return []
        }
    } catch (e) {
        console.log(e)
    }
}

let groupInviteUsers = async (payloadData, userData) => {
    try {
        if (payloadData.venueId) {
            if (payloadData.emailArray) {
                let venueDetails = await getRequired(Model.VenueGroups, {
                    _id: payloadData.venueId,
                    isDeleted: false
                }, { venueTitle: 1 }, { lean: true })
                for (let email of payloadData.emailArray) {
                    let check = await getRequired(Model.Users, { email: email, isDeleted: false }, {}, { lean: true })
                    if (!check.length) {
                        let url = `http://52.35.234.66:8000/user/emailVerification?id=${userData._id}&timestamp=${+new Date()}`;
                        let inviteTemplate = await emailTemplates.inviteTemplate({
                            venueName: venueDetails[0].venueTitle,
                            email: email,
                            senderName: userData.fullName
                        }, url)
                        sendEmail.sendEmail(email, "Check It Invite", inviteTemplate)
                    } else {
                        let checkMember = await getRequired(Model.VenueGroupMembers, {
                            groupId: payloadData.venueId,
                            isDeleted: false,
                            userId: check[0]._id
                        }, {}, { lean: true })
                        if (checkMember.length) {
                            continue
                        }
                        let criteria = {}
                        criteria.venueId = payloadData.venueId
                        criteria.toId = mongoose.Types.ObjectId(check[0]._id)
                        criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE
                        criteria.groupType = "VENUE"

                        let checkNot = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                        console.log("--------------------check", checkNot.length)
                        if (checkNot.length) {
                            await updateData(Model.Notifications, criteria, {
                                $set: {
                                    isDeleted: false,
                                    actionPerformed: false,
                                    isRejected: false,
                                    isRead: false,
                                    createdOn: +new Date
                                }
                            }, { new: true, lean: true })
                            let pushData = {
                                id: payloadData.venueId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                msg: userData.fullName + ' requested you to join ' + venueDetails[0].venueTitle + ' venue'
                            };
                            pushNotification.sendPush(check[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        } else {
                            let query = {
                                venueId: payloadData.venueId,
                                toId: check[0]._id,
                                byId: userData._id,
                                text: userData.fullName + ' requested you to join ' + venueDetails[0].venueTitle + ' venue',
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                groupType: "VENUE",
                                createdOn: +new Date,
                                actionPerformed: false
                            }
                            await createData(Model.Notifications, query)
                            let pushData = {
                                id: payloadData.venueId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                msg: userData.fullName + " has invited you to join " + venueDetails[0].venueTitle + " venue"
                            }

                            pushNotification.sendPush(check[0].deviceToken, pushData, (err, res) => {
                                console.log(res, err)
                            })
                        }
                    }
                }
            }

            if (payloadData.phoneNumberArray) {
                let data = {
                    from: userData.userName,
                    to: payloadData.phoneNumberArray,
                    body: ""
                }
                // pushNotification.sendSMS(data)
            }
        }

        if (payloadData.groupId) {
            let groupDetails = await getRequired(Model.PostGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, { groupName: 1 }, { lean: true })

            if (payloadData.emailArray) {
                for (let email of payloadData.emailArray) {
                    let check = await getRequired(Model.Users, { email: email, isDeleted: false }, {}, { lean: true })
                    if (!check.length) {
                        let url = `http://52.35.234.66:8000/user/emailVerification?id=${userData._id}&timestamp=${+new Date()}`;
                        let inviteTemplate = await emailTemplates.inviteTemplate({
                            groupName: groupDetails[0].groupName,
                            email: email,
                            senderName: userData.fullName
                        }, url)
                        sendEmail.sendEmail(email, "Check It Invite", inviteTemplate)
                    } else {
                        let checkMember = await getRequired(Model.PostGroupMembers, {
                            groupId: payloadData.groupId,
                            isDeleted: false,
                            userId: check[0]._id
                        }, {}, { lean: true })
                        if (checkMember.length) {
                            continue
                        }
                        let criteria = {}
                        criteria.toId = mongoose.Types.ObjectId(check[0]._id)
                        criteria.groupId = payloadData.groupId
                        criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP
                        criteria.groupType = "GROUP"
                        let checkNot = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                        if (checkNot.length) {
                            await updateData(Model.Notifications, criteria, {
                                $set: {
                                    isDeleted: false,
                                    actionPerformed: false,
                                    isRejected: false,
                                    isRead: false,
                                    createdOn: +new Date
                                }
                            }, { new: true, lean: true })
                            let pushData = {
                                id: payloadData.groupId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                                msg: userData.fullName + ' requested you to join ' + groupDetails[0].groupName + ' channel'
                            };
                            pushNotification.sendPush(check[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        } else {

                            let query = {
                                groupId: payloadData.groupId,
                                toId: check[0]._id,
                                text: userData.fullName + ' requested you to join ' + groupDetails[0].groupName + ' channel',
                                byId: userData._id,
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                                groupType: "GROUP",
                                createdOn: +new Date,
                                actionPerformed: false
                            }
                            await createData(Model.Notifications, query)
                            let pushData = {
                                userId: check[0]._id,
                                groupId: payloadData.groupId,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                                msg: userData.userName + " has invited you to join " + groupDetails[0].groupName + " channel"
                            }

                            pushNotification.sendPush(check[0].deviceToken, pushData, (err, res) => {
                                console.log(res, err)
                            })
                        }
                    }
                }
            }

            if (payloadData.phoneNumberArray) {
                let data = {
                    from: userData.userName,
                    to: payloadData.phoneNumberArray,
                    body: ""
                }
                // pushNotification.sendSMS(data)
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/*=================Post Group Related Apis==================*/
/*==============Post Group Related Apis===================*/

/*=================People Related Apis==================*/
/*==============People Related Apis===================*/

/**
 * @description:people whom crossed you recently
 * @param {string} authorization
 */

let crossedPeople = async (payloadData, userData) => {
    try {
        let pipeline = [{
            $match: {
                userId: mongoose.Types.ObjectId(userData._id),
                crossedUserId: { "$nin": userData.blockedBy },
                isDeleted: false
            }
        }, { $sort: { "time": -1 } }, {
            $group: {
                _id: {
                    locationName: "$locationName",
                    date: { $dayOfMonth: "$updatedAt" },
                    month: { $month: "$updatedAt" },
                    year: { $year: "$updatedAt" }
                },
                userCrossed: {
                    $push: {
                        _id: "$_id",
                        conversationId: "$conversationId",
                        crossedUserId: "$crossedUserId",
                        time: "$time",
                        isPrivate: "$isPrivate",
                        locationName: "$locationName",
                        locationAddress: "$locationAddress",
                        location: "$location"
                    }
                },
            }
        }, {
            $project: {
                _id: 0,
                locationName: "$_id.locationName",
                locationAddress: { $arrayElemAt: ["$userCrossed.locationAddress", 0] },
                timestamp: { $arrayElemAt: ["$userCrossed.time", 0] },
                userCrossed: "$userCrossed"
            }
        }, { $sort: { timestamp: -1 } }]
        let populate = [{
            path: "userCrossed.crossedUserId",
            select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
            model: "Users"
        }]
        let data = await aggregateWithPopulate(Model.CrossedUsers, pipeline, populate)
        if (data.length) {
            for (let a of data) {
                for (let b of a.userCrossed) {
                    if (b.crossedUserId) {
                        let attempt = b.crossedUserId
                        b.crossedUserId = attempt.toObject()
                        if (JSON.stringify(b.crossedUserId._id) == JSON.stringify(userData._id)) {
                            continue
                        }
                        if (!b.crossedUserId.imageVisibilityForEveryone) {
                            if (b.crossedUserId.imageVisibilityForFollowers) {
                                b.crossedUserId = await imageVisibilityManipulation(b.crossedUserId, userData)
                            } else if (b.crossedUserId.imageVisibility && b.crossedUserId.imageVisibility.length) {
                                b.crossedUserId = await imageVisibilityManipulation(b.crossedUserId, userData)
                            }
                        }

                        if (!b.crossedUserId.nameVisibilityForEveryone) {
                            if (b.crossedUserId.nameVisibilityForFollowers) {
                                b.crossedUserId = await nameVisibilityManipulation(b.crossedUserId, userData)
                            } else if (b.crossedUserId.nameVisibility && b.crossedUserId.nameVisibility.length) {
                                b.crossedUserId = await nameVisibilityManipulation(b.crossedUserId, userData)
                            }
                        }
                        // if(!b.crossedUserId.imageVisibilityForFollowers && !b.crossedUserId.nameVisibilityForFollowers && !b.crossedUserId.imageVisibility.length && !b.crossedUserId.nameVisibility.length){
                        //     continue
                        // }
                        // b.crossedUserId = await imageVisibilityManipulation(b.crossedUserId, userData)
                        // b.crossedUserId = await nameVisibilityManipulation(b.crossedUserId, userData)
                    }
                }
            }
            return data
        } else
            return []
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description listing user on the basis of interestTags
 * @param {string} authorization
 * @param {string} categoryIds
 * @param {string} pageNo
 */

let interestMatchUsers1 = async (payloadData, userData) => {
    try {
        if (!payloadData.pageNo || payloadData.pageNo === 0)
            payloadData.pageNo = 1

        let finalArray = []

        for (let inter of payloadData.categoryIds) {
            finalArray.push(mongoose.Types.ObjectId(inter))
        }
        let pipeline = [{
            $match: {
                _id: { $nin: userData.blockedBy, $ne: mongoose.Types.ObjectId(userData._id) },
                interestTags: { $in: finalArray },
            }
        }, {
            $lookup: {
                from: "chats",
                let: { userId: "$_id" },
                pipeline: [
                    {
                        $match:
                        {
                            $expr:
                            {
                                $or:
                                    [{
                                        $and: [
                                            { $eq: ["$senderId", mongoose.Types.ObjectId(userData._id)] },
                                            { $eq: ["$receiverId", "$$userId"] }
                                        ]
                                    },
                                    {
                                        $and: [
                                            { $eq: ["$senderId", "$$userId"] },
                                            { $eq: ["$receiverId", mongoose.Types.ObjectId(userData._id)] }
                                        ]
                                    }
                                    ]
                            }
                        }
                    },
                    {
                        $project: {
                            conversationId: {
                                $cond: {
                                    if: { $ifNull: ['$conversationId', true] },
                                    then: "$conversationId",
                                    else: null
                                }
                            }
                        }
                    }
                ],
                as: "chatConversation"
            }
        }, {
            $project: {
                fullName: {
                    "$cond": [
                        {
                            "$or": [
                                {
                                    $cond: {
                                        if: { "$eq": ["$nameVisibilityForFollowers", true] }, then: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                "then": true,
                                                "else": false
                                            }
                                        }, else: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$nameVisibility"] },
                                                then: true,
                                                else: false
                                            }
                                        }
                                    }
                                },
                                {
                                    "$cond": {
                                        "if": {
                                            $and: [{
                                                "$eq": ["$nameVisibilityForFollowers", false]
                                            }, {
                                                "$eq": [{ $size: "$nameVisibility" }, 0]
                                            }]
                                        }, then: true, else: false
                                    }
                                }
                                // { "$in": [ mongoose.Types.ObjectId(userData._id), "$nameVisibility" ] }
                            ]
                        },
                        // { $concat: [ { $substr: [ "$firstName", 0, 1 ] }, ".... ", { $substr: [ "$lastName", 0, 1 ] }, "...." ] },
                        "$firstName",
                        { $concat: [{ $substr: ["$firstName", 0, 1] }, ".... "] }
                    ]
                },
                isOnline: 1,
                interestTags: 1,
                designation: 1,
                imageUrl: {
                    "$cond": [
                        {
                            "$or": [
                                {
                                    $cond: {
                                        if: { "$eq": ["$imageVisibilityForFollowers", false] }, then: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$imageVisibility"] },
                                                then: true,
                                                else: false
                                            }
                                        }, else: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                "then": true,
                                                "else": false
                                            }
                                        }
                                    }
                                },
                                {
                                    "$cond": {
                                        "if": {
                                            $and: [{
                                                "$eq": ["$imageVisibilityForFollowers", false]
                                            }, {
                                                "$eq": [{ $size: "$imageVisibility" }, 0]
                                            }]
                                        }, then: true, else: false
                                    }
                                }
                                // { "$in": [ mongoose.Types.ObjectId(userData._id), "$imageVisibility" ] }
                            ]
                        },
                        "$imageUrl",
                        {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }
                    ]
                },
                bio: 1,
                userName: {
                    "$cond": [
                        {
                            "$or": [
                                {
                                    $cond: {
                                        if: { "$eq": ["$nameVisibilityForFollowers", true] }, then: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$followers"] },
                                                "then": true,
                                                "else": false
                                            }
                                        }, else: {
                                            "$cond": {
                                                "if": { "$in": [mongoose.Types.ObjectId(userData._id), "$nameVisibility"] },
                                                then: true,
                                                else: false
                                            }
                                        }
                                    }
                                },
                                {
                                    "$cond": {
                                        "if": {
                                            $and: [{
                                                "$eq": ["$nameVisibilityForFollowers", false]
                                            }, {
                                                "$eq": [{ $size: "$nameVisibility" }, 0]
                                            }]
                                        }, then: true, else: false
                                    }
                                }
                                // { "$in": [ mongoose.Types.ObjectId(userData._id), "$nameVisibility" ] }
                            ]
                        },
                        "$userName",
                        { $substr: ["$userName", 0, 3] }
                    ]
                },
                email: 1,
                followingCount: { $size: "$following" },
                followerCount: { $size: "$followers" },
                conversationId: { $arrayElemAt: ["$chatConversation.conversationId", 0] }
            }
        }, {
            $skip: (payloadData.pageNo - 1) * 10
        }, {
            $limit: 10
        }]

        let populate = [{
            path: "interestTags",
            select: "categoryName",
            model: "Categories"
        }]
        let data = await aggregateWithPopulate(Model.Users, pipeline, populate)
        if (data)
            return data
    } catch (e) {
        console.log(e)
    }
}

let interestMatchUsers = async (payloadData, userData) => {
    try {
        let finalArray = [], blockedByArray = []
        if (!payloadData.pageNo || payloadData.pageNo === 0)
            payloadData.pageNo = 1

        payloadData.categoryIds.map(obj => finalArray.push(mongoose.Types.ObjectId(obj)))
        userData.blockedBy.map(obj => blockedByArray.push(obj))
        if (payloadData.range) {
            payloadData.range = payloadData.range * (1.60934)
        }
        let populate = [{ path: "interestTags", select: "categoryName", model: "Categories" }]
        // let allMatchUsers = await getRequiredPopulate(Model.Users, {interestTags: { $in : finalArray}, _id: {$nin: blockedByArray, $ne: mongoose.Types.ObjectId(userData._id)}}, {}, {lean: true, new: true, skip: (payloadData.pageNo - 1) * 10, limit: 10}, populate)
        let allMatchUsers = await aggregateWithPopulate(Model.Users, [{
            $geoNear: {
                near: { type: "Point", coordinates: [payloadData.locationLong, payloadData.locationLat] },
                distanceField: "distance",
                maxDistance: payloadData.range * 1000,
                spherical: true,
                // distanceMultiplier: 0.000621371,
                query: {
                    interestTags: { $in: finalArray },
                    _id: { $nin: blockedByArray, $ne: mongoose.Types.ObjectId(userData._id) },
                    isDeleted: false
                }
            }
        }, {
            $skip: (payloadData.pageNo - 1) * 10
        }, {
            $limit: 10
        }, {
            $sort: {
                distance: 1
            }
        }], populate)
        let finalArrayToSend = []
        if (allMatchUsers.length) {
            for (let user of allMatchUsers) {
                let checkConversation = await getRequired(Model.Chats, {
                    $or: [{
                        senderId: userData._id,
                        receiverId: user._id
                    }, { senderId: user._id, receiverId: userData._id }], noChat: true
                }, {}, { lean: true })
                if (!user.imageVisibilityForEveryone) {
                    if (user.imageVisibilityForFollowers) {
                        user = await imageVisibilityManipulation(user, userData)
                    } else if (user.imageVisibility && user.imageVisibility.length) {
                        user = await imageVisibilityManipulation(user, userData)
                    }
                }

                if (!user.nameVisibilityForEveryone) {
                    if (user.nameVisibilityForFollowers) {
                        user = await nameVisibilityManipulation(user, userData)
                    } else if (user.nameVisibility && user.nameVisibility.length) {
                        user = await nameVisibilityManipulation(user, userData)
                    }
                }

                if (!checkConversation.length) {
                    let mongooseId = mongoose.Types.ObjectId()
                    await createData(Model.Chats, {
                        senderId: user._id,
                        receiverId: userData._id,
                        noChat: true,
                        conversationId: mongooseId
                    })
                    finalArrayToSend.push({
                        _id: user._id,
                        fullName: user.fullName,
                        interestTags: user.interestTags,
                        isOnline: user.isOnline,
                        designation: user.designation,
                        imageUrl: user.imageUrl,
                        bio: user.bio,
                        userName: user.userName,
                        distance: user.distance,
                        email: user.email,
                        followerCount: user.followers.length,
                        followingCount: user.following.length,
                        conversationId: mongooseId
                    })
                    continue
                }
                finalArrayToSend.push({
                    _id: user._id,
                    fullName: user.fullName,
                    interestTags: user.interestTags,
                    isOnline: user.isOnline,
                    designation: user.designation,
                    imageUrl: user.imageUrl,
                    bio: user.bio,
                    userName: user.userName,
                    distance: user.distance,
                    email: user.email,
                    followerCount: user.followers.length,
                    followingCount: user.following.length,
                    conversationId: checkConversation[0].conversationId
                })
                // if(!user.imageVisibilityForFollowers && !user.nameVisibilityForFollowers && !user.imageVisibility.length && !user.nameVisibility.length){
                //     if(!checkConversation.length){
                //         let mongooseId = mongoose.Types.ObjectId()
                //         await createData(Model.Chats, {senderId: user._id, receiverId: userData._id, noChat: true, conversationId: mongooseId})
                //         finalArrayToSend.push({
                //             _id: user._id,
                //             fullName: user.fullName,
                //             interestTags: user.interestTags,
                //             isOnline: user.isOnline,
                //             designation: user.designation,
                //             imageUrl: user.imageUrl,
                //             bio: user.bio,
                //             userName: user.userName,
                //             email: user.email,
                //             followerCount: user.followers.length,
                //             distance: user.distance,
                //             followingCount: user.following.length,
                //             conversationId: mongooseId
                //         })
                //         continue
                //     }
                //     finalArrayToSend.push({
                //         _id: user._id,
                //         fullName: user.fullName,
                //         interestTags: user.interestTags,
                //         isOnline: user.isOnline,
                //         designation: user.designation,
                //         imageUrl: user.imageUrl,
                //         bio: user.bio,
                //         userName: user.userName,
                //         email: user.email,
                //         distance: user.distance,
                //         followerCount: user.followers.length,
                //         followingCount: user.following.length,
                //         conversationId: checkConversation[0].conversationId
                //     })
                // }else{
                //     // if(!user.imageVisibilityForEveryone){
                //     //     if(user.imageVisibilityForFollowers){
                //     //         user = await imageVisibilityManipulation(user, userData)
                //     //     }else if(user.imageVisibility && user.imageVisibility.length){
                //     //         user = await imageVisibilityManipulation(user, userData)
                //     //     }
                //     // }

                //     // if(!user.nameVisibilityForEveryone){
                //     //     if(user.nameVisibilityForFollowers){
                //     //         user = await nameVisibilityManipulation(user, userData)
                //     //     }else if(user.nameVisibility && user.nameVisibility.length){
                //     //         user = await nameVisibilityManipulation(user, userData)
                //     //     }
                //     // }
                //     user = await imageVisibilityManipulation(user, userData)
                //     user = await nameVisibilityManipulation(user, userData)
                //     if(!checkConversation.length){
                //         let mongooseId = mongoose.Types.ObjectId()
                //         await createData(Model.Chats, {senderId: user._id, receiverId: userData._id, noChat: true, conversationId: mongooseId})
                //         finalArrayToSend.push({
                //             _id: user._id,
                //             fullName: user.fullName,
                //             interestTags: user.interestTags,
                //             isOnline: user.isOnline,
                //             designation: user.designation,
                //             imageUrl: user.imageUrl,
                //             bio: user.bio,
                //             userName: user.userName,
                //             distance: user.distance,
                //             email: user.email,
                //             followerCount: user.followers.length,
                //             followingCount: user.following.length,
                //             conversationId: mongooseId
                //         })
                //         continue
                //     }
                //     finalArrayToSend.push({
                //         _id: user._id,
                //         fullName: user.fullName,
                //         interestTags: user.interestTags,
                //         isOnline: user.isOnline,
                //         designation: user.designation,
                //         imageUrl: user.imageUrl,
                //         bio: user.bio,
                //         userName: user.userName,
                //         distance: user.distance,
                //         email: user.email,
                //         followerCount: user.followers.length,
                //         followingCount: user.following.length,
                //         conversationId: checkConversation[0].conversationId
                //     })
                // }
            }
        }
        return finalArrayToSend
    } catch (e) {
        console.log(e)
    }
}
/**
 * @description adding participants in groups
 * @param {string} authorization
 * @param {array} participants
 * @param {string} venueId
 * @param {string} groupId
 */

let addParticipants = async (payloadData, userData) => {
    try {
        if (payloadData.venueId) {
            // let check = await getRequired(Model.VenueGroups, {_id: payloadData.venueId, isDeleted: false, adminId: userData._id}, {}, {lean: true})
            // if(!check.length){
            //     return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_ADMIN)
            // }
            if (payloadData.participants && payloadData.participants.length) {
                let criteria = {}
                if (payloadData.venueId) {
                    criteria.venueId = payloadData.venueId
                }
                for (let participant of payloadData.participants) {
                    let checkUserExist = await getRequired(Model.VenueGroupMembers, {
                        groupId: payloadData.venueId,
                        userId: participant,
                        isDeleted: false
                    }, {}, { lean: true })
                    if (checkUserExist.length) {
                        continue
                    }
                    criteria.toId = mongoose.Types.ObjectId(participant)
                    criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE
                    criteria.groupType = "VENUE"

                    let check = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                    console.log("--------------------check", check.length)
                    if (check.length) {
                        await updateData(Model.Notifications, criteria, {
                            $set: {
                                isDeleted: false,
                                actionPerformed: false,
                                isRejected: false,
                                isRead: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, {
                            _id: participant,
                            isDeleted: false
                        }, {}, { lean: true })
                        let groupData = await getRequired(Model.VenueGroups, { _id: payloadData.venueId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.venueId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                            msg: userData.fullName + ' requested you to join ' + groupData[0].venueTitle + ' venue'
                        };
                        socketManager.requestCount(participant)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        continue
                    }
                    let query = {
                        venueId: payloadData.venueId,
                        toId: participant,
                        text: userData.fullName + ' requested you to join ' + groupData[0].venueTitle + ' venue',
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                        groupType: "VENUE",
                        createdOn: +new Date,
                        actionPerformed: false
                    }
                    let notData = await createData(Model.Notifications, query)
                    if (notData) {
                        let toIdData = await getRequired(Model.Users, {
                            _id: participant,
                            isDeleted: false
                        }, {}, { lean: true })
                        let groupData = await getRequired(Model.VenueGroups, { _id: payloadData.venueId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.venueId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_VENUE,
                            msg: userData.fullName + ' requested you to join ' + groupData[0].venueTitle + ' venue'
                        };
                        socketManager.requestCount(participant)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                    // await updateData(Model.VenueGroups, {_id: payloadData.groupId}, {$inc: {memberCount: 1}, $push: {memberIds: mongoose.Types.ObjectId(participant)}}, {new: true, lean: true})

                }
                return (Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.NOTIFICATION_SEND_TO_ALL)
            }
        } else {
            // let check = await getRequired(Model.PostGroups, {_id: payloadData.groupId, isDeleted: false, adminId: userData._id}, {}, {lean: true})
            // if(!check.length){
            //     return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_ADMIN)
            // }
            if (payloadData.participants && payloadData.participants.length) {
                let criteria = {}
                if (payloadData.groupId) {
                    criteria.groupId = payloadData.groupId
                }
                for (let participant of payloadData.participants) {
                    let checkUserExist = await getRequired(Model.PostGroupMembers, {
                        groupId: payloadData.groupId,
                        userId: participant,
                        isDeleted: false
                    }, {}, { lean: true })
                    if (checkUserExist.length) {
                        continue
                    }
                    criteria.toId = mongoose.Types.ObjectId(participant)
                    criteria.type = Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP
                    criteria.groupType = "GROUP"
                    let check = await getRequired(Model.Notifications, criteria, {}, { lean: true })
                    if (check.length) {
                        await updateData(Model.Notifications, criteria, {
                            $set: {
                                isDeleted: false,
                                actionPerformed: false,
                                isRejected: false,
                                isRead: false,
                                createdOn: +new Date
                            }
                        }, { new: true, lean: true })
                        let toIdData = await getRequired(Model.Users, {
                            _id: participant,
                            isDeleted: false
                        }, {}, { lean: true })
                        let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                            msg: userData.fullName + ' requested you to join ' + groupData[0].groupName + ' group'
                        };
                        socketManager.requestCount(participant)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        continue
                    }
                    let query = {
                        groupId: payloadData.groupId,
                        toId: participant,
                        text: userData.fullName + ' requested you to join ' + groupData[0].groupName + ' group',
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                        groupType: "GROUP",
                        createdOn: +new Date,
                        actionPerformed: false
                    }
                    let notData = await createData(Model.Notifications, query)
                    if (notData) {
                        let toIdData = await getRequired(Model.Users, { _id: participant }, {}, { lean: true })
                        let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.INVITE_GROUP,
                            msg: userData.fullName + ' requested you to join ' + groupData[0].groupName + ' group'
                        };
                        socketManager.requestCount(participant)
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                    // await updateData(Model.VenueGroups, {_id: payloadData.groupId}, {$inc: {memberCount: 1}, $push: {memberIds: mongoose.Types.ObjectId(participant)}}, {new: true, lean: true})

                }
                return (Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.NOTIFICATION_SEND_TO_ALL)
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let addParticipantsList = async (payloadData, userData) => {
    try {
        let followerList = []
        let criteria = {}
        criteria._id = userData._id
        let populate = [{
            path: "followers",
            select: "imageUrl userName fullName interestTags firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
            model: "Users",
            populate: {
                path: "interestTags",
                select: "categoryName",
                model: "Categories",
            }
        }]
        if (payloadData.venueId) {

            let venueMembers = await getRequired(Model.VenueGroupMembers, {
                groupId: payloadData.venueId,
                isDeleted: false
            }, {}, { lean: true })
            let venueRequest = await getRequired(Model.Notifications, {
                venueId: payloadData.venueId,
                type: "REQUEST_VENUE",
                actionPerformed: false
            }, {}, { lean: true })

            let venueMemberPreList = [], venueRequestList = []
            venueMembers.map(a => venueMemberPreList.push(a.userId))
            venueRequest.map(a => venueRequestList.push(a.byId))
            let data = await getRequiredPopulate(Model.Users, criteria, { followers: 1 }, { lean: true }, populate)
            for (let user of data[0].followers) {
                let tempObj = {}
                if (JSON.stringify(venueMemberPreList).includes(JSON.stringify(user._id)) || JSON.stringify(venueRequestList).includes(JSON.stringify(user._id))) {
                    continue
                }
                if (!user.imageVisibilityForEveryone) {
                    if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else if (user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else {
                        tempObj.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }
                    }
                } else {
                    tempObj.imageUrl = user.imageUrl
                }

                if (!user.nameVisibilityForEveryone) {
                    if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else if (user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else {
                        tempObj.userName = user.userName.substr(0, 3)
                        tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                    }
                } else {
                    tempObj.userName = user.userName
                    tempObj.fullName = user.fullName
                }
                tempObj._id = user._id
                tempObj.interestTags = user.interestTags

                followerList.push(tempObj)
            }
            return followerList
        } else if (payloadData.groupId) {
            let groupMembers = await getRequired(Model.PostGroupMembers, {
                groupId: payloadData.groupId,
                isDeleted: false
            }, {}, { lean: true })
            let groupRequest = await getRequired(Model.Notifications, {
                groupId: payloadData.groupId,
                type: "REQUEST_GROUP",
                actionPerformed: false
            }, {}, { lean: true })

            let groupMemberPreList = [], groupRequestList = []
            groupMembers.map(a => groupMemberPreList.push(a.userId))
            groupRequest.map(a => groupRequestList.push(a.byId))
            let data = await getRequiredPopulate(Model.Users, criteria, { followers: 1 }, { lean: true }, populate)
            for (let user of data[0].followers) {
                let tempObj = {}
                if (JSON.stringify(groupMemberPreList).includes(JSON.stringify(user._id)) || JSON.stringify(groupRequestList).includes(JSON.stringify(user._id))) {
                    continue
                }
                if (!user.imageVisibilityForEveryone) {
                    if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else if (user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.imageUrl = user.imageUrl
                    } else {
                        tempObj.imageUrl = {
                            "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                            "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        }
                    }
                } else {
                    tempObj.imageUrl = user.imageUrl
                }

                if (!user.nameVisibilityForEveryone) {
                    if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else if (user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    } else {
                        tempObj.userName = user.userName.substr(0, 3)
                        tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                    }
                } else {
                    tempObj.userName = user.userName
                    tempObj.fullName = user.fullName
                }
                tempObj._id = user._id
                tempObj.interestTags = user.interestTags

                followerList.push(tempObj)
            }

            return followerList
        } else {
            let data = await getRequiredPopulate(Model.Users, criteria, { followers: 1 }, { lean: true }, populate)
            for (let user of data[0].followers) {
                followerList.push(user)
            }

            return followerList
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description updating device token
 * @param {string} authorization
 * @param {string} deviceToken
 */

let updateDeviceToken = async (payloadData, userData) => {
    try {
        let criteria = {}
        let populate = [
            {
                path: "interestTags",
                select: "categoryName imageUrl createdOn",
                model: "Categories"
            }, {
                path: "personalInfoVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "imageVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "nameVisibility",
                select: "fullName imageUrl userName",
                model: "Users"
            }, {
                path: "tagPermission",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
        if (payloadData.deviceToken)
            criteria._id = userData._id
        criteria.isDeleted = false

        const updateDeviceInfo = { deviceToken: payloadData.deviceToken };
        if (payloadData.apnsDeviceToken) {
            updateDeviceInfo.apnsDeviceToken = payloadData.apnsDeviceToken;
        }

        let data = await updateData(Model.Users, criteria, { $set: updateDeviceInfo }, { new: true, lean: true })
        if (data) {
            let finalData = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
            let groupCount = await Service.count(Model.PostGroupMembers, { userId: userData._id, isDeleted: false })
            if (groupCount) {
                finalData[0].groupCount = groupCount
            } else {
                finalData[0].groupCount = 0
            }
            return finalData[0]
        }
    } catch (e) {
        console.log(e)
    }

}

/**
 * @description updating device token
 * @param {string} authorization
 * @param {string} flag
 * @param {string} userId
 * @param {boolean} action
 */

let configSetting = async (payloadData, userData) => {
    try {
        let criteria = {}, dataToSet = {}
        criteria._id = mongoose.Types.ObjectId(userData._id)
        criteria.isDeleted = false
        let populate = [{
            path: "interestTags",
            select: "categoryName imageUrl createdOn",
            model: "Categories"
        }, {
            path: "imageVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "nameVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "tagPermission",
            select: "fullName imageUrl userName",
            model: "Users"
        }, {
            path: "personalInfoVisibility",
            select: "fullName imageUrl userName",
            model: "Users"
        }]
        if (!payloadData.userIds) {
            payloadData.userIds = []
        }
        switch (payloadData.flag) {
            case 1: {
                let data = await getRequiredPopulate(Model.Users, criteria, {
                    isAccountPrivate: 1,
                    imageVisibilityForFollowers: 1,
                    imageVisibility: 1,
                    locationVisibility: 1,
                    nameVisibilityForFollowers: 1,
                    nameVisibility: 1,
                    tagPermissionForFollowers: 1,
                    tagPermission: 1,
                    alertNotifications: 1,
                    imageVisibilityForEveryone: 1,
                    nameVisibilityForEveryone: 1,
                    tagPermissionForEveryone: 1
                }, { lean: true }, populate)
                if (data.length) {
                    return data[0]
                }
            }
                break
            case 2: {
                let data
                if (payloadData.action === true) {
                    data = await updateData(Model.Users, criteria, {
                        $set: {
                            isAccountPrivate: true,
                            imageVisibilityForFollowers: true,
                            nameVisibilityForFollowers: true,
                            tagPermissionForFollowers: true,
                            tagPermissionForEveryone: false,
                            nameVisibilityForEveryone: false,
                            imageVisibilityForEveryone: false
                        }
                    }, { new: true, lean: true })
                } else if (payloadData.action === false) {
                    data = await updateData(Model.Users, criteria, { $set: { isAccountPrivate: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (data) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 3: {
                let updateDataUser
                if (payloadData.imageVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            imageVisibilityForFollowers: true,
                            imageVisibilityForEveryone: false,
                            imageVisibility: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.imageVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { imageVisibilityForFollowers: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.imageVisibilityForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            imageVisibilityForEveryone: true,
                            imageVisibilityForFollowers: false,
                            imageVisibility: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.imageVisibilityForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { imageVisibilityForEveryone: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.userIds && payloadData.userIds.length) {
                    // updateDataUser = await updateData(Model.Users, criteria, {$addToSet: {imageVisibility: {$each: payloadData.userIds}}}, {new: true, lean: true})
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            imageVisibility: payloadData.userIds,
                            imageVisibilityForEveryone: false,
                            imageVisibilityForFollowers: false
                        }
                    }, { new: true, lean: true })
                }
                if (payloadData.userIds && !payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { imageVisibility: [] } }, {
                        new: true,
                        lean: true
                    })
                }
                if (updateDataUser) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 4: {
                let updateDataUser
                if (payloadData.nameVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            nameVisibilityForFollowers: true,
                            nameVisibilityForEveryone: false,
                            nameVisibility: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.nameVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { nameVisibilityForFollowers: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.nameVisibilityForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            nameVisibilityForEveryone: true,
                            nameVisibilityForFollowers: false,
                            nameVisibility: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.nameVisibilityForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { nameVisibilityForEveryone: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.userIds && payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            nameVisibility: payloadData.userIds,
                            nameVisibilityForFollowers: false,
                            nameVisibilityForEveryone: false
                        }
                    }, { new: true, lean: true })
                }
                if (payloadData.userIds && !payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { nameVisibility: [] } }, {
                        new: true,
                        lean: true
                    })
                }
                if (updateDataUser) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 5: {
                let data
                if (payloadData.action) {
                    data = await updateData(Model.Users, criteria, { $set: { locationVisibility: true } }, {
                        new: true,
                        lean: true
                    })
                } else {
                    data = await updateData(Model.Users, criteria, { $set: { locationVisibility: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (data) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 6: {
                let updateDataUser
                if (payloadData.tagPermissionForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            tagPermissionForFollowers: true,
                            tagPermissionForEveryone: false,
                            tagPermission: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.tagPermissionForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { tagPermissionForFollowers: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.tagPermissionForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            tagPermissionForEveryone: true,
                            tagPermissionForFollowers: false,
                            tagPermission: []
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.tagPermissionForEveryone) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { tagPermissionForEveryone: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.userIds && payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            tagPermission: payloadData.userIds,
                            tagPermissionForEveryone: false,
                            tagPermissionForFollowers: false
                        }
                    }, { new: true, lean: true })
                }
                if (payloadData.userIds && !payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { tagPermission: [] } }, {
                        new: true,
                        lean: true
                    })
                }
                if (updateDataUser) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 7: {
                let updateDataUser
                if (payloadData.personalInfoVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, {
                        $set: {
                            personalInfoVisibilityForFollowers: true,
                            personalInfoVisibility: [],
                            personalInfoVisibilityForEveryone: false
                        }
                    }, { new: true, lean: true })
                }
                if (!payloadData.personalInfoVisibilityForFollowers) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { personalInfoVisibilityForFollowers: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.userIds && payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { personalInfoVisibility: payloadData.userIds } }, {
                        new: true,
                        lean: true
                    })
                }
                if (payloadData.userIds && !payloadData.userIds.length) {
                    updateDataUser = await updateData(Model.Users, criteria, { $set: { personalInfoVisibility: [] } }, {
                        new: true,
                        lean: true
                    })
                }
                if ("personalInfoVisibilityForEveryone" in payloadData) {
                    let dataToUp = {
                        personalInfoVisibilityForEveryone: payloadData.personalInfoVisibilityForEveryone
                    };

                    if (payloadData.personalInfoVisibilityForEveryone) {
                        dataToUp.personalInfoVisibility = [];
                        dataToUp.personalInfoVisibilityForFollowers = false;
                    }

                    updateDataUser = await updateData(Model.Users, criteria, { $set: dataToUp }, { new: true, lean: true })
                }
                if (updateDataUser) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
                break
            case 8: {
                let data
                if (payloadData.action) {
                    data = await updateData(Model.Users, criteria, { $set: { alertNotifications: true } }, {
                        new: true,
                        lean: true
                    })
                } else {
                    data = await updateData(Model.Users, criteria, { $set: { alertNotifications: false } }, {
                        new: true,
                        lean: true
                    })
                }
                if (data) {
                    let userDataFinal = await getRequiredPopulate(Model.Users, criteria, {}, { lean: true }, populate)
                    return userDataFinal[0]
                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let hidePersonalInfo = async (payloadData, userData) => {
    try {
        let criteria = {}, dataToUpdate = {}
        criteria.isDeleted = false
        criteria._id = userData._id
        if (payloadData.profileImage === 1) {
            dataToUpdate.hideProfileImage = true
        }

        if (payloadData.profileImage === 2) {
            dataToUpdate.hideProfileImage = false
        }

        if (payloadData.firstName === 1) {
            dataToUpdate.hideFirstName = true
        }

        if (payloadData.firstName === 2) {
            dataToUpdate.hideFirstName = false
        }

        if (payloadData.lastName === 1) {
            dataToUpdate.hideLastName = true
        }

        if (payloadData.lastName === 2) {
            dataToUpdate.hideLastName = false
        }

        if (payloadData.locationDetails === 1) {
            dataToUpdate.hideLocationDetails = true
        }

        if (payloadData.locationDetails === 2) {
            dataToUpdate.hideLocationDetails = false
        }

        if (payloadData.userName === 1) {
            dataToUpdate.hideUserName = true
        }

        if (payloadData.userName === 2) {
            dataToUpdate.hideUserName = false
        }

        let updateUserInfo = await updateData(Model.Users, criteria, { $set: dataToUpdate }, { new: true, lean: true })
        if (updateUserInfo) {
            return updateUserInfo
        }
    } catch (e) {
        console.log(e)
    }
}
/**
 * @description updating device token
 * @param {string} authorization
 * @param {string} acceptType
 * @param {string} groupType
 * @param {string} userId
 * @param {boolean} action
 */

let acceptInviteRequest = async (payloadData, userData) => {
    try {
        if (payloadData.acceptType == "INVITE") {
            // for invite acception of requests
            if (payloadData.groupType == "VENUE") {
                // venue case
                if (payloadData.accept === true) {
                    // check whether already a member or not
                    let check = await updateData(Model.VenueGroupMembers, {
                        userId: mongoose.Types.ObjectId(payloadData.userId),
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        isDeleted: true
                    }, { $set: { isDeleted: false, joinedAt: +new Date } }, { new: true, lean: true })
                    if (check) {
                        await updateData(Model.VenueGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCount: 1 }, $push: { memberIds: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: "INVITE_VENUE",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_VENUE,
                            msg: toIdData[0].fullName + ' has accepted your invite'
                        };
                        socketManager.requestCount(userData._id)
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            text: toIdData[0].fullName + ' has accepted your invite',
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_VENUE,
                            byId: notData.toId,
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        return
                    }

                    let query = {
                        userId: userData._id,
                        groupId: payloadData.groupId,
                        joinedAt: +new Date()
                    }
                    // create new member
                    let data = await createData(Model.VenueGroupMembers, query)
                    if (data) {
                        // group details update
                        await updateData(Model.VenueGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCount: 1 }, $push: { memberIds: userData._id } }, { new: true, lean: true })
                        // updating notification details
                        let notData = await updateData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: "INVITE_VENUE",
                            toId: userData._id,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })
                        let groupData = await getRequired(Model.VenueGroups, { _id: payloadData.groupId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_VENUE,
                            msg: toIdData[0].fullName + ' has accepted your invite'
                        };
                        let pushDataForJoiner = {
                            id: payloadData.groupId,
                            conversationId: groupData[0].conversationId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_VENUE,
                            msg: 'You have joined the ' + groupData[0].venueTitle + ' venue'
                        };
                        // push notificiation
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        //pushNotification.sendPush(toIdData[0].deviceToken, pushDataForJoiner, (err, res)=>{console.log(err, res)})
                        socketManager.requestCount(userData._id)

                        // creating new notifications
                        await createData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_VENUE,
                            text: toIdData[0].fullName + ' has accepted your invite',
                            byId: notData.toId,
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        await createData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            conversationId: groupData[0].conversationId,
                            text: 'You have joined the ' + groupData[0].venueTitle + ' venue',
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_VENUE,
                            byId: notData.byId,
                            toId: notData.toId,
                            createdOn: +new Date
                        })
                    }
                } else if (payloadData.accept === false) {
                    await socketManager.requestCount(userData._id)
                    await updateData(Model.Notifications, {
                        venueId: payloadData.groupId,
                        type: "INVITE_VENUE",
                        toId: userData._id,
                        isDeleted: false
                    }, { $set: { actionPerformed: true, isRejected: true, isRead: true, isDeleted: true } }, {
                        new: true,
                        lean: true
                    })
                }

            } else {
                if (payloadData.accept === true) {
                    // checking weather person is already a member
                    let check = await updateData(Model.PostGroupMembers, {
                        userId: mongoose.Types.ObjectId(payloadData.userId),
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        isDeleted: true
                    }, { $set: { isDeleted: false, joinedAt: +new Date } }, { new: true, lean: true })
                    if (check) {
                        await updateData(Model.PostGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCounts: 1 }, $push: { isMember: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: "INVITE_GROUP",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })

                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_GROUP,
                            msg: toIdData[0].fullName + ' has accepted your request'
                        };
                        socketManager.requestCount(userData._id)
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_GROUP,
                            byId: notData.toId,
                            text: toIdData[0].fullName + ' has accepted your request',
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        return
                    }

                    let query = {
                        userId: userData._id,
                        groupId: payloadData.groupId,
                        joinedAt: +new Date()
                    }
                    // creating new member
                    let data = await createData(Model.PostGroupMembers, query)
                    if (data) {
                        // updating group info
                        await updateData(Model.PostGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCounts: 1 }, $push: { isMember: userData._id } }, { new: true, lean: true })
                        // updating notification details
                        let notData = await updateData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: "INVITE_GROUP",
                            toId: userData._id,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })
                        let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_GROUP,
                            msg: toIdData[0].fullName + ' has accepted your invite'
                        };
                        let pushDataForJoiner = {
                            id: payloadData.groupId,
                            conversationId: groupData[0].conversationId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_GROUP,
                            msg: 'You have joined the ' + groupData[0].groupName + ' group'
                        };
                        // push notification
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        //pushNotification.sendPush(toIdData[0].deviceToken, pushDataForJoiner, (err, res)=>{console.log(err, res)})
                        socketManager.requestCount(userData._id)
                        // creating new notifications
                        await createData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_INVITE_GROUP,
                            text: toIdData[0].fullName + ' has accepted your invite',
                            byId: notData.toId,
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        await createData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            conversationId: groupData[0].conversationId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.JOINED_GROUP,
                            text: 'You have joined the ' + groupData[0].groupName + ' group',
                            byId: notData.byId,
                            toId: notData.toId,
                            createdOn: +new Date
                        })
                    }
                } else if (payloadData.accept === false) {
                    await socketManager.requestCount(userData._id)
                    await updateData(Model.Notifications, {
                        groupId: payloadData.groupId,
                        type: "INVITE_GROUP",
                        toId: userData._id,
                        isDeleted: false
                    }, { $set: { actionPerformed: true, isRejected: true, isRead: true, isDeleted: true } }, {
                        new: true,
                        lean: true
                    })

                }

            }
        } else if (payloadData.acceptType == "FOLLOW") {
            if (payloadData.accept === true) {
                let update = {}, updateOther = {}, ownCriteria = {}, otherCriteria = {};
                ownCriteria._id = userData._id;

                update.$addToSet = {
                    followers: payloadData.userId
                };

                update.$inc = {
                    followerCount: 1
                }
                otherCriteria._id = payloadData.userId;
                // otherCriteria['followers.userId'] = {$ne : userData._id};

                updateOther.$addToSet = {
                    following: userData._id
                };

                updateOther.$inc = {
                    followingCount: 1
                }
                await updateData(Model.Users, ownCriteria, update, { new: true });

                let notPush = await updateData(Model.Users, otherCriteria, updateOther, { new: true });
                if (notPush) {

                    let checkNotification = await getRequired(Model.Notifications, {
                        toId: payloadData.userId,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                        isDeleted: true
                    }, {}, { lean: true })

                    if (checkNotification && checkNotification.length) {

                        await updateData(Model.Notifications, {
                            toId: payloadData.userId,
                            byId: userData._id,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                            isDeleted: true
                        }, { $set: { createdOn: +new Date, isDeleted: false } }, { lean: true, new: true })

                    } else {

                        await createData(Model.Notifications, {
                            toId: payloadData.userId,
                            byId: userData._id,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                            text: userData.fullName + ' has accepted your follow request',
                            createdOn: +new Date
                        })

                    }
                    await updateData(Model.Notifications, {
                        toId: userData._id,
                        byId: payloadData.userId,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW
                    }, { isRead: true, actionPerformed: true, isDeleted: true }, { lean: true })
                    let toIdData = await getRequired(Model.Users, { _id: payloadData.userId }, {}, { lean: true })

                    let pushData = {
                        id: userData._id,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                        msg: userData.fullName + ' has accepted your follow request'
                    };
                    socketManager.requestCount(userData._id)
                    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                        console.log(err, res)
                    })
                }
                return
            } else if (payloadData.accept === false) {
                await socketManager.requestCount(userData._id)
                await updateData(Model.Notifications, {
                    toId: userData._id,
                    byId: payloadData.userId,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW
                }, { isRead: true, actionPerformed: true, isDeleted: true }, { lean: true })

            }
        } else if (payloadData.acceptType == "REQUEST") {
            if (payloadData.groupType === "VENUE") {
                if (payloadData.accept === true) {
                    let check = await updateData(Model.VenueGroupMembers, {
                        userId: mongoose.Types.ObjectId(payloadData.userId),
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        isDeleted: true
                    }, { $set: { isDeleted: false, joinedAt: +new Date } }, { new: true, lean: true })
                    if (check) {
                        let venueGroupData = await updateData(Model.VenueGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCount: 1 }, $push: { memberIds: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: "REQUEST_VENUE",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            conversationId: venueGroupData.conversationId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_VENUE,
                            msg: toIdData[0].fullName + ' has accepted your request'
                        };
                        socketManager.requestCount(userData._id)
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            conversationId: venueGroupData.conversationId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_VENUE,
                            text: toIdData[0].fullName + ' has accepted your request',
                            byId: notData.toId,
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        return
                    }

                    let query = {
                        userId: payloadData.userId,
                        groupId: payloadData.groupId,
                        joinedAt: +new Date()
                    }
                    let data = await createData(Model.VenueGroupMembers, query)
                    if (data) {
                        await updateData(Model.VenueGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCount: 1 }, $push: { memberIds: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: "REQUEST_VENUE",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })
                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_VENUE,
                            msg: toIdData[0].fullName + ' has accepted your request'
                        };
                        socketManager.requestCount(userData._id)
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            venueId: payloadData.groupId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_VENUE,
                            byId: notData.toId,
                            text: toIdData[0].fullName + ' has accepted your request',
                            toId: notData.byId,
                            createdOn: +new Date
                        })

                        return
                    }
                } else if (payloadData.accept === false) {
                    await socketManager.requestCount(userData._id)
                    await updateData(Model.Notifications, {
                        venueId: payloadData.groupId,
                        type: "REQUEST_VENUE",
                        byId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { actionPerformed: true, isRejected: true, isRead: true, isDeleted: true } }, {
                        new: true,
                        lean: true
                    })

                }

            } else {
                if (payloadData.accept === true) {
                    let check = await updateData(Model.PostGroupMembers, {
                        userId: mongoose.Types.ObjectId(payloadData.userId),
                        groupId: mongoose.Types.ObjectId(payloadData.groupId),
                        isDeleted: true
                    }, { $set: { isDeleted: false, joinedAt: +new Date } }, { new: true, lean: true })
                    if (check) {

                        let groupData = await updateData(Model.PostGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCounts: 1 }, $push: { isMember: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: "REQUEST_GROUP",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })

                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })

                        let pushData = {
                            id: payloadData.groupId,
                            conversationId: groupData.conversationId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_GROUP,
                            msg: toIdData[0].fullName + ' has accepted your request'
                        };
                        socketManager.requestCount(userData._id)
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            conversationId: groupData.conversationId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_GROUP,
                            text: toIdData[0].fullName + ' has accepted your request',
                            byId: notData.toId,
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        return
                    }

                    let query = {
                        userId: payloadData.userId,
                        groupId: payloadData.groupId,
                        joinedAt: +new Date()
                    }
                    let data = await createData(Model.PostGroupMembers, query)
                    if (data) {
                        await updateData(Model.PostGroups, {
                            _id: payloadData.groupId,
                            isDeleted: false
                        }, { $inc: { memberCounts: 1 }, $push: { isMember: payloadData.userId } }, { new: true, lean: true })
                        let notData = await updateData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: "REQUEST_GROUP",
                            byId: payloadData.userId,
                            isDeleted: false
                        }, { $set: { actionPerformed: true, isRead: true, isDeleted: true } }, { new: true, lean: true })
                        let byIdData = await getRequired(Model.Users, { _id: notData.byId }, {}, { lean: true })
                        let toIdData = await getRequired(Model.Users, { _id: notData.toId }, {}, { lean: true })

                        let pushData = {
                            id: payloadData.groupId,
                            byId: notData.byId,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_GROUP,
                            msg: toIdData[0].fullName + ' has accepted your request'
                        };
                        pushNotification.sendPush(byIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        await createData(Model.Notifications, {
                            groupId: payloadData.groupId,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_GROUP,
                            byId: notData.toId,
                            text: toIdData[0].fullName + ' has accepted your request',
                            toId: notData.byId,
                            createdOn: +new Date
                        })
                        socketManager.requestCount(userData._id)

                        return
                    }
                } else if (payloadData.accept === false) {
                    await socketManager.requestCount(userData._id)
                    await updateData(Model.Notifications, {
                        groupId: payloadData.groupId,
                        type: "REQUEST_GROUP",
                        byId: payloadData.userId,
                        isDeleted: false
                    }, { $set: { actionPerformed: true, isRejected: true, isRead: true, isDeleted: true } }, {
                        new: true,
                        lean: true
                    })

                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description reading notifications
 * @param {string} authorization
 * @param {string} notificationId
 */

let readNotifications = async (payloadData, userdata) => {
    try {
        let criteria = {}
        let dataToUpdate = {}
        if (payloadData.notificationId)
            criteria._id = payloadData.notificationId
        criteria.isDeleted = false
        dataToUpdate.isRead = true
        let data = await updateData(Model.Notifications, criteria, { $set: dataToUpdate }, { new: true, lean: true })
        if (data)
            return data
    } catch (e) {
        console.log(e)
    }
};

let rejectRequest = async (payloadData, userdata) => {
    try {
        let criteria = {}
        let dataToUpdate = {}
        if (payloadData.notificationId)
            criteria._id = payloadData.notificationId
        criteria.isDeleted = false
        dataToUpdate.isRead = true
        dataToUpdate.isRejected = true
        let data = await updateData(Model.Notifications, criteria, { $set: dataToUpdate }, { new: true, lean: true })
        if (data)
            return data
    } catch (e) {
        console.log(e)
    }
};

/**
 * @description get unread notifications
 * @param {string} authorization
 */

let unreadCount = async (payloadData, userdata) => {
    try {
        let data;
        let criteria = { toId: userdata._id, isRead: false, isDeleted: false };
        data = await Service.count(Model.Notifications, criteria)
        console.log(data)
        if (data)
            return { count: data }
        else
            return { count: 0 }
    } catch (e) {
        console.log(e)
    }
};

/**
 * @description user logout
 * @param {string} authorization
 */

let logOut = async (payloadData, userData) => {
    try {
        let criteria = {}
        criteria._id = userData._id
        criteria.isDeleted = false
        let data = await updateData(Model.Users, criteria, {
            $set: {
                deviceToken: "",
                accessToken: "",
                locationTime: 0,
                locationName: "",
                locationAddress: ""
            }
        }, { new: true, lean: true })

        if (data)
            return { message: "Successfully LogOut" }
    } catch (e) {
        console.log(e)
    }
}
/*=================Post Group Related Apis==================*/
/*==============Post Group Related Apis===================*/

/*=================Post Page Related Apis==================*/
/*==============Post Page Related Apis===================*/

const userChallengeData = async (userId) => {
    try {
        let criteria = {
            user_id: mongoose.Types.ObjectId(userId),
            status: Config.APP_CONSTANTS.userChallengeStatus.INPROGESS
        };
        const pipeline = [
            { $match: criteria },
            {
                $lookup: {

                    from: "challenges",
                    let: { challenge_id: "$challenge_id" },
                    pipeline: [
                        {
                            $match: {
                                $expr: {
                                    $and:
                                        [
                                            { $eq: ["$_id", "$$challenge_id"] }
                                        ]
                                }
                            }
                        }
                    ],
                    as: "challengeData"
                }

            },
            {
                $unwind: "$challengeData"
            },
            {
                $lookup: {

                    from: "user_challenge_shares",
                    let: { userChallenge_id: "$_id" },
                    pipeline: [
                        {
                            $match: {
                                $expr: {
                                    $and:
                                        [
                                            { $eq: ["$userChallenge_id", "$$userChallenge_id"] }
                                        ]
                                }
                            }
                        }
                    ],
                    as: "userchallengeSharedData"
                }

            },
            {
                $unwind: {
                    path: "$userchallengeSharedData",
                    preserveNullAndEmptyArrays: true
                }
            },
            //  {

            //    $addFields:{
            //        noOfPostShared:{ $size:"$userchallengeSharedData.sharedPost"}
            //        }


            //      }
        ];

        let [result] = await Model.UserChallenges.aggregate(pipeline);

        //  console.log("resultresult", await Model.UserChallenges.aggregate(pipeline));
        // console.log(JSON.stringify(pipeline) );
        console.log(result);

        return result;
    } catch (e) {
        return [];
    }
};


/**
 * @description: creating and editing post
 * @param {string} authorization
 * @param {string} postId
 * @param {string} groupId
 * @param {string} postText
 * @param {string} postImageVideoOriginal
 * @param {string} postImageVideoThumbnail
 * @param {string} type
 * @param {Array} hashTags
 * @returns: saved post data
 */

let addEditPost = async (payloadData, userData) => {
    try {
        let query = {}
        query.imageUrl = {}
        if (payloadData.groupId)
            query.groupId = payloadData.groupId
        if (payloadData.postType)
            query.postType = payloadData.postType
        if (payloadData.postingIn)
            query.postingIn = payloadData.postingIn
        if (payloadData.postText === "" || payloadData.postText) {
            query.postText = payloadData.postText
        }
        if (payloadData.selectInterests)
            query.selectInterests = payloadData.selectInterests
        if (payloadData.locationName)
            query.locationName = payloadData.locationName
        if (payloadData.locationAddress)
            query.locationAddress = payloadData.locationAddress
        if (payloadData.meetingTime)
            query.meetingTime = payloadData.meetingTime
        if (payloadData.expirationTime)
            query.expirationTime = payloadData.expirationTime
        if (payloadData.selectedPeople && payloadData.selectedPeople.length)
            query.selectedPeople = payloadData.selectedPeople
        if (payloadData.locationLat && payloadData.locationLong)
            query.location = [payloadData.locationLong, payloadData.locationLat]

        if (payloadData.imageOriginal && payloadData.imageThumbnail) {
            query.imageUrl.original = payloadData.imageOriginal
            query.imageUrl.thumbnail = payloadData.imageThumbnail
            query.type = Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE
        } else {
            query.type = Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT
        }

        if (payloadData.imageOriginal === '""' && payloadData.imageThumbnail === '""') {
            query.imageUrl.original = ""
            query.imageUrl.thumbnail = ""
            query.type = Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT
        }

        if (payloadData.hashTags)
            query.hashTags = payloadData.hashTags

        // for media object
        if (payloadData.media) {
            query.media = payloadData.media
        }
        console.log('+++', query);
        if (payloadData.postId) {
            let criteria = {
                isDeleted: false,
                _id: payloadData.postId
            }
            let data = updateData(Model.Posts, criteria, { $set: query }, { new: true, lean: true })
            if (data)
                return data

        } else {

            query.createdOn = +new Date
            query.postBy = userData._id
            query.readBy = [userData._id]
            let postCat = await getRequired(Model.PostGroups, {
                _id: payloadData.groupId,
                isDeleted: false
            }, { categoryId: 1, isMember: 1, adminId: 1 }, { lean: true })
            if (postCat.length) {
                query.postCategoryId = postCat[0].categoryId
                if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(postCat[0].adminId))) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.BLOCKED_BY_ADMIN)
                }
            }
            //console.log(query);
            let data = await Service.saveData(Model.Posts, query);
            if (data) {


                let videoExist = false;
                let imageExist = false;
                data.media.map((rec) => {
                    if (rec.mediaType === Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO) {
                        videoExist = true;
                    } else if (rec.mediaType === Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE) {
                        imageExist = true;
                    }
                })


                const userChallenge = await userChallengeData(userData._id);

                console.log(videoExist);
                console.log(imageExist);
                console.log(userChallenge);
                console.log("data>>>>>");
                console.log(data);


                const dateFormat = (dateConversion, returnDateObj = true, format = "YYYY-MM-DD") => {

                    if (returnDateObj) {
                        return new Date(moment(dateConversion).format(format));
                    }
                    return moment(dateConversion).format(format);
                };


                if (userChallenge) {
                    const { challengeData, challengeData: { quantity: challengeQuantity, rewardPoint: challengeRewardPoint } } = userChallenge;
                    // const noOfPostShared = userChallenge.noOfPostShared;
                    const noOfPostShared = userChallenge.userchallengeSharedData && userChallenge.userchallengeSharedData.sharedPost ? userChallenge.userchallengeSharedData.sharedPost.length : 0;


                    const currenDate = dateFormat(moment().utc().format("YYYY-MM-DDT00:00:00Z"));
                    const startDate = dateFormat(challengeData.startDate);
                    const endDate = dateFormat(challengeData.endDate);

                    if (userChallenge && challengeData && (startDate >= currenDate) && (currenDate <= endDate)) {

                        if (
                            (imageExist && challengeData.challengeType === Config.APP_CONSTANTS.challengeType.PHOTO) ||
                            (videoExist && challengeData.challengeType === Config.APP_CONSTANTS.challengeType.VIDEO)
                        ) {
                            const updaterec = {
                                userChallenge_id: userChallenge._id,
                            };

                            console.log(updaterec, imageExist)
                            console.log(videoExist)

                            const a = await updateData(Model.UserChallengesShare, { userChallenge_id: userChallenge._id }, {
                                $set: updaterec,
                                $push: {
                                    sharedPost: {
                                        post_id: data._id
                                    }
                                }
                            }, { new: true, lean: true, upsert: true });

                            console.log(a)

                            console.log("noOfPostShared", noOfPostShared)
                            // quantity

                            const incnoOfPostShared = noOfPostShared + 1;
                            if (challengeQuantity === incnoOfPostShared) {

                                let saveData = {
                                    userId: userData._id,
                                    pointEarned: challengeRewardPoint,
                                    source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.CHALLENGE,
                                    date: moment().format('MM/DD/YYYY')
                                }

                                await updateData(Model.Users, { _id: userData._id }, { $inc: { pointEarned: challengeRewardPoint } });
                                await saveData(Models.PointEarnedHistory, saveData);
                                await updateData(Model.UserChallenges, { _id: userChallenge._id }, { $set: { status: Config.APP_CONSTANTS.userChallengeStatus.COMPLETED } });
                            }

                            //quantity


                        }

                    } else {


                        const incnoOfPostShared = noOfPostShared;
                        if (challengeQuantity === incnoOfPostShared) {

                            let saveData2 = {
                                userId: userData._id,
                                pointEarned: challengeRewardPoint,
                                source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.CHALLENGE,
                                date: moment().format('MM/DD/YYYY')
                            }

                            await updateData(Model.Users, { _id: userData._id }, { $inc: { pointEarned: challengeRewardPoint } });
                            await saveData(Models.PointEarnedHistory, saveData);
                            await updateData(Model.UserChallenges, { _id: userChallenge._id }, { $set: { status: Config.APP_CONSTANTS.userChallengeStatus.COMPLETED } });
                        } else {
                            await updateData(Model.UserChallenges, { _id: userChallenge._id }, { $set: { status: Config.APP_CONSTANTS.userChallengeStatus.COMPLETED } });
                        }


                    }


                }

                debug(userChallenge);
                debug("await userChallengeData(userData._id)");

                // data
                // media check media
                /*
  media: [{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
        videoUrl: {type:String,default:""},
        mediaType: {type: String, enum: [
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
            Configs.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF,
        ]},
        likes:[likes],
        likeCount:{type:Number,default:0},
    }],
                */


                if (payloadData.postType === Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY || payloadData.postType === Config.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY) {
                    payloadData.interPostId = data._id
                    payloadData.createdOn = data.createdOn
                    await postPushHelper(payloadData, userData)
                }

                let hashTagsArray = []
                if (payloadData.postText) {
                    let splitArray = payloadData.postText.split(" ")
                    //      console.log('+++++++++', splitArray)
                    if (splitArray.length) {
                        for (let word of splitArray) {
                            console.log('++++++++++++++', word.includes('#'))
                            if (word.includes('#')) {
                                hashTagsArray.push(word)
                            }
                        }
                    }
                }
                //    console.log('+++++++++++++hashTagsArrayhashTagsArray', hashTagsArray)
                if (hashTagsArray && hashTagsArray.length) {
                    for (let tags of hashTagsArray) {
                        // let finalTag = "#"+tags;
                        let finalTag = tags;
                        let checkTags = await getRequired(Model.Tags, {
                            tagName: finalTag,
                            isDeleted: false
                        }, {}, { lean: true })
                        if (!checkTags.length) {
                            await createData(Model.Tags, {
                                tagName: finalTag,
                                imageUrl: {
                                    original: Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                    thumbnail: Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                                }
                            })
                        }
                    }
                }

                let postByData = await getRequired(Model.Users, { _id: userData._id }, { fullName: 1 }, { lean: true })
                if (payloadData.groupId) {
                    await updateData(Model.PostGroups, {
                        _id: payloadData.groupId,
                        isDeleted: false
                    }, { $set: { noPost: false, isArchive: [] } }, {}, { new: true, lean: true })
                    if (postCat[0].isMember.length) {
                        let deviceTokenMembers = []
                        for (let indi of postCat[0].isMember) {
                            if (JSON.stringify(userData.blockedWhom).includes(JSON.stringify(indi))) {
                                continue
                            }
                            let notificationCheck = await getRequired(Model.PostGroupMembers, {
                                groupId: payloadData.groupId,
                                userId: indi
                            }, { isNotify: 1 }, { lean: true })
                            if ((JSON.stringify(indi) == JSON.stringify(userData._id)) || !notificationCheck[0].isNotify) {
                                continue
                            }
                            let deviceTokenData = await getRequired(Model.Users, { _id: indi }, { deviceToken: 1 }, { lean: true })
                            await createData(Model.Notifications, {
                                groupId: payloadData.groupId,
                                postId: data._id,
                                toId: indi,
                                byId: userData._id,
                                text: postByData[0].fullName + " has posted on your " + groupData[0].groupName + " group",
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.POST,
                                createdOn: +new Date
                            })
                            deviceTokenMembers.push(deviceTokenData[0].deviceToken)
                        }


                        let groupData = await getRequired(Model.PostGroups, { _id: payloadData.groupId }, { groupName: 1 }, { lean: true })

                        let pushData = {
                            id: data._id,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.POST,
                            msg: postByData[0].fullName + " has posted on your " + groupData[0].groupName + " group"
                        }
                        pushNotification.sendMultiUser(deviceTokenMembers, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                }
                return data
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let deletePost = async (payloadData, userData) => {
    let criteria = {}
    if (payloadData.postId)
        criteria._id = payloadData.postId

    let deletePost = await updateData(Model.Posts, criteria, { $set: { isDeleted: true } }, { new: true, lean: true })
    if (deletePost) {
        return Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.DELETED
    } else {
        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
    }

}

// let deleteCommentReply = async (payloadData, userData)=>{
//     let criteria = {}, model, modelForReply, criteriaForReply = {}
//     if(payloadData.commentId){
//         criteria._id = payloadData.commentId
//         model = Model.Comments
//         modelForReply = Model.Replies
//         criteriaForReply.commentId = payloadData.commentId
//     }

//     if(payloadData.replyId){
//         criteria._id = payloadData.replyId
//         model = Model.Replies
//     }

//     let deleteCommentReplyData = await updateData(model, criteria, {$set: {isDeleted: true}}, {new: true, lean:true})
//     if(payloadData.commentId){
//         await Service.update(modelForReply, criteriaForReply, {$set: {isDeleted: true}}, {multi: true})
//     }
//     if(deleteCommentReplyData){
//         return Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.DELETED
//     }else{
//         return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
//     }

// }

async function postPushHelper(data, userData) {
    try {
        let populate = [{
            path: "postBy",
            select: "deviceToken _id alertNotifications followers",
            model: "Users"
        }]
        if (data.postType == Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY) {
            if (data.selectInterests && data.selectInterests.length) {
                let selectInterests = [];
                data.selectInterests.map(obj => selectInterests.push(mongoose.Types.ObjectId(obj)))
                let locationPosts = await aggregateWithPopulate(Model.Posts, [{
                    $geoNear: {
                        near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                        distanceField: "calculated",
                        maxDistance: 2000,
                        spherical: true,
                        distanceMultiplier: 0.000621371,
                        query: {
                            postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY,
                            selectInterests: { $in: selectInterests },
                            postBy: { "$nin": userData.blockedBy },
                            isDeleted: false,
                            postingIn: data.postingIn
                        }
                    }
                },
                {
                    $addFields: {
                        newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                        newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                        newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                    }
                },
                {
                    $match: {
                        newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                        newMeetingDay: new Date(data.meetingTime).getDate(),
                        newMeetingYear: new Date(data.meetingTime).getFullYear(),
                        postBy: { $ne: userData._id }
                    }
                },
                {
                    $project: {
                        _id: 0,
                        postBy: 1
                    }
                }], populate)
                let deviceTokenArray = []

                if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS) {
                    for (let obj of locationPosts) {
                        if (obj.postBy && obj.postBy._id && (JSON.stringify(userData.followers).includes(JSON.stringify(obj.postBy._id))) && JSON.stringify(obj.postBy.followers).includes(JSON.stringify(userData._id))) {
                            if (obj.postBy.alertNotifications) {
                                deviceTokenArray.push(obj.postBy.deviceToken)
                            }
                            await createData(Model.Notifications, {
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                toId: mongoose.Types.ObjectId(obj.postBy._id),
                                byId: userData._id,
                                userName: userData.userName,
                                text: userData.fullName + " has posted in your local area",
                                locationName: data.locationName,
                                locationAddress: data.locationAddress,
                                postId: data.interPostId,
                                location: [data.locationLong, data.locationLat],
                                createdOn: data.createdOn
                            })

                            let mutualPostUsers = await aggregateData(Model.Posts, [{
                                $geoNear: {
                                    near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                    distanceField: "calculated",
                                    maxDistance: 2000,
                                    spherical: true,
                                    distanceMultiplier: 0.000621371,
                                    query: {
                                        postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY,
                                        postBy: obj.postBy._id,
                                        isDeleted: false,
                                        postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS
                                    }
                                }
                            }, {
                                $addFields: {
                                    newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                    newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                    newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                }
                            },
                            {
                                $match: {
                                    newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                    newMeetingDay: new Date(data.meetingTime).getDate(),
                                    newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                    postBy: { $ne: userData._id }
                                }
                            },
                            {
                                $project: {
                                    _id: 1,
                                    postBy: 1,
                                    imageUrl: 1,
                                    postText: 1,
                                    location: 1,
                                    locationName: 1,
                                    locationAddress: 1,
                                }
                            }])

                            if (mutualPostUsers.length) {
                                for (let post of mutualPostUsers) {
                                    let postByData = await getRequired(Model.Users, {
                                        _id: post.postBy,
                                        isDeleted: false
                                    }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                    let pushDataForDemander = {
                                        postId: post._id,
                                        userName: postByData[0].userName,
                                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                        msg: postByData[0].fullName + " has posted in your local area"
                                    }
                                    await createData(Model.Notifications, {
                                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                        toId: userData._id,
                                        byId: mongoose.Types.ObjectId(obj.postBy._id),
                                        userName: postByData[0].userName,
                                        text: postByData[0].fullName + " has posted in your local area",
                                        locationName: post.locationName,
                                        locationAddress: post.locationAddress,
                                        postId: post._id,
                                        location: post.location,
                                        createdOn: post.createdOn
                                    })
                                    if (userData.alertNotifications) {
                                        await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                            console.log(err, res)
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.SELECTED_PEOPLE) {
                    for (let obj of locationPosts) {
                        if (obj.postBy && obj.postBy._id && (JSON.stringify(data.selectedPeople).includes(JSON.stringify(obj.postBy._id)))) {
                            if (obj.postBy.alertNotifications) {
                                deviceTokenArray.push(obj.postBy.deviceToken)
                            }
                            await createData(Model.Notifications, {
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                toId: mongoose.Types.ObjectId(obj.postBy._id),
                                byId: userData._id,
                                text: userData.fullName + " has posted in your local area",
                                userName: userData.userName,
                                locationName: data.locationName,
                                locationAddress: data.locationAddress,
                                postId: data.interPostId,
                                location: [data.locationLong, data.locationLat],
                                createdOn: data.createdOn
                            })

                            let mutualPostUsers = await aggregateData(Model.Posts, [{
                                $geoNear: {
                                    near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                    distanceField: "calculated",
                                    maxDistance: 2000,
                                    spherical: true,
                                    distanceMultiplier: 0.000621371,
                                    query: {
                                        postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY,
                                        postBy: obj.postBy._id,
                                        isDeleted: false,
                                        postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.SELECTED_PEOPLE
                                    }
                                }
                            }, {
                                $addFields: {
                                    newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                    newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                    newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                }
                            },
                            {
                                $match: {
                                    newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                    newMeetingDay: new Date(data.meetingTime).getDate(),
                                    newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                    postBy: { $ne: userData._id }
                                }
                            },
                            {
                                $project: {
                                    _id: 1,
                                    postBy: 1,
                                    imageUrl: 1,
                                    postText: 1,
                                    createdOn: 1,
                                    location: 1,
                                    locationName: 1,
                                    locationAddress: 1,
                                }
                            }])

                            if (mutualPostUsers.length) {
                                for (let post of mutualPostUsers) {
                                    let postByData = await getRequired(Model.Users, {
                                        _id: post.postBy,
                                        isDeleted: false
                                    }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                    let pushDataForDemander = {
                                        postId: post._id,
                                        userName: postByData[0].userName,
                                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                        msg: postByData[0].fullName + " has posted in your local area"
                                    }
                                    await createData(Model.Notifications, {
                                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                        toId: userData._id,
                                        byId: mongoose.Types.ObjectId(obj.postBy._id),
                                        text: postByData[0].fullName + " has posted in your local area",
                                        userName: postByData[0].userName,
                                        locationName: post.locationName,
                                        locationAddress: post.locationAddress,
                                        postId: post._id,
                                        location: post.location,
                                        createdOn: post.createdOn
                                    })
                                    if (userData.alertNotifications) {
                                        await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                            console.log(err, res)
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY) {
                    if (locationPosts && locationPosts.length) {
                        for (let obj of locationPosts) {
                            if (obj.postBy && obj.postBy._id) {
                                if (obj.postBy.alertNotifications) {
                                    deviceTokenArray.push(obj.postBy.deviceToken)
                                }

                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                    toId: mongoose.Types.ObjectId(obj.postBy._id),
                                    byId: userData._id,
                                    userName: userData.userName,
                                    text: userData.fullName + " has posted in your local area",
                                    locationName: data.locationName,
                                    locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    location: [data.locationLong, data.locationLat],
                                    createdOn: data.createdOn
                                })

                                let mutualPostUsers = await aggregateData(Model.Posts, [{
                                    $geoNear: {
                                        near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                        distanceField: "calculated",
                                        maxDistance: 2000,
                                        spherical: true,
                                        distanceMultiplier: 0.000621371,
                                        query: {
                                            postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.CONVERSE_NEARBY,
                                            postBy: obj.postBy._id,
                                            isDeleted: false,
                                            postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY
                                        }
                                    }
                                }, {
                                    $addFields: {
                                        newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                        newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                        newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                    }
                                },
                                {
                                    $match: {
                                        newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                        newMeetingDay: new Date(data.meetingTime).getDate(),
                                        newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                        postBy: { $ne: userData._id }
                                    }
                                },
                                {
                                    $project: {
                                        _id: 1,
                                        postBy: 1,
                                        imageUrl: 1,
                                        postText: 1,
                                        createdOn: 1,
                                        location: 1,
                                        locationName: 1,
                                        locationAddress: 1,
                                    }
                                }])

                                if (mutualPostUsers.length) {
                                    for (let post of mutualPostUsers) {
                                        let postByData = await getRequired(Model.Users, {
                                            _id: post.postBy,
                                            isDeleted: false
                                        }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                        let pushDataForDemander = {
                                            postId: post._id,
                                            userName: postByData[0].userName,
                                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                            msg: postByData[0].fullName + " has posted in your local area"
                                        }
                                        await createData(Model.Notifications, {
                                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                                            toId: userData._id,
                                            byId: mongoose.Types.ObjectId(obj.postBy._id),
                                            userName: postByData[0].userName,
                                            text: postByData[0].fullName + " has posted in your local area",
                                            locationName: post.locationName,
                                            locationAddress: post.locationAddress,
                                            postId: post._id,
                                            location: post.location,
                                            createdOn: post.createdOn
                                        })
                                        if (userData.alertNotifications) {
                                            await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                                console.log(err, res)
                                            })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                deviceTokenArray = _.uniq(deviceTokenArray)

                let pushData = {
                    userId: userData._id,
                    imageUrl: userData.imageUrl,
                    postId: data.interPostId,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_CONVERSE_NEARBY_PUSH,
                    msg: userData.fullName + " has posted in your local area"
                }
                await pushNotification.sendMultiUser(deviceTokenArray, pushData, (err, res) => {
                    console.log(err, res)
                })
            }
        }

        if (data.postType == Config.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY) {
            let selectInterests = [];
            data.selectInterests.map(obj => selectInterests.push(mongoose.Types.ObjectId(obj)))
            let criteria = {}
            criteria.userId = userData._id
            criteria.crossedUserId = { "$nin": userData.blockedBy }
            let crossedPeopleData = await getRequiredPopulate(Model.CrossedUsers, criteria, {
                crossedUserId: 1,
                location: 1,
                locationAddress: 1,
                locationName: 1
            }, { lean: true }, [{
                path: "crossedUserId",
                select: "deviceToken alertNotifications followers",
                model: "Users"
            }])
            let deviceTokenArray = []

            if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS) {
                for (let obj of crossedPeopleData) {
                    if (JSON.stringify(userData.followers).includes(JSON.stringify(obj.crossedUserId._id)) && JSON.stringify(obj.crossedUserId.followers).includes(JSON.stringify(userData._id))) {
                        if (obj.crossedUserId && obj.crossedUserId.deviceToken && obj.crossedUserId._id) {

                            if (obj.crossedUserId.alertNotifications) {
                                deviceTokenArray.push(obj.crossedUserId.deviceToken)
                            }

                            let crossedUserPosts = await aggregateData(Model.Posts, [{
                                $geoNear: {
                                    near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                    distanceField: "calculated",
                                    maxDistance: 2000,
                                    spherical: true,
                                    distanceMultiplier: 0.000621371,
                                    query: {
                                        postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY,
                                        selectInterests: { $in: selectInterests },
                                        postBy: obj.crossedUserId._id,
                                        isDeleted: false,
                                        postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.FOLLOWERS
                                    }
                                }
                            }, {
                                $addFields: {
                                    newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                    newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                    newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                }
                            },
                            {
                                $match: {
                                    newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                    newMeetingDay: new Date(data.meetingTime).getDate(),
                                    newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                    postBy: { $ne: userData._id }
                                }
                            },
                            {
                                $project: {
                                    _id: 1,
                                    postBy: 1,
                                    imageUrl: 1,
                                    postText: 1,
                                }
                            }])
                            //       console.log("-----------------------))))))))))))))))))", crossedUserPosts)

                            if (crossedUserPosts.length) {
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: mongoose.Types.ObjectId(obj.crossedUserId._id),
                                    byId: userData._id,
                                    userName: userData.userName,
                                    text: userData.fullName + " has posted in your local area",
                                    locationName: data.locationName,
                                    locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    location: [data.locationLong, data.locationLat],
                                    createdOn: data.createdOn
                                })

                                // for(let post of crossedUserPosts){
                                let lastPost = crossedUserPosts.pop()
                                let postByData = await getRequired(Model.Users, {
                                    _id: lastPost.postBy,
                                    isDeleted: false
                                }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                let pushDataForDemander = {
                                    postId: lastPost._id,
                                    userName: postByData[0].userName,
                                    TYPE: "ALERT_LOOK_NEARBY_PUSH",
                                    msg: postByData[0].fullName + " has posted in your local area"
                                }
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: userData._id,
                                    byId: mongoose.Types.ObjectId(obj.crossedUserId._id),
                                    userName: postByData[0].userName,
                                    text: postByData[0].fullName + " has posted in your local area",
                                    locationName: lastPost.locationName,
                                    locationAddress: lastPost.locationAddress,
                                    postId: lastPost._id,
                                    location: lastPost.location,
                                    createdOn: lastPost.createdOn
                                })
                                if (userData.alertNotifications) {
                                    await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                        console.log(err, res)
                                    })
                                }
                                // }
                                let pushData = {
                                    userName: userData.userName,
                                    // locationName: data.locationName,
                                    // locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    msg: userData.fullName + " has posted in your local area"
                                }
                                if (obj.crossedUserId.alertNotifications) {
                                    await pushNotification.sendPush(obj.crossedUserId.deviceToken, pushData, (err, res) => {
                                        console.log(err, res)
                                    })
                                }
                            }
                        }
                    }
                }
            }

            if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.SELECTED_PEOPLE) {
                for (let obj of crossedPeopleData) {

                    if (JSON.stringify(data.selectedPeople).includes(JSON.stringify(obj.crossedUserId._id))) {
                        if (obj.crossedUserId && obj.crossedUserId.deviceToken && obj.crossedUserId._id) {
                            if (obj.crossedUserId.alertNotifications) {
                                deviceTokenArray.push(obj.crossedUserId.deviceToken)
                            }


                            let crossedUserPosts = await aggregateData(Model.Posts, [{
                                $geoNear: {
                                    near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                    distanceField: "calculated",
                                    maxDistance: 2000,
                                    spherical: true,
                                    distanceMultiplier: 0.000621371,
                                    query: {
                                        postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY,
                                        selectInterests: { $in: selectInterests },
                                        postBy: obj.crossedUserId._id,
                                        isDeleted: false,
                                        postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.SELECTED_PEOPLE
                                    }
                                }
                            }, {
                                $addFields: {
                                    newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                    newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                    newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                }
                            },
                            {
                                $match: {
                                    newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                    newMeetingDay: new Date(data.meetingTime).getDate(),
                                    newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                    postBy: { $ne: userData._id }
                                }
                            },
                            {
                                $project: {
                                    _id: 1,
                                    postBy: 1,
                                    imageUrl: 1,
                                    postText: 1,
                                }
                            }])
                            // console.log("-----------------------))))))))))))))))))", crossedUserPosts)

                            if (crossedUserPosts.length) {
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: mongoose.Types.ObjectId(obj.crossedUserId._id),
                                    byId: userData._id,
                                    userName: userData.userName,
                                    locationName: data.locationName,
                                    text: userData.fullName + " has posted in your local area",
                                    locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    location: [data.locationLong, data.locationLat],
                                    createdOn: data.createdOn
                                })

                                // for(let post of crossedUserPosts){
                                let lastPost = crossedUserPosts.pop()
                                let postByData = await getRequired(Model.Users, {
                                    _id: lastPost.postBy,
                                    isDeleted: false
                                }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                let pushDataForDemander = {
                                    postId: lastPost._id,
                                    userName: postByData[0].userName,
                                    TYPE: "ALERT_LOOK_NEARBY_PUSH",
                                    msg: postByData[0].fullName + " has posted in your local area"
                                }
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: userData._id,
                                    byId: mongoose.Types.ObjectId(obj.crossedUserId._id),
                                    userName: postByData[0].userName,
                                    text: postByData[0].fullName + " has posted in your local area",
                                    locationName: lastPost.locationName,
                                    locationAddress: lastPost.locationAddress,
                                    postId: lastPost._id,
                                    location: lastPost.location,
                                    createdOn: lastPost.createdOn
                                })
                                if (userData.alertNotifications) {
                                    await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                        console.log(err, res)
                                    })
                                }
                                // }

                                let pushData = {
                                    userName: userData.userName,
                                    // locationName: data.locationName,
                                    // locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    msg: userData.fullName + " has posted in your local area"
                                }
                                if (obj.crossedUserId.alertNotifications) {
                                    await pushNotification.sendPush(obj.crossedUserId.deviceToken, pushData, (err, res) => {
                                        console.log(err, res)
                                    })
                                }

                            }
                        }
                    }
                }
            }

            if (data.postingIn === Config.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY) {
                if (crossedPeopleData && crossedPeopleData.length) {
                    for (let obj of crossedPeopleData) {
                        if (obj.crossedUserId && obj.crossedUserId.deviceToken && obj.crossedUserId._id) {
                            if (obj.crossedUserId.alertNotifications) {
                                deviceTokenArray.push(obj.crossedUserId.deviceToken)
                            }

                            let crossedUserPosts = await aggregateData(Model.Posts, [{
                                $geoNear: {
                                    near: { type: "Point", coordinates: [data.locationLong, data.locationLat] },
                                    distanceField: "calculated",
                                    maxDistance: 2000,
                                    spherical: true,
                                    distanceMultiplier: 0.000621371,
                                    query: {
                                        postType: Config.APP_CONSTANTS.DATABASE.POST_TYPE.LOOK_NEARBY,
                                        selectInterests: { $in: selectInterests },
                                        postBy: obj.crossedUserId._id,
                                        isDeleted: false,
                                        postingIn: Config.APP_CONSTANTS.DATABASE.POSTING_IN.PUBLICILY
                                    }
                                }
                            }, {
                                $addFields: {
                                    newMeetingDate: { $month: { $toDate: "$meetingTime" } },
                                    newMeetingDay: { $dayOfMonth: { $toDate: "$meetingTime" } },
                                    newMeetingYear: { $year: { $toDate: "$meetingTime" } }
                                }
                            },
                            {
                                $match: {
                                    newMeetingDate: (new Date(data.meetingTime).getMonth() + 1),
                                    newMeetingDay: new Date(data.meetingTime).getDate(),
                                    newMeetingYear: new Date(data.meetingTime).getFullYear(),
                                    postBy: { $ne: userData._id }
                                }
                            },
                            {
                                $project: {
                                    _id: 1,
                                    postBy: 1,
                                    imageUrl: 1,
                                    postText: 1,
                                }
                            }])
                            //     console.log("-----------------------))))))))))))))))))", crossedUserPosts.length)

                            if (crossedUserPosts.length) {
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: obj.crossedUserId._id,
                                    byId: userData._id,
                                    userName: userData.userName,
                                    text: userData.fullName + " has posted in your local area",
                                    locationName: data.locationName,
                                    locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    location: [data.locationLong, data.locationLat],
                                    createdOn: data.createdOn
                                })

                                // for(let post of crossedUserPosts){
                                let lastPost = crossedUserPosts.pop()
                                let postByData = await getRequired(Model.Users, {
                                    _id: lastPost.postBy,
                                    isDeleted: false
                                }, { fullName: 1, userName: 1, imageUrl: 1 }, { lean: true })
                                let pushDataForDemander = {
                                    postId: lastPost._id,
                                    userName: postByData[0].userName,
                                    TYPE: "ALERT_LOOK_NEARBY_PUSH",
                                    msg: postByData[0].fullName + " has posted in your local area"
                                }
                                await createData(Model.Notifications, {
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    toId: userData._id,
                                    byId: mongoose.Types.ObjectId(obj.crossedUserId._id),
                                    userName: postByData[0].userName,
                                    text: postByData[0].fullName + " has posted in your local area",
                                    locationName: lastPost.locationName,
                                    locationAddress: lastPost.locationAddress,
                                    postId: lastPost._id,
                                    location: lastPost.location,
                                    createdOn: lastPost.createdOn
                                })
                                if (userData.alertNotifications) {
                                    await pushNotification.sendPush(userData.deviceToken, pushDataForDemander, (err, res) => {
                                        console.log(err, res)
                                    })
                                }
                                // }

                                let pushData = {
                                    userName: userData.userName,
                                    // locationName: data.locationName,
                                    // locationAddress: data.locationAddress,
                                    postId: data.interPostId,
                                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ALERT_LOOK_NEARBY_PUSH,
                                    msg: userData.fullName + " has posted in your local area"
                                }
                                if (obj.crossedUserId.alertNotifications) {
                                    await pushNotification.sendPush(obj.crossedUserId.deviceToken, pushData, (err, res) => {
                                        console.log(err, res)
                                    })
                                }
                            }
                        }
                    }
                }
            }
            // deviceTokenArray = _.uniq(deviceTokenArray)
        }
    } catch (e) {
        console.log(e)
    }
}

let addEditChatGroup = async (payloadData, userData) => {
    try {
        let query = {}
        if (payloadData.name)
            query.name = payloadData.name

        if (payloadData.groupId) {
            let criteria = {
                _id: payloadData.groupId,
                isDeleted: false
            }
            if (payloadData.imageOriginal && payloadData.imageThumbnail) {
                query.imageUrl = {
                    original: payloadData.imageOriginal,
                    thumbnail: payloadData.imageThumbnail
                }
            }

            let data = await updateData(Model.ChatGroups, criteria, { $set: query }, { new: true, lean: true })
            if (data) {
                return data
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        } else {
            query.adminId = userData._id
            query.memberCount = 1
            query.memberIds = [userData._id]
            query.conversationId = mongoose.Types.ObjectId()
            query.createdOn = +new Date
            if (payloadData.memberIds.length) {
                for (let id of payloadData.memberIds) {
                    query.memberIds.push(id)
                }
            }
            if (payloadData.imageOriginal && payloadData.imageThumbnail) {
                query.imageUrl = {
                    original: payloadData.imageOriginal,
                    thumbnail: payloadData.imageThumbnail
                }
            } else {
                query.imageUrl = {
                    "original": "https://s3-us-west-2.amazonaws.com/conversifybucket/venue_icon.jpg",
                    "thumbnail": "https://s3-us-west-2.amazonaws.com/conversifybucket/venue_icon.jpg"
                }
            }
            let data = await Service.saveData(Model.ChatGroups, query)
            if (data) {
                for (let member of data.memberIds) {
                    let query = {
                        groupId: data._id,
                        userId: member,
                        joinedAt: +new Date(),
                    }
                    if (member === userData._id) {
                        query.isAdmin = true
                    }

                    await Service.saveData(Model.ChatGroupMembers, query)
                }

                return data
            } else
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.SOMETHING_WENT_WRONG)
        }
    } catch (e) {
        console.log(e)
    }
}
/*=================Post Page Related Apis==================*/
/*==============Post Page Related Apis===================*/

/**
 * @description shows the chat summary page data
 */
let chatSummary = async (payloadData, userData) => {
    try {
        if (payloadData.flag === 1) {
            let pipeline = [{
                $match: {
                    $or: [{
                        senderId: mongoose.Types.ObjectId(userData._id)
                    }, {
                        receiverId: mongoose.Types.ObjectId(userData._id)
                    }],
                    chatType: "INDIVIDUAL",
                    noChat: false,
                    isDeleted: false
                }
            }, {
                $group: {
                    _id: "$conversationId",
                    chatDetails: { $last: "$chatDetails" },
                    createdDate: { $last: "$createdDate" },
                    senderId: { $last: { $cond: [{ $ne: ["$senderId", mongoose.Types.ObjectId(userData._id)] }, "$senderId", "$receiverId"] } },
                    unreadCount: {
                        $sum: {
                            $cond: [
                                {
                                    $and:
                                        [
                                            // {$eq: ['$receiverId', mongoose.Types.ObjectId(userData._id)]},
                                            { $in: [mongoose.Types.ObjectId(userData._id), "$readBy"] }
                                        ]
                                }, 0, 1]
                        }
                    }
                }
            }, {
                $project: {
                    conversationId: "$_id",
                    lastChatDetails: "$chatDetails",
                    createdDate: "$createdDate",
                    senderId: "$senderId",
                    unreadCount: "$unreadCount",
                }
            }, {
                $sort: {
                    createdDate: -1
                }
            }]

            let populate = [{
                path: "senderId",
                select: "fullName userName imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users"
            }]
            var data = await aggregateWithPopulate(Model.Chats, pipeline, populate)
            if (data.length) {
                for (let a of data) {
                    if (a.senderId) {
                        a.senderId = a.senderId.toObject()
                        if (JSON.stringify(a.senderId._id) == JSON.stringify(userData._id)) {
                            continue
                        }
                        if (!a.senderId.imageVisibilityForEveryone) {
                            if (a.senderId.imageVisibilityForFollowers) {
                                a.senderId = await imageVisibilityManipulation(a.senderId, userData)
                            } else if (a.senderId.imageVisibility && a.senderId.imageVisibility.length) {
                                a.senderId = await imageVisibilityManipulation(a.senderId, userData)
                            }
                        }

                        if (!a.senderId.nameVisibilityForEveryone) {
                            if (a.senderId.nameVisibilityForFollowers) {
                                a.senderId = await nameVisibilityManipulation(a.senderId, userData)
                            } else if (a.senderId.nameVisibility && a.senderId.nameVisibility.length) {
                                a.senderId = await nameVisibilityManipulation(a.senderId, userData)
                            }
                        }
                        // if(!a.senderId.imageVisibilityForFollowers && !a.senderId.nameVisibilityForFollowers && !a.senderId.imageVisibility.length && !a.senderId.nameVisibility.length){
                        //     continue
                        // }
                        // a.senderId = await imageVisibilityManipulation(a.senderId, userData)
                        // a.senderId = await nameVisibilityManipulation(a.senderId, userData)
                    }
                }
            }

        } else if (payloadData.flag === 2) {

            let pipeline = [{
                $lookup: {
                    "from": "chats",
                    "localField": "groupId",
                    "foreignField": "groupId",
                    "as": "chatInfo"
                }
            }, {
                $lookup: {
                    "from": "postgroups",
                    "localField": "groupId",
                    "foreignField": "_id",
                    "as": "groupInfo"
                }
            }, {
                $match: {
                    userId: mongoose.Types.ObjectId(userData._id),
                    "groupInfo.isArchive": { $nin: [userData._id] },
                    "groupInfo.adminId": { $nin: userData.blockedBy },
                }
            }, {
                $unwind: {
                    "path": "$chatInfo",
                    "preserveNullAndEmptyArrays": true,
                }
            }, {
                $match: {
                    "chatInfo.isDeleted": false,
                }
            }, {
                $unwind: {
                    "path": "$groupInfo",
                    "preserveNullAndEmptyArrays": true,
                }
            }, {
                $project: {
                    _id: 1,
                    groupId: 1,
                    groupInfo: 1,
                    createdOn: {
                        $cond: {
                            if: { $gte: ["$groupInfo.createdOn", "$chatInfo.createdDate"] },
                            then: "$groupInfo.createdOn",
                            else: "$chatInfo.createdDate"
                        }
                    },
                    chatInfoReadBy: { $ifNull: ["$chatInfo.readBy", []] },
                    chatInfo: { $ifNull: ["$chatInfo", null] }
                }
            }, {
                $group: {
                    _id: "$groupId",
                    groupId: { $last: "$groupId" },
                    // imageUrl: {$last: "$groupInfo.imageUrl"},
                    // groupName: {$last: "$groupInfo.name"},
                    // adminId: {$last: "$groupInfo.adminId"},
                    createdDate: { $last: "$createdOn" },
                    conversationId: { $last: "$groupInfo.conversationId" },
                    lastChatDetails: { $last: "$chatInfo.chatDetails" },
                    "unreadCount": {
                        $sum: {
                            $cond: [
                                {
                                    $or: [
                                        { $eq: [true, "$groupInfo.noChat"] },
                                        {
                                            $and: [
                                                { $eq: [false, "$groupInfo.noChat"] },
                                                { $in: [mongoose.Types.ObjectId(userData._id), "$chatInfo.readBy"] }
                                            ]
                                        }
                                    ]
                                }, 0, 1]
                        }
                    }
                }
            }, {
                $sort: {
                    createdDate: -1
                }
            }]
            let populate = [{
                path: "groupId",
                select: "groupName adminId imageUrl",
                model: "PostGroups"
            }]
            data = await aggregateWithPopulate(Model.PostGroupMembers, pipeline, populate)
        }
        if (data)
            return data
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description detail of a chat conversation
 * @param {string} conversationId mongodbId
 * @returns chat conversation data
 */

let chatConversation = async (payloadData, userData) => {
    try {
        var data
        if (!payloadData.conversationId) {
            return []
        }
        if (payloadData.chatId) {

            let chatDetails = await getRequired(Model.Chats, {
                _id: payloadData.chatId,
                isDeleted: false
            }, { createdDate: 1 }, { lean: true })
            let pipeline = [{
                $match: {
                    conversationId: mongoose.Types.ObjectId(payloadData.conversationId),
                    createdDate: { $lt: chatDetails[0].createdDate },
                    isDeleted: false
                }
            }, {
                $group: {
                    _id: "$groupId",
                    chat: {
                        $push: {
                            _id: "$_id",
                            chatDetails: "$chatDetails",
                            // isDelivered: "$isDelivered",
                            // readBy: "$readBy",
                            senderId: "$senderId",
                            createdDate: "$createdDate",
                            groupId: "$groupId",
                            receiverId: "$receiverId",
                        }
                    }
                }
            }, {
                $project: {
                    _id: 1,
                    chats: { $slice: ["$chat", -40, 40] }
                }
            }]
            let populate = [{
                path: "chats.senderId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]

            data = await aggregateWithPopulate(Model.Chats, pipeline, populate)
            if (data.length) {
                data = data[0].chats
            }
        } else {
            let userGroupId = await getRequired(Model.PostGroups, { conversationId: payloadData.conversationId }, { groupId: 1 }, { lean: true })
            if (userGroupId.length) {
                await socketManager.joinUserToGroupOnlogin([userGroupId[0]._id], userData._id)
            }
            let pipeline = [{
                $match: {
                    conversationId: mongoose.Types.ObjectId(payloadData.conversationId),
                    noChat: false,
                    isDeleted: false
                }
            }, {
                $project: {
                    _id: "$_id",
                    senderId: 1,
                    groupId: 1,
                    createdDate: 1,
                    chatDetails: 1
                }
            }, { $sort: { createdDate: -1 } }, { $limit: 40 }, { $sort: { createdDate: 1 } }]
            let populate = [{
                path: "senderId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers",
                model: "Users",
                populate: [{ path: "interestTags", select: "categoryName imageUrl createdOn", model: "Categories" }, {
                    path: "imageVisibility",
                    select: "userName imageUrl fullName",
                    model: "Users"
                }, {
                    path: "nameVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "tagPermission",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }, {
                    path: "personalInfoVisibility",
                    select: "fullName imageUrl userName",
                    model: "Users"
                }]
            }]

            data = await aggregateWithPopulate(Model.Chats, pipeline, populate)
            if (data) {
                await Service.update(Model.Chats, {
                    conversationId: payloadData.conversationId,
                    readBy: { $nin: [mongoose.Types.ObjectId(userData._id)] }
                }, { $addToSet: { readBy: mongoose.Types.ObjectId(userData._id) } }, { multi: true, new: true, lean: true })
            }
        }
        // if(data.length){
        //     for(let a of data){
        //         a.senderId = a.senderId.toObject()
        //         if(!a.senderId.imageVisibilityForFollowers && !a.senderId.nameVisibilityForFollowers && !a.senderId.imageVisibility.length && !a.senderId.nameVisibility.length){
        //             continue
        //         }
        //         if(a.senderId.imageVisibilityForFollowers && JSON.stringify(a.senderId.followers).includes(userData._id)){
        //             a.senderId.imageUrl = a.senderId.imageUrl
        //         }else{
        //             if(JSON.stringify(a.senderId.imageVisibility).includes(userData._id)  && JSON.stringify(a.senderId._id) != JSON.stringify(userData._id)){
        //                 a.senderId.imageUrl = a.senderId.imageUrl
        //             }else{
        //                 a.senderId.imageUrl = {"original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
        //                 "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL}
        //             }
        //         }

        //         if(a.senderId.nameVisibilityForFollowers && JSON.stringify(a.senderId.followers).includes(userData._id)){
        //             a.senderId.fullName = a.senderId.fullName
        //             a.senderId.userName = a.senderId.userName
        //         }else{
        //             if(JSON.stringify(a.senderId.nameVisibility).includes(userData._id)  && JSON.stringify(a.senderId._id) != JSON.stringify(userData._id)){
        //                 a.senderId.fullName = a.senderId.fullName
        //                 a.senderId.userName = a.senderId.userName
        //             }else{
        //                 // a.senderId.fullName = a.senderId.firstName.substr(0, 1) + ".... " + a.senderId.lastName.substr(0, 1) + "...."
        //                 a.senderId.fullName = a.senderId.firstName.substr(0, 1) + ".... "
        //                 a.senderId.userName = a.senderId.userName.substr(0, 3)
        //             }
        //         }
        //         delete a.senderId.firstName
        //         delete a.senderId.lastName
        //         delete a.senderId.isAccountPrivate
        //         delete a.senderId.imageVisibility
        //         delete a.senderId.nameVisibility
        //         delete a.senderId.locationVisibility
        //         delete a.senderId.nameVisibilityForFollowers
        //         delete a.senderId.imageVisibilityForFollowers
        //         delete a.senderId.locationVisibility
        //         delete a.senderId.followers
        //     }
        // }
        let groupId = await getRequired(Model.PostGroups, { conversationId: payloadData.conversationId }, { _id: 1 }, { lean: true })

        if (groupId.length) {
            let groupDataCriteria = {
                groupId: groupId[0]._id,
                isDeleted: false
            }

            let groupDataProject = {
                _id: 0,
                userId: 1,
                isAdmin: 1
            }

            let groupDataPopulate = [{
                path: "userId",
                select: "fullName userName bio imageUrl firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users"
            }]
            var userNotify = await getRequired(Model.PostGroupMembers, {
                groupId: groupId[0]._id,
                userId: userData._id
            }, { isNotify: 1 }, { lean: true })
            var groupData = await getRequiredPopulate(Model.PostGroupMembers, groupDataCriteria, groupDataProject, {
                lean: true,
                sort: { joinedAt: 1 }
            }, groupDataPopulate)
            var groupDetails = await getRequired(Model.PostGroups, { _id: groupId[0]._id }, {
                groupName: 1,
                imageUrl: 1,
                adminId: 1,
                description: 1
            }, { lean: true })
            for (let a of groupData) {
                if (JSON.stringify(a.userId._id) == JSON.stringify(userData._id) || (a.isAdmin && JSON.stringify(userData.blockedBy).includes(a.userId))) {
                    continue
                }
                if (!a.userId.imageVisibilityForEveryone) {
                    if (a.userId.imageVisibilityForFollowers) {
                        a.userId = await imageVisibilityManipulation(a.userId, userData)
                    } else if (a.userId.imageVisibility && a.userId.imageVisibility.length) {
                        a.userId = await imageVisibilityManipulation(a.userId, userData)
                    }
                }

                if (!a.userId.nameVisibilityForEveryone) {
                    if (a.userId.nameVisibilityForFollowers) {
                        a.userId = await nameVisibilityManipulation(a.userId, userData)
                    } else if (a.userId.nameVisibility && a.userId.nameVisibility.length) {
                        a.userId = await nameVisibilityManipulation(a.userId, userData)
                    }
                }
                // if(!a.userId.imageVisibilityForFollowers && !a.userId.nameVisibilityForFollowers && !a.userId.imageVisibility.length && !a.userId.nameVisibility.length){
                //     continue
                // }
                // a.userId = await imageVisibilityManipulation(a.userId, userData)
                // a.userId = await nameVisibilityManipulation(a.userId, userData)
            }
            return {
                chatData: data || [],
                groupData: groupData || null,
                groupId: groupId[0]._id,
                groupName: groupDetails[0].groupName || null,
                imageUrl: groupDetails[0].imageUrl || null,
                adminId: groupDetails[0].adminId || null,
                description: groupDetails[0].description || null,
                notification: userNotify[0].isNotify || null,
            }
        } else {
            return {
                chatData: data || []
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let archiveListing = async (payloadData, userData) => {
    try {
        let criteria = {}, project = {}
        criteria.isDeleted = false
        criteria.isArchive = { $in: [userData._id] }
        if (payloadData.groupType === "VENUE") {
            let data = await getRequired(Model.VenueGroups, criteria, {}, { lean: true })
            return data
        } else {
            let data = await getRequired(Model.PostGroups, criteria, {}, { lean: true })
            return data
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: creating and editing comment
 * @param {string} authorization
 * @param {string} postId
 * @param {string} commentId
 * @param {Array} userIdTag
 * @param {string} comment
 * @returns: saved comment data
 */

let addEditComment = async (payloadData, userData) => {
    try {
        let query = {}, toIdData;
        if (payloadData.postId) {
            let checkPost = await getRequired(Model.Posts, { _id: payloadData.postId, isDeleted: true }, {}, { lean: true })
            if (checkPost.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
            } else {
                query.postId = payloadData.postId
            }
        }
        // if media id is present then call media comment func
        if (payloadData.mediaId) {
            return await addEditMediaComment(payloadData, userData);
        }

        if (payloadData.comment)
            query.comment = payloadData.comment

        if (payloadData.attachmentUrl) query.attachmentUrl = payloadData.attachmentUrl;

        // if(payloadData.userIdTag)
        //     query.userIdTag = payloadData.userIdTag

        if (payloadData.commentId) {
            let criteria = {
                isDeleted: false,
                _id: payloadData.commentId
            };
            query.editAt = +new Date;
            let data = await updateData(Model.Comments, criteria, query, { new: true })
            if (data)
                return data

        } else {
            query.createdOn = +new Date;
            query.commentBy = userData._id;
            query.postId = payloadData.postId;
            let data = await Service.saveData(Model.Comments, query)
            if (data) {
                let commentorDeviceTokens = [], commentorUserId = [], isMemberData, postByIsMember = false
                let postData = await getRequired(Model.Posts, { _id: payloadData.postId }, {
                    groupId: 1,
                    postBy: 1
                }, { lean: true })
                let commentOfPosts = await getRequiredPopulate(Model.Comments, {
                    postId: payloadData.postId,
                    commentBy: { $ne: postData[0].postBy }
                }, { commentBy: 1 }, { lean: true }, [{
                    path: "commentBy",
                    select: " _id userName deviceToken",
                    model: "Users"
                }])
                let postByData = await getRequired(Model.Users, { _id: payloadData.postBy }, { userName: 1 }, { lean: true })
                if (postData && postData[0].groupId) {
                    //   console.log(payloadData.postBy, postData[0].groupId)
                    isMemberData = await getRequired(Model.PostGroupMembers, {
                        userId: payloadData.postBy,
                        isDeleted: false,
                        groupId: postData[0].groupId
                    }, {}, { lean: true })
                    if (isMemberData.length) {
                        postByIsMember = true
                    }
                }

                for (let obj of commentOfPosts) {
                    console.log('{{{{{{{{{{{{{{{{{{{{{{{{{', JSON.stringify(userData._id) != JSON.stringify(obj.commentBy._id), (JSON.stringify(payloadData.postBy) != JSON.stringify(obj.commentBy._id)), (JSON.stringify(payloadData.postBy) != JSON.stringify(userData._id)))

                    if (JSON.stringify(userData._id) != JSON.stringify(obj.commentBy._id) && (JSON.stringify(payloadData.postBy) != JSON.stringify(obj.commentBy._id))) {
                        if (payloadData.userIdTag && payloadData.userIdTag.length && JSON.stringify(payloadData.userIdTag).includes(JSON.stringify(obj.commentBy.userName))) {
                            continue
                        }
                        if (JSON.stringify(userData.blockedWhom).includes(payloadData.postBy)) {
                            continue
                        }
                        if (postData && postData[0].groupId) {
                            let getConfirmationCommentor = await getRequired(Model.PostGroups, {
                                _id: postData[0].groupId,
                                isMember: { $in: [obj.commentBy._id] }
                            }, {}, { lean: true })
                            if (getConfirmationCommentor && getConfirmationCommentor.length) {
                                commentorDeviceTokens.push(obj.commentBy.deviceToken)
                                commentorUserId.push(obj.commentBy._id)
                            }
                        } else {
                            commentorDeviceTokens.push(obj.commentBy.deviceToken)
                            commentorUserId.push(obj.commentBy._id)
                        }
                    }
                }
                commentorDeviceTokens = _.uniqBy(commentorDeviceTokens, "deviceToken")
                commentorUserId = _.uniqBy(commentorUserId, "_id")
                //   console.log("++++++++++++++++++++++kkk", commentorDeviceTokens)
                if (!payloadData.userIdTag) {
                    payloadData.userIdTag = []
                }
                if (postData[0].groupId && postByIsMember) {
                    if (postData[0].postBy && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
                        //     console.log("--------------+++", userData.blockedWhom, payloadData.postBy)
                        if (JSON.stringify(payloadData.postBy) !== JSON.stringify(userData._id)) {

                            await createData(Model.Notifications, {
                                toId: payloadData.postBy,
                                byId: userData._id,
                                text: userData.fullName + ' has commented on your post',
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                postId: payloadData.postId,
                                commentId: data._id,
                                createdOn: +new Date
                            })

                            toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true })

                            let pushData = {
                                id: payloadData.postId,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                msg: userData.fullName + ' has commented on your post'
                            };
                            console.log("---------------------1")
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        }
                    }
                } else if (!postData[0].groupId) {
                    if (postData[0].postBy && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
                        if (JSON.stringify(payloadData.postBy) !== JSON.stringify(userData._id)) {
                            if (!JSON.stringify(userData.blockedWhom).includes(payloadData.postBy)) {
                                await createData(Model.Notifications, {
                                    toId: payloadData.postBy,
                                    byId: userData._id,
                                    text: userData.fullName + ' has commented on your post',
                                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                    postId: payloadData.postId,
                                    commentId: data._id,
                                    createdOn: +new Date
                                })

                                toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true })

                                let pushData = {
                                    id: payloadData.postId,
                                    byId: userData._id,
                                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                    msg: userData.fullName + ' has commented on your post'
                                };
                                console.log("---------------------11")
                                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                    console.log(err, res)
                                })
                            }
                        }
                    }
                }

                if (commentorDeviceTokens.length) {
                    for (let id of commentorUserId) {
                        let getConfirmationCommentor = await getRequired(Model.PostGroups, {
                            _id: postData[0].groupId,
                            isMember: { $in: [id] }
                        }, {}, { lean: true })
                        if (getConfirmationCommentor && getConfirmationCommentor.length) {
                            await createData(Model.Notifications, {
                                toId: id,
                                byId: userData._id,
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                postId: payloadData.postId,
                                text: userData.fullName + ' has also commented',
                                groupId: postData[0].groupId,
                                commentId: data._id,
                                createdOn: +new Date
                            })
                        } else {
                            await createData(Model.Notifications, {
                                toId: id,
                                byId: userData._id,
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                                text: userData.fullName + ' has also commented',
                                postId: payloadData.postId,
                                commentId: data._id,
                                createdOn: +new Date
                            })
                        }
                    }

                    let pushData = {
                        id: payloadData.postId,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                        msg: userData.fullName + ' has also commented'
                    };
                    console.log("---------------------2")

                    pushNotification.sendMultiUser(commentorDeviceTokens, pushData, (err, res) => {
                        console.log(err, res)
                    })
                }

                if (payloadData.userIdTag && payloadData.userIdTag.length) {

                    let deviceTokenCollection = [];
                    for (let user of payloadData.userIdTag) {

                        let deviceTokens = await getRequired(Model.Users, { userName: new RegExp(user, 'i') }, {
                            _id: 1,
                            deviceToken: 1
                        }, { lean: true })

                        if (deviceTokens.length) {

                            await createData(Model.Notifications, {
                                toId: deviceTokens[0]._id,
                                byId: userData._id, createdOn: +new Date, commentId: data._id,
                                postId: payloadData.postId,
                                text: userData.userName + " has tagged you in comment.",
                                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_COMMENT
                            });

                            deviceTokenCollection.push(deviceTokens[0].deviceToken)
                        }

                    }

                    let pushData = {
                        id: data._id,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_COMMENT,
                        msg: userData.userName + " has tagged you in comment."
                    };

                    console.log('++++++++++++++++++++', pushData, deviceTokenCollection)

                    pushNotification.sendMultiUser(deviceTokenCollection, pushData, (err, res) => {
                    })

                }

                await updateData(Model.Posts, { _id: payloadData.postId }, { $inc: { commentCount: 1 } }, {
                    new: true,
                    lean: true
                })
                var finalData = await Service.populateTheSearchData(Model.Comments, data, [{
                    path: "commentBy",
                    select: "fullName userName imageUrl",
                    model: "Users"
                }])
                let copyFinal = finalData.toObject()
                copyFinal.liked = false
                return copyFinal
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let addEditMediaComment = async (payloadData, userData) => {
    payloadData.commentBy = userData._id;
    // update
    if (payloadData.commentId) {
        try {
            payloadData.editAt = +new Date;
            let update = await Model.MediaComments.findOneAndUpdate({ _id: payloadData.commentId }, payloadData, { new: true })

            return update;
        } catch (e) {
            console.log(e);
        }
    } else {
        // insert
        payloadData.createdOn = +new Date;
        let save = await new Model.MediaComments(payloadData).save();
        let find = await Model.MediaComments.findById(save._id).populate([{
            path: "commentBy",
            select: "fullName userName imageUrl",
            model: "Users"
        }]).lean();
        find.liked = false;

        // send notification
        try {
            let notifications = await sendNotiMediaComment(payloadData, userData, find);
            console.log(notifications);
        } catch (err) {
            return err;
        }

        return find;

    }

}

let sendNotiMediaComment = async (payloadData, userData, data) => {

    let commentorDeviceTokens = [], commentorUserId = [], isMemberData, postByIsMember = false
    let postData = await getRequired(Model.Posts, { _id: payloadData.postId }, { groupId: 1, postBy: 1 }, { lean: true })
    let commentOfPosts = await getRequiredPopulate(Model.MediaComments, {
        postId: payloadData.postId,
        commentBy: { $ne: postData[0].postBy }
    }, { commentBy: 1 }, { lean: true }, [{ path: "commentBy", select: " _id userName deviceToken", model: "Users" }])
    let postByData = await getRequired(Model.Users, { _id: payloadData.postBy }, { userName: 1 }, { lean: true })
    if (postData && postData[0].groupId) {
        console.log(payloadData.postBy, postData[0].groupId)
        isMemberData = await getRequired(Model.PostGroupMembers, {
            userId: payloadData.postBy,
            isDeleted: false,
            groupId: postData[0].groupId
        }, {}, { lean: true })
        if (isMemberData.length) {
            postByIsMember = true
        }
    }

    for (let obj of commentOfPosts) {
        console.log('{{{{{{{{{{{{{{{{{{{{{{{{{', JSON.stringify(userData._id) != JSON.stringify(obj.commentBy._id), (JSON.stringify(payloadData.postBy) != JSON.stringify(obj.commentBy._id)), (JSON.stringify(payloadData.postBy) != JSON.stringify(userData._id)))

        if (JSON.stringify(userData._id) != JSON.stringify(obj.commentBy._id) && (JSON.stringify(payloadData.postBy) != JSON.stringify(obj.commentBy._id))) {
            if (payloadData.userIdTag && payloadData.userIdTag.length && JSON.stringify(payloadData.userIdTag).includes(JSON.stringify(obj.commentBy.userName))) {
                continue
            }
            if (JSON.stringify(userData.blockedWhom).includes(payloadData.postBy)) {
                continue
            }
            if (postData && postData[0].groupId) {
                let getConfirmationCommentor = await getRequired(Model.PostGroups, {
                    _id: postData[0].groupId,
                    isMember: { $in: [obj.commentBy._id] }
                }, {}, { lean: true })
                if (getConfirmationCommentor && getConfirmationCommentor.length) {
                    commentorDeviceTokens.push(obj.commentBy.deviceToken)
                    commentorUserId.push(obj.commentBy._id)
                }
            } else {
                commentorDeviceTokens.push(obj.commentBy.deviceToken)
                commentorUserId.push(obj.commentBy._id)
            }
        }
    }
    commentorDeviceTokens = _.uniqBy(commentorDeviceTokens, "deviceToken")
    commentorUserId = _.uniqBy(commentorUserId, "_id")
    console.log("++++++++++++++++++++++kkk", commentorDeviceTokens)
    if (!payloadData.userIdTag) {
        payloadData.userIdTag = []
    }
    if (postData[0].groupId && postByIsMember) {
        if (postData[0].postBy && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
            console.log("--------------+++", userData.blockedWhom, payloadData.postBy)
            if (JSON.stringify(payloadData.postBy) !== JSON.stringify(userData._id)) {

                await createData(Model.Notifications, {
                    toId: payloadData.postBy,
                    byId: userData._id,
                    text: userData.fullName + ' has commented on your post',
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                    postId: payloadData.postId,
                    commentId: data._id,
                    createdOn: +new Date
                })

                let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true })

                let pushData = {
                    id: payloadData.postId,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                    msg: userData.fullName + ' has commented on your post'
                };
                console.log("---------------------1")
                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
            }
        }
    } else if (!postData[0].groupId) {
        if (postData[0].postBy && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
            if (JSON.stringify(payloadData.postBy) !== JSON.stringify(userData._id)) {
                if (!JSON.stringify(userData.blockedWhom).includes(payloadData.postBy)) {
                    await createData(Model.Notifications, {
                        toId: payloadData.postBy,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                        postId: payloadData.postId,
                        text: userData.fullName + ' has commented on your post',
                        commentId: data._id,
                        createdOn: +new Date
                    })

                    let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true })

                    let pushData = {
                        id: payloadData.postId,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                        msg: userData.fullName + ' has commented on your post'
                    };
                    console.log("---------------------11")
                    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                        console.log(err, res)
                    })
                }
            }
        }
    }

    if (commentorDeviceTokens.length) {
        for (let id of commentorUserId) {
            let getConfirmationCommentor = await getRequired(Model.PostGroups, {
                _id: postData[0].groupId,
                isMember: { $in: [id] }
            }, {}, { lean: true })
            if (getConfirmationCommentor && getConfirmationCommentor.length) {
                await createData(Model.Notifications, {
                    toId: id,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                    postId: payloadData.postId,
                    text: userData.fullName + ' has also commented',
                    groupId: postData[0].groupId,
                    commentId: data._id,
                    createdOn: +new Date
                })
            } else {
                await createData(Model.Notifications, {
                    toId: id,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
                    text: userData.fullName + ' has also commented',
                    postId: payloadData.postId,
                    commentId: data._id,
                    createdOn: +new Date
                })
            }
        }

        let pushData = {
            id: payloadData.postId,
            byId: userData._id,
            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.COMMENT,
            msg: userData.fullName + ' has also commented'
        };
        console.log("---------------------2")

        pushNotification.sendMultiUser(commentorDeviceTokens, pushData, (err, res) => {
            console.log(err, res)
        })
    }

    if (payloadData.userIdTag && payloadData.userIdTag.length) {

        let deviceTokenCollection = [];
        for (let user of payloadData.userIdTag) {

            let deviceTokens = await getRequired(Model.Users, { userName: new RegExp(user, 'i') }, {
                _id: 1,
                deviceToken: 1
            }, { lean: true })

            if (deviceTokens.length) {

                await createData(Model.Notifications, {
                    toId: deviceTokens[0]._id,
                    byId: userData._id, createdOn: +new Date, commentId: data._id,
                    postId: payloadData.postId,
                    text: userData.userName + " has tagged you in comment.",
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_COMMENT
                });

                deviceTokenCollection.push(deviceTokens[0].deviceToken)
            }

        }

        let pushData = {
            id: data._id,
            byId: userData._id,
            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_COMMENT,
            msg: userData.userName + " has tagged you in comment."
        };

        console.log('++++++++++++++++++++', pushData, deviceTokenCollection)

        pushNotification.sendMultiUser(deviceTokenCollection, pushData, (err, res) => {
        })

    }

    // await updateData(Model.Posts, {_id: payloadData.postId}, {$inc:{commentCount: 1}}, {new: true, lean: true})
    // var finalData = await Service.populateTheSearchData(Model.MediaComments, data, [{path: "commentBy", select: "fullName userName imageUrl", model: "Users"}])
    // let copyFinal = finalData.toObject()
    // copyFinal.liked = false
    return data

}

let deleteCommentReply = async (payloadData, userData) => {
    try {
        let criteria = {}
        if (payloadData.commentId) {
            criteria._id = payloadData.commentId
            // criteria.commentBy = userData._id
            criteria.isDeleted = false

            let deleteData = await updateData(Model.Comments, criteria, { $set: { isDeleted: true } }, {
                new: true,
                lean: true
            })
            if (deleteData) {
                let repliesList = await Service.update(Model.Replies, {
                    commentId: payloadData.commentId,
                    isDeleted: false
                }, { $set: { isDeleted: true } }, { multi: true })
                if (repliesList.n) {
                    await Service.update(Model.Posts, {
                        _id: deleteData.postId,
                        isDeleted: false
                    }, { $inc: { commentCount: -(repliesList.n + 1) } }, { new: true, lean: true })
                }
                return deleteData
            }
        }
        if (payloadData.replyId) {
            criteria._id = payloadData.replyId
            // criteria.replyBy = userData._id
            criteria.isDeleted = false

            let deleteData = await updateData(Model.Replies, criteria, { $set: { isDeleted: true } }, {
                new: true,
                lean: true
            })
            if (deleteData) {
                await Service.update(Model.Posts, {
                    _id: deleteData.postId,
                    isDeleted: false
                }, { $inc: { commentCount: -1 } }, { new: true, lean: true })
                return deleteData
            }
        }


        if (payloadData.mediaId) {
            if (payloadData.commentId) {
                criteria._id = payloadData.commentId
                // criteria.commentBy = userData._id
                criteria.isDeleted = false
                let deleteData = await updateData(Model.MediaComments, criteria, { $set: { isDeleted: true } }, {
                    new: true,
                    lean: true
                })
                if (deleteData) {
                    let repliesList = await Service.update(Model.MediaReplies, {
                        commentId: payloadData.commentId,
                        isDeleted: false
                    }, { $set: { isDeleted: true } }, { multi: true })
                    console.log(repliesList.n);
                    //    if(repliesList.n){
                    //         await Service.update(Model.Posts, {_id: deleteData.postId, isDeleted: false}, {$inc: {commentCount: -(repliesList.n + 1)}}, {new: true, lean: true})
                    //     }
                    return deleteData
                }
            }

            if (payloadData.replyId) {
                criteria._id = payloadData.replyId
                // criteria.replyBy = userData._id
                criteria.isDeleted = false

                let deleteData = await updateData(Model.MediaReplies, criteria, { $set: { isDeleted: true } }, {
                    new: true,
                    lean: true
                })
                if (deleteData) {
                    await Service.update(Model.Posts, {
                        _id: deleteData.postId,
                        isDeleted: false
                    }, { $inc: { commentCount: -1 } }, { new: true, lean: true })
                    return deleteData
                }
            }
        }
    } catch (e) {
        console.log(e)
    }
}
/**
 * @description: creating and editing reply
 * @param {string} authorization
 * @param {string} replyId
 * @param {string} commentId
 * @param {Array} userIdTag
 * @param {string} reply
 * @returns: saved reply data
 */
let addEditMediaReplies = async (payloadData, userData) => {
    payloadData.replyBy = userData._id;
    // update
    if (payloadData.replyId) {
        try {
            payloadData.editAt = +new Date;
            let update = await Model.MediaReplies.findOneAndUpdate({ _id: payloadData.replyId }, payloadData, { new: true });
            let find = await Model.MediaReplies.findById(update._id).populate([{
                path: "replyBy",
                select: "fullName userName imageUrl",
                model: "Users"
            }]);
            return find;
        } catch (e) {
            console.log(e);
        }
    } else {
        // insert
        payloadData.createdOn = +new Date;
        let save = await new Model.MediaReplies(payloadData).save();
        let find = await Model.MediaReplies.findById(save._id).populate([{
            path: "replyBy",
            select: "fullName userName imageUrl",
            model: "Users"
        }]);

        // send notification
        try {
            let notifications = await sendNotiMediaReplies(payloadData, userData, find);
            console.log(notifications);
        } catch (err) {
            return err;
        }

        return find;
    }
}

let sendNotiMediaReplies = async (payloadData, userData, data) => {


    if (!payloadData.userIdTag) {
        payloadData.userIdTag = []
    }
    let postData = await getRequired(Model.Posts, { _id: payloadData.postId }, { groupId: 1, postBy: 1 }, { lean: true })

    let postByData = await getRequired(Model.Users, { _id: postData[0].postBy, isDeleted: false }, {
        userName: 1,
        _id: 1,
        deviceToken: 1
    }, { lean: true })
    let commenterData = await getRequired(Model.Users, { _id: payloadData.commentBy, isDeleted: false }, {
        userName: 1,
        _id: 1,
        deviceToken: 1
    }, { lean: true })
    let replyData = await getRequiredPopulate(Model.MediaReplies, {
        commentId: payloadData.commentId,
        isDeleted: false
    }, {}, { lean: true }, [{ path: "replyBy", select: " _id deviceToken", model: "Users" }])
    let replierDeviceToken = [], replierUserId = []

    for (let obj of replyData) {
        if (JSON.stringify(userData._id) != JSON.stringify(obj.replyBy._id) && (JSON.stringify(postData[0].postBy) != JSON.stringify(obj.replyBy._id))) {
            replierDeviceToken.push(obj.replyBy.deviceToken)
            replierUserId.push(obj.replyBy._id)
        }
    }

    replierDeviceToken = _.uniqBy(replierDeviceToken, "deviceToken")
    if (postByData && postByData.length && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
        if (JSON.stringify(postByData[0]._id) !== JSON.stringify(userData._id)) {

            await createData(Model.Notifications, {
                toId: postByData[0]._id,
                text: userData.fullName + ' has replied on your post',
                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                postId: payloadData.postId,
                commentId: payloadData.commentId, replyId: data._id, createdOn: +new Date
            })

            // toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})

            let pushData = {
                id: payloadData.commentId,
                byId: userData._id,
                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                msg: userData.fullName + ' has replied on your post'
            };

            pushNotification.sendPush(postByData[0].deviceToken, pushData, (err, res) => {
                console.log(err, res)
            })
        }
    }

    if (commenterData && commenterData.length && !(JSON.stringify(payloadData.userIdTag).includes(commenterData[0].userName))) {
        if (JSON.stringify(payloadData.commentBy) !== JSON.stringify(userData._id)) {

            await createData(Model.Notifications, {
                toId: payloadData.commentBy,
                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                text: userData.fullName + ' has replied on your comment',
                postId: payloadData.postId,
                commentId: payloadData.commentId, replyId: data._id, createdOn: +new Date
            })

            // toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})

            let pushData = {
                id: payloadData.commentId,
                byId: userData._id,
                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                msg: userData.fullName + ' has replied on your comment'
            };

            pushNotification.sendPush(commenterData[0].deviceToken, pushData, (err, res) => {
                console.log(err, res)
            })
        }
    }

    if (replierDeviceToken.length) {
        for (let id of replierUserId) {
            await createData(Model.Notifications, {
                toId: id,
                byId: userData._id,
                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                postId: payloadData.postId,
                text: userData.fullName + ' has replied on your comment',
                commentId: payloadData.commentId,
                replyId: data._id,
                createdOn: +new Date
            })
        }

        let pushData = {
            id: payloadData.commentId,
            byId: userData._id,
            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
            msg: userData.fullName + ' has replied on your comment'
        };

        pushNotification.sendMultiUser(replierDeviceToken, pushData, (err, res) => {
            console.log(err, res)
        })
    }

    if (payloadData.userIdTag && payloadData.userIdTag.length) {
        console.log("+++++++++++++++++++++++++++++++++++++++++++++++++ user Tags");

        let deviceTokenCollection = [];

        for (let user of payloadData.userIdTag) {

            let deviceTokens = await getRequired(Model.Users, { userName: user }, { _id: 1, deviceToken: 1 }, { lean: true })
            console.log("-----------------", deviceTokens)
            if (deviceTokens && deviceTokens.length) {
                if (JSON.stringify(userData._id) === JSON.stringify(deviceTokens[0]._id) /*|| JSON.stringify(payloadData.commentBy) === JSON.stringify(userData._id)*/) {
                    continue
                }

                await createData(Model.Notifications, {
                    toId: deviceTokens[0]._id,
                    byId: userData._id, createdOn: +new Date,
                    text: userData.userName + " has tagged you in the reply",
                    postId: payloadData.postId,
                    replyId: data._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_REPLY
                });

                deviceTokenCollection.push(deviceTokens[0].deviceToken)
            }
        }
        let pushData = {
            id: data._id,
            byId: userData._id,
            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_REPLY,
            msg: userData.userName + " has tagged you in the reply"
        };
        pushNotification.sendMultiUser(deviceTokenCollection, pushData, (err, res) => {
            console.log(err, res)
        })
    }

    // await updateData(Model.Posts, {_id: payloadData.postId}, {$inc:{commentCount: 1}}, {new: true, lean: true})
    // let finalData =  await Service.populateTheSearchData(Model.MediaReplies, data, [{path: "replyBy", select: "fullName userName imageUrl", model: "Users"}])

    // let copyFinal = finalData.toObject()
    // copyFinal.liked = false
    return data

}

let addEditReplies = async (payloadData, userData) => {
    try {
        // if media id then call add edit media reply
        if (payloadData.mediaId) {
            return addEditMediaReplies(payloadData, userData);
        }

        let query = {}
        if (payloadData.commentId)
            query.commentId = payloadData.commentId
        if (payloadData.postId) {
            let checkPost = await getRequired(Model.Posts, { _id: payloadData.postId, isDeleted: true }, {}, { lean: true })
            if (checkPost.length) {
                return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
            } else {
                query.postId = payloadData.postId
            }
        }
        if (payloadData.reply)
            query.reply = payloadData.reply
        // if(payloadData.userIdTag)
        //     query.userIdTag = payloadData.userIdTag

        if (payloadData.replyId) {
            let criteria = {
                isDeleted: false,
                _id: payloadData.replyId
            };
            query.editAt = +new Date;

            let data = await updateData(Model.Replies, criteria, query, { new: true, lean: true })
            if (data)
                return data
        } else {

            query.createdOn = +new Date;
            query.replyBy = userData._id;
            query.commentId = payloadData.commentId;
            query.postId = payloadData.postId;
            let data = await Service.saveData(Model.Replies, query)
            data.liked = false;

            if (data) {

                if (!payloadData.userIdTag) {
                    payloadData.userIdTag = []
                }
                let postData = await getRequired(Model.Posts, { _id: payloadData.postId }, {
                    groupId: 1,
                    postBy: 1
                }, { lean: true })

                let postByData = await getRequired(Model.Users, {
                    _id: postData[0].postBy,
                    isDeleted: false
                }, { userName: 1, _id: 1, deviceToken: 1 }, { lean: true })
                let commenterData = await getRequired(Model.Users, {
                    _id: payloadData.commentBy,
                    isDeleted: false
                }, { userName: 1, _id: 1, deviceToken: 1 }, { lean: true })
                let replyData = await getRequiredPopulate(Model.Replies, {
                    commentId: payloadData.commentId,
                    isDeleted: false
                }, {}, { lean: true }, [{ path: "replyBy", select: " _id deviceToken", model: "Users" }])
                let replierDeviceToken = [], replierUserId = []

                for (let obj of replyData) {
                    if (JSON.stringify(userData._id) != JSON.stringify(obj.replyBy._id) && (JSON.stringify(postData[0].postBy) != JSON.stringify(obj.replyBy._id))) {
                        replierDeviceToken.push(obj.replyBy.deviceToken)
                        replierUserId.push(obj.replyBy._id)
                    }
                }

                replierDeviceToken = _.uniqBy(replierDeviceToken, "deviceToken")
                if (postByData && postByData.length && !(JSON.stringify(payloadData.userIdTag).includes(postByData[0].userName))) {
                    if (JSON.stringify(postByData[0]._id) !== JSON.stringify(userData._id)) {

                        await createData(Model.Notifications, {
                            toId: postByData[0]._id,
                            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                            text: userData.fullName + ' has replied on your post',
                            postId: payloadData.postId,
                            commentId: payloadData.commentId, replyId: data._id, createdOn: +new Date
                        })

                        // toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})

                        let pushData = {
                            id: payloadData.commentId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                            msg: userData.fullName + ' has replied on your post'
                        };

                        pushNotification.sendPush(postByData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                }

                if (commenterData && commenterData.length && !(JSON.stringify(payloadData.userIdTag).includes(commenterData[0].userName))) {
                    if (JSON.stringify(payloadData.commentBy) !== JSON.stringify(userData._id)) {

                        await createData(Model.Notifications, {
                            toId: payloadData.commentBy,
                            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                            text: userData.fullName + ' has replied on your comment',
                            postId: payloadData.postId,
                            commentId: payloadData.commentId, replyId: data._id, createdOn: +new Date
                        })

                        // toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})

                        let pushData = {
                            id: payloadData.commentId,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                            msg: userData.fullName + ' has replied on your comment'
                        };

                        pushNotification.sendPush(commenterData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                }

                if (replierDeviceToken.length) {
                    for (let id of replierUserId) {
                        await createData(Model.Notifications, {
                            toId: id,
                            byId: userData._id,
                            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                            postId: payloadData.postId,
                            text: userData.fullName + ' has replied on your comment',
                            commentId: payloadData.commentId,
                            replyId: data._id,
                            createdOn: +new Date
                        })
                    }

                    let pushData = {
                        id: payloadData.commentId,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REPLY,
                        msg: userData.fullName + ' has replied on your comment'
                    };

                    pushNotification.sendMultiUser(replierDeviceToken, pushData, (err, res) => {
                        console.log(err, res)
                    })
                }

                if (payloadData.userIdTag && payloadData.userIdTag.length) {
                    console.log("+++++++++++++++++++++++++++++++++++++++++++++++++ user Tags");

                    let deviceTokenCollection = [];

                    for (let user of payloadData.userIdTag) {

                        let deviceTokens = await getRequired(Model.Users, { userName: user }, {
                            _id: 1,
                            deviceToken: 1
                        }, { lean: true })
                        console.log("-----------------", deviceTokens)
                        if (deviceTokens && deviceTokens.length) {
                            if (JSON.stringify(userData._id) === JSON.stringify(deviceTokens[0]._id) /*|| JSON.stringify(payloadData.commentBy) === JSON.stringify(userData._id)*/) {
                                continue
                            }

                            await createData(Model.Notifications, {
                                toId: deviceTokens[0]._id,
                                byId: userData._id, createdOn: +new Date,
                                text: userData.userName + " has tagged you in the reply",
                                postId: payloadData.postId,
                                replyId: data._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_REPLY
                            });

                            deviceTokenCollection.push(deviceTokens[0].deviceToken)
                        }
                    }
                    let pushData = {
                        id: data._id,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.TAG_REPLY,
                        msg: userData.userName + " has tagged you in the reply"
                    };
                    pushNotification.sendMultiUser(deviceTokenCollection, pushData, (err, res) => {
                        console.log(err, res)
                    })
                }

                await updateData(Model.Posts, { _id: payloadData.postId }, { $inc: { commentCount: 1 } }, {
                    new: true,
                    lean: true
                })
                let finalData = await Service.populateTheSearchData(Model.Replies, data, [{
                    path: "replyBy",
                    select: "fullName userName imageUrl",
                    model: "Users"
                }])

                let copyFinal = finalData.toObject()
                copyFinal.liked = false
                return copyFinal
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: can follow or unfollow user
 * @param {string} authorization
 * @param {string} userId
 * @param {double} action
 * @returns: follow unfollow data
 */
let followUnfollow = async (payloadData, userData) => {
    try {
        let update = {}, updateOther = {}, ownCriteria = {}, otherCriteria = {};

        if (payloadData.action === 1) {

            let userProfileData = await getRequired(Model.Users, {
                _id: payloadData.userId,
                isDeleted: false
            }, { isAccountPrivate: 1, deviceToken: 1 }, { lean: true })
            console.log(userProfileData[0].isAccountPrivate)
            if (userProfileData && userProfileData.length && userProfileData[0].isAccountPrivate) {
                let checkNotificationNotAccepted = await getRequired(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                    actionPerformed: false
                }, {}, { lean: true })
                if (checkNotificationNotAccepted.length) {
                    return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.REQUEST_SEND_ALREADY)
                }

                let checkNotification = await getRequired(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                    actionPerformed: true
                }, {}, { lean: true })
                console.log("+++++++++++++++++++++++", checkNotification.length)

                if (checkNotification.length) {
                    await updateData(Model.Notifications, {
                        toId: payloadData.userId,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                        actionPerformed: true
                    }, {
                        $set: {
                            isDeleted: false,
                            actionPerformed: false,
                            isRead: false,
                            createdOn: +new Date
                        }
                    }, { new: true, lean: true })
                    let pushData = {
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                        msg: userData.fullName + " has requested you to follow"
                    }
                    pushNotification.sendPush(userProfileData[0].deviceToken, pushData)
                    socketManager.requestCount(userProfileData[0]._id)
                    return
                } else {
                    await createData(Model.Notifications, {
                        toId: payloadData.userId,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                        text: userData.fullName + " has requested you to follow",
                        createdOn: +new Date,
                        actionPerformed: false
                    })

                    let pushData = {
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
                        msg: userData.fullName + " has requested you to follow"
                    }
                    pushNotification.sendPush(userProfileData[0].deviceToken, pushData)
                    socketManager.requestCount(userProfileData[0]._id)
                    return
                }

            }
            ownCriteria._id = userData._id;

            update.$addToSet = {
                following: payloadData.userId
            };

            update.$inc = {
                followingCount: 1
            }
            otherCriteria._id = payloadData.userId;
            // otherCriteria['followers.userId'] = {$ne : userData._id};

            updateOther.$addToSet = {
                followers: userData._id
            };

            updateOther.$inc = {
                followerCount: 1
            }
            await updateData(Model.Users, ownCriteria, update, { new: true });

            let notPush = await updateData(Model.Users, otherCriteria, updateOther, { new: true });
            if (notPush) {
                let checkNotification = await getRequired(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW
                }, {}, { lean: true })
                if (checkNotification.length) {
                    await updateData(Model.Notifications, {
                        toId: payloadData.userId,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW
                    }, { $set: { createdOn: +new Date, isDeleted: false } }, { lean: true })
                    return
                } else {

                    let toIdData = await getRequired(Model.Users, { _id: payloadData.userId }, {}, { lean: true })
                    let byIdData = await getRequired(Model.Users, { _id: userData._id }, {}, { lean: true })

                    await createData(Model.Notifications, {
                        toId: payloadData.userId,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW,
                        text: byIdData[0].fullName + ' has started following you',
                        createdOn: +new Date
                    })

                    let pushData = {
                        id: userData._id,
                        byId: userData._id,
                        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW,
                        msg: byIdData[0].fullName + ' has started following you'
                    };
                    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                        console.log(err, res)
                    })
                    return
                }
            }

        } else {
            update.$pull = {
                following: payloadData.userId
            }
            update.$inc = {
                followingCount: -1
            }
            updateOther.$pull = {
                followers: userData._id
            }
            updateOther.$inc = {
                followerCount: -1
            }

            await updateData(Model.Users, { _id: userData._id }, update, { new: true });

            await updateData(Model.Users, { _id: payloadData.userId }, updateOther, { new: true });
            await updateData(Model.Notifications, {
                toId: userData._id,
                byId: payloadData.userId,
                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                isDeleted: false
            }, { $set: { isDeleted: true } }, { lean: true, new: true })
            await updateData(Model.Notifications, {
                toId: payloadData.userId,
                byId: userData._id,
                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.FOLLOW,
                isDeleted: false
            }, { $set: { isDeleted: true } }, { lean: true, new: true })

        }
    } catch (e) {
        console.log(e)
    }
}

let acceptFollowRequest = async (payloadData, userData) => {
    if (payloadData.action) {
        let update = {}, updateOther = {}, ownCriteria = {}, otherCriteria = {};
        ownCriteria._id = userData._id;

        update.$addToSet = {
            followers: payloadData.userId
        };

        update.$inc = {
            followerCount: 1
        }
        otherCriteria._id = payloadData.userId;
        // otherCriteria['followers.userId'] = {$ne : userData._id};

        updateOther.$addToSet = {
            following: userData._id
        };

        updateOther.$inc = {
            followingCount: 1
        }
        await updateData(Model.Users, ownCriteria, update, { new: true });

        let notPush = await updateData(Model.Users, otherCriteria, updateOther, { new: true });
        if (notPush) {
            let checkNotification = await getRequired(Model.Notifications, {
                toId: payloadData.userId,
                byId: userData._id,
                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW
            }, {}, { lean: true })
            if (checkNotification && checkNotification.length) {
                await updateData(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW
                }, { $set: { createdOn: +new Date } }, { lean: true, new: true })
            } else {
                await createData(Model.Notifications, {
                    toId: payloadData.userId,
                    byId: userData._id,
                    text: userData.fullName + ' has accepted your follow request',
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                    createdOn: +new Date
                })
            }
            await updateData(Model.Notifications, {
                toId: userData._id,
                byId: payloadData.userId,
                type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW
            }, { isRead: true, actionPerformed: true })
            let toIdData = await getRequired(Model.Users, { _id: payloadData.userId }, {}, { lean: true })
            let pushData = {
                id: userData._id,
                byId: userData._id,
                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.ACCEPT_REQUEST_FOLLOW,
                msg: userData.fullName + ' has accepted your follow request'
            };
            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                console.log(err, res)
            })
        }
        return
    } else {
        await updateData(Model.Notifications, {
            toId: userData._id,
            byId: payloadData.userId,
            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.REQUEST_FOLLOW,
            createdOn: +new Date
        }, { isRead: true, actionPerformed: true })
    }
}

/**
 * @description listing the followers and the followings
 * @param {string} authorization
 * @param {string} flag
 */

let listFollowerFollowing = async (payloadData, userData) => {
    try {
        if (payloadData.flag === 1) {
            let criteria = {}
            criteria._id = userData._id
            let populate = [{
                path: "followers",
                select: "_id imageUrl userName fullName interestTags firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users",
                populate: {
                    path: "interestTags",
                    select: "categoryName",
                    model: "Categories",
                }
            }]
            let data = await getRequiredPopulate(Model.Users, criteria, { followers: 1 }, {
                lean: true,
                skip: ((payloadData.pageNo - 1) * 10),
                limit: 10
            }, populate)
            if (data.length) {
                let followerList = []
                for (let user of data[0].followers) {
                    let tempObj = {}
                    if (JSON.stringify(userData.blockedBy).includes(user._id)) {
                        continue
                    }
                    if (!user.imageVisibilityForEveryone) {
                        if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else if (user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else {
                            tempObj.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                        }
                    } else {
                        tempObj.imageUrl = user.imageUrl
                    }

                    if (!user.nameVisibilityForEveryone) {
                        if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else if (user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else {
                            tempObj.userName = user.userName.substr(0, 3)
                            tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                        }
                    } else {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    }
                    tempObj._id = user._id

                    followerList.push(tempObj)
                }
                return followerList
            } else
                return []
        } else {
            let criteria = {}
            criteria._id = userData._id
            let populate = [{
                path: "following",
                select: "_id imageUrl userName fullName interestTags firstName lastName isAccountPrivate imageVisibility imageVisibilityForFollowers nameVisibilityForFollowers locationVisibility nameVisibility followers imageVisibilityForEveryone nameVisibilityForEveryone",
                model: "Users",
                populate: {
                    path: "interestTags",
                    select: "categoryName",
                    model: "Categories",
                }
            }]
            let data = await getRequiredPopulate(Model.Users, criteria, { following: 1 }, {
                lean: true,
                skip: ((payloadData.pageNo - 1) * 10),
                limit: 10
            }, populate)
            if (data.length) {
                let followingList = []
                for (let user of data[0].following) {
                    let tempObj = {}
                    if (JSON.stringify(userData.blockedBy).includes(user._id)) {
                        continue
                    }
                    if (!user.imageVisibilityForEveryone) {
                        if (user.imageVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else if (user.imageVisibility.length && JSON.stringify(user.imageVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.imageUrl = user.imageUrl
                        } else {
                            tempObj.imageUrl = {
                                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                            }
                        }
                    } else {
                        tempObj.imageUrl = user.imageUrl
                    }

                    if (!user.nameVisibilityForEveryone) {
                        if (user.nameVisibilityForFollowers && JSON.stringify(user.followers).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else if (user.nameVisibility.length && JSON.stringify(user.nameVisibility).includes(JSON.stringify(userData._id))) {
                            tempObj.userName = user.userName
                            tempObj.fullName = user.fullName
                        } else {
                            tempObj.userName = user.userName.substr(0, 3)
                            tempObj.fullName = user.firstName.substr(0, 1) + ".... "
                        }
                    } else {
                        tempObj.userName = user.userName
                        tempObj.fullName = user.fullName
                    }
                    tempObj._id = user._id

                    followingList.push(tempObj)
                }
                return followingList
            } else
                return []
        }
    } catch (e) {
        console.log(e)
    }
}

let userFollowerFollowing = async (payloadData, userData) => {
    try {
        let data = await getRequired(Model.Users, { _id: userData._id }, { followers: 1, following: 1 }, {
            lean: true,
            limit: 1
        })
        if (data.length) {
            let followerList = data[0].followers
            let followingList = data[0].following
            let final = _.values(_.merge(
                _.keyBy(followerList, 'userId'),
                _.keyBy(followingList, 'userId')
            ))
            let populateData = await Service.populateTheSearchData(Model.Users, final, [{
                path: "userId",
                select: "fullName userName imageUrl",
                model: "Users"
            }])
            if (populateData.length) {
                let combinedList = []
                for (let user of populateData) {
                    combinedList.push(user.userId)
                }
                return combinedList
            } else
                return []
        }
    } catch (e) {
        console.log(e)
    }
};

let requestCounts = async (payloadData, userData) => {
    try {
        let criteria = {}
        criteria.toId = userData._id
        criteria.$or = [{ type: { $eq: "REQUEST_VENUE" } }, { type: { $eq: "REQUEST_GROUP" } }]
        criteria.actionPerformed = false

        let requestCount = await Service.count(Model.Notifications, criteria)
        if (requestCount) {
            return { requestCount }
        } else {
            return { requestCount: 0 }
        }
    } catch (e) {
        console.log(e)
    }
}
/**
 * @description: can follow or unfollow user
 * @param {string} authorization
 * @param {string} userId
 * @param {double} action
 * @returns: follow unfollow data
 */

let likeOrUnlike = async (payloadData, userData) => {
    try {
        console.log('----***---', payloadData);
        // call it to like media
        if (payloadData.mediaId && payloadData.commentId == undefined && payloadData.replyId == undefined) {
            console.log('---likeOrUnlikeMedia---');
            return likeOrUnlikeMedia(payloadData, userData);
        }

        // call it to like media comments
        if (payloadData.mediaId && payloadData.commentId != undefined && payloadData.replyId == undefined) {
            console.log('---likeOrUnlikeMediaCommentId---');
            return likeOrUnlikeMediaCommentId(payloadData, userData);
        }

        // call it to like media comments
        if (payloadData.mediaId && payloadData.replyId != undefined) {
            console.log('---likeOrUnlikeMediaReplyId---');
            return likeOrUnlikeMediaReplyId(payloadData, userData);
        }

        console.log('like unlike main');
        if (payloadData.action === 1) {
            let criteria = {};
            let dataToSet = {};
            dataToSet.$addToSet = { likes: userData._id }
            dataToSet.$inc = {
                likeCount: 1
            };

            if (payloadData.postId) {
                criteria._id = payloadData.postId;
                criteria.isDeleted = false;
            }
            if (payloadData.commentId) {
                criteria._id = payloadData.commentId;
                criteria.isDeleted = false;
            }
            if (payloadData.replyId) {
                criteria._id = payloadData.replyId;
                criteria.isDeleted = false;
            }
            if (payloadData.postId) {
                let checkBooleanVar = false
                let data = await updateData(Model.Posts, criteria, dataToSet, { new: true, lean: true })
                if (data) {
                    let checkNotification = await Service.count(Model.Notifications, {
                        toId: payloadData.postBy,
                        byId: userData._id,
                        type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                        postId: payloadData.postId
                    })
                    let postDetails = await getRequired(Model.Posts, { _id: payloadData.postId }, {
                        postBy: 1,
                        groupId: 1,
                        isDeleted: 1
                    }, { lean: true })
                    if (postDetails && postDetails[0].isDeleted) {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
                    }
                    if (postDetails && postDetails[0].groupId) {
                        let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                            userId: payloadData.postBy,
                            isDeleted: false,
                            groupId: postDetails[0].groupId
                        }, { lean: true })
                        if (postGroupMemberCheck.length) {
                            checkBooleanVar = true
                        }
                    }
                    if (checkNotification) {
                        if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.postBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                                postId: payloadData.postId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        } else if (!postDetails[0].groupId && !checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.postBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                                postId: payloadData.postId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        }

                        // let toIdData = await getRequired(Model.Users, {_id: payloadData.postBy}, {}, {lean: true});

                        // let pushData ={
                        //     id: data._id,
                        //     byId: userData._id,
                        //     TYPE : Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                        //     msg : userData.fullName +' has liked your post'
                        // };

                        // pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res)=>{console.log(err, res)})
                    }
                    if ((JSON.stringify(payloadData.postBy) !== JSON.stringify(userData._id)) && !checkNotification) {

                        if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                            await createData(Model.Notifications, {
                                toId: payloadData.postBy,
                                text: userData.fullName + ' has liked your post',
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                                postId: payloadData.postId, createdOn: +new Date
                            });

                            let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true });

                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                                msg: userData.fullName + ' has liked your post'
                            };

                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        } else if (!postDetails[0].groupId && !checkBooleanVar) {
                            await createData(Model.Notifications, {
                                toId: payloadData.postBy,
                                text: userData.fullName + ' has liked your post',
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                                postId: payloadData.postId, createdOn: +new Date
                            });

                            let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true });

                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_MEDIA,
                                msg: userData.fullName + ' has liked your post'
                            };

                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        }

                    }
                    return data
                } else return Promise.reject()
            } else if (payloadData.commentId) {
                let checkBooleanVar = false

                let data = await updateData(Model.Comments, criteria, dataToSet, { new: true, lean: true })
                if (data) {
                    let checkNotification = await Service.count(Model.Notifications, {
                        toId: payloadData.commentBy,
                        byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                        postId: data.postId,
                        commentId: payloadData.commentId
                    })
                    let commentDetails = await getRequired(Model.Comments, {
                        _id: payloadData.commentId,
                        isDeleted: false
                    }, { postId: 1 }, { lean: true })
                    let postDetails = await getRequired(Model.Posts, { _id: commentDetails[0].postId }, {
                        postBy: 1,
                        groupId: 1,
                        isDeleted: 1
                    }, { lean: true })
                    if (postDetails && postDetails[0].isDeleted) {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
                    }
                    if (postDetails && postDetails[0].groupId) {
                        let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                            userId: payloadData.postBy,
                            isDeleted: false,
                            groupId: postDetails[0].groupId
                        }, { lean: true })
                        if (postGroupMemberCheck.length) {
                            checkBooleanVar = true
                        }
                    }
                    if (checkNotification) {
                        if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.commentBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                                postId: data.postId, commentId: payloadData.commentId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        } else if (!postDetails[0].groupId && !checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.commentBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                                postId: data.postId, commentId: payloadData.commentId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        }
                        // let toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})
                        // let pushData ={
                        //     id: data._id,
                        //     byId: userData._id,
                        //     TYPE : Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                        //     msg : userData.fullName +' has liked your comment'
                        // };
                        // pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res)=>{console.log(err, res)})
                    }
                    if ((JSON.stringify(payloadData.commentBy) !== JSON.stringify(userData._id)) && !checkNotification && postDetails && postDetails[0].groupId && checkBooleanVar) {
                        // if(postDetails && postDetails[0].groupId && checkBooleanVar){
                        await createData(Model.Notifications, {
                            toId: payloadData.commentBy,
                            text: userData.fullName + ' has liked your comment',
                            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                            postId: data.postId,
                            commentId: payloadData.commentId, createdOn: +new Date
                        });

                        let toIdData = await getRequired(Model.Users, { _id: payloadData.commentBy }, {}, { lean: true })
                        let pushData = {
                            id: data._id,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                            msg: userData.fullName + ' has liked your comment'
                        };
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                        // }
                    } else if ((JSON.stringify(payloadData.commentBy) !== JSON.stringify(userData._id)) && !checkNotification && !postDetails[0].groupId && !checkBooleanVar) {
                        await createData(Model.Notifications, {
                            toId: payloadData.commentBy,
                            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                            postId: data.postId,
                            text: userData.fullName + ' has liked your comment',
                            commentId: payloadData.commentId, createdOn: +new Date
                        });

                        let toIdData = await getRequired(Model.Users, { _id: payloadData.commentBy }, {}, { lean: true })
                        let pushData = {
                            id: data._id,
                            byId: userData._id,
                            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                            msg: userData.fullName + ' has liked your comment'
                        };
                        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                            console.log(err, res)
                        })
                    }
                    return data
                } else return Promise.reject()
            } else if (payloadData.replyId) {

                let data = await updateData(Model.Replies, criteria, dataToSet, { new: true, lean: true });
                if (data) {
                    let checkBooleanVar = false
                    var allData = await Service.populateTheSearchData(Model.Comments, data, [{
                        path: "commentId",
                        select: "postId",
                        model: "Comments"
                    }])
                    let checkNotification = await Service.count(Model.Notifications, {
                        toId: payloadData.replyBy,
                        byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                        postId: allData.commentId.postId,
                        replyId: payloadData.replyId
                    })
                    let replyDetails = await getRequired(Model.Replies, {
                        _id: payloadData.replyId,
                        isDeleted: false
                    }, { postId: 1 }, { lean: true })
                    let postDetails = await getRequired(Model.Posts, { _id: replyDetails[0].postId }, {
                        postBy: 1,
                        groupId: 1,
                        isDeleted: 1
                    }, { lean: true })
                    if (postDetails && postDetails[0].isDeleted) {
                        return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
                    }
                    if (postDetails && postDetails[0].groupId) {
                        let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                            userId: payloadData.postBy,
                            isDeleted: false,
                            groupId: postDetails[0].groupId
                        }, { lean: true })
                        if (postGroupMemberCheck.length) {
                            checkBooleanVar = true
                        }
                    }
                    if (checkNotification) {
                        if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.replyBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                postId: allData.commentId.postId,
                                replyId: payloadData.replyId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        } else if (!postDetails[0].groupId && !checkBooleanVar) {
                            await updateData(Model.Notifications, {
                                toId: payloadData.replyBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                postId: allData.commentId.postId,
                                replyId: payloadData.replyId, isDeleted: true
                            }, { $set: { isDeleted: false } }, { lean: true })
                        }
                        //     let toIdData = await getRequired(Model.Users, {_id: payloadData.replyBy}, {}, {lean: true});

                        // let pushData ={
                        //     id: data._id,
                        //     byId: userData._id,
                        //     TYPE : Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                        //     msg : userData.fullName +' has liked your reply'
                        // };
                        // pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res)=>{console.log(err, res)})
                    }
                    if ((JSON.stringify(payloadData.replyBy) != JSON.stringify(userData._id)) && !checkNotification) {
                        if (postDetails && postDetails[0].groupId && checkBooleanVar) {

                            await createData(Model.Notifications, {
                                toId: payloadData.replyBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                postId: allData.commentId.postId,
                                text: userData.fullName + ' has liked your reply',
                                replyId: payloadData.replyId, createdOn: +new Date
                            });

                            let toIdData = await getRequired(Model.Users, { _id: payloadData.replyBy }, {}, { lean: true });

                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                msg: userData.fullName + ' has liked your reply'
                            };
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        } else if (!postDetails[0].groupId && !checkBooleanVar) {
                            await createData(Model.Notifications, {
                                toId: payloadData.replyBy,
                                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                postId: allData.commentId.postId,
                                text: userData.fullName + ' has liked your reply',
                                replyId: payloadData.replyId, createdOn: +new Date
                            });

                            let toIdData = await getRequired(Model.Users, { _id: payloadData.replyBy }, {}, { lean: true });

                            let pushData = {
                                id: data._id,
                                byId: userData._id,
                                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                                msg: userData.fullName + ' has liked your reply'
                            };
                            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                                console.log(err, res)
                            })
                        }
                    }

                    return data
                } else return Promise.reject()
            }
        } else if (payloadData.action === 2) {

            let criteria = {};
            let dataToSet = {};
            dataToSet.$pull = {
                likes: userData._id
            };

            dataToSet.$inc = {
                likeCount: -1
            };

            if (payloadData.postId) {
                criteria._id = payloadData.postId
                criteria.isDeleted = false
            }

            if (payloadData.commentId) {
                criteria._id = payloadData.commentId
                criteria.isDeleted = false
            }

            if (payloadData.replyId) {
                criteria._id = payloadData.replyId
                criteria.isDeleted = false
            }
            if (payloadData.postId) {

                let data = await updateData(Model.Posts, criteria, dataToSet, { new: true, lean: true })
                if (data) {
                    await updateData(Model.Notifications, {
                        toId: payloadData.postBy,
                        byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                        postId: payloadData.postId
                    }, { $set: { isDeleted: true } }, { lean: true })

                } else {
                    return Promise.reject()
                }
            } else if (payloadData.commentId) {

                let data = await updateData(Model.Comments, criteria, dataToSet, { new: true, lean: true })
                if (data) {
                    await updateData(Model.Notifications, {
                        toId: payloadData.commentBy,
                        byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                        postId: data.postId,
                        commentId: payloadData.commentId
                    }, { $set: { isDeleted: true } }, { lean: true })
                } else return Promise.reject()
            } else if (payloadData.replyId) {

                let data = await updateData(Model.Replies, criteria, dataToSet, { new: true, lean: true })
                if (data) {
                    allData = await Service.populateTheSearchData(Model.Comments, data, [{
                        path: "commentId",
                        select: "postId",
                        model: "Comments"
                    }])
                    await updateData(Model.Notifications, {
                        toId: payloadData.replyBy,
                        byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                        postId: allData.commentId.postId,
                        replyId: payloadData.replyId
                    }, { $set: { isDeleted: true } }, { lean: true })
                } else return Promise.reject()
            }
        }

    } catch (e) {
        console.log(e)
    }
}

let likeOrUnlikeMediaReplyId = async (payloadData, userData) => {
    let criteria = {};
    let dataToSet = {};
    if (payloadData.action === 1) {
        dataToSet.$addToSet = { likes: userData._id }
        dataToSet.$inc = {
            likeCount: 1
        };
    } else if (payloadData.action === 2) {
        dataToSet.$pull = {
            likes: userData._id
        };
        dataToSet.$inc = {
            likeCount: -1
        };
    }
    criteria._id = payloadData.replyId;
    let update = Model.MediaReplies.findOneAndUpdate(criteria, dataToSet, { new: true });

    // send notification
    try {
        let notifications = await sendLikeNotiMediaReply(payloadData, userData, update);
        console.log(notifications);
    } catch (err) {
        return err;
    }
    return update;
}

let sendLikeNotiMediaReply = async (payloadData, userData, data) => {
    if (payloadData.action == 1) {
        let checkBooleanVar = false;
        var allData = await Service.populateTheSearchData(Model.MediaComments, data, [{
            path: "commentId",
            select: "postId",
            model: "Comments"
        }])
        let checkNotification = await Service.count(Model.Notifications, {
            toId: payloadData.replyBy,
            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
            postId: allData.commentId.postId,
            replyId: payloadData.replyId
        })
        let replyDetails = await getRequired(Model.MediaReplies, {
            _id: payloadData.replyId,
            isDeleted: false
        }, { postId: 1 }, { lean: true })
        let postDetails = await getRequired(Model.Posts, { _id: replyDetails[0].postId }, {
            postBy: 1,
            groupId: 1,
            isDeleted: 1
        }, { lean: true })
        if (postDetails && postDetails[0].isDeleted) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
        }
        if (postDetails && postDetails[0].groupId) {
            let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                userId: payloadData.postBy,
                isDeleted: false,
                groupId: postDetails[0].groupId
            }, { lean: true })
            if (postGroupMemberCheck.length) {
                checkBooleanVar = true
            }
        }
        if (checkNotification) {
            if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.replyBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    postId: allData.commentId.postId,
                    replyId: payloadData.replyId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            } else if (!postDetails[0].groupId && !checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.replyBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    postId: allData.commentId.postId,
                    replyId: payloadData.replyId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            }
        }
        if ((JSON.stringify(payloadData.replyBy) != JSON.stringify(userData._id)) && !checkNotification) {
            if (postDetails && postDetails[0].groupId && checkBooleanVar) {

                await createData(Model.Notifications, {
                    toId: payloadData.replyBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    postId: allData.commentId.postId,
                    text: userData.fullName + ' has liked your reply',
                    replyId: payloadData.replyId, createdOn: +new Date
                });

                let toIdData = await getRequired(Model.Users, { _id: payloadData.replyBy }, {}, { lean: true });

                let pushData = {
                    id: data._id,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    msg: userData.fullName + ' has liked your reply'
                };
                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
            } else if (!postDetails[0].groupId && !checkBooleanVar) {
                await createData(Model.Notifications, {
                    toId: payloadData.replyBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    postId: allData.commentId.postId,
                    text: userData.fullName + ' has liked your reply',
                    replyId: payloadData.replyId, createdOn: +new Date
                });

                let toIdData = await getRequired(Model.Users, { _id: payloadData.replyBy }, {}, { lean: true });

                let pushData = {
                    id: data._id,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
                    msg: userData.fullName + ' has liked your reply'
                };
                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
            }
        }

        return data
    } else {
        allData = await Service.populateTheSearchData(Model.MediaComments, data, [{
            path: "commentId",
            select: "postId",
            model: "Comments"
        }])
        await updateData(Model.Notifications, {
            toId: payloadData.replyBy,
            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_REPLY,
            postId: allData.commentId.postId,
            replyId: payloadData.replyId
        }, { $set: { isDeleted: true } }, { lean: true })
    }

}

let likeOrUnlikeMediaCommentId = async (payloadData, userData) => {
    console.log('like media comment');
    let criteria = {};
    let dataToSet = {};
    if (payloadData.action === 1) {
        dataToSet.$addToSet = { likes: userData._id }
        dataToSet.$inc = {
            likeCount: 1
        };
    } else if (payloadData.action === 2) {
        dataToSet.$pull = {
            likes: userData._id
        };
        dataToSet.$inc = {
            likeCount: -1
        };
    }
    criteria._id = payloadData.commentId;
    let update = await Model.MediaComments.findOneAndUpdate(criteria, dataToSet, { new: true });
    console.log('--****new value---**', update);
    // send notification
    try {
        let notifications = await sendLikeNotiMediaComment(payloadData, userData, update);
        console.log(notifications);
    } catch (err) {
        return err;
    }

    return update;
}

let sendLikeNotiMediaComment = async (payloadData, userData, data) => {
    if (payloadData.action == 1) {
        let checkBooleanVar = false;

        let checkNotification = await Service.count(Model.Notifications, {
            toId: payloadData.commentBy,
            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
            postId: data.postId,
            commentId: payloadData.commentId
        })
        let commentDetails = await getRequired(Model.MediaComments, {
            _id: payloadData.commentId,
            isDeleted: false
        }, { postId: 1 }, { lean: true })
        let postDetails = await getRequired(Model.Posts, { _id: commentDetails[0].postId }, {
            postBy: 1,
            groupId: 1,
            isDeleted: 1
        }, { lean: true })
        if (postDetails && postDetails[0].isDeleted) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
        }
        if (postDetails && postDetails[0].groupId) {
            let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                userId: payloadData.postBy,
                isDeleted: false,
                groupId: postDetails[0].groupId
            }, { lean: true })
            if (postGroupMemberCheck.length) {
                checkBooleanVar = true
            }
        }
        if (checkNotification) {
            if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.commentBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                    postId: data.postId, commentId: payloadData.commentId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            } else if (!postDetails[0].groupId && !checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.commentBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                    postId: data.postId, commentId: payloadData.commentId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            }
            // let toIdData = await getRequired(Model.Users, {_id: payloadData.commentBy}, {}, {lean: true})
            // let pushData ={
            //     id: data._id,
            //     byId: userData._id,
            //     TYPE : Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
            //     msg : userData.fullName +' has liked your comment'
            // };
            // pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res)=>{console.log(err, res)})
        }
        if ((JSON.stringify(payloadData.commentBy) != JSON.stringify(userData._id)) && !checkNotification && postDetails && postDetails[0].groupId && checkBooleanVar) {
            // if(postDetails && postDetails[0].groupId && checkBooleanVar){
            await createData(Model.Notifications, {
                toId: payloadData.commentBy,
                text: userData.fullName + ' has liked your comment',
                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                postId: data.postId,
                commentId: payloadData.commentId, createdOn: +new Date
            });

            let toIdData = await getRequired(Model.Users, { _id: payloadData.commentBy }, {}, { lean: true })
            let pushData = {
                id: data._id,
                byId: userData._id,
                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                msg: userData.fullName + ' has liked your comment'
            };
            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                console.log(err, res)
            })
            // }
        } else if ((JSON.stringify(payloadData.commentBy) != JSON.stringify(userData._id)) && !checkNotification && !postDetails[0].groupId && !checkBooleanVar) {
            await createData(Model.Notifications, {
                toId: payloadData.commentBy,
                text: userData.fullName + ' has liked your comment',
                byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                postId: data.postId,
                commentId: payloadData.commentId, createdOn: +new Date
            });

            let toIdData = await getRequired(Model.Users, { _id: payloadData.commentBy }, {}, { lean: true })
            let pushData = {
                id: data._id,
                byId: userData._id,
                TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
                msg: userData.fullName + ' has liked your comment'
            };
            pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                console.log(err, res)
            })
        }
        return data
    } else {
        await updateData(Model.Notifications, {
            toId: payloadData.commentBy,
            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_COMMENT,
            postId: data.postId,
            commentId: payloadData.commentId
        }, { $set: { isDeleted: true } }, { lean: true })
    }

}

let likeOrUnlikeMedia = async (payloadData, userData) => {
    try {
        if (payloadData.action === 1) {
            let criteria = {};

            if (payloadData.postId) {
                criteria._id = payloadData.postId;
                criteria.isDeleted = false;
            }

            let data = await Model.Posts.findOne(criteria);
            let checkMediaIndex = data.media.findIndex((val) => val._id == payloadData.mediaId);
            // get media key
            if (checkMediaIndex != -1) {
                // check if user has already liked
                let checkUserIndex = data.media[checkMediaIndex].likes.indexOf(userData._id);

                // if user has not liked it then like it
                if (checkUserIndex == -1) {
                    data.media[checkMediaIndex].likeCount += 1;
                    data.media[checkMediaIndex].likes.push(userData._id);
                }
            }

            let savePost = await data.save();
            // send notification
            try {
                let notifications = await sendLikeNotiMedia(payloadData, userData, data);
                console.log(notifications);
            } catch (err) {
                return err;
            }

            return savePost;

        } else {
            let criteria = {};

            if (payloadData.postId) {
                criteria._id = payloadData.postId;
                criteria.isDeleted = false;
            }

            let data = await Model.Posts.findOne(criteria);
            let checkMediaIndex = data.media.findIndex((val) => val._id == payloadData.mediaId);

            if (checkMediaIndex != -1) {
                //data.media[checkMediaIndex].likes.push(userData._id);
                // remove user
                let checkUserIndex2 = data.media[checkMediaIndex].likes.indexOf(userData._id)

                if (checkUserIndex2 != -1) {
                    data.media[checkMediaIndex].likeCount -= 1;
                    data.media[checkMediaIndex].likes.splice(checkUserIndex2, 1);
                }
            }
            let save = await data.save();
            return data;
        }
    } catch (e) {
        console.log(e);
    }
}

let sendLikeNotiMedia = async (payloadData, userData, data) => {
    if (payloadData.action == 1) {

        let checkBooleanVar = false;

        let checkNotification = await Service.count(Model.Notifications, {
            toId: payloadData.postBy,
            byId: userData._id,
            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
            postId: payloadData.postId
        })
        let postDetails = await getRequired(Model.Posts, { _id: payloadData.postId }, {
            postBy: 1,
            groupId: 1,
            isDeleted: 1
        }, { lean: true })
        if (postDetails && postDetails[0].isDeleted) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.POST_DELETED)
        }
        if (postDetails && postDetails[0].groupId) {
            let postGroupMemberCheck = await getRequired(Model.PostGroupMembers, {
                userId: payloadData.postBy,
                isDeleted: false,
                groupId: postDetails[0].groupId
            }, { lean: true })
            if (postGroupMemberCheck.length) {
                checkBooleanVar = true
            }
        }
        if (checkNotification) {
            if (postDetails && postDetails[0].groupId && checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.postBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                    postId: payloadData.postId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            } else if (!postDetails[0].groupId && !checkBooleanVar) {
                await updateData(Model.Notifications, {
                    toId: payloadData.postBy,
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                    postId: payloadData.postId, isDeleted: true
                }, { $set: { isDeleted: false } }, { lean: true })
            }
        }

        if ((payloadData.postBy != userData._id) && !checkNotification) {

            if (postDetails && postDetails[0].groupId && checkBooleanVar) {

                await createData(Model.Notifications, {
                    toId: payloadData.postBy,
                    text: userData.fullName + ' has liked your post',
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                    postId: payloadData.postId, createdOn: +new Date
                });

                let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true });

                let pushData = {
                    id: data._id,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                    msg: userData.fullName + ' has liked your post'
                };

                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
            } else if (!postDetails[0].groupId && !checkBooleanVar) {
                console.log('1');
                await createData(Model.Notifications, {
                    toId: payloadData.postBy,
                    text: userData.fullName + ' has liked your post',
                    byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
                    postId: payloadData.postId, createdOn: +new Date
                });

                let toIdData = await getRequired(Model.Users, { _id: payloadData.postBy }, {}, { lean: true });

                let pushData = {
                    id: data._id,
                    byId: userData._id,
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_MEDIA,
                    msg: userData.fullName + ' has liked your post'
                };

                pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })
            }

        }

        console.log('response sent');
        return data
    } else {
        await updateData(Model.Notifications, {
            toId: payloadData.postBy,
            byId: userData._id, type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.LIKE_POST,
            postId: payloadData.postId
        }, { $set: { isDeleted: true } }, { lean: true })
    }
}

let listLikers = async (payloadData, userData) => {
    try {
        if (payloadData.postId) {
            let uniqLikeData = []
            let likeData = await getRequiredPopulate(Model.Posts, {
                _id: payloadData.postId,
                isDeleted: false
            }, { likes: 1 }, { lean: true }, [{ path: "likes", select: "fullName imageUrl userName", model: "Users" }])
            if (likeData.length) {
                for (let like of likeData[0].likes) {
                    if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(like._id))) {
                        continue
                    }
                    uniqLikeData.push(like)
                }
            }
            uniqLikeData = _.uniqBy(uniqLikeData, "_id")

            if (uniqLikeData.length) {
                return uniqLikeData
            } else {
                return []
            }
        }

        if (payloadData.commentId) {
            let uniqLikeData = []
            let likeData = await getRequiredPopulate(Model.Comments, {
                _id: payloadData.commentId,
                isDeleted: false
            }, { likes: 1 }, { lean: true }, [{ path: "likes", select: "fullName imageUrl userName", model: "Users" }])
            if (likeData.length) {
                for (let like of likeData[0].likes) {
                    if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(like._id))) {
                        continue
                    }
                    uniqLikeData.push(like)
                }
            }
            uniqLikeData = _.uniqBy(uniqLikeData, "_id")

            if (uniqLikeData.length) {
                return uniqLikeData
            } else {
                return []
            }
        }

        if (payloadData.replyId) {
            let uniqLikeData = []
            let likeData = await getRequiredPopulate(Model.Replies, {
                _id: payloadData.replyId,
                isDeleted: false
            }, { likes: 1 }, { lean: true }, [{ path: "likes", select: "fullName imageUrl userName", model: "Users" }])
            if (likeData.length) {
                for (let like of likeData[0].likes) {
                    uniqLikeData.push(like)
                }
            }
            uniqLikeData = _.uniqBy(uniqLikeData, "_id")

            if (uniqLikeData.length) {
                return uniqLikeData
            } else {
                return []
            }
        }
    } catch (e) {
        console.log(e)
    }
}

let listRepliers = async (payloadData, userData) => {
    try {
        if (payloadData.postId) {
            let uniqCommenterData = [], uniqReplierData = []
            let populate = [{ path: "commentBy", select: "fullName imageUrl userName", model: "Users" }, {
                path: "replyBy",
                select: "fullName imageUrl userName",
                model: "Users"
            }]
            let criteria = {}
            criteria.postId = payloadData.postId
            criteria.isDeleted = false
            let commenters = await getRequiredPopulate(Model.Comments, criteria, { commentBy: 1 }, { lean: true }, populate)
            let repliers = await getRequiredPopulate(Model.Replies, criteria, { replyBy: 1 }, { lean: true }, populate)
            if (commenters.length) {
                for (let user of commenters) {
                    if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(user._id))) {
                        continue
                    }
                    uniqCommenterData.push(user.commentBy)
                }
            }
            if (repliers.length) {
                for (let user of repliers) {
                    if (JSON.stringify(userData.blockedBy).includes(JSON.stringify(user._id))) {
                        continue
                    }
                    uniqReplierData.push(user.replyBy)
                }
            }
            var merged = _.merge(_.keyBy(uniqCommenterData, '_id'), _.keyBy(uniqReplierData, '_id'));
            var values = _.values(merged);
            if (values.length) {
                return values
            } else {
                return []
            }
        }
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: for resending otp
 * @param {string} authorization
 * @param {string} email
 * @param {string} countryCode
 * @param {string} phoneNumber
 * @returns: otp data
 */

let resendOTP = async (payloadData) => {
    try {
        let otp = await codegenrate.generateUniqueCustomerId(4)
        console.log(otp)

        return new Promise((resolve, reject) => {
            let criteria = {};

            if (payloadData.email) {
                criteria.email = payloadData.email

                let update = {
                    OTPcode: otp,
                    //OTPcode: '4444',
                };
                Service.findAndUpdate(Model.Users, criteria, update, { new: true, lean: true }).then(result => {
                    if (result) {
                        // otpMail(result,otp);
                        resolve();
                    } else reject(UniversalFunctions.CONFIG.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_EMAIL)
                }).catch(reason => {
                    reject(reason)
                })
            } else if (payloadData.phoneNumber && payloadData.countryCode) {
                criteria.phoneNumber = payloadData.phoneNumber
                criteria.countryCode = payloadData.countryCode

                let update = {
                    //OTPcode: '4444',
                    OTPcode: otp,
                };

                Service.findAndUpdate(Model.Users, criteriat67, update, { new: true, lean: true }).then(result => {
                    if (result) {
                        // let smsData = {To: payloadData.countryCode+payloadData.phoneNumber,
                        //             From: "+12108797398",
                        //             Body: "OTP: " + otp }
                        // await pushNotification.sendSMS(smsData)
                        otpMail(result, otp);
                        resolve();
                    } else reject(UniversalFunctions.CONFIG.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_EMAIL)
                }).catch(reason => {
                    reject(reason)
                })
            }
        })
    } catch (e) {
        console.log(e)
    }
}

/**
 * @description: when user forgets the password
 * @param {string} email
 * @returns: email sending
 */
let forgotPassword = async (payloadData) => {
    try {
        return new Promise((resolve, reject) => {
            let criteria = {
                email: payloadData.email,
            };
            let option = {
                lean: true
            };
            getRequired(Model.Users, criteria, {}, option).then(async result => {
                if (result.length) {
                    if (result[0].isBlocked === true)
                        reject(UniversalFunctions.CONFIG.APP_CONSTANTS.STATUS_MSG.ERROR.BLOCKED);
                    else {
                        Service.findAndUpdate(Model.Users, criteria, { isPasswordReset: false }, {}, (err) => {
                        });
                        // let url =`http://52.35.234.66/reset-password?id=${result[0]._id}&time=${+new Date()}&isReset=false`;
                        let url = `http://35.161.59.237:9000/reset-password?id=${result[0]._id}&time=${+new Date()}&isReset=false`;
                        let forgotPasswordTemplate = await emailTemplates.forgotPasswordTemplate(url)
                        sendEmail.sendEmail(payloadData.email, "Reset Password", forgotPasswordTemplate)
                        resolve(Config.APP_CONSTANTS.STATUS_MSG.SUCCESS.RESET_PASSWORD_LINK)
                    }
                } else reject(UniversalFunctions.CONFIG.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_EMAIL)
            }).catch(reason => {
                reject(reason)
            })
        });
    } catch (e) {
        console.log(e)
    }
}

let resetPassword = async (payloadData) => {
    try {
        let model;
        let criteria = {
            _id: payloadData.id,
            isDeleted: false
        };
        // if(payloadData.type === 1) model = Modal.Admins;
        // if(payloadData.type === 2) model = Model.Users;

        let check = await getRequired(Model.Users, criteria, {}, { lean: true });

        if (check[0].isPasswordReset) return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.LINK_EXPIRE);
        else if (check[0].password === UniversalFunctions.CryptData(payloadData.password)) {
            return Promise.reject(UniversalFunctions.CONFIG.APP_CONSTANTS.STATUS_MSG.ERROR.SAME_PASSWORD)
        } else {
            let time = (+new Date() - payloadData.time) / 60000;
            if (time <= 10) {
                return new Promise((resolve, reject) => {

                    let dataToSet = {
                        password: UniversalFunctions.CryptData(payloadData.password),
                        isPasswordReset: true
                    };
                    Service.findAndUpdate(Model.Users, criteria, dataToSet, {}).then(result => {
                        resolve()
                    }).catch(reason => {
                        reject(reason)
                    })
                });
            } else return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.LINK_EXPIRE)
        }
    } catch (e) {
        console.log(e)
    }
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

async function sendMail(data, url) {

    let strVar = await emailTemplates.registrationEmail(data, url)

    sendEmail.sendEmail(data.email, 'Password reset', strVar)
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
 * @description: helping function for creating data
 */
var createData = (collection, condition) => {
    return new Promise((resolve, reject) => {
        Service.saveData(collection, condition).then(result => {
            resolve(result)
        }).catch(reason => {
            reject(reason)
        })
    });
}

function requiredOne(collection, criteria, project, option) {

    return new Promise((resolve, reject) => {
        Service.findOne(collection, criteria, project, option).then((result) => {
            if (result)
                resolve(result);
            else resolve([])
        }).catch((reason) => {
            reject(reason)
        })
    });
}

/**
 * @description: helping function for updating data
 */
function updateData(collection, criteria, dataToUpdate, option) {
    return new Promise((resolve, reject) => {
        Service.findAndUpdate(collection, criteria, dataToUpdate, option).then(result => {
            resolve(result)
        }).catch(reason => {
            reject(reason)
        })
    })
}

function otpMail(userData, otp) {
    console.log(userData)
    let strVar = "";
    strVar += "<!DOCTYPE html>";
    strVar += "    <html>";
    strVar += "    <head>";
    strVar += "    <title></title>";
    strVar += "    </head>";
    strVar += "    <body>";
    strVar += "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse:collapse\">";
    strVar += "    <tbody>";
    strVar += "    <tr>";
    strVar += "    <td align=\"center\" valign=\"top\" style=\"background:#ffffff none no-repeat center/cover;background-color:#ffffff;background-image:none;background-repeat:no-repeat;background-position:center;background-size:cover;border-top:0;border-bottom:0;padding-top:27px;padding-bottom:63px\">";
    strVar += "    <table align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"border-collapse:collapse;max-width:600px!important\">";
    strVar += "    <tbody>";
    strVar += "    <tr>";
    strVar += "    <td valign=\"top\" style=\"background:transparent none no-repeat center/cover;background-color:transparent;background-image:none;background-repeat:no-repeat;background-position:center;background-size:cover;border-top:0;border-bottom:0;padding-top:0;padding-bottom:0;width:65%\">";
    strVar += "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" style=\"min-width:100%;border-collapse:collapse\">";
    strVar += "    <tbody>";
    strVar += "    <tr>";
    strVar += "    <td valign=\"top\" style=\"padding-top:9px\">";
    strVar += "    <table align=\"left\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:100%;min-width:100%;border-collapse:collapse\" width=\"100%\">";
    strVar += "    <tbody>";
    strVar += "    <tr>";
    strVar += "    <td valign=\"top\" style=\"padding-top:0;padding-right:18px;padding-bottom:9px;padding-left:18px;word-break:break-word;color:#808080;font-family:Helvetica;font-size:16px;line-height:150%;text-align:left\">";
    strVar += "    <p style=\"font-size: 35px;";
    strVar += "    color: black; line-height: 35px;\">Hello " + userData.fullName + "</p>";
    strVar += "<p style=\"font-size:16px!important;margin:10px 0;padding:0;color:#808080;font-family:Helvetica;line-height:150%;text-align:left\"></p>";
    strVar += " <p style=\"font-size:16px!important;margin:10px 0;padding:0;color:#808080;font-family:Helvetica;line-height:150%;text-align:left\">Your OTP for Invire app is - " + otp + ".</p>";
    strVar += "                ";
    strVar += "<p style=\"font-size:16px!important;margin:10px 0;padding:0;color:#808080;font-family:Helvetica;line-height:150%;text-align:left\">Please use this to verify your email.ThankYou</p>";
    strVar += "</td>";
    strVar += "</tr>";
    strVar += "</tbody>";
    strVar += "</table>";
    strVar += "</td>";
    strVar += "</tr>";
    strVar += "</tbody>";
    strVar += "</table>";
    strVar += "";
    strVar += "<span class=\"HOEnZb\">";
    strVar += "    <font color=\"#888888\"></font>";
    strVar += "    </span>";
    strVar += "    </td>";
    strVar += "    </tr>";
    strVar += "    </tbody>";
    strVar += "    </table>";
    strVar += "    </body>";
    strVar += "    </html>";
    strVar += "";
    strVar += "   ";

    sendEmail.sendEmail(userData.email, 'Photo Drip OTP', strVar)
}

/**
 * @description: helping function for getting populated data
 */
function getRequiredPopulate(collection, criteria, project, option, populate) {

    return new Promise((resolve, reject) => {
        Service.populateData(collection, criteria, project, option, populate).then(result => {
            if (result.length)
                resolve(result);
            else resolve([])
        }).catch(reason => {
            reject(reason)
        })
    });
}

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

/**
 * @description: helping function for aggregate data with populate
 */
function aggregateWithPopulate(collection, pipeline, populateOptions) {

    return new Promise((resolve, reject) => {
        Service.aggregateDataWithPopulate(collection, pipeline, populateOptions).then(result => {
            resolve(result)
        }).catch(reason => {
            reject(reason)
        })
    });
}

function calcAge(dateString) {
    var birthday = +new Date(dateString);
    return ~~((Date.now() - birthday) / (31557600000));
}

async function maintainHistory(userId, userIdToStore = null, userNameToStore = null) {
    let criteria = {}, dataToSet = {}
    criteria._id = userId
    criteria.isDeleted = false

    if (userNameToStore) {
        let userNameData = await getRequired(Model.Users, { userName: userNameToStore }, { _id: 1 }, { lean: true })
        userIdToStore = userNameData[0]._id
    }
    if (userIdToStore) {
        dataToSet.$push = {
            homeSearchTop: {
                $each: [userIdToStore],
                $position: 0
            }
        }
    }
    let checkHistory = await getRequired(Model.Users, {
        _id: userId,
        homeSearchTop: { $in: [userIdToStore] },
        isDeleted: false
    }, {}, { lean: true })
    console.log("+++++++++++++++++", checkHistory.length)
    if (checkHistory.length) {
        await updateData(Model.Users, {
            _id: userId,
            isDeleted: false
        }, { $pull: { homeSearchTop: userIdToStore } }, { new: true, lean: true })
        await updateData(Model.Users, { _id: userId, isDeleted: false }, {
            $push: {
                homeSearchTop: {
                    $each: [userIdToStore],
                    $position: 0
                }
            }
        }, { new: true, lean: true })
        return
    }
    let data = await updateData(Model.Users, criteria, dataToSet, { lean: true, new: true })
    if (data) {
        dataToSet = {}
        dataToSet.$pop = {
            homeSearchTop: -1
        }
        console.log("-------------------{{{{{{{{{", data.homeSearchTop.length)
        if (data.homeSearchTop.length > 10) {
            console.log(criteria, dataToSet)

            await updateData(Model.Users, criteria, dataToSet, { lean: true, new: true })
        }
        return
    }
}

async function imageVisibilityManipulation(data, userData) {
    // console.log(data)
    if (data.imageVisibilityForFollowers && JSON.stringify(data.followers).includes(userData._id)) {
        data.imageUrl = data.imageUrl
    } else {
        if (JSON.stringify(data.imageVisibility).includes(userData._id)) {
            data.imageUrl = data.imageUrl
        } else {
            data.imageUrl = {
                "original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
            }
        }
    }
    delete data.imageVisibility
    delete data.imageVisibilityForFollowers
    return data
}

async function nameVisibilityManipulation(data, userData) {
    if (data.nameVisibilityForFollowers && JSON.stringify(data.followers).includes(userData._id)) {
        data.fullName = data.fullName
        data.userName = data.userName
    } else {
        if (JSON.stringify(data.nameVisibility).includes(userData._id)) {
            data.fullName = data.fullName
            data.userName = data.userName
        } else {
            data.fullName = data.firstName.substr(0, 1) + ".... "
            data.userName = data.userName.substr(0, 3)
        }
    }
    delete data.nameVisibility
    delete data.firstName
    delete data.lastName
    delete data.nameVisibilityForFollowers
    return data
}

let takeSurveyProperties = async (payloadData, userData) => {
    try {
        let criteria = {}

        criteria._id = userData._id;
        criteria.isDeleted = false

        let dataToSet = {};

        dataToSet.isTakeSurvey = true;

        if (payloadData.gender) {
            dataToSet.gender = payloadData.gender;
        }

        if (payloadData.race) {
            dataToSet.race = payloadData.race;
        }

        if (payloadData.dateOfBirth) {
            dataToSet.dateOfBirth = payloadData.dateOfBirth;
        }
        if (payloadData.homeOwnership) {
            dataToSet.homeOwnership = payloadData.homeOwnership;
        }

        if (payloadData.houseHoldIncome) {
            dataToSet.houseHoldIncome = payloadData.houseHoldIncome;
        }
        if (payloadData.education) {
            dataToSet.education = payloadData.education;
        }
        if (payloadData.employementStatus) {
            dataToSet.employementStatus = payloadData.employementStatus;
        }
        if (payloadData.maritalStatus) {
            dataToSet.maritalStatus = payloadData.maritalStatus;
        }
        console.log('criteria', criteria);
        console.log('dataToSet', dataToSet);

        let data = await updateData(Model.Users, criteria, { $set: dataToSet }, { new: true, lean: true })
        return {};
    } catch (e) {
        console.log(e)
    }
}
let getTakeSurveyProperties = async (payloadData, userData) => {
    try {
        console.log('getTakeSurveyProperties');

        return {
            "dateOfBirth": userData.dateOfBirth,
            "gender": [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.GENDER.MALE,
                    "isSelected": (userData.gender == Config.APP_CONSTANTS.DATABASE.GENDER.MALE) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.GENDER.FEMALE,
                    "isSelected": (userData.gender == Config.APP_CONSTANTS.DATABASE.GENDER.FEMALE) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.GENDER.OTHERS,
                    "isSelected": (userData.gender == Config.APP_CONSTANTS.DATABASE.GENDER.OTHERS) ? 1 : 0
                }],
            "race": [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.RACE.AMERICAN_INDIAN,
                    "isSelected": (userData.race == Config.APP_CONSTANTS.DATABASE.RACE.AMERICAN_INDIAN) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.RACE.AFRICAN_AMERICAN,
                    "isSelected": (userData.race == Config.APP_CONSTANTS.DATABASE.RACE.AFRICAN_AMERICAN) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.RACE.ASIAN,
                    "isSelected": (userData.race == Config.APP_CONSTANTS.DATABASE.RACE.ASIAN) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.RACE.NATIVE_HAWAIIAN,
                    "isSelected": (userData.race == Config.APP_CONSTANTS.DATABASE.RACE.NATIVE_HAWAIIAN) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.RACE.WHITE,
                    "isSelected": (userData.race == Config.APP_CONSTANTS.DATABASE.RACE.WHITE) ? 1 : 0
                }
            ],
            "houseHoldIncome": [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_1,
                    "isSelected": (userData.houseHoldIncome == Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_1) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_2,
                    "isSelected": (userData.houseHoldIncome == Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_2) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_3,
                    "isSelected": (userData.houseHoldIncome == Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_3) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_4,
                    "isSelected": (userData.houseHoldIncome == Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_4) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_5,
                    "isSelected": (userData.houseHoldIncome == Config.APP_CONSTANTS.DATABASE.HOUSE_HOLD_INCOME.VALUE_5) ? 1 : 0
                }
            ],
            "homeOwnership": [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_MORTGAGE,
                    "isSelected": (userData.homeOwnership == Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_MORTGAGE) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_OUTRIGHT,
                    "isSelected": (userData.homeOwnership == Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_OUTRIGHT) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.RENTED,
                    "isSelected": (userData.homeOwnership == Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.RENTED) ? 1 : 0
                }],
            education: [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.ELEMENTRY_SCHOOL,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.ELEMENTRY_SCHOOL) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.MIDDLE_SCHOOL,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.MIDDLE_SCHOOL) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.HIGH_SCHOOL,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.HIGH_SCHOOL) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.BACHELORS,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.BACHELORS) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.MASTERS,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.MASTERS) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EDUCATION.DOCTORAL,
                    "isSelected": (userData.education == Config.APP_CONSTANTS.DATABASE.EDUCATION.DOCTORAL) ? 1 : 0
                }],
            employementStatus: [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.EMPLOYED,
                    "isSelected": (userData.employementStatus == Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.EMPLOYED) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.UNEMPLOYED,
                    "isSelected": (userData.employementStatus == Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.UNEMPLOYED) ? 1 : 0
                }],
            maritalStatus: [
                {
                    "key": Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SINGLE,
                    "isSelected": (userData.maritalStatus == Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SINGLE) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.MARRIED,
                    "isSelected": (userData.maritalStatus == Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.MARRIED) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SEPERATED,
                    "isSelected": (userData.maritalStatus == Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SEPERATED) ? 1 : 0
                },
                {
                    "key": Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.WIDOWED,
                    "isSelected": (userData.maritalStatus == Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.WIDOWED) ? 1 : 0
                }]
        };
    } catch (e) {
        console.log(e)
    }
}


async function sendPushIos(userDeviceToken, data) {
    return new Promise((resolve, reject) => {


        //     let result = { title: "Ribbit",
        //     body:  data.callerInfo.fullName + " calling...",
        //     TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALLING,
        //     date:data
        // };

        // let result = {
        //     title: "Ribbit",
        //     body:  data.callerInfo.fullName + " calling...",
        //     name: "sdfsaf",
        //     data:data,
        //     user_id : "data.user_id"
        //     // TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALLING,
        //     //      token: "data.token",
        //     //   name: "data.name" ,
        //     //    user_id : "data.user_id" ,
        //     //     profile_pic : "data.profile_pic",
        // };


        let result = data;

        //  let result = {
        //     session_id: "data.sessionId" ,
        //     token: "data.token",
        //     name: "data.name"
        //     };

        // let result = { title: "process.env.APP_NAME",
        // 	body:  "data.name" + " calling...",
        //     tag: "null", session_id: "data.sessionId" ,
        //      token: "data.token",
        //       name: "data.name" ,
        //        user_id : "data.user_id" ,
        //         profile_pic : "data.profile_pic",
        //         date:"data.date"
        //     };

        result = {
            data: result
        };

        let note = new apn.Notification();
        note.expiry = Math.floor(Date.now() / 1000) + 25; // Expires 25 sec from now.
        note.badge = 1;
        note.sound = "ping.aiff";
        note.payload = result;

        console.log(note);


        if (false) {
            // let certificatePathuserPush = appRoot+"/configs/userVoipCert.pem";

            // const normalPushOptions = {
            //    cert: certificatePathuserPush,
            //    certData: null,
            //    key: certificatePathuserPush,
            //    keyData: null,
            //    passphrase: "123",
            //    sandBox : true,
            //    ca: null,
            //    pfx: null,
            //    pfxData: null,
            //    gateway: Config.APP_CONSTANTS.SERVER.APPLE_GATEWAY,
            //    //port: 2195,
            //    rejectUnauthorized: true,
            //    enhanced: true,
            //    cacheLength: 100,
            //    autoAdjustCache: true,
            //    connectionTimeout: 0,
            //    ssl: true,
            //    production: false
            // };
            // const voidpushUser = new apn.Provider(normalPushOptions);


            // 	voidpushUser.send(note, data.receiver_device_token).then(function(result) {
            // 		return resolve(note);
            // })
            // .catch(function(error) {
            // 	return  reject(error.message);

            // });
        } else {

            let certificatePathForDriverPush = path.join(__dirname, "../", "VoIPRibbitDev.pem");

            const voipPushOptions = {
                cert: certificatePathForDriverPush,
                certData: null,
                key: certificatePathForDriverPush,
                keyData: null,
                passphrase: "",
                sandBox: true,
                ca: null,
                pfx: null,
                pfxData: null,
                gateway: Config.APP_CONSTANTS.SERVER.APPLE_GATEWAY,
                //port: 2195,
                rejectUnauthorized: true,
                enhanced: true,
                cacheLength: 100,
                autoAdjustCache: true,
                connectionTimeout: 0,
                ssl: true,
                production: false
            };

            const voipPushdriver = new apn.Provider(voipPushOptions);
            voipPushdriver.send(note, userDeviceToken).then(function (result) {
                return resolve(note);

            })
                .catch(function (error) {
                    return reject(error.message);
                });
        }
    });

}


async function userContactUs(data, userData) {
    const result = {
        message: data.message,
        user_id: userData._id
    };

    return createData(Model.UserContactUs, result, { new: true, lean: true })
}


// let pushData = {
//     id: "data.callToUserId",
//     byId: "userData._id",
//     session: "openTokSession.session",
//     token: "Tokbox.NN_createTokenFromSession(openTokSession.session)",
//     TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALLING,
//     msg: 'Calling  tressssttt'
// };

// pushNotification.sendPush("fQECt5oxzZQ:APA91bHkLGtow1Q-VYzvtfZzcyrYMCqzwGI0zV8vAiLqMcBoneyOfTTPEWsral9v9BNb-Mmcq4ooTj9yFwQQF_CF9aP7_DNB4oTZ9oMHHtkl30AUAOh99b10goOgzELiGWhsJe29cdiO", pushData, (err, res) => { console.log(err, res) });

// Tokbox.NN_createTokenFromSession("2_MX40NjUyNDI3Mn5-MTU4MzU2NjM5NzA1M35hUGc3bFdLN25GWUYzeGRQY1lQbWkzNmF-UH4");

async function callInitiate(data, userData) {

    const result = {
        callToUserId: data.callToUserId,
        user_id: userData._id
    };

    console.log("userData");
    console.log(userData);

    console.log("userData");
    const openTokSession = await Tokbox.NN_createSession();
    // {session: session.sessionId, token:session.generateToken()}

    //notification
    let toIdData = await getRequired(Model.Users, { _id: data.callToUserId }, {}, { lean: true });

    let pushData = {
        id: data.callToUserId,
        callerInfo: {
            _id: userData._id,
            imageUrl: userData.imageUrl || {},
            fullName: userData.fullName
        },
        receiverId: data.callToUserId,
        // callerId: userData._id,
        // callerName: userData._id,
        // callerImage: userData._id,
        session: openTokSession.session,
        token: Tokbox.NN_createTokenFromSession(openTokSession.session),
        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALLING,
        msg: 'Calling'
    };


    console.log(pushData);
    console.log(toIdData[0].platform);

    if (toIdData[0].platform === Config.APP_CONSTANTS.platform.ANDROID) {
        console.log("Calling to android", toIdData[0].deviceToken)
        pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
            console.log(err, res)
        })
    } else if (toIdData[0].platform === Config.APP_CONSTANTS.platform.IOS) {
        console.log("Calling to ios")
        sendPushIos(toIdData[0].apnsDeviceToken, pushData).then(e => {
            console.log("IOS notification", e)
        }).catch(e => {
            console.log("IOS notification failer", e)
        })
    }

    return { ...openTokSession };
    // return createData(Model.UserContactUs, result, { new: true, lean: true })
}

async function callDisconnect(data, userData) {

    const result = {
        callerId: data.callerId,
        receiverId: data.receiverId,
        user_id: userData._id
    };

    let toIdData;

    if (result.user_id.toString() === result.callerId.toString()) {
        // send notification to receiver
        toIdData = await getRequired(Model.Users, { _id: result.receiverId }, {}, { lean: true });
    } else {
        // send notification to caller
        //notification
        toIdData = await getRequired(Model.Users, { _id: result.callerId }, {}, { lean: true });
    }

    let pushData = {
        id: result.user_id,
        callerId: result.callerId,
        receiverId: result.receiverId,
        TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALL_DISCONNECT,
        msg: 'Call_Disconnect'
    };

    pushNotification.sendPush(toIdData[0].deviceToken, pushData, (err, res) => {
        console.log(err, res)
    })

    return result;
}


async function startChallenge(request, userData) {
    try {


        const challengeData = await Model.Challenges.findById(request.params.id);
        const isExist = await Model.UserChallenges.countDocuments({
            "user_id": userData._id,
            "status": Config.APP_CONSTANTS.userChallengeStatus.INPROGESS
        });

        if (isExist) {
            return Promise.reject("One Challenge is already in progress.");
        }

        const dateFormat = (dateConversion, returnDateObj = true, format = "YYYY-MM-DD") => {

            if (returnDateObj) {
                return new Date(moment(dateConversion).format(format));
            }
            return moment(dateConversion).format(format);
        };

        // const currenDate =new Date(moment().utc().format("YYYY-MM-DDT00:00:00Z"));
        const currenDate = dateFormat(moment().utc().format("YYYY-MM-DDT00:00:00Z"));
        const startDate = dateFormat(challengeData.startDate);
        const endDate = dateFormat(challengeData.endDate);


        console.log(currenDate);
        console.log(startDate);
        console.log(endDate);
        // return Promise.reject("One Challenge is already in progress.");


        if ((startDate >= currenDate) && (currenDate <= endDate)) {

            const reponse = {
                user_id: userData._id,
                createdBy: userData._id,
                challenge_id: request.params.id
            };

            // { challengeType: 'photo',
            // quantity: 1,
            // description: 'sdfasfd',
            // rewardPoint: 12,
            // startDate: '2018-02-10',
            // endDate: '2018-02-13' },

            let groups = await Model.UserChallenges.create(reponse);
            return groups;
        }
        else if (currenDate < startDate) {
            return Promise.reject(`Challenge can not start Before ${dateFormat(startDate, false)}`);
        } else {
            return Promise.reject(`Challenge expired`);
        }
    } catch (e) {
        console.log(e);
    }
}

// console.log(
//     startChallenge({params:{id:"5e6b615b5516782abcf91f53"}},{_id:"5d95e47e13ba822ddf6fbfed"})
//     .catch(e=>console.log("aaa",e) )

//     );


// async function callConnect(data, userData) {

//     const result = {
//         callFromSession: data.callFromSession,
//         callFromUserId: data.callFromUserId,
//         user_id: userData._id
//     };

//     const openTokSession = await Tokbox.NN_createSession();
//     // {session: session.sessionId, token:session.generateToken()}

//     return {result, openTokSession};

//     // return createData(Model.UserContactUs, result, { new: true, lean: true })
// };

async function stats(userData) {
    try {
        let reponse = await Model.Users.findById(userData._id, {
            pointEarned: 1,
            pointRedeemed: 1,
            totalSurveyGiven: 1
        }).populate('totalSurveys');

        console.log("reponse--", reponse);

        reponse.set('totalSurveyGiven', reponse.totalSurveys, { strict: false });

        return reponse;

    } catch (e) {
        console.log(e);
        throw e;
    }
}


// (async function(){
//    console.log(await  sendPushIos("BFDAB1EAF95F814A2EB468538358C431CADB2875501912B2548182E0E0A3A1B9",
//    {
//     id: "data.callToUserId",
//     callerInfo: {
//         _id:"userData._id",
//         imageUrl: {},
//         fullName:"userData.fullName"
//                 },
//                 receiverId:  "data.callToUserId",
//     // callerId: userData._id,
//     // callerName: userData._id,
//     // callerImage: userData._id,
//     session: "openTokSession.session",
//     token: "Tokbox.NN_createTokenFromSession(openTokSession.session)",
//     TYPE: "Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.CALLING",
//     msg: 'Calling'
// }));
// })()

let tangoGetCatalog = async () => {
    try {

    //     let identifier = Config.tangoConfig.tangoCredentials.accountIdentifier
    //    // /accounts/{identifier}
    //    let options1 = {
    //     hostname: Config.tangoConfig.tangoCredentials.hostname,
    //     path: `/raas/v2/accounts/{identifier}`,
    //     method: "GET"
    //    };

    //    let authToken = await getTangoResponse(options1);


     //  console.log(authToken)

     let url =  Config.tangoConfig.tangoCredentials.hostname +"catalogs?verbose=true";

     console.log(url)

     let options = {
        url: url,
        method: 'GET',
        'auth': {
            'user': Config.tangoConfig.tangoCredentials.platformName,
            'pass': Config.tangoConfig.tangoCredentials.apiKey
          },
        json: true
    };

        // let options = {
        //     hostname: Config.tangoConfig.tangoCredentials.hostname,
        //     path: "/raas/v2/catalogs?verbose=true",
        //     method: "GET",
        //     headers: {
        //         "Content-Type": "application/json",
        //         "Authorization": "Basic " + Config.tangoConfig.tangoCredentials.authToken
        //     }
        // };
        // console.log(options);
        // let tangoResponse = await getTangoResponse(options);
        let tangoResponse = await requestApi(options);
        if (tangoResponse) {
           // console.log(tangoResponse.catalogName);
            return (tangoResponse);
        }
    } catch (e) {
        console.log("Error in userController->tangoGetCatalog" + JSON.stringify(e));
    }
};




// let tangoCreateCustomer = async (payload, id) => {
//     try {

//         let url = `https://integration-api.tangocard.com/raas/v2/customers`;
//         // let token = new Buffer(Config.tangoConfig.platformName + ':' + Config.tangoConfig.apiKey).toString('base64')       
//         let obj = {
//             customerIdentifier: id,
//             displayName: payload.userName
//         }

//         let options = {
//             url: url,
//             method: "POST",
//             headers: {
//                 "Content-Type": "application/json",
//                 "Authorization": "Basic " + Config.tangoConfig.tangoCredentials.authToken
//             },
//             body: obj,
//             json: true
//         };
//         console.log(options);
//         let tangoResponse = await requestApi(options);
//         if (tangoResponse) {
//             console.log("TangoResponse-----------------",tangoResponse);
//             return (tangoResponse);
//         }
//     }catch(e) {
//         console.log("Error in userController->tangoCreateCustomer" + JSON.stringify(e));
//     }
// };


let tangoPostOrders = async (userData, payload) => {
    try {
        let criteria = {
            _id: userData._id
        }
        let projection = {
            _id: 1,
            firstName: 1,
            lastName: 1,
            email: 1,
            pointEarned: 1,
            pointRedeemed: 1,
            deviceToken: 1
        }

        let options1 = {
            lean: true
        }
        let user = await Model.Users.findOne(criteria, projection, options1);

        //console.log("User--------------", user);


        user.amount = user.pointEarned * Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD;

        if (user.amount < payload.faceValue) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INSUFFICIENT_AMOUNT);
        }
        let url = `https://api.tangocard.com/raas/v2/orders`;

        let obj = {
            accountIdentifier: Config.tangoConfig.tangoCredentials.accountIdentifier,
            amount: payload.faceValue,
            customerIdentifier: Config.tangoConfig.tangoCredentials.accountIdentifier,
            recipient: {
                email: user.email,
                firstName: user.firstName,
                lastName: user.lastName
            },
            sendEmail: true,
            utid: payload.utid
        }
        let options = {
            url: url,
            method: 'POST',
            'auth': {
                'user': Config.tangoConfig.tangoCredentials.platformName,
                'pass': Config.tangoConfig.tangoCredentials.apiKey
              },
            body: obj,
            json: true
        };
        let result = await requestApi(options);
        if (result) {
            let setData = {}
            console.log(JSON.stringify(result));

            setData.referenceOrderID = result.referenceOrderID;
            setData.userId = userData._id;
            setData.rewardName = result.rewardName;
            setData.amount = result.amountCharged.total;
            setData.image = payload.image;
            setData.claimCode = result.reward.credentialList[0].value;


            let savedCard = await Service.saveData(Model.GiftOrder, setData);

            let saveData = {
                redeemType: Config.APP_CONSTANTS.DATABASE.REDEEM_POINT_TYPE.GIFTCARD,
                name: savedCard.rewardName,
                point: Math.round(payload.faceValue * Config.APP_CONSTANTS.DATABASE.POINT_PER_DOLLAR.POINT),
                userId: userData._id
            }

            await Service.saveData(Model.RedeemHistory, saveData)

            console.log(savedCard);

            if (result.status === 'COMPLETE') {

                let pointEarned = (user.amount - payload.faceValue) / Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD;
                let pointRedeemed = (user.pointEarned - pointEarned) + user.pointRedeemed;

                // console.log(typeof dataToSet.pointRedeemed)

                //update Earned Points and Redeem Points of User    
                await Model.Users.updateOne({ _id: userData._id }, { $set: { pointEarned: parseInt(pointEarned), pointRedeemed: parseInt(pointRedeemed) } });

                let pushData = {
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.SPEND_EARNED_POINT,
                    id: userData._id,
                    msg: `${userData.fullName} Congratulations you has Redeem ${payload.faceValue * Config.APP_CONSTANTS.DATABASE.POINT_PER_DOLLAR.POINT} pts to Purchase Gift Card`
                };

                await createData(Model.Notifications, {
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.SPEND_EARNED_POINT,
                    //conversationId: venueData.conversationId,
                    toId: userData._id,
                    text: `${userData.fullName} Congratulations you has Redeem ${payload.faceValue * Config.APP_CONSTANTS.DATABASE.POINT_PER_DOLLAR.POINT} pts to Purchase Gift Card`,
                    createdOn: +new Date,
                    //venueId: payloadData.groupId
                });

                pushNotification.sendPush(user.deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                });
            }

            console.log(result);
            return (result);
        }
    } catch (e) {
        throw e;
    }
};


function requestApi(options) {
    console.log(options)
    return new Promise(resolve => {
        Request(options,
            function (error, response, body) {
                //console.log("==========body==========", body);
                resolve(body);
            });
    });
};

let getTangoResponse = (options) => {
    let data = "";
    return new Promise(async (resolve, reject) => {
        await https.request(options, res => {
            console.log("Response Status Code:" + res.statusCode);
            //console.log("Inside Response:"+JSON.stringify(res));  
            res.on("data", d => {
                data += d;
            });
            res.on("end", () => {
                //console.log("In request end:" + JSON.stringify(data));
                resolve(JSON.parse(data));
            });
        }).on("error", err => {
            console.log("In error:" + JSON.stringify(err));
            reject(err);
        }).end();
    });
};


// let getGiftCardOrderData = async (request, userData) => {
//     try {
//         let criteria = {
//             userId: userData._id
//         }

//         let Data = await Service.getData(Model.GiftOrder, criteria)

//         if (Data == null) {
//             return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
//         } else {

//             return Data;
//         }

//     } catch (e) {
//         console.log(e);
//         throw e;
//     }
// }


let showSpinWheel = async (request, userData) => {
    try {
        let criteria = {
            _id: userData._id
        }
        let projection = {
            _id: 1,
            getSpinWheel: 1,
            spinWheelTime: 1
        }

        var user = await Service.findOne(Model.Users, criteria, projection, { lean: true });

        if (user.spinWheelTime == undefined) {
            console.log("--------");

            let projection = {
                prize: 1,
                createdAt: 1
            };

            let options = {
                lean: true,
                sort: { createdAt: -1 }
            }

            let SpinWheel = await Service.getData(Model.SpinWheel, { isDeleted: false, isActive: true }, projection, options);

            console.log(SpinWheel[0])

            return SpinWheel[0];

        }

        let currenDate = moment();
        console.log(currenDate);
        let spin = parseInt(user.spinWheelTime);
        console.log(user.spinWheelTime);
        console.log(spin);
        let duration = currenDate.diff(spin, 'hours');

        console.log("DURATION____________________", duration);

        if (duration >= Config.APP_CONSTANTS.DATABASE.SPIN_WHEEL_DURATION.HOURS || user.spinWheelTime == "") {

            console.log("--------");

            let projection = {
                prize: 1,
                createdAt: 1
            };

            let options = {
                lean: true,
                sort: { createdAt: -1 }
            }

            let SpinWheel = await Service.getData(Model.SpinWheel, { isDeleted: false, isActive: true }, projection, options);

            return SpinWheel[0];
        }
        else {
            console.log("spinwheel")
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.ALREADY_SPIN_WHEEL);
        }
    } catch (e) {
        console.log(e);
        throw e;
    }
};


let addSpinWheelPrize = async (payload, userData) => {
    try {

        let criteria = {
            _id: userData._id,
            isDeleted: false
        }

        let setData = {
            $inc: { pointEarned: payload.value },
            $set: {
                spinWheelTime: moment()
            }
        }

        let saveData = {
            userId: userData._id,
            pointEarned: payload.value,
            source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.SPIN_WHEEL,
            date: moment().format('MM/DD/YYYY')
        }

        var result = await Service.update(Model.Users, criteria, setData);

        var res = await Service.saveData(Model.PointEarnedHistory, saveData);

        console.log("POINT_EARNED HISTORY", res);

        let pushData = {
            TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
            id: userData._id,
            msg: `${userData.fullName} Congratulations you has won ${payload.value} pts through Spin Wheel`
        };

        await createData(Model.Notifications, {
            type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
            //conversationId: venueData.conversationId,
            toId: userData._id,
            text: `${userData.fullName} Congratulations you has won ${payload.value} pts through Spin Wheel`,
            createdOn: +new Date,
            //venueId: payloadData.groupId
        })

        pushNotification.sendPush(userData.deviceToken, pushData, (err, res) => {
            console.log(err, res)
        })

        let formattedResult = `congratulations you have won ${payload.value} pts`;

        return formattedResult;

    } catch (e) {
        console.log(e);
    }
};

/* Add Time Spend On twitter
   INPUT: UserId,Twitter Time
   OUTPUT:Result  */

let addTwitterTimming = async (request, userData) => {
    try {

        let criteria = {
            userId: userData._id,
        }

        let finalResponse;

        var user = await Service.findOne(Model.twitterTimming, criteria, {}, { lean: true });

        if (!user) {
            //console.log("first");
            let setData = {};

            setData.timeCount = Config.APP_CONSTANTS.DATABASE.TWITTER_TIME_COUNT.TIME
            setData.userId = userData._id

            await Service.saveData(Model.twitterTimming, setData, { returnNewDocument: true })
        }
        else {
            let minuteTime = parseInt(Math.floor(user.timeCount / 60));
            if (minuteTime === Config.APP_CONSTANTS.DATABASE.TWITTER_FIX_TIME.TIME) {
                //console.log("fourth")
                criteria = {
                    _id: userData._id,
                    isDeleted: false
                };

                let setData = {
                    $inc: { pointEarned: Config.APP_CONSTANTS.DATABASE.POINT_PER_MINUTE.POINT },
                };

                let saveData = {
                    userId: userData._id,
                    pointEarned: Config.APP_CONSTANTS.DATABASE.POINT_PER_MINUTE.POINT,
                    source: Config.APP_CONSTANTS.DATABASE.POINT_EARNED_SOURCE.TWITTER,
                    date: moment().format('MM/DD/YYYY')
                }


                let data = await Service.findAndUpdate(Model.Users, criteria, setData, { new: true });
                var res = await Service.saveData(Model.PointEarnedHistory, saveData);

                let pushData = {
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
                    id: userData._id,
                    msg: `${userData.fullName} Congratulations you has won ${Config.APP_CONSTANTS.DATABASE.POINT_PER_MINUTE.POINT} pts thrpugh Twitter`
                };

                await createData(Model.Notifications, {
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.RECEIVED_REDEEM_POINTS,
                    //conversationId: venueData.conversationId,
                    toId: userData._id,
                    text: `${userData.fullName} Congratulations you has won ${Config.APP_CONSTANTS.DATABASE.POINT_PER_MINUTE.POINT} pts thrpugh Twitter`,
                    createdOn: +new Date,
                    //venueId: payloadData.groupId
                })

                pushNotification.sendPush(userData.deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })



                if (data) {
                    //console.log("setData")
                    criteria = {
                        userId: userData._id
                    };

                    let setData = {
                        $inc: {
                            totalEarnedPoints: Config.APP_CONSTANTS.DATABASE.POINT_PER_MINUTE.POINT,
                            totalTimeSpend: Config.APP_CONSTANTS.DATABASE.TWITTER_FIX_TIME.TIME
                        },
                        $set: {
                            timeCount: 0
                        }
                    };

                    let result2 = await Service.findAndUpdate(Model.twitterTimming, criteria, setData);

                    return result2;
                }
            }
            else {

                //console.log("thrd")

                let criteria = {
                    userId: userData._id
                };

                let setData = {
                    $inc: {
                        timeCount: Config.APP_CONSTANTS.DATABASE.TWITTER_TIME_COUNT.TIME
                    }
                }

                var updateTime = await Service.update(Model.twitterTimming, criteria, setData);

                return updateTime;
            }
        }
    } catch (e) {

        console.log(e);
    }
};


let showCharityOrgList = async (request, userData) => {
    try {
        let projection = {
            organizationName: 1
        };

        let options = {
            lean: true,
            sort: { createdAt: 1 },

        }

        let CharityOrgList = await Service.getData(Model.CharityOrgList, {}, projection, options);

        return CharityOrgList;

    } catch (e) {
        console.log(e);
        throw e;
    }
};

let addCharityDonation = async (payloadData, userData) => {
    try {
        let criteria = {
            _id: userData._id
        }

        let projection = {
            _id: 1,
            pointEarned: 1,
            pointRedeemed: 1
        }

        //console.log(">>>>>>>>>>>>>>>>>>>payloadData>>>>>", payloadData);

        let pointPerDollar = 1 / Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD;

        //console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>pointPerDollar", pointPerDollar)

        let givenPoints = payloadData.dollars * pointPerDollar;

        //console.log(">>>>>>>>>>>>>>>>givenPoints>>>>>>>>>>>>>>>>>", givenPoints)

        var user = await Model.Users.findOne(criteria, projection, { lean: true }); // Find user for check there points

        //console.log("user>>>>>>>>>>>>>>>>", user)

        if (user.pointEarned < givenPoints) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INSUFFICIENT_POINT);
        }
        else {
            let saveData = {
                organizationId: payloadData.organizationId,
                userId: userData._id,
                givenPoint: Math.round(givenPoints)
            }

            let amount = givenPoints * Config.APP_CONSTANTS.DATABASE.DOLLAR_PER_POINT.USD;


            saveData.amount = Math.round(amount)

            console.log("saveData>>>>>>>>>>>>>>>>>>>", saveData)

            let result = await Service.saveData(Model.CharityDonationList, saveData);   // If user point is emough then save to charity

            console.log(">>>>>>>>>>>>>>>>>result", result)


            if (result._id) {
                let criteria = {
                    _id: userData._id
                }

                let setData = {
                    pointEarned: user.pointEarned - result.givenPoint,
                    pointRedeemed: result.givenPoint + user.pointRedeemed
                }

                console.log(">>>>>>>>>>>>>>>>>>>>>>>>>setData>>>>>", setData);

                await Service.findAndUpdate(Model.Users, criteria, setData)  // update user poits after the donation


                let pushData = {
                    TYPE: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.SPEND_EARNED_POINT,
                    id: userData._id,
                    msg: `${userData.fullName} Redeem ${result.givenPoint} pts on Charity`
                };

                await createData(Model.Notifications, {
                    type: Config.APP_CONSTANTS.DATABASE.NOTIFICATION_TYPE.SPEND_EARNED_POINT,
                    //conversationId: venueData.conversationId,
                    toId: userData._id,
                    text: `${userData.fullName} Redeem ${result.givenPoint} pts on Charity`,
                    createdOn: +new Date,
                    //venueId: payloadData.groupId
                })

                pushNotification.sendPush(userData.deviceToken, pushData, (err, res) => {
                    console.log(err, res)
                })

                criteria = {
                    _id: result.organizationId,
                }

                let result2 = await Service.findOne(Model.CharityOrgList, criteria)  //Find organization Details for save redeem history

                saveData = {
                    redeemType: Config.APP_CONSTANTS.DATABASE.REDEEM_POINT_TYPE.DONATION,
                    name: result2.organizationName,
                    point: Math.round(givenPoints),
                    userId: userData._id
                }

                let saved = await Service.saveData(Model.RedeemHistory, saveData);

                console.log(">........>", saved);

            }
            return {};
        }

    } catch (e) {
        console.log(e);
        throw e;

    }
};


let tangoGetOrders = async (userData) => {
    try {

        let criteria = {
            userId: userData._id,
            isDeleted: false
        }

        let options = {
            sort: { createdAt: 1 }
        }


        let response = await Service.getData(Model.GiftOrder, criteria, {}, options);

        console.log("RESPONSE______________", response);

        if (response.length < 0) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NO_GIFT_CARD_FOUND);
        }

        return response;

    } catch (e) {
        console.log("Error in userController->tangoGetOrders" + JSON.stringify(e));
    }
};

let showRedeemHistory = async (userData) => {
    try {
        let criteria = {
            userId: userData._id
        }

        let options = {
            sort: { createdAt: -1 }
        }

        let projection = {
            _id: 1,
            name: 1,
            point: 1,
            redeemType: 1
        }

        let response = await Service.getData(Model.RedeemHistory, criteria, projection, options);

        console.log("RESPONSE______________", response);

        if (response.length < 0) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
        }

        return response;

    } catch (e) {
        console.log(e);
    }
};


let getPointEarnedHistory = async (userData) => {

    try {

        let formattedResult = {
            'points_by_dates': [],
            'points_earned': [],
            'points_redeem': [],
            'messagesCount': [],
            'postCount_byDate': [],
            'commentCount_byDate': [],
            'storiesCount_byDate': [],
            'twitter_time_spend': Number
        };

        let match = {
            $match: {
                userId: userData._id
            }
        };


        let group = {
            $group: {
                _id: "$date",
                totalPointEarned: { $sum: "$pointEarned" }
            }
        };

        let project = {
            $project: {
                _id: 0,
                date: "$_id",
                totalPointEarned: "$totalPointEarned"
            }
        };

        let sort = {
            $sort: {
                date: -1
            }
        };
        //console.log("--------------------");
        let res1 = await aggregateData(Model.PointEarnedHistory, [match, group, project, sort]);

        console.log(">>>>>>>>>>>>>>>>>>RESPONSE1", res1)

        formattedResult.points_by_dates = res1;
        //console.log("-----------------------");

        // = await Service.getData(Model.PointEarnedHistory, match.$match, {}, {});


        //console.log("0RESPONSE", res3);



        let match1 = {
            $match: {
                userId: userData._id
            }
        };

        let group1 = {
            $group: {
                _id: "$source",
                totalPointEarned: { $sum: "$pointEarned" }
            }
        };

        let project1 = {
            $project: {
                _id: 0,
                source: "$_id",
                totalPointEarned: "$totalPointEarned"
            }
        }

        let res2 = await aggregateData(Model.PointEarnedHistory, [match1, group1, project1]);

        console.log(">>>>>>>>>>>>>>>>>>RESPONSE2", res2)

        formattedResult.points_earned = res2;


        //formattedResult.totalPointEarned = res2;

        let group2 = {
            $group: {
                _id: "$redeemType",
                totalPointRedeem: { $sum: "$point" }
            }
        };

        let project2 = {
            $project: {
                _id: 0,
                source: "$_id",
                totalPointRedeem: "$totalPointRedeem"
            }
        }

        let res3 = await aggregateData(Model.RedeemHistory, [match, group2, project2]);

        console.log(">>>>>>>>>>>>>>>>>>RESPONSE3", res3)

        formattedResult.points_redeem = res3;

        let pipeline = [
            {
                $match: {
                    $or: [{ senderId: mongoose.Types.ObjectId(userData._id) },
                    { receiverId: mongoose.Types.ObjectId(userData._id) }]
                }
            },
            {
                $addFields: {
                    date: { $toDate: "$createdDate" },
                    sent: {
                        "$cond": {
                            if: { $eq: ["$senderId", mongoose.Types.ObjectId(userData._id)] },
                            then: 1,
                            else: 0
                        }
                    },
                    recieve: {
                        "$cond": {
                            if: { $eq: ["$receiverId", mongoose.Types.ObjectId(userData._id)] },
                            then: 1,
                            else: 0
                        }
                    }
                }
            },
            {
                $project: {
                    sent: "$sent",
                    recieve: "$recieve",
                    date: {
                        $dateToString: { format: "%m/%d/%Y", date: "$date" }
                    }
                }
            },
            {
                $group: {
                    _id: "$date",
                    sentMessages: { $sum: "$sent" },
                    recieveMessages: { $sum: "$recieve" }
                }
            },
            {
                $project: {
                    _id: 0,
                    date: "$_id",
                    sentMessages: "$sentMessages",
                    recieveMessages: "$recieveMessages"
                }
            },
            {
                $sort: {
                    date: -1
                }
            }
        ];

        let messages = await aggregateData(Model.Chats, pipeline);

        formattedResult.messagesCount = messages;



        let pipeLine6 = [
            {
                $match: {
                    postBy: userData._id
                }
            },
            {
                $addFields: {
                    date: {
                        $dateToString: { format: "%m/%d/%Y", date: "$createdAt" }
                    }
                }
            },
            {
                $group: {
                    _id: "$date",
                    count: { $sum: 1 }
                }
            },
            {
                $project: {
                    date: "$_id",
                    count: "$count"
                }
            }
        ];

        let result2 = await aggregateData(Model.Posts, pipeLine6);

        console.log(result2)

        formattedResult.postCount_byDate = result2;

        let pipeLine7 = [
            {
                $match: {
                    commentBy: userData._id
                }
            },
            {
                $addFields: {
                    date: {
                        $dateToString: { format: "%m/%d/%Y", date: "$createdAt" }
                    }
                }
            },
            {
                $group: {
                    _id: "$date",
                    count: { $sum: 1 }
                }
            },
            {
                $project: {
                    date: "$_id",
                    count: "$count"
                }
            }
        ];

        let result3 = await aggregateData(Model.Comments, pipeLine7);

        console.log(result3)

        formattedResult.commentCount_byDate = result3;

        let pipeLine8 = [
            {
                $match: {
                    postBy: userData._id
                }
            },
            {
                $addFields: {
                    date: {
                        $dateToString: { format: "%m/%d/%Y", date: "$createdAt" }
                    }
                }
            },
            {
                $group: {
                    _id: "$date",
                    count: { $sum: 1 }
                }
            },
            {
                $project: {
                    _id: 0,
                    date: "$_id",
                    count: "$count"
                }
            }
        ];

        let result4 = await aggregateData(Model.Stories, pipeLine8);

        console.log(result4)

        formattedResult.storiesCount_byDate = result4;


        let twitter = await Service.findOne(Model.twitterTimming, { userId: userData._id }, { totalTimeSpend: 1 }, {});

        if (twitter) {
            formattedResult.twitter_time_spend = twitter.totalTimeSpend;
        }
        else {
            formattedResult.twitter_time_spend = 0;
        }

        console.log(formattedResult);

        return formattedResult;
    }
    catch (e) {
        return e;
        console.log(e);
    }

};



let addStory = async (payload, userData) => {

    try {
        let dataToSave = {
            postBy: userData._id,
            expirationTime: moment().add(24, 'hour').valueOf(),
            createdOn: moment().valueOf()
        }

        dataToSave.media = {}


        if (payload.media) {
            let media = payload.media;
            for (let i = 0; i < media.length; i++) {

                dataToSave.media.original = media[i].original;
                dataToSave.media.thumbnail = media[i].thumbnail;
                dataToSave.media.videoUrl = media[i].videoUrl;
                dataToSave.media.mediaType = media[i].mediaType;

                await Service.saveData(Model.Stories, dataToSave);

            }
        }

        return 'sucess';

    } catch (error) {
        console.log(error);

    }

}

let getStories = async (userData) => {
    try {

        //let users = await Service.getData(Model.Users, { _id: userData._id }, { _id: 0, followers: 1 }, { lean: true });
        let pipeline = [
            {
                $match: {
                    _id: userData._id
                },
            },
            {
                $project: {
                    _id: 0,
                    followers: "$following"
                }
            }
        ];

        let users2 = await aggregateData(Model.Users, pipeline);

        users2[0].followers.push(userData._id);

        let user = users2[0].followers;

        user.unshift(userData._id);

        //console.log(user);
        let pipeLine = [
            {
                $match: {
                    postBy: {
                        $in: user
                    },
                    expirationTime: { $gte: moment().valueOf() },
                    isDeleted: false
                }
            },
            {
                $group: {
                    _id: null,
                    userIds: { $addToSet: "$postBy" }
                }
            },
            {
                $project: {
                    _id: 0,
                    userIds: "$userIds"
                }
            }
        ];

        let users = await aggregateData(Model.Stories, pipeLine);

        let userArr;

        if (users.length > 0) {
            userArr = users[0].userIds
            console.log(userArr)
        } else {
            userArr = [];
        }


        let pipeLine2 = [
            {
                $match: {
                    _id: {
                        $in: userArr
                    }
                }
            },
            {
                $addFields: {
                    sortKey: { $cond: { if: { $eq: ["$_id", userData._id] }, then: 2, else: 1 } }
                }
            },
            {
                $lookup:
                {
                    from: "stories",
                    let: { postBy: "$_id" },
                    pipeline: [
                        {
                            $match: {
                                $expr: {
                                    $and: [
                                        { $eq: ["$postBy", "$$postBy"] },
                                        { $gte: ["$expirationTime", +new Date()] },
                                        { $eq: ["$isDeleted", false] }
                                    ]
                                }
                            }
                        },
                        {
                            $project: {
                                postBy: "$postBy",
                                expirationTime: "$expirationTime",
                                isDeleted: "$isDeleted",
                                createdOn: "$createdOn",
                                media: "$media",
                                viewBy: "$viewBy"
                                // isSeen: {
                                //     $size: "$views"
                                // }
                            }
                        }
                    ],
                    as: "stories"
                }
            },
            {
                $sort: {
                    sortKey: -1
                }
            },
            {
                $project: {
                    _id: "$_id",
                    firstName: "$firstName",
                    lastName: "$lastName",
                    imageUrl: "$imageUrl",
                    stories: "$stories"
                }
            }
        ];

        // console.log(JSON.stringify(pipeLine2));

        let stories = await aggregateData(Model.Users, pipeLine2);

        //console.log(userData._id)

        if (stories.length > 0) {
            for (let k = 0; k < stories.length; k++) {
                for (let i = 0; i < stories[k].stories.length; i++) {
                    if (stories[k].stories[i].viewBy.length > 0) {
                        for (let j = 0; j < stories[k].stories[i].viewBy.length; j++) {
                            // console.log(stories[k].stories[i].viewBy[j].toString() === userData._id.toString())
                            if (stories[k].stories[i].viewBy[j] != null) {
                                if (stories[k].stories[i].viewBy[j].toString() === userData._id.toString()) {
                                    stories[k].stories[i].isSeen = 1;
                                    break;
                                } else {
                                    stories[k].stories[i].isSeen = 0;
                                }
                            }

                        }

                    } else {
                        stories[k].stories[i].isSeen = 0;
                    }
                }
            }

        }
        //console.log(JSON.stringify(stories));



        return stories;

    } catch (err) {
        console.log(err);
    }
}


let deleteStory = async (query, userData) => {
    try {

        let criteria = {
            postBy: userData._id,
            _id: mongoose.Types.ObjectId(query.id)
        }

        let res = await Service.update(Model.Stories, criteria, { $set: { isDeleted: true } });

        return res;
    } catch (err) {
        console.log(err);
    }
}

let promoteUser = async (payload , userData) => {
    try {
        let criteria = {
            _id: userData._id
        }

        let linkData = await Service.findOne(Models.Admins, { email:"admin@gmail.com" },{ androidAppLink:1, iosAppLink: 1},{ lean:true });
         
        console.log(linkData)

        let user = await Service.findOne(Model.Users, criteria, { reffrealCode : 1 } ,{ lean : true});

        if(!user) {
            return Promise.reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.NOT_FOUND);
        }

        let message = `Download our Application Through this link for IOS:${linkData.iosAppLink} and for Android:${linkData.androidAppLink} and use Reffreal Code on SignUp Process ${user.reffrealCode}`;


        let userPhoneNumbers = payload.phoneNumbers;

        console.log(user)


        for (let i = 0; i < userPhoneNumbers.length; i++) {
            let smsData = {
                to: userPhoneNumbers[i],
                from: "+15045094820",
                body: message
            }
            await pushNotification.sendSMS(smsData);
        }

       let NewUser =  await Service.update(Model.Users, criteria, {$set : { isPromoted:true }},{ lean: true, new: true });

        return NewUser;
    } catch (err) {
        console.log(err);
    }
}



let getNewMessageCount = async (userData) => {
    try {
        let pipeline = [{
            $match: {
                $or: [{
                    senderId: mongoose.Types.ObjectId(userData._id)
                }, {
                    receiverId: mongoose.Types.ObjectId(userData._id)
                }],
                chatType: "INDIVIDUAL",
                noChat: false,
                isDeleted: false
            }
        }, {
            $group: {
                _id: "$conversationId",
                chatDetails: { $last: "$chatDetails" },
                createdDate: { $last: "$createdDate" },
                senderId: { $last: { $cond: [{ $ne: ["$senderId", mongoose.Types.ObjectId(userData._id)] }, "$senderId", "$receiverId"] } },
                unreadCount: {
                    $sum: {
                        $cond: [
                            {
                                $and:
                                    [
                                        // {$eq: ['$receiverId', mongoose.Types.ObjectId(userData._id)]},
                                        { $in: [mongoose.Types.ObjectId(userData._id), "$readBy"] }
                                    ]
                            }, 0, 1]
                    }
                }
            }
        }, {
            $project: {
                unreadCount: "$unreadCount"
            }
        }, {
            $group: {
                _id: null,
                totalCount: {
                    $sum: "$unreadCount"
                }
            }
        },
        {
            $project: {
                _id: 0,
                totalCount: "$totalCount"
            }
        }
        ];

        console.log(JSON.stringify(pipeline))

        let res = await aggregateData(Model.Chats, pipeline);

        console.log(res);

        let pipeline2 = [{
            $lookup: {
                "from": "chats",
                "localField": "groupId",
                "foreignField": "groupId",
                "as": "chatInfo"
            }
        }, {
            $lookup: {
                "from": "postgroups",
                "localField": "groupId",
                "foreignField": "_id",
                "as": "groupInfo"
            }
        }, {
            $match: {
                userId: mongoose.Types.ObjectId(userData._id),
                "groupInfo.isArchive": { $nin: [userData._id] },
                "groupInfo.adminId": { $nin: userData.blockedBy },
            }
        }, {
            $unwind: {
                "path": "$chatInfo",
                "preserveNullAndEmptyArrays": true,
            }
        }, {
            $match: {
                "chatInfo.isDeleted": false,
            }
        }, {
            $unwind: {
                "path": "$groupInfo",
                "preserveNullAndEmptyArrays": true,
            }
        }, {
            $project: {
                _id: 1,
                groupId: 1,
                groupInfo: 1,
                createdOn: {
                    $cond: {
                        if: { $gte: ["$groupInfo.createdOn", "$chatInfo.createdDate"] },
                        then: "$groupInfo.createdOn",
                        else: "$chatInfo.createdDate"
                    }
                },
                chatInfoReadBy: { $ifNull: ["$chatInfo.readBy", []] },
                chatInfo: { $ifNull: ["$chatInfo", null] }
            }
        }, {
            $group: {
                _id: "$groupId",
                //groupId: { $last: "$groupId" },
                // imageUrl: {$last: "$groupInfo.imageUrl"},
                // groupName: {$last: "$groupInfo.name"},
                // adminId: {$last: "$groupInfo.adminId"},
                //createdDate: { $last: "$createdOn" },
                // conversationId: { $last: "$groupInfo.conversationId" },
                //lastChatDetails: { $last: "$chatInfo.chatDetails" },
                "unreadCount": {
                    $sum: {
                        $cond: [
                            {
                                $or: [
                                    { $eq: [true, "$groupInfo.noChat"] },
                                    {
                                        $and: [
                                            { $eq: [false, "$groupInfo.noChat"] },
                                            { $in: [mongoose.Types.ObjectId(userData._id), "$chatInfo.readBy"] }
                                        ]
                                    }
                                ]
                            }, 0, 1]
                    }
                }
            }
        },
        {
            $project: {
                _id: 0,
                unreadCount: "$unreadCount"
            }
        }, {
            $group: {
                _id: null,
                totalCount: {
                    $sum: "$unreadCount"
                }
            }
        },
        {
            $project: {
                _id: 0,
                totalCount: "$totalCount"
            }
        }]

        let res2 = await aggregateData(Model.PostGroupMembers, pipeline2);

        console.log(res2)

        let totalUnreadMessages = 0;
        if (res.length) {
            // console.log(totalUnreadMessages = totalUnreadMessages + res[0].totalCount)
            totalUnreadMessages = totalUnreadMessages + res[0].totalCount;
        }
        if (res2.length) {
            console.log(2)
            totalUnreadMessages = totalUnreadMessages + res2[0].totalCount;
        }

        console.log(totalUnreadMessages)
        return {
            totalUnreadMessages
        }
    } catch (e) {
        throw e;
    }

}



module.exports = {
    userSignUp,
    userLogIn,
    regEmailOrPhone,
    verifyOtp,
    resendOTP,
    getData,
    updateUserCategories,
    addEditVenueGroup,
    addEditPost,
    venueConversationDetails,
    getUserPosts,
    addEditComment,
    addEditReplies,
    likeOrUnlike,
    getPostWithComment,
    followUnfollow,
    forgotPassword,
    resetPassword,
    exitGroup,
    getVenueFilter,
    getProfileData,
    searchVenue,
    addEditPostGroup,
    listOfFilters,
    postGroupConversation,
    getCatPostGroups,
    userNameCheck,
    searchPostGroup,
    getCommentReplies,
    crossedPeople,
    joinGroup,
    getNotifications,
    chatSummary,
    getNewMessageCount,
    chatConversation,
    interestMatchUsers,
    configNotification,
    searchUser,
    addParticipants,
    addEditChatGroup,
    configSetting,
    updateDeviceToken,
    logOut,
    acceptInviteRequest,
    editProfile,
    readNotifications,
    // searchfollowers,
    listFollowerFollowing,
    userFollowerFollowing,
    unreadCount,
    // userNameCheckWithAuth,
    listLikers,
    listRepliers,
    rejectRequest,
    groupDetails,
    addParticipantsList,
    hidePersonalInfo,
    requestCounts,
    deleteCommentReply,
    assignAdminAndExit,
    assignAdmin,
    deleteGroup,
    archiveGroup,
    homeSearchTop,
    homeSearchTag,
    homeSearchPost,
    homeSearchVenue,
    homeSearchGroup,
    archiveListing,
    followUnfollowTag,
    searchTags,
    settingVerification,
    emailVerification,
    settingInvitePeople,
    groupInviteUsers,
    blockUser,
    listBlockedUsers,
    phoneVerification,
    getOTPForVerificiation,
    acceptFollowRequest,
    clearNotification,
    deletePost,
    addEditMediaComment,
    addEditMediaReplies,
    // deleteCommentReply,
    takeSurveyProperties,
    getTakeSurveyProperties,

    // contact us
    userContactUs,

    // tokbox
    callInitiate,
    callDisconnect,
    // callConnect


    // challengeType
    startChallenge,

    //stats
    stats,
    tangoGetCatalog,
    tangoGetOrders,
    tangoPostOrders,
    // getGiftCardOrderData,
    showSpinWheel,
    addSpinWheelPrize,
    addTwitterTimming,
    showCharityOrgList,
    addCharityDonation,
    showRedeemHistory,
    getPointEarnedHistory,
    addStory,
    getStories,
    deleteStory,
    promoteUser
}
