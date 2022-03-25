let Config = require('../Configs');
var UniversalFunctions = require('../Utils/UniversalFunction');
var async = require('async');
var Path = require('path');
var fsExtra = require('fs-extra');
var Fs = require('fs');
var AWS = require("aws-sdk");
var mime = require('mime-types')
const getVideoInfo = require('get-video-info');
const sharp = require('sharp');
const Thumbler = require('thumbler');

AWS.config.update({
    accessKeyId: Config.awsS3Config.s3BucketCredentials.accessKeyId,
    secretAccessKey: Config.awsS3Config.s3BucketCredentials.secretAccessKey
    //  region:' '
});
var s3 = new AWS.S3();

function uploadMultipart(fileInfo, uploadCb) {
    var options = {
        Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
        Key: fileInfo.filename,
        ACL: 'public-read',
        ContentType: mime.lookup(fileInfo.filename),
        // ServerSideEncryption: 'AES256'
    };

    s3.createMultipartUpload(options, (mpErr, multipart) => {
        if (!mpErr) {
            //console.log("multipart created", multipart.UploadId);
            Fs.readFile(fileInfo.path, (err, fileData) => {

                var partSize = 5242880;
                var parts = Math.ceil(fileData.length / partSize);

                async.times(parts, (partNum, next) => {

                    var rangeStart = partNum * partSize;
                    var end = Math.min(rangeStart + partSize, fileData.length);

                    console.log("uploading ", fileInfo.filename, " % ", (partNum / parts).toFixed(2));

                    partNum++;
                    async.retry((retryCb) => {
                        s3.uploadPart({
                            Body: fileData.slice(rangeStart, end),
                            Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
                            Key: fileInfo.filename,
                            PartNumber: partNum,
                            UploadId: multipart.UploadId
                        }, (err, mData) => {
                            retryCb(err, mData);
                        });
                    }, (err, data) => {
                        console.log(data);
                        next(err, { ETag: data.ETag, PartNumber: partNum });
                    });

                }, (err, dataPacks) => {
                    s3.completeMultipartUpload({
                        Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
                        Key: fileInfo.filename,
                        MultipartUpload: {
                            Parts: dataPacks
                        },
                        UploadId: multipart.UploadId
                    }, uploadCb);
                });
            });
        } else {
            uploadCb(mpErr);
        }
    });
}

function uploadFile1(fileObj, uploadCb) {

    var fileName = Path.basename(fileObj.finalUrl);

    var stats = Fs.statSync(fileObj.path);
    console.log("fileName", fileName)
    var fileSizeInBytes = stats["size"];

    async.retry((retryCb) => {
        Fs.readFile(fileObj.path, (err, fileData) => {
            s3.putObject({
                Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
                Key: fileName,
                Body: fileData,
                ContentType: mime.lookup(fileName)
            }, retryCb);
        });
    }, uploadCb);

}






function uploadFile2(buffer, fileName, uploadCb) {
    console.log("buffer, fileName", buffer, fileName)

    async.retry((retryCb) => {
        s3.putObject({
            Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
            Key: fileName,
            Body: buffer,
            ContentType: mime.lookup(fileName)
        }, retryCb);

    }, uploadCb);
}


