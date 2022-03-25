const OpenTok = require('opentok');
const opentok = new OpenTok("46524272", "a6ffaaa6eb3e23d9253a576f37fdd9588502fff4");

function NN_createSession(){

    return new Promise((resolve, reject)=>{

    

opentok.createSession(function(err, session) {
    if (err){ 
        reject(err);
    }
    // save the sessionId
    resolve({session: session.sessionId, token:session.generateToken()});
  });
});
}

function NN_createTokenFromSession(sessionId){

    // return new Promise((resolve, reject)=>{
        
        const a = opentok.generateToken(sessionId);
        console.log(a);
        return a;

    

// opentok.createTokenFromSession(function(err, session) {
//     if (err){ 
//         reject(err);
//     }
//     // save the sessionId
//     resolve({session: session.sessionId, token:session.generateToken()});
//   });
// });
}


exports.NN_createSession = NN_createSession;
exports.NN_createTokenFromSession = NN_createTokenFromSession;