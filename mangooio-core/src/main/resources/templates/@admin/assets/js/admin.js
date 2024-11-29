const button = document.getElementById('login');
if (button) {
    button.addEventListener('click', () => {
        button.textContent = '...';
        button.classList.add('is-loading');
        button.disabled = true;
        document.getElementById("login-form").submit();
    });
}