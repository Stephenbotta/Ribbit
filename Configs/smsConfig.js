var twilioCredentials = {
    accountSid : process.env.twilio_account_Sid,
    authToken : /*'7efae19f6a8d09e80446123e6a8dda91'*/process.env.TWILIO_AUTH_TOKEN,
    smsFromNumber : '+15045094820'
};

module.exports = {
    twilioCredentials: twilioCredentials
};
