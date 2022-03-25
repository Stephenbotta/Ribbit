
const mongoose = require('mongoose');
const Config = require('../Configs');
const Model = require('../Models');
mongoose.Promise = global.Promise;
let Service = require('../Services/queries');
let async = require('async');
// const TokenManager = require('../Libs/TokenManager');
const UniversalFunctions = require('../Utils/UniversalFunction');
let SocketManager = require('../Libs/SocketManager');

mongoose.connect(Config.dbConfig.config.dbURI,{ useNewUrlParser: true, useCreateIndex: true, useUnifiedTopology: true }, function (err) {
    console.log('MongoDB URI :',Config.dbConfig.config.dbURI);
    if (err) {
        console.log("DB Error: ", err);
        process.exit(1);
    } else {
        console.log('MongoDB Connected');
    }
});

exports.bootstrapAdmin = function (callback) {
    // console.log('here');
    let adminData1 = {
        email: 'sudhanshu@checkit.com',
        password: '897c8fde25c5cc5270cda61425eed3c8',   //qwerty
        name: 'sudhanshu admin',
        superAdmin:true
    };
    let adminData2 = {
        email: 'admin@gmail.com',
        password: '897c8fde25c5cc5270cda61425eed3c8',    //qwerty
        name: 'admin',
        superAdmin:true
    };
    let adminData3 = {
        email: 'test@checkit.com',
        password: '897c8fde25c5cc5270cda61425eed3c8',    //qwerty
        name: 'test name',
        superAdmin:true
    };
    let adminData4 = {
        email: 'demo@checkit.com',
        password: '897c8fde25c5cc5270cda61425eed3c8',    //qwerty
        name: 'demo name',
        superAdmin:true
    };
    async.parallel([
        function (cb) {
            insertData(adminData1.email, adminData1, cb)
        },
        function (cb) {
            insertData(adminData2.email, adminData2, cb)
        },
        function (cb) {
            insertData(adminData3.email, adminData3, cb)
        },
        function (cb) {
            insertData(adminData4.email, adminData3, cb)
        },
    ], function (err, done) {
        callback(err, 'Bootstrapping finished');
    })
};


exports.bootstrapAppVersion = function (callback) {
    let appVersion1 = {
        latestIOSVersion: 1,
        criticalIOSVersion: 1,
        latestAndroidVersion: 1,
        criticalAndroidVersion: 1,
        appType: Config.APP_CONSTANTS.DATABASE.USER_TYPE.USER
    };

    async.parallel([
        function (cb) {
            insertVersionData(appVersion1.appType, appVersion1, cb)
        },
    ], function (err, done) {
        callback(err, 'Bootstrapping finished For App Version');
    })
};

function insertVersionData(appType, versionData, callback) {
    let needToCreate = true;
    async.series([
        function (cb) {
            let criteria = {
                appType: appType
            };
            Service.getData(Model.AppVersions,criteria, {_id:1}, {},(err, data)=> {
                if (data && data.length > 0) {
                    needToCreate = false;
                }
                cb()
            })
        }, function (cb) {
            if (needToCreate) {
                Service.saveData(Model.AppVersions,versionData, function (err, data) {
                    cb(err, data)
                })
            } else {
                cb();
            }
        }], function (err, data) {
        console.log('Bootstrapping finished for ' + appType);
        callback(err, 'Bootstrapping finished For Admin Data')
    })
}

function insertData(email, adminData, callback) {
    //console.log('inset___________', adminData);

    let needToCreate = true;

    let criteria = {
        email: email
    };
    
    Model.Admins.findOne(criteria , (err, data) => {
        if(err)
            throw err;

        //console.log(data);
        if(data == undefined){
            //console.log(adminData);
            new Model.Admins(adminData).save((err, result) => {
                console.log(err);
                console.log(result);
            });
        }
    });
   
}

//exports.connectSocket = SocketManager.connectSocket;