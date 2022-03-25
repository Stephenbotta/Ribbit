'use strict';

const TokenManager = require('../Libs/TokenManager'),
    Config = require('../Configs'),
    UniversalFunctions = require('../Utils/UniversalFunction'),
    Controller = require('../Controllers'),
    Service = require('../Services/queries'),
    Models = require('../Models'),
    mongoose = require('mongoose');

let io = require('socket.io');
const socketInfo = {};
const async = require('async')
const pushNotification = require('../Libs/pushNotification');
const _ = require('lodash');
const { func } = require('joi');
const { object } = require('twilio/lib/base/serialize');


const connectSocket = function (server) {
    // storing socket information
    if (!socketInfo.app) {
        socketInfo.app = {};
    }
    socketInfo.app.socketConnections = {};

    io = io.listen(server.listener);
    io.on('connection', function (socket) {
        console.log('Socket start')
        // console.log(socketInfo)      

        console.log("socket id is....", socket.id, socket.handshake.query.accessToken)
        if (socket && socket.handshake.query && socket.handshake.query.accessToken && socket.id) {
            TokenManager.verifyChatToken(socket.handshake.query.accessToken)
                .then(result => {
                    // joining socket into room with his own _id
                    // socket.join(result._id,()=>{
                    //     debugConnect(result._id +' joined the room ' +result._id);
                    // });
                    // console.log('------------->1',socketInfo.app.socketConnections)

                    console.log(result, ">>>>>>>>>>>>>>>>>>>>>>>>")
                    if (socketInfo.app.socketConnections.hasOwnProperty(result[0]._id)) {
                        // console.log('------------->1',socketInfo.app.socketConnections)

                        socketInfo.app.socketConnections[result[0]._id] = { socketId: socket.id };



                        console.log("user socket id--->>>>>>", server.app.socketConnections, socket.id);
                        // debugConnect("user socket id--->>>>>>", server.app.socketConnections,socket.id);

                        // emitting receiver event when socket successfully conencted as new.

                        socket.emit("socketConnected", {
                            statusCode: 200,
                            message: 'Added To socketConnections',
                            data: { socketId: socket.id }
                        });

                    }
                    else {
                        // debugConnect("user socket id is going to add--->>>>>>", result[0]._id,socketInfo.app.socketConnections,socket.id);

                        // updating existing user socket

                        socketInfo.app.socketConnections[result[0]._id] = {
                            socketId: socket.id
                        };

                        // adding userId corresponding to socket id

                        socketInfo.app.socketConnections[socket.id] = {
                            userId: result[0]._id
                        };

                        console.log("Step 2222222222222222", socketInfo.app.socketConnections);

                        // debugConnect("user socket id added successfully-->>>>>>", socketInfo.app.socketConnections,socket.id);

                        // emitting receiver event when socket successfully conencted updated.

                        socket.emit("socketConnected", {
                            statusCode: 200,
                            message: 'Socket id Updated',
                            data: { socketId: socket.id }
                        });
                    }

                    // updating user status to online
                    updateOnlineStatusOfUser(result[0]._id, true);

                    return result[0]._id;
                })
                .then(function (userId) {
                    //  console.log('------------>2',userId)
                    return getUsersGroups(userId)
                })
                .catch(reason => {
                    //console.log('Error in socket connect',reason);
                    // debugConnect('Error in socket connect',reason);
                    socket.emit("socketError", "This is socket Error");
                });

            socket.on("currentLocation", (locationData, ack) => {
                console.log('------------>6', locationData.userId)
                if (!locationData.locationLong)
                    return socket.emit("parmeterError", "locationLong Required");
                if (!locationData.locationLat)
                    return socket.emit("parmeterError", "locationLat Required");
                if (!locationData.locationName)
                    return socket.emit("parmeterError", "locationName Required");
                if (!locationData.locationAddress)
                    return socket.emit("parmeterError", "locationName Required");
                if (!locationData.userId)
                    return socket.emit("parmeterError", "userId Required");
                updateLoc(locationData).then(result => {
                    socket.emit('recentUserCrossed', { result })
                    ack(1)
                }).catch(reason => {
                    ack(0)
                    console.log(reason)
                })
            })

            socket.on("sendMessage", async function (chatdata, ack) {

                if (chatdata.groupType === "VENUE")
                    chatdata.messageFor = "GROUP"
                else if (chatdata.groupType === "GROUP")
                    chatdata.messageFor = "GROUP"
                else
                    chatdata.messageFor = "INDIVIDUAL"

                // check for parameters
                // console.log('-------------chatdatttttttttttt>5',chatdata)
                if (!chatdata.type)
                    return socket.emit("parmeterError", "type Required");

                if (!chatdata.senderId)
                    return socket.emit("parmeterError", "SenderId Required");

                if (!chatdata.receiverId && !chatdata.groupId)
                    return socket.emit("parmeterError", "GroupId Required");

                if (!Service.checkObjectId(Models.Users, chatdata.senderId) || !Service.checkObjectId(Models.Users, chatdata.receiverId))
                    return socket.emit("parameterError", "Invalid id type");


                let socketId;
                let receiverSocketId;
                let receiveMessageData = {};
                let messageSave

                let message = {
                    senderId: chatdata.senderId,
                    createdDate: +new Date(),
                    readBy: [chatdata.senderId],
                    // sendAt : +new Date(),
                    isDelivered: true,
                    _id: mongoose.Types.ObjectId(),
                    type: chatdata.type,
                    groupType: chatdata.groupType,
                    // url:{}
                };
                // socket.emit('forClientEnd',message);

                if (chatdata.type === 'TEXT') {
                    if (chatdata.message) {
                        message["chatDetails.message"] = chatdata.message;
                        message["chatDetails.type"] = chatdata.type;

                        if (chatdata.userIdTags)
                            message["chatDetails.userIdTags"] = chatdata.userIdTags
                    }
                    else return socket.emit('parameterError', "Message Required");
                }
                else if (chatdata.type === "VIDEO") {

                    if (chatdata.videoUrl && chatdata.imageUrl) {
                        message["chatDetails.videoUrl.original"] = chatdata.videoUrl;
                        message["chatDetails.videoUrl.thumbnail"] = chatdata.videoUrl;
                        message["chatDetails.imageUrl.original"] = chatdata.imageUrl;
                        message["chatDetails.imageUrl.thumbnail"] = chatdata.imageUrl;
                        message["chatDetails.type"] = chatdata.type;

                        if (chatdata.userIdTags)
                            message["chatDetails.userIdTags"] = chatdata.userIdTags
                    }
                    else return socket.emit('parameterError', "File Required");

                } else if (chatdata.type === "AUDIO") {

                    if (chatdata.audioUrl && chatdata.audioDuration) {
                        message["chatDetails.audioUrl.original"] = chatdata.audioUrl;
                        message["chatDetails.type"] = chatdata.type;
                        message["chatDetails.audioDuration"] = chatdata.audioDuration;

                        if (chatdata.userIdTags)
                            message["chatDetails.userIdTags"] = chatdata.userIdTags
                    }
                    else return socket.emit('parameterError', "File Required");

                }
                else if (chatdata.type === "IMAGE") {
                    if (chatdata.imageUrl) {
                        message["chatDetails.imageUrl.original"] = chatdata.imageUrl;
                        message["chatDetails.imageUrl.thumbnail"] = chatdata.imageUrl;
                        message["chatDetails.type"] = chatdata.type;

                        if (chatdata.userIdTags)
                            message["chatDetails.userIdTags"] = chatdata.userIdTags
                    }
                    else return socket.emit('parameterError', "File Required");
                }
                else if (chatdata.type === "GIF") {
                    if (chatdata.imageUrl) {
                        message["chatDetails.imageUrl.original"] = chatdata.imageUrl;
                        message["chatDetails.imageUrl.thumbnail"] = chatdata.imageUrl;
                        message["chatDetails.type"] = chatdata.type;

                        if (chatdata.userIdTags)
                            message["chatDetails.userIdTags"] = chatdata.userIdTags
                    }
                    else return socket.emit('parameterError', "File Required");
                }

                if (chatdata.messageFor === "GROUP") {

                    socketId = chatdata.groupId;
                    message.groupId = chatdata.groupId;
                    message.chatType = "GROUP";
                    if (chatdata.groupType === "VENUE") {
                        await Service.update(Models.VenueGroups, { _id: chatdata.groupId, isDeleted: false }, { $set: { isArchive: [] } }, { lean: true })
                        var convIdCheck = await Service.getData(Models.VenueGroups, { _id: chatdata.groupId }, { conversationId: 1 }, { limit: 1, lean: true })
                    }
                    if (chatdata.groupType === "GROUP") {
                        await Service.update(Models.PostGroups, { _id: chatdata.groupId, isDeleted: false }, { $set: { isArchive: [] } }, { lean: true })
                        var convIdCheck = await Service.getData(Models.PostGroups, { _id: chatdata.groupId }, { conversationId: 1 }, { limit: 1, lean: true })
                        message.noChat = false
                    }
                    if (convIdCheck.length) {
                        message.conversationId = convIdCheck[0].conversationId
                    } else {
                        message.conversationId = mongoose.Types.ObjectId()
                    }
                }
                else {

                    message.receiverId = chatdata.receiverId;
                    message.conversationId = chatdata.conversationId;
                    message.chatType = "INDIVIDUAL";
                    delete message['chatDetails.userIdTags']
                    message.groupType = "NON";
                    message.noChat = false

                    // let convIdCheck = await Service.getData(Models.Chats, {$or:[{$and:[{senderId: chatdata.senderId}, {receiverId: chatdata.receiverId}]}, {$and: [{senderId: chatdata.receiverId}, {receiverId: chatdata.senderId}]}], noChat: true}, {conversationId:1}, {limit:1, lean: true})


                    // if(convIdCheck.length){
                    //     await Service.update(Models.Chats, {$or:[{$and:[{senderId: chatdata.senderId}, {receiverId: chatdata.receiverId}]}, {$and: [{senderId: chatdata.receiverId}, {receiverId: chatdata.senderId}]}], noChat: true}, {$set: {noChat: false}}, {new: true, lean: true, multi: true})
                    // }else{
                    // message.conversationId = convIdCheck[0].conversationId
                    // }

                    let listSocketId = socketInfo.app.socketConnections;

                    if (listSocketId.hasOwnProperty(chatdata.receiverId)) {
                        receiverSocketId = socketInfo.app.socketConnections[chatdata.receiverId].socketId
                    } else {
                        receiverSocketId = false
                    }

                    let userData1 = await Service.getData(Models.Users, { _id: chatdata.senderId }, { imageUrl: 1, fullName: 1, userName: 1, blockedBy: 1, blockedWhom: 1 }, { lean: true })

                    if (!JSON.stringify(userData1[0].blockedBy).includes(JSON.stringify(chatdata.receiverId)) /*&& !JSON.stringify(userData1[0].blockedWhom).includes(JSON.stringify(chatdata.receiverId))*/) {
                        // console.log("------------------PPPPPPPPPPP",message)
                        messageSave = await storeMessage(message);
                    }
                    console.log('reciverrrrrrrrrrrrrrr socckt id-', receiverSocketId)
                    console.log(chatdata.type);
                    if (chatdata.type === "TEXT") {

                        receiveMessageData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            receiverId: message.receiverId,
                            createdDate: message.createdDate,
                            conversationId: message.conversationId,
                            chatDetails: {
                                message: chatdata.message,
                                type: chatdata.type,
                            }
                        }
                    }

                    if (chatdata.type === "VIDEO") {

                        receiveMessageData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            receiverId: message.receiverId,
                            createdDate: message.createdDate,
                            conversationId: message.conversationId,
                            chatDetails: {
                                videoUrl: {
                                    original: chatdata.videoUrl,
                                    thumbnail: chatdata.videoUrl,
                                },
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                            }
                        }
                    }

                    if (chatdata.type === "IMAGE") {
                        receiveMessageData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            receiverId: message.receiverId,
                            createdDate: message.createdDate,
                            conversationId: message.conversationId,
                            chatDetails: {
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                            }
                        }
                    }


                    if (chatdata.type === "AUDIO") {
                        receiveMessageData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            receiverId: message.receiverId,
                            createdDate: message.createdDate,
                            conversationId: message.conversationId,
                            chatDetails: {
                                audioUrl: {
                                    original: chatdata.audioUrl
                                },
                                type: chatdata.type,
                            }
                        }
                    }

                    if (chatdata.type === "GIF") {
                        receiveMessageData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            receiverId: message.receiverId,
                            createdDate: message.createdDate,
                            conversationId: message.conversationId,
                            chatDetails: {
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                            }
                        }
                    }

                    if (receiverSocketId && !JSON.stringify(userData1[0].blockedBy).includes(JSON.stringify(chatdata.receiverId)) && !JSON.stringify(userData1[0].blockedWhom).includes(JSON.stringify(chatdata.receiverId))) {

                        if (message.conversationId)
                            receiveMessageData.conversationId = message.conversationId;

                        socket.to(receiverSocketId).emit('receiveMessage', receiveMessageData);
                        // console.log('&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&', receiveMessageData)
                        ack(receiveMessageData)
                    } else {
                        ack(receiveMessageData)
                    }


                    let receiveDeviceToken = await Service.findOne(Models.Users, { _id: message.receiverId }, { deviceToken: 1 }, { lean: true })

                    if (receiveDeviceToken && receiveDeviceToken.deviceToken /*&& !JSON.stringify(userData1[0].blockedWhom).includes(JSON.stringify(chatdata.receiverId))*/) {

                        let data = {
                            id: message.conversationId,
                            senderDetails: userData1[0],
                            TYPE: "CHAT",
                            userId: userData1[0]._id,
                            imageUrl: userData1[0].imageUrl.original,
                            fullName: userData1[0].fullName,
                            userName: userData1[0].userName,
                            msg: userData1[0].fullName + ' has sent you a message',
                        };

                        await pushNotification.sendPush(receiveDeviceToken.deviceToken, data)
                        ack({ conversationId: message.conversationId })
                    }
                    else console.log('no device Tokennnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn')

                }

                console.log('$$$$$$$$$$$$$$ Socket id ####', socketId);

                if (socketId) {
                    let messageSave = await storeMessage(message);
                    let receiveData, receiveDatatoDiff = {}, socketIdsToDiffData = []
                    // it will be either room id or socket id
                    // const socketsInRoom = io.sockets.adapter.rooms[socketId].sockets
                    // console.log('---------->>>12121',socketsInRoom)
                    // console.log('---------OOO', socketInfo.app.socketConnections) 
                    let userData1 = await Service.getData(Models.Users, { _id: mongoose.Types.ObjectId(chatdata.senderId) }, { imageUrl: 1, fullName: 1, userName: 1, firstName: 1, lastName: 1, hidePersonalInfoFromUser: 1, hideProfileImage: 1, hideFirstName: 1, hideLastName: 1, hideLocationDetails: 1, hidePersonalInfo: 1 }, { lean: true })
                    // if(socketInfo.app.socketConnections.hasOwnProperty(userData1[0].hidePersonalInfoFromUser) && userData1[0].hidePersonalInfo || userData1[0].hideProfileImage || userData1[0].hideFirstName || userData1[0].hideLastName){
                    //     for(let one of userData1[0].hidePersonalInfoFromUser){
                    //         socketIdsToDiffData.push(socketInfo.app.socketConnections[one].socketId)
                    //     }
                    // }
                    // if(userData1[0].hidePersonalInfo){
                    //     for(let a in socketsInRoom){
                    //         socketIdsToDiffData.push(a)
                    //     }
                    // }
                    // console.log('QQQQQQQQQQQQQQQQQQQQQQQQQQQQ socketIdsToDiffData',socketIdsToDiffData)

                    // socketIdsToDiffData = _.uniq(socketIdsToDiffData)
                    // console.log('QQQQQQQQQQQQQQQQQQQQQQQQQQQQ socketIdsToDiffData',socketIdsToDiffData)

                    if (chatdata.type === "TEXT") {
                        receiveData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            createdDate: message.createdDate,
                            groupId: message.groupId,
                            conversationId: message.conversationId,
                            // readBy: chatdata.readBy,
                            // isDelivered: chatdata.isDelivered,
                            chatDetails: {
                                message: chatdata.message,
                                type: chatdata.type,
                                userIdTags: chatdata.userIdTags
                            }
                        }
                        // if(socketIdsToDiffData.length){
                        //     receiveDatatoDiff = {
                        //         senderId: {
                        //             imageUrl: {"original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                        //             "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL},
                        //             fullName: userData1[0].firstName.substr(0, 1) + ".... " + userData1[0].lastName.substr(0, 1) + "....",
                        //             userName: userData1[0].userName.substr(0, 3)
                        //         },
                        //         createdDate: message.createdDate,
                        //         groupId: message.groupId,
                        //         conversationId: message.conversationId,
                        //         // readBy: chatdata.readBy,
                        //         // isDelivered: chatdata.isDelivered,
                        //         chatDetails: {
                        //             message: chatdata.message,
                        //             type: chatdata.type,
                        //             userIdTags: chatdata.userIdTags
                        //         }
                        //     }
                        //     if(userData1[0].hideProfileImage && !userData1[0].hidePersonalInfo){
                        //         receiveDatatoDiff.senderId.imageUrl.original = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL
                        //         receiveDatatoDiff.senderId.imageUrl.thumbnail = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        //         receiveDatatoDiff.senderId.userName = userData1[0].userName
                        //         receiveDatatoDiff.senderId.fullName = userData1[0].fullName
                        //     }
                        // }                            
                    }
                    if (chatdata.type === "VIDEO") {
                        receiveData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            createdDate: message.createdDate,
                            groupId: message.groupId,
                            conversationId: message.conversationId,
                            // readBy: chatdata.readBy,
                            // isDelivered: chatdata.isDelivered,
                            chatDetails: {
                                videoUrl: {
                                    original: chatdata.videoUrl,
                                    thumbnail: chatdata.videoUrl,
                                },
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                                userIdTags: chatdata.userIdTags
                            }
                        }
                        // receiveDatatoDiff = {
                        //     senderId: {
                        //         imageUrl: {"original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                        //         "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL},
                        //         fullName: userData1[0].firstName.substr(0, 1) + ".... " + userData1[0].lastName.substr(0, 1) + "....",
                        //         userName: userData1[0].userName.substr(0, 3)
                        //     },
                        //     createdDate: message.createdDate,
                        //     groupId: message.groupId,
                        //     conversationId: message.conversationId,
                        //     // readBy: chatdata.readBy,
                        //     // isDelivered: chatdata.isDelivered,
                        //     chatDetails: {
                        //         videoUrl: {
                        //             original: chatdata.videoUrl,
                        //             thumbnail: chatdata.videoUrl,
                        //         },
                        //         imageUrl: {
                        //             original: chatdata.imageUrl,
                        //             thumbnail: chatdata.imageUrl,
                        //         },
                        //         type: chatdata.type,
                        //         userIdTags: chatdata.userIdTags
                        //     }
                        // }
                        // if(userData1[0].hideProfileImage && !userData1[0].hidePersonalInfo){
                        //     receiveDatatoDiff.senderId.imageUrl.original = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL
                        //     receiveDatatoDiff.senderId.imageUrl.thumbnail = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        //     receiveDatatoDiff.senderId.userName = userData1[0].userName
                        //     receiveDatatoDiff.senderId.fullName = userData1[0].fullName
                        // }
                    }
                    if (chatdata.type === "IMAGE") {
                        receiveData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            createdDate: message.createdDate,
                            groupId: message.groupId,
                            conversationId: message.conversationId,
                            // readBy: chatdata.readBy,
                            // isDelivered: chatdata.isDelivered,
                            chatDetails: {
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                                userIdTags: chatdata.userIdTags
                            }
                        }
                        // receiveDatatoDiff = {
                        //     senderId: {
                        //         imageUrl: {"original": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL,
                        //         "thumbnail": Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL},
                        //         fullName: userData1[0].firstName.substr(0, 1) + ".... " + userData1[0].lastName.substr(0, 1) + "....",
                        //         userName: userData1[0].userName.substr(0, 3)
                        //     },
                        //     createdDate: message.createdDate,
                        //     groupId: message.groupId,
                        //     conversationId: message.conversationId,
                        //     // readBy: chatdata.readBy,
                        //     // isDelivered: chatdata.isDelivered,
                        //     chatDetails: {
                        //         imageUrl: {
                        //             original: chatdata.imageUrl,
                        //             thumbnail: chatdata.imageUrl,
                        //         },                                    
                        //         type: chatdata.type,
                        //         userIdTags: chatdata.userIdTags
                        //     }
                        // }
                        // if(userData1[0].hideProfileImage && !userData1[0].hidePersonalInfo){
                        //     receiveDatatoDiff.senderId.imageUrl.original = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.ORIGINAL
                        //     receiveDatatoDiff.senderId.imageUrl.thumbnail = Config.APP_CONSTANTS.DATABASE.DEFAULT_IMAGE_URL.PROFILE.THUMBNAIL
                        //     receiveDatatoDiff.senderId.userName = userData1[0].userName
                        //     receiveDatatoDiff.senderId.fullName = userData1[0].fullName
                        // }
                    }

                    if (chatdata.type === "GIF") {
                        receiveData = {
                            _id: messageSave._id,
                            senderId: userData1[0],
                            createdDate: message.createdDate,
                            groupId: message.groupId,
                            conversationId: message.conversationId,
                            // readBy: chatdata.readBy,
                            // isDelivered: chatdata.isDelivered,
                            chatDetails: {
                                imageUrl: {
                                    original: chatdata.imageUrl,
                                    thumbnail: chatdata.imageUrl,
                                },
                                type: chatdata.type,
                                userIdTags: chatdata.userIdTags
                            }
                        }
                    }

                    // delete receiveData.senderId.hideFirstName
                    // delete receiveData.senderId.hideLocationDetails
                    // delete receiveData.senderId.hideLastName
                    // delete receiveData.senderId.hidePersonalInfo
                    // delete receiveData.senderId.hidePersonalInfoFromUser
                    // delete receiveData.senderId.hideProfileImage
                    // delete receiveData.senderId.firstName
                    // delete receiveData.senderId.lastName

                    if (chatdata.groupType === "VENUE") {
                        let groupUsers = await Service.getData(Models.VenueGroups, { _id: chatdata.groupId }, { memberIds: 1 }, { lean: true })

                        notifyMembersOfGroup(userData1[0]._id, message.conversationId, groupUsers[0].memberIds, chatdata.groupType, chatdata.groupId)
                        Service.findAndUpdate(Models.VenueGroups, { _id: chatdata.groupId }, { $set: { infoUpdated: +new Date } }, { lean: true, new: true })
                    }
                    if (chatdata.groupType === "GROUP") {
                        let groupUsers = await Service.getData(Models.PostGroups, { _id: chatdata.groupId }, { isMember: 1 }, { lean: true })

                        notifyMembersOfGroup(userData1[0]._id, message.conversationId, groupUsers[0].isMember, chatdata.groupType, chatdata.groupId)
                    }

                    // if(socketIdsToDiffData.length){
                    //     console.log(receiveDatatoDiff)
                    //     for(let b of socketIdsToDiffData){
                    //         if(b == socketInfo.app.socketConnections[chatdata.senderId].socketId){
                    //             continue
                    //         }
                    //         io.sockets.in(b).emit('receiveMessage', receiveDatatoDiff);
                    //     }
                    //     // let otherSockets = []

                    //     for(let a in socketsInRoom){
                    //         console.log("++++++++++++++++++++", a, socketInfo.app.socketConnections[chatdata.senderId].socketId)
                    //         if(socketIdsToDiffData.includes(a) || (a == socketInfo.app.socketConnections[chatdata.senderId].socketId)){
                    //             console.log('SSSSSSSSSSSSSS')
                    //             continue
                    //         }
                    //         io.sockets.in(a).emit('receiveMessage', receiveData);
                    //         // otherSockets.push(a)
                    //     }
                    //     // console.log("______________________________",otherSockets)
                    //     // io.sockets.in(otherSockets).emit('receiveMessage', receiveData);
                    // }else{
                    socket.to(socketId).emit('receiveMessage', receiveData);
                    // }

                    // socket.to(socketId).emit('receiveMessage',receiveData);
                    // sending success acknowledgement

                    ack(receiveData);
                } else {
                    // receiver user is not active acknowledgement

                    ack(0);
                }

                // saving message in db
                // console.log('------------>7', message)
                // storeMessage(message);

            });

            socket.on("messageDelete", async function (messageData, ack) {
                console.log("++++++++++++++++++++++", messageData)
                let modelToUse
                if (messageData.type === "VENUE")
                    modelToUse = Models.VenueChats
                if (messageData.type === "GROUP")
                    modelToUse = Models.Chats
                if (messageData.type === "INDIVIDUAL")
                    modelToUse = Models.Chats


                let deleteMessage = await Service.update(modelToUse, { _id: messageData.messageId, isDeleted: false }, { $set: { isDeleted: true } }, { lean: true, new: true })
                if (deleteMessage) {
                    if (messageData.groupId) {
                        let messageDeleteDataToGroup = {
                            messageId: messageData.messageId,
                            type: messageData.type,
                            groupId: messageData.groupId,
                        }
                        socket.to(messageData.groupId).emit('messageDelete', messageDeleteDataToGroup);
                    }
                    if (messageData.receiverId) {
                        let receiverSocketId
                        let messageDeleteData = {
                            messageId: messageData.messageId,
                            type: messageData.type,
                            receiverId: messageData.receiverId,
                            senderId: messageData.senderId
                        }
                        if (socketInfo.app.socketConnections.hasOwnProperty(messageData.receiverId)) {
                            receiverSocketId = socketInfo.app.socketConnections[messageData.receiverId].socketId
                        }
                        if (receiverSocketId) {
                            socket.to(receiverSocketId).emit('messageDelete', messageDeleteData);
                        }
                    }
                    ack(1)
                } else {
                    ack(0)
                }
            });

            socket.on('isTyping', (data) => {

                if (data.literallyTyping)
                    data.userTyping = true
                else
                    data.userTyping = false

                console.log("Data Come From socket in isTyping Event-->>>>>", data);
                let socketId = data.groupId
                if (socketId)
                    socket.to(socketId).emit('isTyping', { userTyping: data.userTyping });
            });

            socket.on("readMessage", function (messageData, ack) {

                // checking messageId come or not
                console.log('---------------->10', messageData)
                let modelToUse
                if (messageData.groupType === "VENUE")
                    modelToUse = Models.VenueChats
                else
                    modelToUse = Models.Chats

                if (!messageData.messageId)
                    return socket.emit("parameterError", "MessageId required");

                // if(!messageData.groupType)
                //     return socket.emit("parmeterError","Message for Required");

                if (!Service.checkMessageObjectId(modelToUse, { _id: messageData.messageId, isDeleted: false }))
                    return socket.emit("parameterError", "Invalid message Id");

                let userId = socketInfo.app.socketConnections[this.id];
                console.log('------------>11', userId)
                if (userId) {
                    readMessage(messageData, userId);
                     ack(1);
                } else {
                     ack(0);
                    return socket.emit("socketError", "This is socket error");
                }

            });

            socket.on("viewStory", async function (storyData, ack) {

                console.log(storyData)

                let criteria = {
                    _id: mongoose.Types.ObjectId(storyData.id),
                    isDeleted: false
                }

                let story = await Service.findAndUpdate(Models.Stories, criteria, { $addToSet: { viewBy: storyData.userId } }, { new: true });
                //console.log(story);
                if (story) {
                    //let socketId = socketInfo.app.socketConnections[this.id].socketId;
                    //socket.to(socketId).emit('story', { story: story });
                    console.log(story)
                    ack({ story: story });
                }
                else {
                    ack(0)
                }
            })

            socket.on("disconnect", function () {
                // console.log("######################### Here in disconnect  ####################",socket.id)

                if (socketInfo.app.socketConnections.hasOwnProperty(socket.id)) {

                    // console.log('%%%%%%%%%%%%%%%%%%%%%% Sockets before %%%%%%',socket.id,' ***** ',socketInfo);

                    let userId = socketInfo.app.socketConnections[socket.id].userId;
                    console.log("######################### Here in disconnect  ####################", userId)

                        // deleting user id corresponding socket id
                        - 654
                    // updating online status to offline of user

                    updateOnlineStatusOfUser(userId, false);

                    // console.log('%%%%%%%%%%%%%%%%%%%%%% Sockets after %%%%%%',socket.id,' ***** ',socketInfo);



                }
                // console.log(' ############  Socket disconnected #############', socketInfo.app.socketConnections);
            });
        } else {
            console.log('no accessToken');
        }
    });
};

