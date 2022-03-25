
let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Configs = require('../Configs');
let SchemaTypes = mongoose.Schema.Types;

let Tags = new Schema({
    tagName: {type: String, trim: true,sparse:true},
    imageUrl:{
        original:{type:String,default:""},
        thumbnail:{type:String,default:""},
    },
    postCount:{type:Number,default:0},
    isBlocked:{type:Boolean,default:false},
    isDeleted:{type:Boolean,default:false},
}, {timestamps: true});


module.exports = mongoose.model('Tags', Tags);




