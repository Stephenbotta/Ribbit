const Hapi = require('hapi');
const Configs = require('./Configs');
const Plugins = require('./Plugins');
const Routes = require('./Routes');
const bootStrap = require('./Utils/bootStrap');
const socketManager = require('./Libs/SocketManager')
let cron = require('./CronFile');
const privacyPolicy = require('./Utils/privacyPolicy');
let cronJob = require('cron').CronJob;
const { CronJob } = require('cron');

let job = new CronJob('50 59 23 * * *', async () => {
    await cron.dailyChallenge();
}, null, true, 'Asia/Kolkata');

job.start();

cron.autoDeletePosts.start();



// console.log('autoDeletePosts is running',cron.autoDeletePosts.running);

//Create Server
const server = new Hapi.Server();

//create connection
server.connection({
    port: Configs.dbConfig.config.PORT,
    routes: { cors: true }
});

server.route([
    {
        method: 'GET',
        path: '/',
        handler: (req, res) => {
            res.file('./p1.html');
            // res('Hi')
        }
    },
    {
        method: 'GET',
        path: '/contactUs',
        handler: function (req, res) {
            privacyPolicy.privacyPolicy((err, result) => {
                res(result)
            });
        }
    },
    {
        method: 'GET',
        path: '/termsandcondition',
        handler: function (req, res) {
            privacyPolicy.privacyPolicy((err, result) => {
                res(result)
            });
        }
    }
]);

bootStrap.bootstrapAdmin();

//Register All Plugins
server.register(Plugins, (err) => {
    if (err) {
        server.error('Error while loading plugins : ' + err)
    } else {
        server.route(Routes);
        server.log('info', 'Plugins Loaded');
    }
});

server.on('response', (request) => {
    console.log(request.info.remoteAddress + ': ' + request.method.toUpperCase() +
        ' ' + request.url.path + ' --> ' + request.response.statusCode);
    console.log('Request payload:', JSON.stringify(request.payload));
});

socketManager.connectSocket(server)

//Start Server
server.start((err, result) => {
    if (err) console.log(err);
    else console.log('Server running at: ' + server.info.uri);
});
