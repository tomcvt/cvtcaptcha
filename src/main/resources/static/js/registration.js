
const form = document.getElementById('registerForm');
const result = document.getElementById('result')
const resultRedirect = document.getElementById('result-redirect')

form.addEventListener('submit', async function (e) {
    e.preventDefault();
    const submitButton = form.querySelector('button[type="submit"]');
    submitButton.disabled = true;

    const username = form.elements['username'].value;
    const password = form.elements['password'].value;
    const email = form.elements['email'].value;
    //const validation = validatePassword(rawPassword);
    /*if (validation != '') {
        result.innerText = validation;
        submitButton.disabled = false;
        return;
    }*/
    result.innerText = '';

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-type': 'application/json'
            },
            body: JSON.stringify({ username: username, rawPassword: password, email: email }),
            credentials: 'include'
        });

        if (response.ok) {
            const res = await response.json();
            result.innerText = res.message || 'Registration successful';
            result.style.display = 'block';
            result.classList.add('alert-success');
        } else {
            const err = await response.json();
            const errorText = err.message || 'Unknown error';
            result.innerText = 'Error: ' + errorText;
            result.style.display = 'block';
            result.classList.add('alert-danger');
        }
    } catch (err) {
        result.innerText = 'Error: ' + err;
        result.style.display = 'block';
        result.classList.add('alert-danger');
    }
    submitButton.disabled = false;
})

function validatePassword(password) {
    if (password.length < 8) {
        return 'Password must be at least 8 characters long';
    }
    if (!/[A-Z]/.test(password)) {
        return 'Password must have at least one upper case letter';
    }
    if (!/[a-z]/.test(password)) {
        return 'lower case';
    }
    if (!/[0-9]/.test(password)) {
        return 'digit';
    }
    return '';
}