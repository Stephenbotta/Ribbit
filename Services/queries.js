
let saveData = (model, dataToSave) => {
    return new Promise((resolve, reject)=>{
        new model(dataToSave).save( (err,result)=>{
            if(err){
                console.log(err)
                return reject(err);
            } 
            resolve(result);
        })
    })
}

let getData = (model, query, projection, options) => {
    return new Promise((resolve, reject)=>{
        model.find(query, projection, options, (err, data)=> {
            if (err) return reject(err);
            else return resolve(data);
        });
    })    
};

let removeData = (model, query) => {
    return new Promise((resolve, reject)=>{
        model.remove(query, (err, data)=> {
            if (err) return reject(err);
            else return resolve(data);
        });
    })    
};

let findAndUpdate = (model, conditions, update, options) => {
    return new Promise((resolve, reject)=>{
        model.findOneAndUpdate(conditions, update, options, function (error, result) {
            if (error) {
                return reject(error);
            }
            return resolve(result);
        })
    })   
};

let populateData = function (model, query, projection, options, collectionOptions) {
    return new Promise((resolve, reject)=>{
        model.find(query, projection, options).populate(collectionOptions).exec(function (err, data) {
            if (err) reject(err);
            resolve(data);
        });
    })    
};

let populateTheSearchData = (model, data, populate)=>{
    return new Promise((resolve, reject)=>{
        model.populate(data, populate, function (err, populatedDocs) {
            if (err) reject(err);
            resolve(populatedDocs);// This object should now be populated accordingly.
        });
    })    
}

let checkObjectId = function(model, Id){
    return new Promise((resolve, reject)=>{
        model.find({_id: Id}, {}, {}, (err, res)=>{
            if(err){
                reject(false)
            }else{
                resolve(res)
            }
        })
    })
}

let checkMessageObjectId = function(model, criteria){
    return new Promise((resolve, reject)=>{
        model.find(criteria, {}, {}, (err, res)=>{
            if(err){
                reject(false)
            }else{
                resolve(res)
            }
        })
    })
}

let aggregateDataWithPopulate = function (model, group, populateOptions) {
    return new Promise((resolve, reject)=>{
        model.aggregate(group, (err, data) => {
            if (err) {
                reject(err);
            }
            model.populate(data, populateOptions, function (err, populatedDocs) {
                    if (err) reject(err);
                    resolve(populatedDocs);// This object should now be populated accordingly.
                });
        });
    })    
};


let findOne = function (model, query, projection, options) {
    return new Promise((resolve, reject)=>{
        model.findOne(query, projection, options, function (err, data) {
            if (err) return reject(err);
            return resolve(data);
        });
    })    
};

let update = function (model, conditions, update, options) {
    return new Promise((resolve, reject)=>{
        model.update(conditions, update, options, function (err, result) {
            if (err) {
                reject(err);
            }
             resolve(result);
    
        });
    })    
};


let count = function (model, condition) {

    return new Promise((resolve, reject)=>{

        model.count(condition, function (error, count) {
            if (error) reject (error);
            resolve(count);
        })
    })
}


module.exports = {
    saveData,
    getData,
    findAndUpdate,
    populateData,
    checkObjectId,
    checkMessageObjectId,
    aggregateDataWithPopulate,
    findOne,
    populateTheSearchData,
    count,
    update,
    removeData
}