const storeMessage = function (messageData) {
    let modelToUse
    if (messageData.groupType === "VENUE")
        modelToUse = Models.VenueChats
    else if (messageData.groupType === "GROUP")
        modelToUse = Models.Chats
    else
        modelToUse = Models.Chats
    return new Promise((resolve, reject) => {
        if (messageData.groupType === "GROUP") {
            Service.findAndUpdate(Models.PostGroups, { _id: messageData.groupId, isDeleted: false }, { $set: { noChat: false } }, { new: true, lean: true }).then((result) => {

            })
        }
        Service.saveData(modelToUse, messageData)
            .then((result) => {
                // console.log('Message successfully added.....');
                // debugConnect('Message successfully added.....');
                console.log("+++++++++++++++++1111", result)
                resolve(result)
            })
            .catch((reason) => {
                // console.log('Error in message adding.....',reason);
                // debugConnect('Error in message adding.....',reason);
            });
    })
    // console.log('----------',messageData);

}

const getUsersGroups = async function (userId) {

    // getting groupIds of user

    let groupIds = [];
    let criteria = {
        userId: userId,
        isDeleted: false,
        isBlocked: false
    };

    let projection = {
        groupId: 1
    };

    let result = await Service.getData(Models.VenueGroupMembers, criteria, projection, {});
    // let resultChatGroup =  await Service.getData(Models.ChatGroupMembers,criteria,projection,{});
    let resultPostGroup = await Service.getData(Models.PostGroupMembers, criteria, projection, {});

    if (resultPostGroup.length) {
        for (let res of resultPostGroup) {
            result.push(res)
        }
    }
    // separating _id of all groups
    // console.log('------------->3(a)', result)
    if (result && result.length) {

        for (let i = 0; i < result.length; i++) {
            groupIds.push(result[i].groupId)
        }
        // console.log('------------>4', groupIds)
        // join the room to user

        await joinUserToGroupOnlogin(groupIds, userId);

    }

}

