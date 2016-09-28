<#macro myLayout title="Layout example">
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="mangoo I/O archetypte template">
    <meta name="author" content="Sven Kubiak">
    <title>mangoo I/O Template for Bootstrap</title>
    <link href="https://maxcdn.bootstrapcdn.com/bootswatch/3.3.7/paper/bootstrap.min.css" rel="stylesheet">
	<style>body{padding-top:70px;padding-bottom:20px;}</style>
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
      <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
    <![endif]-->
    </head>
  <body>
    <nav class="navbar navbar-inverse navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar" aria-expanded="false" aria-controls="navbar">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <a class="navbar-brand" href="#">mangoo I/O</a>
        </div>
        <div id="navbar" class="navbar-collapse collapse">
        <ul class="nav navbar-nav">
        <li><a href="/">Home</a></li>
        <li class="dropdown">
          <a href="#" class="dropdown-toggle" data-toggle="dropdown">Demos <span class="caret"></span></a>
          <ul class="dropdown-menu" role="menu">
            <li><a href="/auth">Authentication</a></li>
            <li><a href="/json">Working with JSON</a></li>
            <li><a href="/sse">Server Sent Event</a></li>
          </ul>
        </li>
      </ul>
        <#if !subject.isAuthenticated()>
          <form method="post" action="/authenticate" class="navbar-form navbar-right">
            <div class="form-group">
              <input type="text" placeholder="Username" name="username" class="form-control">
            </div>
            <div class="form-group">
              <input type="password" placeholder="Password" name="password" class="form-control">
            </div>
            <@authenticityForm/>
            <button type="submit" class="btn btn-success">Sign in</button>
          </form>
        <#else>
        <form method="post" action="/authenticate" class="navbar-form navbar-right">
        <a href="/logout?authenticity=<@authenticity/>" class="btn btn-success" id="logout">Logout</a></li>
        </form>
        </#if>
        </div>
      </div>
    </nav>
    <#nested/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
  </body>
</html>
</#macro>