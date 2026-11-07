<#import "layout.ftl" as layout>
<@layout.auth title="Two-Step Verification" subtitle="From your authenticator app">
<#if form.hasErrors()><div class="alert alert-danger" role="alert"><@layout.icon "warning"/><span>Invalid code, please try again.</span></div></#if>
<form action="/@admin/verify" method="post" id="login-form" data-otp novalidate>
    <fieldset class="otp">
        <legend>Verification code</legend>
        <div class="otp-digits">
            <#list 1..6 as digit>
            <input type="text" class="otp-digit" size="1" maxlength="6" inputmode="numeric" pattern="[0-9]*" autocomplete="<#if digit == 1>one-time-code<#else>off</#if>" autocapitalize="off" autocorrect="off" spellcheck="false" aria-label="Digit ${digit} of 6" required<#if digit == 1> autofocus</#if><#if form.hasErrors()> aria-invalid="true"</#if>>
            </#list>
        </div>
        <input type="hidden" name="code" id="code">
    </fieldset>
    <button type="submit" id="login">Verify</button>
    <@csrfform/>
</form>
<p class="auth-cancel"><a href="/@admin/logout" class="secondary">Cancel and return to login</a></p>
</@layout.auth>
