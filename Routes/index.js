
'use strict';

const userRoute = require('./userRoutes');

const adminRoute = require('./adminRoutes');

const all = [].concat(userRoute, adminRoute);

module.exports = all;