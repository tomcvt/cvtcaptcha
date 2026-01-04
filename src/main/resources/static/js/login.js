const urlParams = new URLSearchParams(window.location.search);

if (urlParams.has('error')) {
    document.getElementById('login-alert').style.display = 'block';
}
if (urlParams.has('logout')) {
    document.getElementById('logout-alert').style.display = 'block';
}
if (urlParams.has('activated')) {
    document.getElementById('activation-alert').style.display = 'block';
}
if (urlParams.has('activationError')) {
    document.getElementById('activation-error-alert').style.display = 'block';
}

document.getElementById('login-form').addEventListener('submit', async function (e) {
    e.preventDefault();
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });
        if (response.ok) {
            window.location.href = '/'; // redirect on success
        } else {
            document.getElementById('login-alert').style.display = 'block';
        }
    } catch (err) {
        document.getElementById('login-alert').style.display = 'block';
    }
});
