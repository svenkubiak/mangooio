const form = document.getElementById('login-form');
if (form) {
    const digits = [...form.querySelectorAll('.otp-digit')];
    const code = document.getElementById('code');

    if (digits.length > 0 && code) {
        let submitting = false;

        const sync = () => {
            code.value = digits.map(digit => digit.value).join('');
        };

        const submitCode = () => {
            if (submitting || !digits.every(digit => /^[0-9]$/.test(digit.value))) {
                return;
            }
            submitting = true;
            sync();
            form.requestSubmit();
        };

        const write = (start, text) => {
            const chars = [...text.replace(/\D/g, '')].slice(0, digits.length - start);
            if (chars.length === 0) {
                digits[start].value = '';
                sync();
                return;
            }
            chars.forEach((char, offset) => {
                digits[start + offset].value = char;
            });
            const focusAt = Math.min(start + chars.length, digits.length - 1);
            digits[focusAt].focus();
            digits[focusAt].select();
            sync();
            submitCode();
        };

        let pasted = false;
        digits.forEach((input, index) => {
            input.addEventListener('input', () => {
                if (pasted) {
                    pasted = false;
                    return;
                }
                write(index, input.value);
            });

            input.addEventListener('keydown', event => {
                if (event.key === 'Backspace' && input.value === '' && index > 0) {
                    digits[index - 1].value = '';
                    digits[index - 1].focus();
                    sync();
                    event.preventDefault();
                } else if (event.key === 'ArrowLeft' && index > 0) {
                    digits[index - 1].focus();
                    event.preventDefault();
                } else if (event.key === 'ArrowRight' && index < digits.length - 1) {
                    digits[index + 1].focus();
                    event.preventDefault();
                }
            });

            input.addEventListener('paste', event => {
                event.preventDefault();
                pasted = true;
                write(index, event.clipboardData.getData('text'));
            });

            input.addEventListener('focus', () => input.select());
        });

        form.addEventListener('submit', sync, true);
    }

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

const liveSections = document.querySelectorAll('[data-live]');
if (liveSections.length > 0) {
    const indicator = document.querySelector('[data-live-indicator]');
    let source = null;

    const connect = () => {
        source = new EventSource('/@admin/stream');
        source.onopen = () => indicator && (indicator.hidden = false);
        liveSections.forEach(section => source.addEventListener(section.getAttribute('data-live'), event => {
            section.innerHTML = event.data;
        }));
        // The stream answers 401 once the admin cookie expired, the reload then leads to the login
        source.onerror = () => {
            if (indicator) {
                indicator.hidden = true;
            }
            if (source.readyState === EventSource.CLOSED) {
                window.location.reload();
            }
        };
    };

    const disconnect = () => {
        if (source) {
            source.close();
            source = null;
        }
        if (indicator) {
            indicator.hidden = true;
        }
    };

    document.addEventListener('visibilitychange', () => document.hidden ? disconnect() : connect());
    if (!document.hidden) {
        connect();
    }
}
