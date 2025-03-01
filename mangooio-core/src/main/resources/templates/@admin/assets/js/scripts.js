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

document.getElementById("keypair").addEventListener('click', () => {
	fetch("/@admin/tools", {
		method: "POST",
		body: JSON.stringify({"function": "keypair"}),
		headers: {
			"Content-type": "application/json"
		}
	}).then(response => response.json().then(data => {
		document.getElementById("publickey").value = data.publickey;
		document.getElementById("privatekey").value = data.privatekey;
	}))
});

document.getElementById("encrypt").addEventListener('click', () => {
	let cleartext = document.getElementById("cleartext").value;
	let pubkey = document.getElementById("pubkey").value;

	fetch("/@admin/tools", {
		method: "POST",
		body: JSON.stringify({"function": "encrypt", "cleartext": cleartext, "key": pubkey}),
		headers: {
			"Content-type": "application/json"
		}
	}).then(response => response.json().then(data => {
		document.getElementById("encryptedvalue").value = data.encrypted;
	}))
});