var uploadFilesOnS3 = function (fileData, callback) {

    var imageFile = false;
    var pdfFile = false;
    var filename;
    var ext;
    var dataToUpload = []

    console.log(fileData);

    if (!fileData || !fileData.filename) {
        return callback(Config.APP_CONSTANTS.STATUS_MSG.ERROR.IMP_ERROR)
    } else {
        filename = fileData.filename.toString();
        console.log(filename)
        ext = filename.substr(filename.lastIndexOf('.'))
        var videosFilesExt = ['.3gp', '.3GP', '.mp4', '.MP4', '.avi', '.AVI', '.WEBM', '.webm'];
        var imageFilesExt = ['.jpg', '.JPG', '.jpeg', '.JPEG', '.png', '.PNG', '.gif', '.GIF'];
        var pdfFileExt = ['.pdf', '.PDF'];
        if (ext) {
            if (imageFilesExt.indexOf(ext) >= 0) {
                imageFile = true
            } else if (pdfFileExt.indexOf(ext) >= 0) {
                pdfFile = true;
            }
            else {
                if (!(videosFilesExt.indexOf(ext) >= 0)) {
                    return callback()
                }
            }
        } else {
            return callback()
        }
    }

    //  create file names ==============

    fileData.original = UniversalFunctions.getFileNameWithUserId(false, filename);
    console.log("fileData.original", fileData.original);
    fileData.thumb = UniversalFunctions.getFileNameWithUserId(true, imageFile && filename || (filename.substr(0, filename.lastIndexOf('.'))) + '.jpg');
    console.log(" fileData.thumb", fileData.thumb);


    // for set parrallel uploads on s3 bucket

    dataToUpload.push({
        path: Path.resolve('.') + '/uploads/' + fileData.thumb,
        finalUrl: Config.awsS3Config.s3BucketCredentials.s3URL + fileData.thumb,
    })

    dataToUpload.push({
        path: fileData.path,
        finalUrl: Config.awsS3Config.s3BucketCredentials.s3URL + fileData.original
    })


    async.auto({
        uploadPdfFile: (cb) => {
            if (pdfFile) {
                let buffer = Fs.readFileSync(fileData.path);

                uploadFile2(buffer, fileData.original);

                let responseObject = {
                    original: Config.awsS3Config.s3BucketCredentials.s3URL + fileData.original
                };
                callback(err, responseObject)
            }
            else {
                cb()
            }

        },
        checkVideoDuration: function (cb) {
            console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", imageFile);
            if (!imageFile) {
                getVideoInfo(fileData.path).then(info => {
                    if (info.format.duration < 10) {
                        cb()
                    } else {
                        cb()
                    }
                })
            } else {
                cb()
            }
        },
        creatingThumb: ['checkVideoDuration', function (err, cb) {
            if (imageFile) {
                console.log('=======  IMAGE ===============', Path.resolve('.') + '/uploads/' + fileData.thumb);
                createThumbnailImage(fileData.path, Path.resolve('.') + '/uploads/' + fileData.thumb, function (err, data) {
                    // console.log("fileData.path", data)
                    cb(null, data)
                })
                //cb()
            } else {
                // cb(null)
                createVideoThumb(fileData, Path.resolve('.') + '/uploads/' + fileData.thumb, function (err) {
                    if (err) cb(err)
                    else {
                        cb(null)
                    }
                })
            }
        }],
        uploadOnS3: ['creatingThumb', function (err, cb) {
            console.log(err)
            console.log("functionsArray")
            var functionsArray = [];



            if (err && err.creatingThumb) {
                if (err && err.creatingThumb && err.creatingThumb.original) {
                    uploadFile2(err.creatingThumb.original, fileData.original);
                }

                if (err && err.creatingThumb && err.creatingThumb.thumbnail) {
                    uploadFile2(err.creatingThumb.thumbnail, fileData.thumb)
                }
                cb(null)
            }
            else {
                console.log("Video case")
                // dataToUpload.forEach(function (obj) {
                //     functionsArray.push((function (data) {
                //         return function (internalCb) {
                //             uploadFile1(data,)
                //         }
                //     })(obj))
                // });
                for (let obj of dataToUpload) {
                    uploadFile1(obj,)
                }
                cb(null)
            }


            // async.parallel(functionsArray, (err, result) => {
            //     console.log("result", result)
            //     deleteFile(Path.resolve('.') + '/uploads/' + fileData.thumb);
            //     if(err){
            //         cb(err)
            //     }
            //     else{
            //         cb(null)
            //     }
            // })

        }]
    }, function (err) {
        let responseObject = {
            original: Config.awsS3Config.s3BucketCredentials.s3URL + fileData.original,
            thumbnail: Config.awsS3Config.s3BucketCredentials.s3URL + fileData.thumb,
            //  type: imageFile && 'IMAGE' || 'VIDEO'
        };
        console.log('response', responseObject);
        callback(err, responseObject);
    })
};


