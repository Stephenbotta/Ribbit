/**
 * @description database configuration
 */

'use strict';

if (process.env.NODE_ENV === 'dev') {
    exports.config = {
        PORT: 8005,
        dbURI: /*'mongodb://ribbitnodejsuser:aduevapabhdFGF6jdxsUkra@localhost/ribbitDev'*/process.env.MONGO_DB_URL_DEV
        // dbURI : 'mongodb://localhost:27017/conversifyDev'
    }
} else if (process.env.NODE_ENV === 'live') {
    exports.config = {
        PORT: 8007,
        dbURI: /*'mongodb://conversify:cKFpCAuZa6D5QXkh@52.25.110.218/conversifyLive'*/process.env.MONGO_DB_URL_LIVE
    }
} else if (process.env.NODE_ENV === 'local') {
    exports.config = {
        PORT: 8006,
        // dbURI: 'mongodb://ribbitnodejsuser:aduevapabhdFGF6jdxsUkra@100.21.168.56/ribbitDev'
        // dbURI: 'mongodb://ribbitnodejsuser:aduevapabhdFGF6jdxsUkra@localhost/ribbitDev'
        dbURI: /*'mongodb://localhost/ribbitDev'*/process.env.MONGO_DB_URL_LOCAL
    }
}
else {
    exports.config = {
        PORT: 8005,
        //dbURI : 'mongodb://localhost:27017/check_it',
        // dbURI: 'mongodb://ribbitnodejsuser:aduevapabhdFGF6jdxsUkra@100.21.168.56/ribbitDev'
        dbURI: /*'mongodb://localhost/ribbitDev'*/process.env.MONGO_DB_URL_LOCAL

    };
}