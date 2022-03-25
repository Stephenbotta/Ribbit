
const Joi = require('joi');
const Controller = require('../Controllers');
const UniversalFunctions = require('../Utils/UniversalFunction');
const Config = require('../Configs');
const { joinGroup } = require('../Controllers/UserController');

module.exports = [
    {
        method: 'GET',
        path: '/admin/test',
        config: {
            handler: function (request, reply) {
                reply(UniversalFunctions.sendSuccess(null, 'test'))
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
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
        path: '/admin/adminLogin',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.adminLogin(request.payload).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            validate: {
                payload: {
                    email: Joi.string().email().trim().lowercase(),
                    password: Joi.string().trim()
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
        method: 'GET',
        path: '/admin/getUsers',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getUsers(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    userType: Joi.string().allow('', null).empty(['', null]).valid(['STUDENT', 'MENTOR']),
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    fullName: Joi.string().description("search on full name"),
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
        method: 'PUT',
        path: '/admin/updateUser/{userId}',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.updateUser(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    userId: Joi.string().required()
                },
                payload: {
                    isBlocked: Joi.boolean()
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
        path: '/admin/getPosts',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getPosts(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    postId: Joi.string(),
                    userId: Joi.string(),
                    groupId: Joi.string(),
                    interstId: Joi.string(),
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
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
        method: 'PUT',
        path: '/admin/updatePost/{postId}',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.updatePost(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    postId: Joi.string().required()
                },
                payload: {
                    isBlocked: Joi.boolean()
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
        path: '/admin/addEditInterst',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.addEditInterst(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    id: Joi.string(),
                    interstName: Joi.string(),
                    imageOriginal: Joi.string().allow("").allow(null).description('image Url'),
                    imageThumbnail: Joi.string().allow("").allow(null).description('image Url'),
                    isBlocked: Joi.boolean()
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
        path: '/admin/getIntersts',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getIntersts(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    interstName: Joi.string().description("search on interstName"),
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
        path: '/admin/getGroups',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getGroups(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    userId: Joi.string(),
                    groupId: Joi.string(),
                    groupName: Joi.string().description("search on groupName"),
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
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
        method: 'PUT',
        path: '/admin/updateGroups/{groupId}',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.updateGroups(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    groupId: Joi.string().required()
                },
                payload: {
                    isBlocked: Joi.boolean()
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
        path: '/admin/getCommentsOfPost/{postId}',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getCommentsOfPost(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    postId: Joi.string().required(),
                },
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
        method: 'POST',
        path: '/admin/dashboardData',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.dashboardData(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    startDate: Joi.string().optional(),
                    endDate: Joi.string().optional()
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
        path: '/admin/uploadFileToS3',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.uploadFileToS3(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            payload: {
                maxBytes: 200000000,
                parse: true,
                output: 'file',
                timeout: false
            },
            tags: ['admin', 'api'],
            // auth: 'AdminAuth',
            auth: false,
            validate: {
                payload: {
                    image: Joi.any()
                        .meta({ swaggerType: 'file' })
                        .optional()
                        .description('image file'),
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
        path: '/admin/addEditSurvey',
        config: {
            handler: function (request, reply) {
                Controller.SurveyController.addEditSurvey(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    surveyId: Joi.string().description("Survey id for update the document"),
                    name: Joi.string().description("name of the survey"),
                    //categoryIds:Joi.array()
                    description: Joi.string().description("description of the survey"),
                    expiryDate: Joi.number().description("expiryDate of the survey in timestamp"),
                    totalTime: Joi.number().description("totalTime duration for servery in mintes"),
                    rewardPoints: Joi.number().default(0).description("rewards points giving to user after complete servey"),
                    media: Joi.array().description(`{
                        "original" : "https://IMG_52591903-7533-448f-86be-764b65925bd6.jpg",
                        "thumbnail" : "https://IMG_52591903-7533-448f-86be-764b65925bd6.jpg",
                        "videoUrl" : "",
                        "mediaType" : "IMAGE" // VIDEO, IMAGE,TEXT,GIF
                    }`).items(Joi.object().keys({
                        original: Joi.string().allow(""),
                        thumbnail: Joi.string().allow(""),
                        videoUrl: Joi.string().allow(""),
                        mediaType: Joi.string().required().valid([
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF
                        ])
                    })).optional(),
                    categoryIds: Joi.array().items(Joi.string().required()).required().description(`["5d5150fc61f3a33f3da50b06] -interst category ids`)
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
        path: '/admin/getSurvey',
        config: {
            handler: function (request, reply) {
                Controller.SurveyController.getSurvey(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
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
        path: '/admin/getQuestions',
        config: {
            handler: function (request, reply) {
                Controller.QuestionController.getQuestions(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    surveyId: Joi.string().required().description("Survey id"),
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
        method: 'POST',
        path: '/admin/blockedSurvey',
        config: {
            handler: function (request, reply) {
                Controller.SurveyController.blockedSurvey(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'blocked or unblock survey from admin',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    surveyId: Joi.string().required().description("Survey id"),
                    action: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/deletedSurvey',
        config: {
            handler: function (request, reply) {
                Controller.SurveyController.deletedSurvey(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'deleted survey from admin',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    surveyId: Joi.string().required().description("Survey id"),
                    action: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/addEditQuestion',
        config: {
            handler: function (request, reply) {
                Controller.QuestionController.addEditQuestion(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    questionId: Joi.string().description("question id for update the document"),
                    surveyId: Joi.string().description("Survey id"),
                    name: Joi.string().description("name of the survey"),
                    questionType: Joi.number().required().valid([
                        Config.APP_CONSTANTS.DATABASE.QUESTION_TYPES.SINGLE_VALUE,
                        Config.APP_CONSTANTS.DATABASE.QUESTION_TYPES.MULTI_VALUE,
                    ]).default(Config.APP_CONSTANTS.DATABASE.QUESTION_TYPES.SINGLE_VALUE).description("questionType must be single 1 or multi 2"),
                    options: Joi.array().items(Joi.object().keys({
                        name: Joi.string().allow("")
                    })).description(`[{
                        "name" : "option 1"
                    }]`).optional(),
                    media: Joi.object().description(`{
                        "original" : "https://IMG_52591903-7533-448f-86be-764b65925bd6.jpg",
                        "thumbnail" : "https://IMG_52591903-7533-448f-86be-764b65925bd6.jpg",
                        "videoUrl" : "",
                        "mediaType" : "IMAGE" // VIDEO, IMAGE,TEXT,GIF
                    }`).keys({
                        original: Joi.string().allow(""),
                        thumbnail: Joi.string().allow(""),
                        videoUrl: Joi.string().allow(""),
                        mediaType: Joi.string().required().valid([
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.VIDEO,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.IMAGE,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.TEXT,
                            Config.APP_CONSTANTS.DATABASE.MEDIA_TYPE.GIF
                        ])
                    }).optional(),
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
        path: '/admin/blockedQuestion',
        config: {
            handler: function (request, reply) {
                Controller.QuestionController.blockedQuestion(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'blocked or unblock Question from admin',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    questionId: Joi.string().required().description("Question id"),
                    action: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/deletedQuestion',
        config: {
            handler: function (request, reply) {
                Controller.QuestionController.deletedQuestion(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'deleted Question from admin',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    questionId: Joi.string().required().description("Question id"),
                    action: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/pointStructure/getList',
        config: {
            handler: function (request, reply) {
                Controller.PointStructures.getList(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get the list of all points structures',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
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
        method: 'POST',
        path: '/admin/pointStructure/add',
        config: {
            handler: function (request, reply) {
                Controller.PointStructures.add(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Add new point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    name: Joi.string().required().description("name"),
                    description: Joi.string().description("description"),
                    rewardPoint: Joi.number().required().description("rewardPoint"),
                    parentId: Joi.string().description("parentId for child nodes"),
                    pointType: Joi.string().valid([
                        Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SURVEY,
                        Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.CHALLENGE,
                        Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SIGNUP_REWARDS,
                        Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.GAMIFICATION,
                        Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.THIRD_PARTY,
                        ''
                    ]).description("pointType should be from never change"),
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
        method: 'PUT',
        path: '/admin/pointStructure/edit/{id}',
        config: {
            handler: function (request, reply) {
                Controller.PointStructures.edit(request).then(result => {
                    reply.redirect('/admin/pointStructure/getList').code(303);

                    // reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Edit point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    id: Joi.string().required().description("id for nodes"),
                },
                payload: {
                    // name: Joi.string().required().description("name"),
                    description: Joi.string().description("description"),
                    rewardPoint: Joi.number().required().description("rewardPoint"),
                    quantity: Joi.number().required().description("set quantity in case of challanges for sms or survey"),
                    // id: Joi.string().required().description("id for nodes"),
                    // parentId: Joi.string().description("parentId for child nodes"),
                    // pointType: Joi.string().valid([
                    //     Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SURVEY,
                    //     Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.CHALLENGE,
                    //     Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.SIGNUP_REWARDS,
                    //     Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.GAMIFICATION,
                    //     Config.APP_CONSTANTS.DATABASE.POINT_STRUCTURE.THIRD_PARTY,
                    //     ''
                    // ]).description("pointType should be from never change"),
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
        path: '/admin/challenges',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;

                Controller.Challenge.readChallenge("admin", request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result));
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Challenge list',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    search: Joi.string()
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
        path: '/admin/editChallenges',
        config: {
            handler: function (request, reply) {

                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;

                Controller.Challenge.editChallenge(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Edit point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                // params: {
                //     id: Joi.string().required().description("id for nodes"),
                // },
                payload: {
                    challengeId: Joi.string().required().description("challengeId"),
                    challengeType: Joi.string().valid(...Object.values(Config.APP_CONSTANTS.challengeType)).required().description("challenge Type"),
                    quantity: Joi.number().required().min(1).description("set quantity in case of challanges for sms or survey"),
                    title: Joi.string().required().description("title"),
                    description: Joi.string().required().description("description"),
                    rewardPoint: Joi.number().min(1).required().description("rewardPoint"),
                    imageUrl: {
                        original: Joi.string(),
                        thumbnail: Joi.string(),
                    },
                    startDate: Joi.string().required().description("eg 2019-02-19 (YYYY-MM-DD)"),
                    endDate: Joi.string().required().description("eg 2019-02-19 (YYYY-MM-DD)"),
                    // isBlocked: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/challengesBlock',
        config: {
            handler: function (request, reply) {

                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;

                Controller.Challenge.editChallenge(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Edit point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                // params: {
                //     id: Joi.string().required().description("id for nodes"),
                // },
                payload: {
                    challengeId: Joi.string().required().description("challengeId"),
                    isBlocked: Joi.boolean().description("action should be true or false"),
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
        path: '/admin/challenges',
        config: {
            handler: function (request, reply) {

                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;

                Controller.Challenge.createChallenge(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Edit point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                // params: {
                //     id: Joi.string().required().description("id for nodes"),
                // },
                payload: {
                    challengeType: Joi.string().valid(...Object.values(Config.APP_CONSTANTS.challengeType)).required().description("challenge Type"),
                    quantity: Joi.number().required().min(1).description("set quantity in case of challanges for sms or survey"),
                    title: Joi.string().required().description("title"),
                    description: Joi.string().required().description("description"),
                    rewardPoint: Joi.number().min(1).required().description("rewardPoint"),
                    imageUrl: {
                        original: Joi.string(),
                        thumbnail: Joi.string(),
                    },
                    startDate: Joi.string().required().description("eg 2019-02-19 (YYYY-MM-DD)"),
                    endDate: Joi.string().required().description("eg 2019-02-19 (YYYY-MM-DD)")
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
        method: 'DELETE',
        path: '/admin/challenges/{id}',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.Challenge.deleteChallenge(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Delete challenge',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
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
        path: '/admin/userContact',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.userContact(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result));
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'User Contact list',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    search: Joi.string()
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
        path: '/admin/addSpinWheel',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.addSpinWheel(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    prize: Joi.array().items(Joi.object().keys({
                        value: Joi.string(),
                        color: Joi.string()
                    }))
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    //payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'POST',
        path: '/admin/addEditCharityOrgList',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.addEditCharityOrgList(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    organizationId: Joi.string().optional(),
                    organizationName: Joi.string().required(),
                    organizationLink: Joi.string().required()
                },
                headers: UniversalFunctions.authorizationHeaderObj,
                failAction: UniversalFunctions.failActionFunction
            },
            plugins: {
                'hapi-swagger': {
                    //payloadType: 'form',
                    responses: Config.APP_CONSTANTS.swaggerDefaultResponseMessages
                }
            }
        }
    },
    {
        method: 'GET',
        path: '/admin/showCharityDonationList',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.showCharityDonationList(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result));
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'User Contact list',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    search: Joi.string()
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
        path: '/admin/showSpinWheels',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.showSpinWheelList(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get SpinWheels',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
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
        path: '/admin/charityOrgList',
        config: {                         //showCharityOrgList
            handler: function (request, reply) {
                console.log(request);
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.showCharityOrgList(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get Charity Organisations List',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    organizationName: Joi.string()
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
        method: 'PUT',
        path: '/admin/updateSpinWheel',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.updateSpinWheel(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Edit point structure',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    spinId: Joi.string().required().description("id for spinWheel"),
                    isActive: Joi.boolean().required()
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
        method: 'DELETE',
        path: '/admin/spinWheel/{id}',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.deleteSpinWheel(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Delete SpinWheel',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    id: Joi.string().required().description("SpinWheel id"),
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
        method: 'DELETE',
        path: '/admin/charityOrg/{id}',
        config: {
            handler: function (request, reply) {
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.deleteOrganization(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Delete Organisation',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                params: {
                    id: Joi.string().required().description("Organisation id"),
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
        path: '/admin/redeemGiftCardsList',
        config: {                         //showCharityOrgList
            handler: function (request, reply) {
                console.log(request);
                let userData = request.auth && request.auth.credentials && request.auth.credentials.userData;
                Controller.AdminController.showRedeemCardsList(request, userData).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get Redeem Cards List',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                query: {
                    pageNo: Joi.number().description("by default 1"),
                    limit: Joi.number().description("by default 10"),
                    search: Joi.string()
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
        path: '/admin/addDailyChallenge',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.addDailyChallenge(request).then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Add Daily Challange',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
            validate: {
                payload: {
                    title: Joi.string().required(),
                    description: Joi.string().required(),
                    challengeType: Joi.string()
                        .valid([Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.SMS_CHALLENGE,
                        Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.STORY_CHALLENGE,
                        Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.POST_CHALLENGE]).
                        default(Config.APP_CONSTANTS.DATABASE.DAILY_CHALLENGES.SMS_CHALLENGE),
                    isActive: Joi.boolean().optional(),
                    rewardPoints: Joi.number().required()
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
        path: '/admin/dailyChallenge',
        config: {
            handler: function (request, reply) {
                Controller.AdminController.getDailyChallenges().then(result => {
                    reply(UniversalFunctions.sendSuccess(null, result))
                }).catch(reason => {
                    reply(UniversalFunctions.sendError(reason));
                });
            },
            description: 'Get Daily Challange',
            tags: ['admin', 'api'],
            auth: 'AdminAuth',
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
    }
]
 

