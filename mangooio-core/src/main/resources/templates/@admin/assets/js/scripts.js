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

$(document).ready(function () {
	let $table = $("table").stupidtable();
	let $th_to_sort = $table.find("thead th").eq(0);
	$th_to_sort.stupidsort();
	(function ($) {
		$('#filter').keyup(function () {
			let rex = new RegExp($(this).val(), 'i');
			$('.searchable tr').hide();
			$('.searchable tr').filter(function () {
				return rex.test($(this).text());
			}).show();
		})
	}(jQuery));
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