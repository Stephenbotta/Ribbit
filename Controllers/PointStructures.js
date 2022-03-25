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

let add = async (request) => {
    let dataToSave = request.payload;

    let response = await new Model.PointStructures(dataToSave).save();

    return response;
}

let edit = async (request) => {
    let id = request.params.id;
    let dataToSave = request.payload;

    let response = {};
    response = await Model.PointStructures.findOneAndUpdate({ _id: id }, dataToSave, { new: true });
    return response;
}

let getList = async (request) => {
    let payload = request.query;
    let Models, query = { isDeleted: { $ne: true } }, populate = [], projection = {
        "updatedAt": 0,
        "__v": 0
    }, keyword;

    let options = { skip: (payload.pageNo - 1) * payload.limit, limit: payload.limit, sort: { 'createdAt': 1 } }

    if (payload.search) keyword = { $regex: new RegExp(payload.search, 'i') };
    if (payload.search) query.name = keyword;

    query.$or = [
        { parentId: { $ne: null } },
        { parentId: { $exists: false } }];

    populate = [
        {
            path: "subPoints",
            select: "name description rewardPoint quantity",
            model: "PointStructures"
        }
    ]
    Models = Model.PointStructures;


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



module.exports = {
    getList,
    add,
    edit,
}