const joinUserToGroupOnlogin = function (groupIds, userId) {

    let promisesArray = [];

    let socketId = socketInfo.app.socketConnections[userId] || null;
    for (let i = 0; socketId && i < groupIds.length; i++) {
        promisesArray.push((function (userId, groupId) {
            return new Promise((resolve, reject) => {
                io.sockets.connected[socketId.socketId].join(groupId, () => {
                    console.log(userId + ' joined the room ' + groupId);
                    // debugConnect(userId +' joined the room ' +groupId);
                    return resolve();
                });
            })
        })(userId, groupIds[i]));
    }
    // console.log(JSON.stringify(promisesArray))

    return Promise.all(promisesArray);
}

const updateOnlineStatusOfUser = function (userId, status) {
    let criteria = {
        _id: userId,
        isDeleted: false,
        isBlocked: false
    };

    let setQuery = {
        isOnline: status
    };

    let option = {
        new: true
    };

    Service.findAndUpdate(Models.Users, criteria, setQuery, option)
        .then((result) => {
            //console.log("updated online status successfully for user "+userId +" to "+status);
            // debugConnect("updated online status successfully for user "+userId +" to "+status);
        })
        .catch((reason) => {
            //console.log("error occured during updation status for user "+reason);
            // debugConnect("error occured during updation status for user "+reason);
        })
}

