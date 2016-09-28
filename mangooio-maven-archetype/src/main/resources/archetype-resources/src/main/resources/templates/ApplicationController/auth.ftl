<#import "../layout.ftl" as layout>
<@layout.myLayout "Layout">
<div class="jumbotron">
	<div class="container">
    	<h1>Authentication</h1>
    	<p>You may have noticed the login form in the navigation bar. You can login with username 'demo' and password 'demo'. To checkout what is happening in the backend, check the AuthenticationController in the controllers package, specially the authenticate and logout method. You may also want to checkout the layout.ftl file in the src/main/resources/templates folder on how the login/logout is handled in the frontend.</p>
    	<p><a class="btn btn-primary btn-lg" href="https://mangoo.io/docs/authentication/" role="button">Authentication documentation &raquo;</a></p>
	</div>
</div>
</@layout.myLayout>