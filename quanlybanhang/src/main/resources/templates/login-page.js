const container = document.getElementById('container');

const registerBtn = document.getElementById('register');
const loginBtn = document.getElementById('login');

function clearInputs(formSelector) {
    const form = document.querySelector(formSelector);
    if (form) {
        const inputs = form.querySelectorAll('input');
        inputs.forEach(input => input.value = '');
    }
}

registerBtn.addEventListener('click', () => {
    container.classList.add('active');
    clearInputs('.form-container.sign-in form');
});

loginBtn.addEventListener('click', () => {
    container.classList.remove('active');
    clearInputs('.form-container.sign-up form');
});