function deleteFile(path) {
    fsExtra.remove(path, function (err) {
        console.log('error deleting file>>', err)
    });
}
function deleteFromS3(fileName, callback) {
    let key = fileName.split('/');
    s3.deleteObject({
        Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
        Key: key[4],
    }, function (err, data) {
        //console.log('333333333333',err,data,key[4]);
        if (err) callback(err);
        else callback()
    });
}
/*
function createThumbnailImage(originalPath, thumbnailPath, callback) {
    var gm = require('gm').subClass({imageMagick: true});
    gm(originalPath)
        .resize(Config.APP_CONSTANTS.SERVER.THUMB_WIDTH, Config.APP_CONSTANTS.SERVER.THUMB_HEIGHT, "!")
        .autoOrient()
        .write(thumbnailPath, function (err, data) {
            callback(err)
        })
};*/

// function createThumbnailImage(originalPath, thumbnailPath, callback) {
//     let gm = require('gm').subClass({ imageMagick: true });

//     console.log("aaaaaaaaaaaaaaaaa")
//     gm(originalPath)
//         // .define()
//         .resize(200, 200, "!")
//         .autoOrient()
//         .write(thumbnailPath, function (err, data) {
//             console.log("tumbnail path---", thumbnailPath, data);
//             callback(null);
//         });
// }

function createThumbnailImage(originalPath, thumbnailPath, cb) {
    //var gm = require('gm').subClass({ imageMagick: true });

    var readStream = Fs.readFileSync(originalPath);
    console.log("readStream", readStream);
    sharpFunction(readStream, thumbnailPath).then(data => {
        cb(null, data)
    }).catch(err => {
        console.log(err)
        cb(err)
    })

    //let ratio;
    // gm(readStream)
    //     .resize(400, 400).stream('png', function (err, stdout, stderr) {
    //         if (err) {
    //             callback(err)
    // }
    //             else {
    //     console.log("thumbnailPath", thumbnailPath)
    //     var writeStream = Fs.createWriteStream(thumbnailPath);
    //     stdout.pipe(writeStream);
    //     callback(null)
    // }
    //         });
    // .resize(400, 400).write(thumbnailPath, function (err) {
    //     if (!err) callback(null);
    //     else {
    //         callback(err)
    //     }
    //   }); 
    // {
    //     console.log("sixw***********", size);
    //     console.log(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", err)
    //     // if (size) {
    //     this.thumb(size.width, size.height, thumbnailPath, 20,
    //         /* .autoOrient()
    //          .write(thumbnailPath1,*/ function (err, data) {
    //             if (err)
    //                 callback(err)
    //             else callback()
    //         })
    // }
    /*  if (size.width > 200) {
          this.resize(200, (size.height / size.width) * 200)
      } else {
          this.resize(100, (size.height / size.width) * 100)
      }*/

    // });
}

let sharpFunction = async (readStream, thumbnailPath) => {
    return new Promise((resolve, reject) => {
        sharp(readStream)
            .resize(400)
            // .min()
            .toFormat('png', 50)
            .toBuffer()
            .then(data => {
                console.log("data", data)
                // var writeStream = Fs.createWriteStream(thumbnailPath);
                // // writeStream.pipe(data);
                // writeStream.write(data)
                resolve({ original: readStream, thumbnail: data })
            })
            .catch(err => {
                reject(err)
            })
    })

}

function createVideoThumb(fileData, thumbnailPath, callback) {
    console.log("createVideoThumb, thumbnailPath", fileData, thumbnailPath)
    Thumbler({
        type: 'video',
        input: fileData.path,
        output: thumbnailPath,
        time: '00:00:03',
        // size: '300x200' // this optional if null will use the desimention of the video
    }, function (err) {
        callback(err)
    });

}

const uploadRandomFile = (fileData) => {
    return new Promise((resolve, reject) => {
        Fs.readFile(fileData.path, async (err, data) => {
            let params = {
                Bucket: Config.awsS3Config.s3BucketCredentials.bucket,
                Key: Date.now() + fileData.filename,
                Body: data
            };
            try {
                let awsUpload = await new AWS.S3().putObject(params).promise();
                if (awsUpload) {
                    console.log(Config.awsS3Config.s3BucketCredentials.s3URL + params.Key)
                    resolve(Config.awsS3Config.s3BucketCredentials.s3URL + params.Key)
                }
            } catch (e) {
                console.log("Error uploading data: ", e);
            }
        })
    })

};

module.exports = {
    uploadFilesOnS3: uploadFilesOnS3,
    deleteFromS3: deleteFromS3,
    uploadRandomFile: uploadRandomFile
}