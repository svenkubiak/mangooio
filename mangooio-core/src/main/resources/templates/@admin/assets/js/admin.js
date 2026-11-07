const form = document.getElementById('login-form');
if (form) {
    const digits = [...form.querySelectorAll('.otp-digit')];
    const code = document.getElementById('code');

    if (digits.length > 0 && code) {
        let submitting = false;
        let focusTimer = 0;
        let backspaceHandled = false;
        const finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;

        const sync = () => {
            code.value = digits.map(digit => digit.value).join('');
        };

        const scheduleFocus = index => {
            const next = Math.max(0, Math.min(index, digits.length - 1));
            window.clearTimeout(focusTimer);
            // A phone keyboard ignores focus() inside the input event and then types into the same box.
            focusTimer = window.setTimeout(() => {
                digits[next].focus({preventScroll: true});
            });
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
            sync();
            const next = start + chars.length;
            if (next >= digits.length) {
                scheduleFocus(digits.length - 1);
                submitCode();
            } else {
                scheduleFocus(next);
            }
        };

        const stepBack = index => {
            if (index === 0) {
                return;
            }
            digits[index - 1].value = '';
            sync();
            scheduleFocus(index - 1);
        };

        digits.forEach((input, index) => {
            input.addEventListener('beforeinput', event => {
                if (backspaceHandled || event.inputType !== 'deleteContentBackward' || input.value !== '' || index === 0) {
                    return;
                }
                if (event.cancelable) {
                    event.preventDefault();
                }
                stepBack(index);
            });

            input.addEventListener('input', event => {
                if (event.inputType && event.inputType.indexOf('delete') === 0) {
                    sync();
                    return;
                }
                write(index, input.value);
            });

            input.addEventListener('keydown', event => {
                if (event.key === 'Backspace') {
                    backspaceHandled = true;
                    window.queueMicrotask(() => {
                        backspaceHandled = false;
                    });
                    if (input.value === '') {
                        event.preventDefault();
                        stepBack(index);
                    }
                } else if (event.key === 'ArrowLeft' && index > 0) {
                    scheduleFocus(index - 1);
                    event.preventDefault();
                } else if (event.key === 'ArrowRight' && index < digits.length - 1) {
                    scheduleFocus(index + 1);
                    event.preventDefault();
                }
            });

            input.addEventListener('paste', event => {
                const text = event.clipboardData ? event.clipboardData.getData('text') : '';
                if (text === '') {
                    return;
                }
                event.preventDefault();
                write(index, text);
            });

            input.addEventListener('focus', () => {
                if (finePointer) {
                    input.select();
                }
            });
        });

        form.addEventListener('invalid', () => {
            submitting = false;
        }, true);

        form.addEventListener('submit', event => {
            const combined = digits.map(digit => digit.value).join('').replace(/\D/g, '').slice(0, digits.length);
            digits.forEach((digit, index) => {
                digit.value = combined.charAt(index);
            });
            sync();
            if (combined.length !== digits.length) {
                event.preventDefault();
                submitting = false;
                scheduleFocus(combined.length);
            }
        }, true);

        form.addEventListener('submit', event => {
            window.setTimeout(() => {
                if (event.defaultPrevented) {
                    submitting = false;
                }
            });
        });
    }

    form.addEventListener('submit', event => {
        if (event.defaultPrevented) {
            return;
        }
        const button = document.getElementById('login');
        if (!button) {
            return;
        }
        button.setAttribute('aria-busy', 'true');
        // Safari drops the submission when the submit button is disabled inside this event.
        window.setTimeout(() => {
            button.disabled = true;
        });
    });
}

const themeToggle = document.querySelector('[data-theme-toggle]');
if (themeToggle) {
    const themeMeta = document.querySelector('meta[name="color-scheme"]');

    const applyTheme = theme => {
        document.documentElement.setAttribute('data-theme', theme);
        if (themeMeta) {
            themeMeta.setAttribute('content', theme);
        }
        themeToggle.setAttribute('aria-label', theme === 'dark' ? 'Switch to light mode' : 'Switch to dark mode');
        try {
            localStorage.setItem('mangoo-admin-theme', theme);
        } catch (ignore) {}
    };

    applyTheme(document.documentElement.getAttribute('data-theme') === 'light' ? 'light' : 'dark');
    themeToggle.addEventListener('click', () => {
        applyTheme(document.documentElement.getAttribute('data-theme') === 'light' ? 'dark' : 'light');
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