const readMessage = function (messageData, userId) {

    let modelToUse
    if (messageData.groupType === "VENUE")
        modelToUse = Models.VenueChats
        messageData.chatType = "GROUP"

    if (messageData.chatType === "INDIVIDUAL") {
        let criteria = {
            _id: messageData.messageId,
            isDeleted: false
        };

        let setQuery = {
            $addToSet: {
                readBy: userId.userId,
            },
            $set: {
                isRead: true,
            }
        };

        let options = {
            new: true,
            lean: true
        };

        Service.findAndUpdate(modelToUse, criteria, setQuery, options)
            .then((result) => {
                console.log("updated read for user " + userId.userId + " for message " + messageData.messageId);
                // debugConnect("updated read for user "+userId.userId +" for message "+messageId);
            })
            .catch((reason) => {
                console.log("error occured during updation message read for id " + messageData.messageId + " for user " + userId + " with error " + reason);
                // debugConnect("error occured during updation message read for id "+messageId+" for user "+userId + " with error "+reason);
            });
    } else {
        let criteria = {
            _id: messageData.messageId,
            isDeleted: false
        };

        let setQuery = {
            $addToSet: {
                readBy: userId.userId,
            }
        };

        let options = {
            new: true,
            lean: true
        };
        Service.findAndUpdate(modelToUse, criteria, setQuery, options)
            .then((result) => {
                console.log("updated read for user " + userId.userId + " for message " + messageData.messageId);
                // debugConnect("updated read for user "+userId.userId +" for message "+messageData.messageId);
            })
            .catch((reason) => {
                console.log("error occured during updation message read for id " + messageData.messageId + " for user " + userId.userId + " with error " + reason);
                // debugConnect("error occured during updation message read for id "+messageData.messageId+" for user "+userId.userId + " with error "+reason);
            });
    }
}

