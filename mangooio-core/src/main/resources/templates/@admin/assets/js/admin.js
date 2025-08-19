const button = document.getElementById('login');
if (button) {
    button.addEventListener('click', () => {
        button.textContent = '...';
        button.classList.add('is-loading');
        button.disabled = true;
        document.getElementById("login-form").submit();
    });
}

const copyIcons = document.querySelectorAll('.gg-copy');
copyIcons.forEach(icon => {
    icon.addEventListener('click', () => {
        const targetId = icon.getAttribute('data-copy-target');
        const targetElement = document.getElementById(targetId);
        const status = document.getElementById(targetId + "-copy-status");

        if (targetElement) {
            const textToCopy = targetElement.value;
            navigator.clipboard.writeText(textToCopy)
                .then(() => {
                    status.textContent = 'Copied!';
                    setTimeout(() => status.textContent = '', 2000);
                })
                .catch(err => {
                    status.textContent = 'Failed to copy text.';
                });
        }
    });
});

document.addEventListener('DOMContentLoaded', () => {
    const $navbarBurgers = Array.prototype.slice.call(document.querySelectorAll('.navbar-burger'), 0);
    $navbarBurgers.forEach( el => {
        el.addEventListener('click', () => {
            const target = el.dataset.target;
            const $target = document.getElementById(target);

            el.classList.toggle('is-active');
            $target.classList.toggle('is-active');

        });
    });
});