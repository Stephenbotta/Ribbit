'use strict';
/**
 * @description aws bucket info
 */

let s3BucketCredentials = {
    // "bucket": "pulses3bucket",
    // "accessKeyId": "AKIA5FUYRS6WXXNAYHV2",
    // "secretAccessKey": "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",
    // "s3URL": "https://s3-us-west-2.amazonaws.com/pulses3bucket/",
    // "bucket": "checkits3bucket",
    // "accessKeyId": "AKIAVLEAN3T7BDUEIKIU",
    // "secretAccessKey": "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",
    // "s3URL": "https://s3-us-west-2.amazonaws.com/checkits3bucket/"  
    "bucket": "ribbitrewardsdev",
    "accessKeyId": /*"AKIA4623DYXVNOS534N2"*/process.env.AWS_ACCESS_KEY,
    "secretAccessKey": /*"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"*/process.env.AWS_SECRET_KEY,
    "s3URL": "https://ribbitrewardsdev.s3.us-west-2.amazonaws.com/"    
};

/**
 * @description make json available globally
 */
module.exports = {
    s3BucketCredentials: s3BucketCredentials
};




// private const val BUCKET_NAME = "ribbitrewardsdev"
// private const val IDENTITY_POOL_ID = "us-west-2:fd850246-9ff4-4fe1-bfec-d7019fb19ca0"
// private val REGION = Regions.US_WEST_2

