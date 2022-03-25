let config = require('../Configs');
let aws = require('../Configs/awsS3Config');
let nodemailer = require('nodemailer');
let sesTransport = require('nodemailer-ses-transport');
const smtpTransport = require('nodemailer-smtp-transport')



// exports.sendEmail = function (email, subject, content) {
//     let transporter = nodemailer.createTransport(sesTransport({
//         accessKeyId: aws.s3BucketCredentials.accessKeyId,
//         secretAccessKey: aws.s3BucketCredentials.secretAccessKey,
//         region: 'us-west-2'
//     }));

//     return new Promise((resolve, reject) => {
//         transporter.sendMail({
//             from: "RibbIt<info@ribbit.com>", // sender address
//             to: email, // list of receivers
//             subject: subject, // Subject line
//             html: content
//         }, (err, res) => {
//             console.log('send mail', err, res);
//             resolve()
//         });
//     })

// };


exports.sendEmail = function (email, subject, content) {


    let transporter = nodemailer.createTransport(smtpTransport({
        service: "Gmail",
        secure: true,
        auth: {
            user: config.emailConfig.emailCredentials.username,
            pass: config.emailConfig.emailCredentials.password
        }
    }));

    return new Promise((resolve, reject) => {
        transporter.sendMail({
            from: "Ribbit<notification@code-brew.com>", // sender address
            to: email, // list of receivers
            subject: subject, // Subject line
            html: content,
            priority: 'high'
        }, (err, res) => {
            console.log('send mail', err, res);
            resolve(res)
        });
    })
};