<#setting number_format=",##0">
<#setting locale="en_US">
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
    <link rel="stylesheet" href="/@admin/assets/css/admin.min.css">
</head>
<body>
<nav class="navbar is-white">
    <div class="container">
        <div class="navbar-brand">
            <a role="button" class="navbar-burger" aria-label="menu" aria-expanded="false" data-target="navMenu">
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
                <span aria-hidden="true"></span>
            </a>
        </div>
        <div id="navMenu" class="navbar-menu">
            <div class="navbar-start">
                <a class="navbar-item" href="/@admin">Dashboard</a>
                <a class="navbar-item" href="/@admin/scheduler">Scheduler</a>
                <a class="navbar-item" href="/@admin/cache">Cache</a>
                <a class="navbar-item" href="/@admin/security">Security</a>
            </div>
            <div class="navbar-end">
                <a class="navbar-item logout" href="/@admin/logout">Logout</a>
            </div>            
        </div>
    </div>
</nav>