const sendMessageToGroup = function (message, groupId, senderId) {
    // emitting groups socket

    if (senderId) {
        let senderSoceketId = socketInfo.app.socketConnections[senderId] && socketInfo.app.socketConnections[senderId].socketId || null;

        //console.log('$$$$$$$$$$$$$$$$$$',senderSoceketId);
        // debugConnect('$$$$$$$$$$$$$$$$$$',senderSoceketId);
        if (senderSoceketId)
            io.sockets.connected[senderSoceketId].to(groupId).emit('receiveMessage', message);
    } else
        io.in(groupId).emit('receiveMessage', message);

}

const sendMessageToSingleUser = function (message, receiverId, senderId) {
    // debugConnect(' ******** In function of sending messsage to indivudual *********',message,receiverId,senderId);

    let senderSocketId = socketInfo.app.socketConnections[senderId] && socketInfo.app.socketConnections[senderId].socketId || null,
        receiverSocketId = socketInfo.app.socketConnections[receiverId] && socketInfo.app.socketConnections[receiverId].socketId || null;

    // emitting indicidual receiver socket

    if (receiverSocketId) {
        //io.sockets.connected[senderSocketId].to(receiverSocketId).emit('receiveMessage',message);
        io.to(receiverSocketId).emit('receiveMessage', message);
    } else {
        // debugConnect(' ******** In function of sending messsage to indivudual receiver id is not found *********',receiverSocketId,' ========= ',socketInfo.app.socketConnections);
    }
}

