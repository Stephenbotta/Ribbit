
const Inert = require('inert');
const Vision = require('vision');
const HapiSwagger = require('hapi-swagger');
const Pack = require('../package');
let Configs = require('../Configs');


exports.register = function(server,option,next){
    server.register([
        Inert,
        Vision,
        {
            'register': HapiSwagger,
            'options': {
                info: {
                    'title':Configs.APP_CONSTANTS.SERVER.APP_NAME,
                    'version': Pack.version,
                }
            }
        }],function(err){
        if (err) {
            server.log(['error'], 'hapi-swagger load error: ' + err)
        }else{
            // console.log('hapi-swagger interface loaded')
            server.log(['start'], 'hapi-swagger interface loaded')
        }
    });
    next()
};

exports.register.attributes = {
    name: 'swagger-plugin'
};