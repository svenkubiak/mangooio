<#import "layout.ftl" as layout>
<@layout.auth title="Two-Step Verification" subtitle="Enter the 6 digit code from your authenticator app">
<#if form.hasErrors()><div class="alert alert-danger" role="alert"><@layout.icon "warning"/><span>Invalid code, please try again.</span></div></#if>
<form action="/@admin/verify" method="post" id="login-form">
    <label for="code">Verification code</label>
    <input type="text" id="code" name="code" class="otp" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autocomplete="one-time-code" placeholder="000000" required autofocus<#if form.hasErrors()> aria-invalid="true"</#if>>
    <button type="submit" id="login">Verify</button>
    <@csrfform/>
</form>
<p class="auth-cancel"><a href="/@admin/logout" class="secondary">Cancel and return to login</a></p>
</@layout.auth>
