import CaptchaModule from '/js/captchaModule.js';



CaptchaModule.Captcha({
    type: 'CLICK_IN_ORDER',
    onSuccess: (data, captchaToken) => {
        alert('Captcha solved! Data: ' + JSON.stringify(data));
        console.log('Received Captcha Token: ', captchaToken);
        verifyToken(captchaToken);
    }
}).renderInto('#captchaContainer');


function verifyToken(token) {
    const response = fetch('/api/captcha/verify?token=' + encodeURIComponent(token), {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => {
        if (!response.ok) {
            throw new Error('Failed to verify captcha token: ' + response.statusText);
        }
        return response.text();
    }).then(data => {
        alert('Captcha token verified successfully! Response: ' + data);
    }).catch(error => {
        alert('Error verifying captcha token: ' + error.message);
    });
}
