let Config = require('../Configs');

let header = `<html>
<head>
   <meta charset="utf-8">
   <meta name="viewport" content="width=device-width">
   <meta http-equiv="X-UA-Compatible" content="IE=edge">
   <meta name="x-apple-disable-message-reformatting">
   <title></title>
   <style>html, body{margin: 0 auto !important; letter-spacing: 0.5px; padding: 0 !important; height: 100% !important; width: 100% !important;}*{-ms-text-size-adjust: 100%; -webkit-text-size-adjust: 100%;}div[style*="margin: 16px 0"]{margin: 0 !important;}table, td{mso-table-lspace: 0pt !important; mso-table-rspace: 0pt !important;}table{border-spacing: 0 !important; border-collapse: collapse !important; table-layout: fixed !important; margin: 0 auto !important;}table table table{table-layout: auto;}img{-ms-interpolation-mode: bicubic;}[x-apple-data-detectors], .x-gmail-data-detectors, .x-gmail-data-detectors *, .aBn{border-bottom: 0 !important; cursor: default !important; color: inherit !important; text-decoration: none !important; font-size: inherit !important; font-family: inherit !important; font-weight: inherit !important; line-height: inherit !important;}.a6S{display: none !important; opacity: 0.01 !important;}img.g-img div{display: none !important;}.button-link{text-decoration: none !important;}@media only screen and (min-device-width: 375px) and (max-device-width: 413px){/ iPhone 6 and 6 / .email-container{min-width: 375px !important;}}</style>
</head>
<body width="100%" bgcolor="#222222" style="margin: 0; mso-line-height-rule: exactly;">
   <center style="width: 100%; background: #EDF2F7; text-align: left;">
   <div style="max-width: 600px; margin: auto;" class="email-container">
   <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center" width="100%" style="max-width: 600px; box-shadow: 0px 5px 30px rgba(29, 43, 56, 0.21);">
   <tbody>
      <tr>
         <td bgcolor="#ffffff">
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%">
   <tbody>
      <tr>
         <td style="font-size: 17px; line-height: 20px; color: #555555;text-align: center;padding-bottom: 50px;">
            <div style="width: 100%;text-align: center;background: #0072ff;padding: 50px 0px;margin-bottom: 50px;">
               <h3 style="color: #ffff;font-size: 44px;">${Config.APP_CONSTANTS.SERVER.APP_NAME}</h3>
            </div>`;


let footer = `<hr style="width: 500px;margin: 0 auto;max-width: 90%;height: 2px;background: #ddd;border: 1px solid #ddd;margin-bottom: 50px !important;">
<div style="width: 550px;margin: 0 auto;max-width: 90%;">
   <div style="float:right;width:60%;text-align: right">
      <p style="color: #999;font-size: 17px;line-height: 26px;letter-spacing: 0.5px;">Team ${Config.APP_CONSTANTS.SERVER.APP_NAME}<br>info@ribbit.com<br></p>
      <p> <a href="#">Privacy Policy </a> | <a href="#">Terms of Services</a> </p>
   </div>
</div>
</tr></tbody> </table> </div></center></body></html>`;


let registrationEmail = async (data, url) => {
    let finaltemp = `
    ${header}

        <p style="margin: 0;font-size: 12px;line-height: 14px"><span
        style="font-size: 20px; line-height: 24px;">Dear ${data.fullName} </span> <br><br><span
        style="font-size: 20px; line-height: 24px;">We received a request to reset your password
        for your ${Config.APP_CONSTANTS.SERVER.APP_NAME} App account.</span>
        </p>
        <a
        style=" background-color: #0db82d;color: #fff;padding: 15px 0px;border: none;border-radius: 4px;font-weight: bold;font-size: 17px;margin: 19px auto;display: block;width: 260px;text-align: center;text-decoration: none;"
        href=${url}>Reset Password</a>
        <p style="margin: 0;font-size: 12px;line-height: 14px"><span
        style="font-size: 20px; line-height: 24px;">If you ignore this message, your password
        won’t be changed.</span>
        </p>
        <p style="margin: 0;font-size: 12px;line-height: 14px"> <br><span
        style="font-size: 20px; line-height: 24px;"></span></p>

    ${footer}`

    return finaltemp

}

