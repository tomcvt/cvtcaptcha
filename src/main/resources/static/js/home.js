import CaptchaModule from '/js/captchaModule.js';

CaptchaModule.Captcha({
    type: 'CLICK_IN_ORDER',
    onSuccess: function (data) {
        alert('Captcha solved! Data: ' + JSON.stringify(data));
    }
}).render('#captchaContainer');




document.getElementById('captchaButton').addEventListener('click', function () {
    fetch('/api/captcha/create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            requestId: '00000000-0000-0000-0000-000000000000',
            type: 'CLICK_IN_ORDER'
        })
    })
        .then(response => response.json())
        .then(data => {
            document.getElementById('captchaData').innerText = JSON.stringify(data, null, 2);
        })
        .catch(error => console.error('Error:', error));
});