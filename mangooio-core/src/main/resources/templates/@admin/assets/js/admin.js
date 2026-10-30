const form = document.getElementById('login-form');
if (form) {
    form.addEventListener('submit', () => {
        const button = document.getElementById('login');
        button.setAttribute('aria-busy', 'true');
        button.disabled = true;
    });
}

document.querySelectorAll('[data-copy-target]').forEach(button => {
    button.addEventListener('click', () => {
        const targetId = button.getAttribute('data-copy-target');
        const target = document.getElementById(targetId);
        const status = document.getElementById(targetId + '-copy-status');

        if (target) {
            navigator.clipboard.writeText(target.value)
                .then(() => {
                    status.textContent = 'Copied to clipboard';
                    setTimeout(() => status.textContent = '', 2000);
                })
                .catch(() => {
                    status.textContent = 'Failed to copy text';
                });
        }
    });
});

document.querySelectorAll('form[data-confirm]').forEach(confirmForm => {
    confirmForm.addEventListener('submit', event => {
        if (!window.confirm(confirmForm.getAttribute('data-confirm'))) {
            event.preventDefault();
        }
    });
});

document.querySelectorAll('input[data-filter]').forEach(input => {
    const rows = document.querySelectorAll('#' + input.getAttribute('data-filter') + ' tbody tr');
    input.addEventListener('input', () => {
        const term = input.value.trim().toLowerCase();
        rows.forEach(row => row.hidden = term !== '' && !row.textContent.toLowerCase().includes(term));
    });
});