const joinActiveUsersInGroup = function (userIds, groupId) {

    let promisesArray = [];

    for (let i = 0; i < userIds.length; i++) {
        promisesArray.push((function (userId, groupId) {
            return new Promise((resolve, reject) => {
                let socketId = socketInfo.app.socketConnections[userId] || null;
                if (socketId && io.sockets.connected[socketId]) {
                    io.sockets.connected[socketId].join(groupId, () => {
                        //console.log(userId +' joined the room ' +groupId);
                        // debugConnect(userId +' joined the room ' +groupId);
                        return resolve();
                    });
                } else
                    return resolve();
            })
        })(userIds[i], groupId));
    }

    return Promise.all(promisesArray);
}

const emitNewGroupSocketToRoom = function (senderId, room, data) {
    if (socketInfo.app.socketConnections[senderId]) {
        io.sockets.connected[socketInfo.app.socketConnections[senderId].socketId].to(room).emit('newGroup', data);
    }
}

const updateLoc = async (locationData) => {
    // console.log(locationData)
    try {
        return new Promise((resolve, reject) => {
            async.auto({
                updateMineLoc: (cb) => {
                    let criteria = {
                        _id: locationData.userId,
                        isDeleted: false
                    }
                    let dataToSet = {
                        currentLocation: [locationData.locationLong, locationData.locationLat],
                        locationTime: +new Date,
                        locationName: locationData.locationName,
                        locationAddress: locationData.locationAddress,
                    }
                    Service.findAndUpdate(Models.Users, criteria, { $set: dataToSet }, { new: true, lean: true }).then(result => {
                        if (result.locationTime) {
                            cb(null, result.locationTime)
                        }
                    })
                },
                nearByList: (cb) => {
                    let criteria = {
                        "currentLocation": {
                            $near: {
                                $geometry: {
                                    type: "Point",
                                    coordinates: [locationData.locationLong, locationData.locationLat]
                                },
                                $maxDistance: 10,
                                $minDistance: 0
                            }
                        },
                        locationTime: { $gte: Date.now() - 60 * 60000 },
                        _id: { $ne: locationData.userId }
                    }
                    Service.getData(Models.Users, criteria, {}, { lean: true }).then(result => {
                        cb(null, result)
                    })
                },
                saveMyLocInOtherUsers: ['updateMineLoc', 'nearByList', async (result, cb) => {
                    //    console.log('-------res',result.nearByList.length)
                    for (let user of result.nearByList) {

                        let check = await Service.getData(Models.CrossedUsers, { crossedUserId: locationData.userId, userId: user._id }, {}, { lean: true })
                        //    console.log('------check',check.length)
                        if (check.length) {
                            let criteria = {
                                crossedUserId: mongoose.Types.ObjectId(locationData.userId),
                                userId: mongoose.Types.ObjectId(user._id)
                            }

                            let dataToSet = {
                                $set: {
                                    time: result.updateMineLoc,
                                    locationName: locationData.locationName,
                                    locationAddress: locationData.locationAddress,
                                    location: [locationData.locationLong, locationData.locationLat]
                                }
                            }
                            // console.log(dataToSet)
                            let a = await Service.findAndUpdate(Models.CrossedUsers, criteria, dataToSet, { lean: true, new: true })
                            // console.log('--------------updating')
                        } else {

                            let dataToSet = {
                                userId: user._id,
                                crossedUserId: locationData.userId,
                                time: result.updateMineLoc,
                                locationName: locationData.locationName,
                                locationAddress: locationData.locationAddress,
                                // conversationId: mongoose.Types.ObjectId(),
                                location: [locationData.locationLong, locationData.locationLat]
                            }
                            let checkCrossedEarly = await Service.getData(Models.CrossedUsers, { $or: [{ $and: [{ userId: locationData.userId }, { crossedUserId: user._id }] }, { $and: [{ crossedUserId: user._id }, { userId: locationData.userId }] }] }, { conversationId: 1 }, { lean: true })
                            let checkConversation = await Service.getData(Models.Chats, { $or: [{ $and: [{ senderId: locationData.userId }, { receiverId: user._id }] }, { $and: [{ senderId: user._id }, { receiverId: locationData.userId }] }] }, { conversationId: 1 }, { lean: true })
                            if (checkConversation.length || checkCrossedEarly.length) {
                                console.log('--------------------------------', checkConversation[0])
                                if (checkConversation.length) {
                                    console.log('---------------checkConv', checkConversation[0].conversationId)

                                    dataToSet.conversationId = checkConversation[0].conversationId
                                }
                                if (checkCrossedEarly.length) {
                                    console.log('---------------checkEarly', checkCrossedEarly[0].conversationId)

                                    dataToSet.conversationId = checkCrossedEarly[0].conversationId
                                }
                            }
                            else {
                                dataToSet.conversationId = mongoose.Types.ObjectId()
                                await Service.saveData(Models.Chats, { senderId: locationData.userId, receiverId: user._id, conversationId: dataToSet.conversationId, noChat: true })
                            }

                            let saveCrossedUsers = await Service.saveData(Models.CrossedUsers, dataToSet)
                            let checkOther = await Service.getData(Models.CrossedUsers, { userId: locationData.userId, crossedUserId: user._id }, {}, { lean: true })
                            if (!checkOther.length) {
                                let dataToSetForOther = {
                                    userId: locationData.userId,
                                    crossedUserId: user._id,
                                    time: result.updateMineLoc,
                                    locationName: locationData.locationName,
                                    locationAddress: locationData.locationAddress,
                                    location: [locationData.locationLong, locationData.locationLat],
                                    conversationId: dataToSet.conversationId
                                }
                                await Service.saveData(Models.CrossedUsers, dataToSetForOther)
                            }
                            // if(saveCrossedUsers){
                            //     let crossedUser = await Service.getData(Models.Users, {_id: locationData.userId, isDeleted: false}, {fullName:1, userName: 1, imageUrl:1}, {lean: true})
                            //     let deviceTokenUser = await Service.getData(Models.Users, {_id: user._id, isDeleted: false}, {deviceToken:1}, {lean: true})
                            //     let pushData = {
                            //         userId: locationData.userId,
                            //         imageUrl: crossedUser[0].imageUrl,
                            //         TYPE: "CROSSED_PEOPLE",
                            //         msg: crossedUser[0].fullName + " has crossed your path at " + locationData.locationName + locationData.locationAddress
                            //     }
                            //     console.log(pushData)
                            //     pushNotification.sendPush(deviceTokenUser[0].deviceToken, pushData)
                            // }                            
                        }
                    }
                    cb(null, true)
                }],
                crossedUsers: ['saveMyLocInOtherUsers', (result, cb) => {
                    if (result.saveMyLocInOtherUsers) {
                        let criteria = {
                            userId: mongoose.Types.ObjectId(locationData.userId),
                            isDeleted: false
                        }

                        let populate = [{
                            path: 'crossedUserId',
                            select: 'fullName imageUrl bio',
                            model: 'Users'
                        }]
                        // console.log(criteria)
                        Service.populateData(Models.CrossedUsers, criteria, {}, { new: true, lean: true }, populate).then(result => {
                            // console.log(result)
                            cb(null, result)
                        }).catch(reason => {
                            console.log(reason)
                        })
                    }
                }]
            }, (err, result) => {
                if (err) reject(err)
                resolve(result.crossedUsers)
            })
        })
    } catch (e) {
        console.log(e)
    }
}

