<#import "layout.ftl" as layout>
<@layout.auth title="Welcome back" subtitle="Sign in to the administration area">
<#if form.hasErrors()><div class="alert alert-danger" role="alert"><@layout.icon "warning"/><span>Invalid username and/or password.</span></div></#if>
<form action="/@admin/authenticate" method="post" id="login-form">
    <label for="username">Username</label>
    <input type="text" id="username" name="username" autocomplete="username" required autofocus<#if form.hasErrors()> aria-invalid="true"</#if>>
    <label for="password">Password</label>
    <input type="password" id="password" name="password" autocomplete="current-password" required<#if form.hasErrors()> aria-invalid="true"</#if>>
    <button type="submit" id="login">Sign in</button>
    <@csrfform/>
</form>
</@layout.auth>
