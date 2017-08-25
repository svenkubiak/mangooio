<#macro myLayout title="Layout example">
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <title>mangoo I/O Demo</title>
	<link rel="stylesheet" href="/assets/stylesheets/bootstrap.min.css">
  </head>
  <body>
    <nav class="navbar navbar-expand-md navbar-dark fixed-top bg-dark">
      <a class="navbar-brand" href="/">Demo</a>
      <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExampleDefault" aria-controls="navbarsExampleDefault" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarsExampleDefault">
        <ul class="navbar-nav mr-auto">
          <li class="nav-item active">
            <a class="nav-link" href="#">Home <span class="sr-only">(current)</span></a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="#">Link</a>
          </li>
          <li class="nav-item">
            <a class="nav-link disabled" href="#">Disabled</a>
          </li>
          <li class="nav-item dropdown">
            <a class="nav-link dropdown-toggle" href="http://example.com" id="dropdown01" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">Dropdown</a>
            <div class="dropdown-menu" aria-labelledby="dropdown01">
              <a class="dropdown-item" href="#">Action</a>
              <a class="dropdown-item" href="#">Another action</a>
              <a class="dropdown-item" href="#">Something else here</a>
            </div>
          </li>
        </ul>
        <#if subject.authenticated>
        		<form action="/logout" method="post" class="form-inline my-2 my-lg-0">
        		 <button class="btn btn-outline-success my-2 my-sm-0" type="submit">Logout</button>
        		 <@authenticityForm/>
        		</form>
	    <#else>
	        <form action="/authenticate" method="post" class="form-inline my-2 my-lg-0">
	          <input class="form-control mr-sm-2" type="text" name="username" placeholder="Username" aria-label="Username">
	          <input class="form-control mr-sm-2" type="password" name="password" placeholder="Password" aria-label="Password">
	          <button class="btn btn-outline-success my-2 my-sm-0" type="submit">Login</button>
	          <@authenticityForm/>
	        </form>
        </#if>
      </div>
    </nav>
    <#nested/>
    <script src="/assets/javascripts/jquery.min.js"></script>
    <script src="/assets/javascripts/popper.min.js"></script>
    <script src="/assets/javascripts/bootstrap.min.js"></script>
  </body>
</html>
</#macro>