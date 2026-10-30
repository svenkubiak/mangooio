<#import "layout.ftl" as layout>
<@layout.page title="Security" active="security" subtitle="Two Factor Authentication for the administration area">
<#if qrcode?has_content && secret?has_content>
<article class="setup">
    <header>
        <h2>Set up Two Factor Authentication</h2>
        <p>Scan the QR code with your authenticator app or enter the secret manually. Then add the secret to your configuration as <code>application.admin.secret</code>.</p>
    </header>
    <div class="setup-body">
        <figure class="qrcode">
            <img src="data:image/png;base64,${qrcode}" alt="QR code for the authenticator app" width="200" height="200">
        </figure>
        <div class="setup-secret">
            <label for="secret">Secret</label>
            <div role="group">
                <textarea name="secret" rows="2" readonly id="secret">${secret}</textarea>
                <button type="button" class="secondary outline copy" data-copy-target="secret" aria-label="Copy secret to clipboard"><@layout.icon "copy"/>Copy</button>
            </div>
            <small id="secret-copy-status" class="copy-status" aria-live="polite"></small>
        </div>
    </div>
</article>
<#else>
<article class="empty-state">
    <@layout.icon "verified"/>
    <h2>Two Factor Authentication is configured</h2>
    <p>A secret is already set for this application, so no new one is generated.</p>
</article>
</#if>
</@layout.page>