var joinUserWhenGroupActive = (userId, groupId) => {
    let listSocketId = socketInfo.app.socketConnections
    console.log(')))))))))))))))))))))))))))', listSocketId)
    if (listSocketId.hasOwnProperty(userId)) {
        let userSocketId = listSocketId[userId].socketId
        console.log(')))))))))))))))))))))))))))', listSocketId)

        io.sockets.connected[userSocketId].join(groupId, async () => {
            console.log(userId + ' joined the room ' + groupId);
            // let userDetails = await Service.getData(Models.Users, {_id: userId}, {userName:1, fullName:1}, {lean: true})
            // io.to(groupId).emit('inBetweenJoinedUser', {userDetails});
            // debugConnect(userId +' joined the room ' +groupId);
            // return resolve();
        });
    }

}

let notifyMembersOfGroup = async (senderId, conversationId, memberIds, groupType, groupId) => {
    let allNotifyMembers = []; let data = {}, groupDetails;
    let userData = await Service.getData(Models.Users, { _id: senderId }, { fullName: 1, blockedWhom: 1 }, { lean: true });

    for (let member of memberIds) {
        let notificationCheck
        if (groupType === "VENUE") {
            notificationCheck = await Service.getData(Models.VenueGroupMembers, { groupId: groupId, userId: member }, { isNotify: 1 }, { lean: true })
        }
        if (groupType === "GROUP") {
            notificationCheck = await Service.getData(Models.PostGroupMembers, { groupId: groupId, userId: member }, { isNotify: 1 }, { lean: true })
        }
        console.log(notificationCheck[0].isNotify)
        if ((JSON.stringify(senderId) === JSON.stringify(member)) || !notificationCheck[0].isNotify || (JSON.stringify(userData[0].blockedWhom).includes(JSON.stringify(member)))) {
            console.log('SSSSKKKKKKKIIIIIIIPPPPPPP')
            continue
        }
        // if(!socketInfo.app.socketConnections.hasOwnProperty(member)){
        console.log('--------->>>>>', member)
        let data = await Service.getData(Models.Users, { _id: member }, { deviceToken: 1 }, { lean: true })
        // console.log(data)
        if (data[0].deviceToken === "") {
            continue
        }
        allNotifyMembers.push(data[0].deviceToken)
        // }
    }
    allNotifyMembers = _.uniq(allNotifyMembers);
    // let userData = await Service.getData(Models.Users, {_id: senderId}, {fullName:1}, {lean: true});

    if (groupType === "VENUE") {
        groupDetails = await Service.getData(Models.VenueGroups, { conversationId: conversationId }, { venueTitle: 1, imageUrl: 1 }, { lean: true })

        data = {
            id: conversationId,
            groupDetails: groupDetails[0],
            TYPE: "VENUECHAT",
            venueId: groupDetails[0]._id,
            venueTitle: groupDetails[0].venueTitle,
            imageUrl: groupDetails[0].imageUrl.original,
            msg: userData[0].fullName + ' has send message in the ' + groupDetails[0].venueTitle + ' venue'
        }
    }

    if (groupType === "GROUP") {

        groupDetails = await Service.getData(Models.PostGroups, { conversationId: conversationId }, { groupName: 1, imageUrl: 1 }, { lean: true })
        data = {
            id: conversationId,
            groupDetails: groupDetails[0],
            TYPE: "GROUPCHAT",
            groupId: groupDetails[0]._id,
            groupName: groupDetails[0].groupName,
            imageUrl: groupDetails[0].imageUrl.original,
            msg: userData[0].fullName + ' has send message in the ' + groupDetails[0].groupName + ' group'
        }
    }

    /*   if(groupType === "VENUE"){
            data = {
               id: conversationId,
               groupDetails: groupDetails[0],
               TYPE: "VENUECHAT",
               venueTitle : groupDetails[0].venueTitle,
               imageUrl : groupDetails[0].imageUrl.original,
               msg: userData[0].fullName +' has send message in the venue'
           }
       }
       if(groupType === "GROUP"){
            data = {
               id: conversationId,
               groupDetails: groupDetails[0],
               TYPE: "GROUPCHAT",
               groupName : groupDetails[0].groupName,
               imageUrl : groupDetails[0].imageUrl.original,
               msg: userData[0].fullName +' has send message in the group'
           }
       }
   */
    console.log(data)
    pushNotification.sendMultiUser(allNotifyMembers, data)
}