let emailVerification = async (data, url) => {
    let finaltemp = `
    ${header}

    <p
        style="margin-bottom: 20px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Hi
        <strong>${data.userName}</strong>!<br></p>

        <p
        style="    margin-bottom: 15px !important;line-height: 29px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Please click on the button to complete the verification process for ${data.email}<br>

        </p>
        <p
        style=" margin-bottom: 30px !important;line-height: 29px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        <span style="float: left;width: 100%;margin-bottom: 20px;"><a
            style="background: #0072ff;color: #fff; text-decoration: none;padding: 5px 15px;border-radius: 4px;float: left;"
            href="${url}">Verify Your Email Address</a></span>
        </p>
        <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        If you didn't attempt to verify your email address with Check It, Please delete this email.
        </p>

        <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Thank you very much.</p>  

    ${footer}`
    return finaltemp;
}

let inviteTemplate = async (data, url) => {
    let finaltemp = `
    ${header}
        <p
        style="margin-bottom: 20px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Hi
        <strong>${data.email}</strong>!<br></p>

        <p
            style="    margin-bottom: 15px !important;line-height: 29px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
            Your friend ${data.senderName} has invited to join him on the platform Check It<br>

        </p>
        <p
            style=" margin-bottom: 30px !important;line-height: 29px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
            <span style="float: left;width: 100%;margin-bottom: 20px;"><a
                style="background: #0072ff;color: #fff; text-decoration: none;padding: 5px 15px;border-radius: 4px;float: left;"
                href="${url}">Click to visit the app store</a></span>
        </p>
        <p
            style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
            If you don't want to visit the platform, Please delete or ignore this email.
        </p>

        <p
            style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
            Thank you very much.</p>
    ${footer}`
    return finaltemp;
}

let successTemplate = async (data, color) => {
    let finaltemp = `
    ${header}
        <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        ${data.data}
    </p>

    <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Thank you very much.</p>
    ${footer}`
    return finaltemp;
}

let forgotPasswordTemplate = async (url) => {
    let finaltemp = `
    ${header}

    <p
        style="margin-bottom: 20px !important;color: #000;font-size: 25px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Change Your Password
        </p>

        <p
        style="    margin-bottom: 15px !important;line-height: 23px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Need to reset your password, No Problem. Just need to click below to get started<br>

        </p>
        <p
        style=" margin-bottom: 30px !important;line-height: 29px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        <span style="float: left;width: 100%;margin-bottom: 20px;">
            <a style="background: #0072ff;color: #fff; text-decoration: none;padding: 5px 15px;border-radius: 4px;float: left;"
            href="${url}">Reset my password</a></span>
        </p>
        <p
        style="line-height: 23px;margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        If you didn't attempt to verify your email address with Check It, Please delete this email.
        </p>

        <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Thank you very much.</p>


    ${footer}`
    return finaltemp;
}


let reciveRewardsPointTemplate = async (data) => {
    let finaltemp = `
    ${header}

    <p
    style="margin-bottom: 20px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
    Hi
    <strong>${data.name}</strong>!<br><br></p>

        <p
        style="margin-bottom: 15px !important;line-height: 23px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Thank you for completing our survey information.
        <br>
        </p>
     
        <p
        style="    margin-bottom: 15px !important;line-height: 23px;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Credit points ${data.pointEarned} have been credit to you account for completing our survey.
        <br>
        </p>
        <br>
        <p
        style="margin-bottom: 40px !important;color: #000;font-size: 17px;text-align: left;font-weight: 500;width: 500px;margin: 0 auto;max-width: 90%;">
        Thank you very much.</p>


    ${footer}`
    return finaltemp;
}


module.exports = {
    registrationEmail,
    emailVerification,
    inviteTemplate,
    successTemplate,
    forgotPasswordTemplate,
    reciveRewardsPointTemplate
}