<#include "header.ftl">
<#if qrcode?has_content && secret?has_content>
<div class="container">
    <div class="columns">
        <div class="column">
            <section class="hero is-info is-small">
                <div class="hero-body">
                    <p class="title">Security</p>
                </div>
            </section>
        </div>
    </div>
    <div class="columns">
        <div class="column">
            <section class="info-tiles">
                <div class="tile is-ancestor">
                    <div class="tile is-parent">
                        <article class="tile is-child is-primary box">
                            <p class="title">Setup Two Factor Authentication</p>
                            <div class="content">
                                <form>
                                    <div class="field">
                                        <img src="data:image/png;base64,${qrcode}" alt="QR Code" />
                                    </div>
                                    <div class="field">
                                        <div class="field-label">
                                            <label for="input"><b>Secret</b></label>
                                            <div class="gg-copy" role="button" title="Copy secret to clipboard" aria-label="Copy secret to clipboard" data-copy-target="secret"></div>
                                            <p class="copy-status" id="secret-copy-status"></p>
                                        </div>
                                        <div class="control">
                                            <textarea class="textarea" rows="3" name="secret" id="secret">${secret}</textarea>
                                        </div>
                                    </div>
                                </form>
                            </div>
                        </article>
                    </div>
                </div>
            </section>
        </div>
    </div>
</div>
<#else>
<div class="container">
    <div class="columns">
        <div class="column">
            <section class="hero is-info is-small">
                <div class="hero-body">
                    <p class="title">Not available in Production</p>
                </div>
            </section>
        </div>
    </div>
</div>
</#if>
<#include "footer.ftl">