let requestCount = async (userId) => {
    console.log("-------------------11111", userId)
    let criteria = {}, socketId
    let listSocketId = socketInfo.app.socketConnections

    if (listSocketId.hasOwnProperty(userId)) {
        socketId = listSocketId[userId].socketId
    }

    criteria.toId = mongoose.Types.ObjectId(userId)
    criteria.$or = [{ type: { $eq: "REQUEST_VENUE" } }, { type: { $eq: "REQUEST_GROUP" } }, { type: { $eq: "REQUEST_FOLLOW" } }, { type: { $eq: "INVITE_VENUE" } }, { type: { $eq: "INVITE_GROUP" } }]
    criteria.actionPerformed = false
    criteria.isDeleted = false

    let requestCount = await Service.count(Models.Notifications, criteria)
    console.log("-------------------------------666666", requestCount, socketId)
    if (requestCount) {
        io.to(socketId).emit('requestCount', { requestCount });
    } else {
        io.to(socketId).emit('requestCount', { requestCount: 0 });
    }
}

let emailVerificationSocket = async (userId) => {
    console.log("-------------------11111", userId)
    let criteria = {}, socketId
    let listSocketId = socketInfo.app.socketConnections

    if (listSocketId.hasOwnProperty(userId)) {
        socketId = listSocketId[userId].socketId
    }

    io.to(socketId).emit('emailVerificationSocket', { emailVerificationSocket: true });
}

module.exports = {
    connectSocket: connectSocket,
    joinActiveUsersInGroup: joinActiveUsersInGroup,
    emitNewGroupSocketToRoom: emitNewGroupSocketToRoom,
    updateOnlineStatusOfUser: updateOnlineStatusOfUser,
    sendMessageToGroup: sendMessageToGroup,
    sendMessageToSingleUser: sendMessageToSingleUser,
    joinUserWhenGroupActive: joinUserWhenGroupActive,
    joinUserToGroupOnlogin: joinUserToGroupOnlogin,
    requestCount: requestCount,
    emailVerificationSocket: emailVerificationSocket
};
