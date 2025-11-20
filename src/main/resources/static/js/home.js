import CaptchaModule from '/js/captchaModule.js';

CaptchaModule.Captcha({
    type: 'CLICK_IN_ORDER',
    onSuccess: (data, captchaToken) => {
        alert('Captcha solved! Data: ' + JSON.stringify(data));
    }
}).renderInto('#captchaContainer');