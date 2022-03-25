
const Joi = require('joi');
const Controller = require('../Controllers');
const UniversalFunctions = require('../Utils/UniversalFunction');
const Config = require('../Configs');

module.exports = [
    {
        method: 'POST',
        path: '/user/regEmailOrPhone',
        config: {
            handler: function (request, reply) {
                Controller.UserController.regEmailOrPhone(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'user registering email or Phone number api',
            notes: 'reg details api',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    email: Joi.string().email().trim().lowercase(),
                    countryCode: Joi.string().description('send with +'),
                    phoneNumber: Joi.string().trim(),
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/verifyOTP',
        config: {
            handler: function (request, reply) {
                Controller.UserController.verifyOtp(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'OTP verification',
            notes: 'OTP verification api',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    email: Joi.string().email().trim().lowercase(),
                    otp: Joi.string().trim().required(),
                    countryCode: Joi.string().trim(),
                    phoneNumber: Joi.string().trim(),
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/resendOTP',
        config: {
            handler: function (request, reply) {
                Controller.UserController.resendOTP(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'resendOTP',
            tags: ['api'],
            validate: {
                payload: {
                    email: Joi.string().trim(),
                    countryCode: Joi.string().trim(),
                    phoneNumber: Joi.string().trim(),
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/signUp',
        config: {
            handler: function (request, reply) {
                Controller.UserController.userSignUp(request.payload).then(result => {
                    console.log("Result>>>>>>>>>>>>>>>>>",result)
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'user signup api',
            notes: 'sign up api',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    flag: Joi.number().required().valid([1, 2, 3, 4]).description('1- facebook Login, 2- google Login, 3- phoneNumber Login, 4- email Login').required(),
                    fullName: Joi.string().trim(),
                    userName: Joi.string().trim(),
                    email: Joi.string().email().trim().lowercase(),
                    countryCode: Joi.string().description('send with +'),
                    referralCode: Joi.string().optional(),
                    phoneNumber: Joi.string().trim(),
                    facebookId: Joi.string().trim(),
                    googleId: Joi.string().trim(),
                    password: Joi.string().trim(),
                    deviceId: Joi.string().trim(),
                    deviceToken: Joi.string().trim(),
                    apnsDeviceToken: Joi.string().trim(),
                    platform: Joi.string().valid(Config.APP_CONSTANTS.platform.ANDROID, Config.APP_CONSTANTS.platform.IOS).required().default(Config.APP_CONSTANTS.platform.WEB),
                    userType: Joi.string().allow('', null).empty(['', null]).valid(['STUDENT', 'MENTOR'])
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/logIn',
        config: {
            handler: function (request, reply) {
                Controller.UserController.userLogIn(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'user login api',
            notes: 'login api',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    userCredentials: Joi.string().trim(),
                    email: Joi.string().email().trim().lowercase(),
                    // userName : Joi.string().trim().lowercase(),
                    // countryCode : Joi.string().description('send with +'),
                    // phoneNumber : Joi.string().trim(),
                    deviceToken: Joi.string().trim(),
                    apnsDeviceToken: Joi.string().trim(),
                    facebookId: Joi.string().trim(),
                    platform: Joi.string().valid(Config.APP_CONSTANTS.platform.ANDROID, Config.APP_CONSTANTS.platform.IOS).required().default(Config.APP_CONSTANTS.platform.WEB),
                    googleId: Joi.string().trim(),
                    password: Joi.string().trim(),
                },

                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getData',
        config: {
            handler: function (request, reply) {
                console.log(request.auth)
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                console.log(userData)
                if (userData) {
                    Controller.UserController.getData(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'get different Data',
            notes: 'get different Data',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    flag: Joi.number().required().valid([1, 2, 3, 4, 5, 6]).description('1- Category, 2- Venue Page Data, 3 - Group Page Data, 4 - Post Page Data, 6 -daily Challenge').required(),
                    currentLong: Joi.number().description("user current location longitude"),
                    currentLat: Joi.number().description("user current location latitude"),
                    pageNo: Joi.number().description("page number for flag 4"),
                    limit: Joi.number().description("limiting data for flag 4"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/updateUserCategories',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.updateUserCategories(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'updating user category/interests api',
            notes: 'updating user category/interests',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    categoryArray: Joi.array().required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/promoteUser',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                Controller.UserController.promoteUser(request.payload, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
                 }
            },
            description: 'Promote user for Inviting Contacts',
            notes: 'Promote user for Inviting Contacts',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    phoneNumbers: Joi.array().items(Joi.string()),
                    platform: Joi.string().valid(Config.APP_CONSTANTS.platform.ANDROID, Config.APP_CONSTANTS.platform.IOS).required().default(Config.APP_CONSTANTS.platform.ANDROID)
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditVenueGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addEditVenueGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addEditVenueGroup',
            notes: 'addEditVenueGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    participantIds: Joi.array().description("VENUE participants"),
                    venueGroupId: Joi.string().description('venue groupId'),
                    venueTitle: Joi.string().description('VENUE groupTitle'),
                    venueLocationLong: Joi.number().description('VENUE group location logitude'),
                    venueLocationLat: Joi.number().description('VENUE group location latitude'),
                    venueLocationName: Joi.string().description('VENUE location name'),
                    venueLocationAddress: Joi.string().description('VENUE location address'),
                    venueTags: Joi.array().description('VENUE group tags'),
                    categoryId: Joi.string().description('categoryId for VENUE'),
                    isPrivate: Joi.number().valid([1, 2]).description('1- true, 2- false'),
                    groupImageOriginal: Joi.string().description('image link for VENUE'),
                    groupImageThumbnail: Joi.string().description('image link for VENUE'),
                    venueTime: Joi.number().description('VENUE Time'),
                    venueAdditionalDetailsName: Joi.string().description('VENUE Details Name'),
                    venueAdditionalDetailsDocs: Joi.string().description('VENUE document link'),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getVenueFilter',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getVenueFilter(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getVenueFilter',
            notes: 'getVenueFilter',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    date: Joi.string().description('date format 9/19/2018'),
                    categoryId: Joi.array().description('VENUE categoryId'),
                    private: Joi.array().description('1-true, 2-false'),
                    locationLong: Joi.number().description('VENUE location long'),
                    locationLat: Joi.number().description('VENUE location lat'),

                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/joinGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.joinGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'joinGroup',
            notes: 'joinGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    userId: Joi.string().description('userId'),
                    groupId: Joi.string().description('groupId').required(),
                    adminId: Joi.string().description('adminId').required(),
                    isPrivate: Joi.boolean().description('isPrivate').required(),
                    groupType: Joi.string().description('VENUE/GROUP').required()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/exitGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.exitGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'exitGroup',
            notes: 'exitGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    groupId: Joi.string().description('groupId'),
                    venueId: Joi.string().description('venueId'),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/venueConversationDetails',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.venueConversationDetails(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'individualChatOrGroupChat',
            notes: 'individualChatOrGroupChat',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    // conversationId: Joi.string(),
                    groupId: Joi.string().description("groupId").required(),
                    chatId: Joi.string().description("chatId")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/forgotPassword',
        config: {
            handler: function (request, reply) {
                Controller.UserController.forgotPassword(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'forgot Password',
            tags: ['api'],
            validate: {
                payload: {
                    email: Joi.string().required().lowercase()
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getProfileData',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getProfileData(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getProfileData',
            notes: 'getProfileData',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    userId: Joi.string().description('userId'),
                    userName: Joi.string().description('userName')
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/searchVenue',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.searchVenue(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'searchVenue',
            notes: 'searchVenue',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    // flag : Joi.number().valid([1,2,3,4]).description('on basis of 1- venueGroup, 2- UserProfile, 3- Category, 4- Location Range'),
                    search: Joi.string().description('search terms').required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/api/reset-password',
        config: {
            handler: function (request, reply) {
                Controller.UserController.resetPassword(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'reset-password',
            notes: 'sign up api',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    // type  : Joi.number().description('1- admin,2- user').required(),
                    id: Joi.string().required(),
                    time: Joi.number(),
                    password: Joi.string().required(),
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditPostGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addEditPostGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addEditPostGroup',
            notes: 'addEditPostGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    participantIds: Joi.array(),
                    postGroupId: Joi.string().description('venue groupId'),
                    groupName: Joi.string().description('VENUE groupTitle'),
                    description: Joi.string().allow("").description('venue descriptions'),
                    categoryId: Joi.string().description('categoryId for VENUE'),
                    isPrivate: Joi.number().valid([1, 2]).description('1- true, 2- false'),
                    groupImageOriginal: Joi.string().description('image link for VENUE'),
                    groupImageThumbnail: Joi.string().description('image link for VENUE'),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditPost',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addEditPost(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addEditPost',
            notes: 'addEditPost',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    postId: Joi.string(),
                    groupId: Joi.string(),
                    postText: Joi.string().allow("").allow(null),
                    imageOriginal: Joi.string().allow("").allow(null).description('image Url'),
                    imageThumbnail: Joi.string().allow("").allow(null).description('image Url'),
                    postType: Joi.string().description('REGULAR,\n' +
                        '        CONVERSE_NEARBY,\n' + ' LOOK_NEARBY,\n'),
                    postingIn: Joi.string().description('PUBLICILY,\n' +
                        '        FOLLOWERS,\n' + '        SELECTED_PEOPLE,\n'),
                    selectInterests: Joi.array(),
                    selectedPeople: Joi.array(),
                    locationLong: Joi.number().description(" location longitude"),
                    locationLat: Joi.number().description(" location latitude"),
                    locationName: Joi.string().description(" location name"),
                    locationAddress: Joi.string().description(" location address"),
                    meetingTime: Joi.number().description(" meetingTime"),
                    expirationTime: Joi.number().description(" expirationTime"),
                    hashTags: Joi.array(),
                    media: Joi.array().description(" test desc").items(Joi.object().keys({
                        original: Joi.string(),
                        thumbnail: Joi.string(),
                        videoUrl: Joi.string(),
                        mediaType: Joi.string().required().valid([
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF
                        ])
                    })).optional(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType : 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/userPosts',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.UserController.getUserPosts(request.payload, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'User posts',
            tags: ['api', 'user'],
            auth: 'UserAuth',
            validate: {
                payload: {
                    id: Joi.string().required().description("user id"),
                    limit: Joi.number().description("limit"),
                    pageNo: Joi.number().description("pageno")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/listOfFilters',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.listOfFilters(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'listOfFilters',
            notes: 'listOfFilters',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    flag: Joi.number().required().valid([1, 2]).description('1- for Venue, 2- '),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/postGroupConversation',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.postGroupConversation(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'postGroupConversation',
            notes: 'postGroupConversation',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    groupId: Joi.string().description("groupId").required(),
                    limit: Joi.number().description("limit"),
                    pageNo: Joi.number().description("pageno"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/searchPostGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.searchPostGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'searchPostGroup',
            notes: 'searchPostGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    // flag : Joi.number().valid([1,2,3,4]).description('on basis of 1- venueGroup, 2- UserProfile, 3- Category, 4- Location Range'),
                    search: Joi.string().description('search terms').required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getNotifications',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getNotifications(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getNotification',
            notes: 'getNotification',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    pageNo: Joi.number().description("pageNo")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/unreadMessages',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getNewMessageCount(userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'chatSummary',
            notes: 'chatSummary',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/chatSummary',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.chatSummary(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'chatSummary',
            notes: 'chatSummary',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    flag: Joi.number().valid([1, 2]).description("1- Individual, 2- Group"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/chatConversation',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.chatConversation(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'chatConversation',
            notes: 'chatConversation',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    conversationId: Joi.string().description('conversationId'),
                    chatId: Joi.string().description('chatId'),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getCatPostGroups',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getCatPostGroups(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getCatPostGroups',
            notes: 'getCatPostGroups',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    categoryId: Joi.string().description("groupId").required(),
                    // isMember: Joi.number().required().valid([1,2]).description("1- true, 2- false"),
                    pageNo: Joi.number().description("pageno"),
                    limit: Joi.number().description("limit"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/userNameCheck',
        config: {
            handler: function (request, reply) {
                // let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.UserController.userNameCheck(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'userNameCheck',
            notes: 'userNameCheck',
            // auth: 'UserAuth',         
            tags: ['api', 'user'],
            validate: {
                payload: {
                    userName: Joi.string().description("userName"),
                },
                // headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/crossedPeople',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.crossedPeople(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'readGroupPosts',
            notes: 'readGroupPosts',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                // payload: {
                // postId: Joi.array().description("postId"),
                // groupId: Joi.string().description("groupId"),
                // },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditComment',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.UserController.addEditComment(request.payload, userData ? userData : {}).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });

            },
            description: 'addEditComment',
            notes: 'addEditComment',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    postId: Joi.string().required(),
                    commentId: Joi.string(),
                    postBy: Joi.string(),
                    userIdTag: Joi.array(),
                    comment: Joi.string(),
                    attachmentUrl: {
                        original: Joi.string().allow(""),
                        thumbnail: Joi.string().allow(""),
                    },
                    mediaId: Joi.string().description('required when we need comment on media'), // when media comment
                    //mediaCommentId: Joi.string(), // update media comment
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditReplies',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addEditReplies(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addEditReplies',
            notes: 'addEditReplies',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    commentId: Joi.string(),
                    commentBy: Joi.string(),
                    replyId: Joi.string(),
                    postId: Joi.string(),
                    reply: Joi.string(),
                    userIdTag: Joi.array(),
                    mediaId: Joi.string().description('required when we need reply on media'), // require media id
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/likeOrUnlike',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.likeOrUnlike(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'likeCommentOrReply',
            notes: 'likeCommentOrReply',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    postId: Joi.string(),
                    commentId: Joi.string(),
                    replyId: Joi.string(),
                    mediaId: Joi.string(),
                    postBy: Joi.string(),
                    commentBy: Joi.string(),
                    replyBy: Joi.string(),
                    action: Joi.number().valid([1, 2]).required().description('1- like,2- unlike').required()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getPostWithComment',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getPostWithComment(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getPostWithComment',
            notes: 'getPostWithComment',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    postId: Joi.string().required(),
                    mediaId: Joi.string(),
                    pageNo: Joi.number(),
                    limit: Joi.number(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getCommentReplies',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getCommentReplies(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'getCommentReplies',
            notes: 'getCommentReplies',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    commentId: Joi.string(),
                    replyId: Joi.string(),
                    totalReply: Joi.number(),
                    mediaId: Joi.string()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/interestMatchUsers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.interestMatchUsers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'interestMatchUsers',
            notes: 'interestMatchUsers',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    categoryIds: Joi.array().description("categoryIds"),
                    pageNo: Joi.number().description("starts from 1"),
                    locationLong: Joi.number().description("locationLong"),
                    locationLat: Joi.number().description("locationLat"),
                    range: Joi.number().description("range"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/configNotification',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.configNotification(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'configNotification',
            notes: 'configNotification',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    venueId: Joi.string().description("venueId"),
                    groupId: Joi.string().description("groupId"),
                    action: Joi.boolean().description("action: true/fasle"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/searchUser',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.searchUser(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'searchUser',
            notes: 'searchUser',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    search: Joi.string().description("search").required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addParticipants',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addParticipants(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addParticipants',
            notes: 'addParticipants',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    participants: Joi.array().description("participants").required(),
                    venueId: Joi.string().description("venueId"),
                    groupId: Joi.string().description("groupId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addEditChatGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addEditChatGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'addEditChatGroup',
            notes: 'addEditChatGroup',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    name: Joi.string().description("name of group"),
                    imageOriginal: Joi.string().description("image original"),
                    imageThumbnail: Joi.string().description("image Thumbnail"),
                    memberIds: Joi.array().description("memberIds"),
                    groupId: Joi.string().description("groupId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/followUnfollow',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.followUnfollow(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'followUnfollow',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userId: Joi.string().required(),
                    action: Joi.number().valid([1, 2]).required().description('1- follow,2- unfollow')
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/configSetting',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.configSetting(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'configSetting',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    flag: Joi.number().required().valid([1, 2, 3, 4, 5, 6, 7, 8]).description('1- listing setting status ,2- private account, 3- visibility profile picture from userIds and followers, 4- visibility name from userIds and followers, 5- location Visibility, 6- tag permission for userIds and followers, 7- personal info, 8-alertNotification').required(),
                    userIds: Joi.array().description("with flag -3,4,6 "),
                    imageVisibilityForFollowers: Joi.boolean().description("with flag 3"),
                    imageVisibilityForEveryone: Joi.boolean().description("with flag 3"),
                    nameVisibilityForFollowers: Joi.boolean().description("with flag 4"),
                    nameVisibilityForEveryone: Joi.boolean().description("with flag 4"),
                    tagPermissionForFollowers: Joi.boolean().description("with flag 6"),
                    tagPermissionForEveryone: Joi.boolean().description("with flag 6"),
                    personalInfoVisibilityForFollowers: Joi.boolean().description("with flag 7"),
                    personalInfoVisibilityForEveryone: Joi.boolean().description("with flag 7"),
                    action: Joi.boolean().description('true- true,false- false valid for 2 and 5')
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/updateDeviceToken',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.updateDeviceToken(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'updateDeviceToken',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    deviceToken: Joi.string(),
                    apnsDeviceToken: Joi.string().trim(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/logOut',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.logOut(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'userLogOut',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                // payload: {
                // },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/acceptInviteRequest',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.acceptInviteRequest(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'acceptInviteRequest',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    acceptType: Joi.string().description("INVITE/REQUEST/FOLLOW"),
                    groupType: Joi.string().description("VENUE/GROUP"),
                    userId: Joi.string().description("userId"),
                    groupId: Joi.string().description("groupId"),
                    accept: Joi.boolean().description("true/false"),

                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/editProfile',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.editProfile(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'editProfile',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userName: Joi.string().description("userName"),
                    fullName: Joi.string().description("fullName"),
                    bio: Joi.string().description("bio").allow("").allow(null).optional(),
                    website: Joi.string().description("website").allow("").allow(null).optional(),
                    email: Joi.string().description("email"),
                    gender: Joi.string().description("MALE, FEMALE, OTHERS"),
                    dateOfBirth: Joi.number().description("dateOfBirth"),
                    designation: Joi.string().description("designation").allow("").allow(null).optional(),
                    company: Joi.string().description("company").allow("").allow(null).optional(),
                    isPrivate: Joi.string().description("profile privacy"),
                    imageOriginal: Joi.string().description("image Original"),
                    imageThumbnail: Joi.string().description("image Thumbnail"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/readNotifications',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.readNotifications(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'readNotifications',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    notificationId: Joi.string().description("notificationId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/unreadCount',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.unreadCount(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'unreadCount',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                query: {
                    //   notificationId:Joi.string().description("notificationId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/searchfollowers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.searchfollowers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'searchfollowers',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    // notificationId:Joi.string().description("notificationId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/listFollowerFollowing',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.listFollowerFollowing(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'listFollowerFollowing',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    flag: Joi.number().valid([1, 2]).description("1- follower list, 2- following list"),
                    // pageNo:Joi.number().description("pageNo"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/userFollowerFollowing',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.userFollowerFollowing(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'userFollowerFollowing',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/listLikers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.listLikers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'listLikers',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    postId: Joi.string().description("postId"),
                    commentId: Joi.string().description("commentId"),
                    replyId: Joi.string().description("replyId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/listRepliers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.listRepliers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'listRepliers',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    postId: Joi.string().description("postId"),
                    // commentId: Joi.string().description("commentId"),
                    // replyId: Joi.string().description("replyId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/rejectRequest',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.rejectRequest(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'rejectRequest',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    notificationId: Joi.string().description("notificationId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/groupDetails',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.groupDetails(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'groupDetails',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    venueId: Joi.string().description("venueId"),
                    groupId: Joi.string().description("groupId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addParticipantsList',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addParticipantsList(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'addParticipantsList',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    venueId: Joi.string().description("venueId"),
                    groupId: Joi.string().description("groupId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/hidePersonalInfo',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.hidePersonalInfo(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'hidePersonalInfo',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    profileImage: Joi.number().valid([1, 2]).description("1-true, 2-false"),
                    firstName: Joi.number().valid([1, 2]).description("1-true, 2-false"),
                    lastName: Joi.number().valid([1, 2]).description("1-true, 2-false"),
                    locationDetails: Joi.number().valid([1, 2]).description("1-true, 2-false"),
                    userName: Joi.number().valid([1, 2]).description("1-true, 2-false"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/requestCounts',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.requestCounts(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'requestCounts',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                query: {
                    //   notificationId:Joi.string().description("notificationId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/deleteCommentReply',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.deleteCommentReply(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'deleteCommentReply',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    commentId: Joi.string().description("commentId"),
                    replyId: Joi.string().description("replyId"),
                    mediaId: Joi.string().description("replyId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/assignAdminAndExit',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.assignAdminAndExit(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'assignAdminAndExit',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userId: Joi.string().description("userId"),
                    groupId: Joi.string().description("groupId"),
                    groupType: Joi.string().description("groupType: VENUE, GROUP"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/assignAdmin',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.assignAdmin(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'assignAdmin',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userId: Joi.string().description("userId"),
                    groupId: Joi.string().description("groupId"),
                    groupType: Joi.string().description("groupType: VENUE, GROUP"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/deleteGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.deleteGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'deleteGroup',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    groupId: Joi.string().description("groupId"),
                    groupType: Joi.string().description("groupType: VENUE, GROUP"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/archiveGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.archiveGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'archiveGroup',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    groupId: Joi.string().description("groupId"),
                    groupType: Joi.string().description("groupType: VENUE, GROUP"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/homeSearchTop',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.homeSearchTop(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'homeSearchTop',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search").allow(null),
                    pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/homeSearchTag',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.homeSearchTag(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'homeSearchTag',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search").allow(null),
                    pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/archiveListing',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.archiveListing(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'archiveListing',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    groupType: Joi.string().description("groupType: VENUE, GROUP"),
                    // groupId: Joi.string().description("groupId"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/followUnfollowTag',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.followUnfollowTag(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'followUnfollowTag',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    tagId: Joi.string().description("tagId"),
                    follow: Joi.boolean().description("true/false"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/searchTags',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.searchTags(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'searchTags',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/homeSearchPost',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.homeSearchPost(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'homeSearchPost',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search"),
                    pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/homeSearchGroup',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.homeSearchGroup(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'homeSearchGroup',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search"),
                    pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/homeSearchVenue',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.homeSearchVenue(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'homeSearchVenue',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    search: Joi.string().description("search"),
                    currentLat: Joi.number().description("currentLat"),
                    currentLong: Joi.number().description("currentLong"),
                    pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/settingVerification',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.settingVerification(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'settingVerification',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    email: Joi.string().description("email"),
                    phoneNumber: Joi.number().description("phoneNumber"),
                    passportDocUrl: Joi.string().description("passportDocUrl"),
                    // pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/emailVerification',
        config: {
            handler: function (request, reply) {
                Controller.UserController.emailVerification(request.query).then(result => {
                    reply(result)
                }).catch(reason => {
                    reply(reason);
                });
            },
            description: 'emailVerification',
            tags: ['api'],
            validate: {
                query: {
                    id: Joi.string().required().lowercase(),
                    timestamp: Joi.string().required().lowercase(),
                },
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/settingInvitePeople',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.settingInvitePeople(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'settingInvitePeople',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    emailArray: Joi.array().description("emailArray"),
                    phoneNumberArray: Joi.array().description("phoneNumberArray"),
                    // passportDocUrl: Joi.string().description("passportDocUrl"),
                    // pageNo: Joi.number().description("page number "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/groupInviteUsers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.groupInviteUsers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'groupInviteUsers',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    emailArray: Joi.array().description("emailArray"),
                    phoneNumberArray: Joi.array().description("phoneNumberArray"),
                    venueId: Joi.string().description("venueId"),
                    groupId: Joi.string().description("groupId "),
                    // limit: Joi.number().description("limiting data "),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/blockUser',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.blockUser(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'blockUser',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userId: Joi.string().description("userId"),
                    action: Joi.number().valid([1, 2]).description("1- block 2- unblock"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/listBlockedUsers',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.listBlockedUsers(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'listBlockedUsers',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                // payload: {
                //     userId: Joi.string().description("userId"),
                //     action: Joi.number().valid([1,2]).description("1- block 2- unblock"),
                // },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/phoneVerification',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.phoneVerification(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'phoneVerification',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    OTPcode: Joi.string().description("OTPcode"),
                    // action: Joi.number().valid([1,2]).description("1- block 2- unblock"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/getOTPForVerificiation',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getOTPForVerificiation(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'getOTPForVerificiation',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    // OTPcode: Joi.string().description("OTPcode"),
                    // action: Joi.number().valid([1,2]).description("1- block 2- unblock"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/acceptFollowRequest',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.acceptFollowRequest(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'acceptFollowRequest',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    userId: Joi.string().description("userId"),
                    action: Joi.boolean().description("true/false"),

                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/clearNotification',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.clearNotification(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'clearNotification',
            notes: 'clearNotification',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    pageNo: Joi.number()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/deletePost',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.deletePost(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'deletePost',
            notes: 'deletePost',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    postId: Joi.string().description("postId")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    // {
    //     method: 'POST',
    //     path: '/user/deleteCommentReply',
    //     config: {
    //         handler: function (request, reply) {
    //             let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
    //             if(userData){
    //                 Controller.UserController.deleteCommentReply(request.payload, userData).then(result => {
    //                     reply(UniversalFunctions.sendSuccess(null,result))
    //                 }).catch(reason => {
    //                     reply(UniversalFunctions.sendError(reason));
    //                 });
    //             }                    
    //         },
    //         description: 'deleteCommentReply',
    //         notes: 'deleteCommentReply',  
    //         auth: 'UserAuth',         
    //         tags: ['api', 'user'],
    //         validate: {
    //             payload: { 
    //                 commentId: Joi.string().description("commentId"),
    //                 replyId: Joi.string().description("replyId"),
    //             },
    //             headers: UniversalFunctions.authorizationHeaderObj,
    //             failAction: UniversalFunctions.failActionFunction
    //         },
    //         plugins: {
    //             'hapi-swagger': {
    //                 payloadType : 'form',
    //                 responses:Config.APP_CONSTANTS.swaggerDefaultResponseMessages
    //             }
    //         }
    //     }
    // },    
    {
        method: 'GET',
        path: '/user/getTakeSurveyProperties',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getTakeSurveyProperties(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                } else {
                    Controller.UserController.getTakeSurveyProperties(request.query, null).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'get this api use for save take survey data screenfor first time',
            auth: "UserAuth",
            tags: ['api', 'user'],
            validate: {
                query: {
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/takeSurveyProperties',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.takeSurveyProperties(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'this api use for save take survey data screenfor first time',
            auth: "UserAuth",
            tags: ['api', 'user'],
            validate: {
                payload: {
                    gender: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.GENDER.MALE,
                        Config.APP_CONSTANTS.DATABASE.GENDER.FEMALE,
                        Config.APP_CONSTANTS.DATABASE.GENDER.OTHERS,
                        ""
                    ]).description(`gender should be in between MALE,FEMALE,OTHERS`),
                    race: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.RACE.AMERICAN_INDIAN,
                        Config.APP_CONSTANTS.DATABASE.RACE.AFRICAN_AMERICAN,
                        Config.APP_CONSTANTS.DATABASE.RACE.ASIAN,
                        Config.APP_CONSTANTS.DATABASE.RACE.NATIVE_HAWAIIAN,
                        Config.APP_CONSTANTS.DATABASE.RACE.WHITE,
                    ]).description("American Indian | Asian| African American| Native Hawaiian| White"),
                    dateOfBirth: Joi.number().default(0),
                    houseHoldIncome: Joi.string().allow(''),
                    homeOwnership: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_MORTGAGE,
                        Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.OWN_HOUSE_OUTRIGHT,
                        Config.APP_CONSTANTS.DATABASE.HOME_ONWERSHIP.RENTED
                    ]).description("Home Onwership: Own House (outright)| Own house (mortgage)| Rented"),
                    education: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.ELEMENTRY_SCHOOL,
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.MIDDLE_SCHOOL,
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.HIGH_SCHOOL,
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.BACHELORS,
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.MASTERS,
                        Config.APP_CONSTANTS.DATABASE.EDUCATION.DOCTORAL,
                    ]).description("Education: ElementrySchool | Middle School| High School|bachelor's|master's |doctoral "),
                    employementStatus: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.EMPLOYED,
                        Config.APP_CONSTANTS.DATABASE.EMPLOYMENT_STATUS.UNEMPLOYED
                    ]).description("Education: ElementrySchool | Middle School| High School|bachelor's|master's |doctoral "),
                    maritalStatus: Joi.string().allow('').valid([
                        Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SINGLE,
                        Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.MARRIED,
                        Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.SEPERATED,
                        Config.APP_CONSTANTS.DATABASE.MARITAL_STATUS.WIDOWED,
                    ]).description("Marital status : Single|Married|Seperated|Widowed"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/getSurvey',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.SurveyController.getSurveyUser(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get survey list base on user interest',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                query: {
                    pageNo: Joi.number().default(1).description("by default 1"),
                    limit: Joi.number().default(10).description("by default 10"),
                    search: Joi.string().description("search on name"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/getSurveyQuestions',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.SurveyController.getSurveyQuestions(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get all the list of questions from single survey',
            notes: 'Here questionType 1 reffer for single value selected option type, 2 for mutli value selected option',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                query: {
                    surveyId: Joi.string().required().description("Survey id for get questions"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/sumitUserSurvey',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserSurveysController.sumitUserSurvey(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'This is use for submit the anwsers for survey by user',
            notes: 'This is use for submit the anwsers for survey by user',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    surveyId: Joi.string().required().description("Survey id for get questions"),
                    questions: Joi.array().items(Joi.object().keys({
                        questionId: Joi.string().required(),
                        options: Joi.array().items(Joi.object().keys({
                            optionId: Joi.string().required()
                        }))
                    })).required().description(`[{"questionId":"5dd39a1b9d1f993e85ade79d","options":[{"optionId":"5dd39bd1f2affe456f3b0a8e"}]},{"questionId":"5dd39bdbf2affe456f3b0a8f","options":[{"optionId":"5dd39bdbf2affe456f3b0a91"}]}]`),
                    feedback: Joi.string().default('').optional()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/contactUs',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.userContactUs(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, { _id: result._id }))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'contact us',
            notes: 'User submit contact us.',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    message: Joi.string().max(1000).required().description("Max length: 500")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/callInitiate',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.callInitiate(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'call initiate',
            notes: 'Call initiate',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    callToUserId: Joi.string().required()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/callDisconnect',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.callDisconnect(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'call Disconnect',
            notes: 'Call Disconnect',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    callerId: Joi.string().required(),
                    receiverId: Joi.string().required()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/challenges',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;

                Controller.Challenge.readChallenge("student", request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result));
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Challenge list',
            tags: ['api', 'user'],
            auth: 'UserAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10")
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/challenges/{id}/start',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.UserController.startChallenge(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Start challenge',
            tags: ['api', 'user'],
            auth: 'UserAuth',
            validate: {
                params: {
                    id: Joi.string().required().description("challenges id"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },

    {
        method: 'GET',
        path: '/user/challenges/{id}',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.Challenge.challengeDetail(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Challenge detail',
            tags: ['api', 'user'],
            auth: 'UserAuth',
            validate: {
                params: {
                    id: Joi.string().required().description("challenges id"),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/stats',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.UserController.stats(userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get User stats',
            tags: ['api', 'user'],
            auth: 'UserAuth',
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/tangoGetCatalog',
        config: {
            handler: function (request, reply) {
                // let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                // if (userData) {
                Controller.UserController.tangoGetCatalog().then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
                // }
                // else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'To get Catalogs from tango',
            // auth: 'UserAuth',
            tags: ['api', 'tango'],
            validate: {
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/tangoGetOrders',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.tangoGetOrders(userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'To get list of orders from tango',
            auth: 'UserAuth',
            tags: ['api', 'tango'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/tangoPostOrders',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.tangoPostOrders(userData, request.payload).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'To post order at tango',
            auth: 'UserAuth',
            tags: ['api', 'tango'],
            validate: {
                payload: {
                    faceValue: Joi.number().required(),
                    utid: Joi.string().required(),
                    image: Joi.string().optional()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    // {
    //     method: 'GET',
    //     path: '/user/GiftCardOrderHistory',
    //     config: {
    //         handler: function (request, reply) {
    //             let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
    //             if (userData) {
    //                 Controller.UserController.getGiftCardOrderData(request.query, userData).then(result => {
    //                     reply(UniversalFunctions.sendSuccess(null, result))
    //                 }).catch(reason => {
    //                     reply(UniversalFunctions.sendError(reason));
    //                 });
    //             }
    //         },
    //         description: 'Get Buy Gift Card Historyr',
    //         auth: 'UserAuth',
    //         tags: ['api', 'user'],
    //         validate: {
    //             headers: UniversalFunctions.authorizationHeaderObj,
    //             failAction: UniversalFunctions.failActionFunction
    //         },
    //         plugins: {
    //             'hapi-swagger': {
    //                 payloadType: 'form',
    //                 responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
    //             }
    //         }
    //     }
    // },
    {
        method: 'GET',
        path: '/user/ShowSpinWheel',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.showSpinWheel(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get Spin Wheel',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'PUT',
        path: '/user/addSpinWheelPrize',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addSpinWheelPrize(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'ADD Spin Wheel Prize',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    value: Joi.number().required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },

    {
        method: 'PUT',
        path: '/user/addTwitterTimming',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addTwitterTimming(request, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'ADD Twitter Timming',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                //   payload: {                        
                //         timeCount: Joi.number().required(),               
                // },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/showCharityOrgList',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.showCharityOrgList(request, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get Charity Org List',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                // query: {
                //     limit: Joi.number().min(0).required(),
                //     skip: Joi.number().min(0).required()
                // },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addCharityDonation',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addCharityDonation(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Add Charity donation ',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                payload: {
                    dollars: Joi.number().min(0).required(),
                    organizationId: Joi.string().required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/showRedeemHistory',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.showRedeemHistory(userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get Redeem History',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/getPointEarnedHistory',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getPointEarnedHistory(userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get Redeem History',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/user/addStory',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.addStory(request.payload, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
                else reply(UniversalFunctions.sendError(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR));
            },
            description: 'Add Story Route',
            auth: 'UserAuth',
            tags: ['api'],
            validate: {
                payload: {
                    media: Joi.array().description(" test desc").items(Joi.object().keys({
                        original: Joi.string().allow(""),
                        thumbnail: Joi.string().allow(""),
                        videoUrl: Joi.string().allow(""),
                        mediaType: Joi.string().required().valid([
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE
                        ])
                    })).optional()
                    // storyType: Joi.string().required().valid([Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                    // Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE]).description('for Video VIDEO', 'for Image IMAGE')
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'json',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/user/getStories',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.getStories(userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Get User Stories',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    // payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'DELETE',
        path: '/user/deleteStory',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                if (userData) {
                    Controller.UserController.deleteStory(request.query, userData).then(result => {
                        reply(UniversalFunctions.sendSuccess(null, result))
                    }).catch(reason => {
                        reply(UniversalFunctions.sendError(reason));
                    });
                }
            },
            description: 'Delete Story Api',
            auth: 'UserAuth',
            tags: ['api', 'user'],
            validate: {
                query: {
                    id: Joi.string().required(),
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
]