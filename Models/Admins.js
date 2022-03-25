
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
// let Configs = require('../Configs');

// let likes={
//     userId:{type:Schema.ObjectId,ref:"Users"},
//     time:{type:Number,default:0},
// };

let Admins = new Schema({
    email :{type: String},
    password : {type: String,sparse:true},
    name :{type: String,default:''},
    superAdmin :{type: Boolean,default:false},
    isDeleted:{type:Boolean,default:false},
    androidAppLink:{type:String,default:"" },
    iosAppLink:{type:String, default:""},
    accessToken:{type:String,trim:true,sparse:true},
}, {timestamps: true});

module.exports = mongoose.model('Admins', Admins);
