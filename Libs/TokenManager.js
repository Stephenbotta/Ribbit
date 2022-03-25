'use strict';

let Config = require('../Configs');
let Jwt = require('jsonwebtoken');
let async = require('async');
let Modal = require('../Models');
let Service = require('../Services/queries');


let getTokenFromDB = function (userId, userType,flag,token, callback) {
    
    let userData = null;
    let criteria = {
        _id: userId,
        accessToken :token

    };
    async.series([
        function (cb) {
            if (userType === Config.APP_CONSTANTS.DATABASE.USER_TYPE.USER && flag==='USER'){
                console.log('+++++', userType);
                Service.getData(Modal.Users,criteria,{},{lean:true}).then(result=>{
                    if (result && result.length > 0){
                        
                        userData = result[0];
                        console.log(">>>>>>>>>>>>>>>>>>>>>>",userData)
                        cb();
                    }else {
                        cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_TOKEN)
                    }
                }).catch(reason=>{
                    cb(reason)
                })

            }
            else if (userType === Config.APP_CONSTANTS.DATABASE.USER_TYPE.ADMIN  && flag==='ADMIN'){

                Service.getData(Modal.Admins,criteria,{},{lean:true}).then(result=>{
                    if (result && result.length > 0){
                        userData = result[0];
                        cb();
                    }else {
                        cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_TOKEN)
                    }
                }).catch(reason=>{
                    cb(reason)
                })
                
            }else if (userType === Config.APP_CONSTANTS.DATABASE.USER_TYPE.SELLER  && flag==='SELLER'){
                Service.getData(Modal.Sellers,criteria,{},{lean:true}, function (err, dataAry) {
                    if (err){
                        cb(err)
                    }else {
                        if (dataAry && dataAry.length > 0){
                            userData = dataAry[0];
                            cb();
                        }else {
                            cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_TOKEN)
                        }
                    }

                });
            }
            else {
                cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR)
            }
        }
    ], function (err, result) {
        if (err){
           
            callback(err)
        }else {
            
            if (userData && userData._id){
                userData.id = userData._id;
                userData.type = userType;
            }
            callback(null,{userData: userData})
        }
    });
};

let setTokenInDB = function (userId, userType, tokenToSave) {
    return new Promise((resolve, reject)=>{
        let criteria = {
            _id: userId
        };
        let setQuery = {
            accessToken : tokenToSave
        };
        let dataToSend;
        async.series([
            function (cb) {
                if (userType === Config.APP_CONSTANTS.DATABASE.USER_TYPE.USER){
                    Service.findAndUpdate(Modal.Users,criteria,setQuery,{new:true,lean:true}).then(result=>{
                        
                        if (result && result._id){
                            dataToSend=result;
                            cb(null, result);
                        }else {
                            cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR)
                        }
                    }).catch(reason=>{
                        cb(reason)
                    })
    
                }
                else if (userType === Config.APP_CONSTANTS.DATABASE.USER_TYPE.ADMIN){
                    Service.findAndUpdate(Modal.Admins,criteria,setQuery,{new:true,lean:true}).then(result=>{
                        console.log('token', result);
                        if (result && result._id){
                            dataToSend=result;
                            cb(null, result);
                        }else {
                            cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR)
                        }
                    }).catch(reason=>{
                        cb(reason)
                    })
                } else {
                    cb(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR)
                }
            }
        ], function (err, result) {
            // console.log(err, result)
            if (err){
                reject(err)
            }else {
                resolve(result)
            }
        });
    })
    
};

let verifyToken = function (token,flag, callback) {

    Jwt.verify(token, Config.APP_CONSTANTS.SERVER.JWT_SECRET_KEY, function (err, decoded) {

        if (err) {
            callback(err)
        } else {
            getTokenFromDB(decoded._id, decoded.type,flag,token, callback);
        }
    });
};

let verifyChatToken = (token)=>{
    return new Promise((resolve, reject)=>{
        Jwt.verify(token, 'MaEHqzXzdWrCS6TS', (err, res)=>{
            if(err){
                reject(err)
            }else{
                getChatTokenFromDB(res._id, token).then(result =>{
                    resolve(result)
                }).catch(reason=>{
                    reject(reason)
                })
            }
        })
    })
    
};

let getChatTokenFromDB = function (userId, token) {
    return new Promise((resolve, reject)=>{
        let criteria = {
            _id: userId,
            accessToken :token
    
        };
        Service.getData(Modal.Users, criteria, {}, {}).then(result=>{

            resolve(result)
        }).catch(reason=>{
            reject(reason)
        })
    })    
};

let setToken = function (tokenData) {
    return new Promise((resolve, reject)=>{
        if (!tokenData._id && !tokenData.type) {
            reject(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR);
        } else {
            let tokenToSend = Jwt.sign(tokenData, Config.APP_CONSTANTS.SERVER.JWT_SECRET_KEY);
            setTokenInDB(tokenData._id,tokenData.type, tokenToSend).then(result=>{
                resolve(result)
            }).catch(reason=>{
                reject(reason)
            })
        }
    })    
};

let decodeToken = function (token, callback) {
    Jwt.verify(token, Config.APP_CONSTANTS.SERVER.JWT_SECRET_KEY, function (err, decodedData) {
        if (err) {
            callback(Config.APP_CONSTANTS.STATUS_MSG.ERROR.INVALID_TOKEN);
        } else {
            callback(null, decodedData)
        }
    })
};

module.exports = {
    setToken: setToken,
    verifyToken: verifyToken,
    decodeToken: decodeToken,
    verifyChatToken: verifyChatToken

};