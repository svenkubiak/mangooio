<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>mangoo I/O Admin</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/@admin/assets/favicons/apple-touch-icon.png">
	<link rel="icon" type="image/png" sizes="32x32" href="/@admin/assets/favicons/favicon-32x32.png">
	<link rel="icon" type="image/png" sizes="16x16" href="/@admin/assets/favicons/favicon-16x16.png">
	<link rel="manifest" href="/@admin/assets/favicons/site.webmanifest">
    <link rel="stylesheet" href="/@admin/assets/css/bulma.min.css">
</head>
<body>
    <section class="hero is-fullheight">
        <div class="hero-body">
            <div class="container has-text-centered">
                <div class="column is-4 is-offset-4">
                    <h3 class="title has-text-black">Two-Step Verification</h3>
                    <hr class="login-hr">
                    <p class="subtitle has-text-black">Please enter your TOTP.</p>
                    <#if form.hasErrors()><p class="has-text-danger has-text-weight-bold">Invalid Code. <a href="/@admin/logout">Cancel?</a></p></#if>
                    <div class="box">
                        <form action="/@admin/verify" method="post" id="login-form">
                            <div class="field">
                                <div class="control">
                                    <input class="input is-large" type="text" maxlength="6" name="code" placeholder="6 digit code" autofocus="">
                                </div>
                            </div>
                            <button class="button is-block is-info is-large is-fullwidth" id="login">Verify <i class="fa fa-sign-in" aria-hidden="true"></i></button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </section>
    <script src="/@admin/assets/js/admin.min.js" type="text/javascript" defer></script>
</body>
</html>