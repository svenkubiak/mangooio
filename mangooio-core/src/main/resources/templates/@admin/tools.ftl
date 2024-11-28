<#include "header.ftl">
<div class="container">
    <div class="columns">
        <div class="column">
            <section class="hero is-info is-small">
                <div class="hero-body">
                    <p class="title">Config tools</p>
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
                            <p class="title">Generate key pair</p>
                            <div class="content">
                                <form>
                                    <div class="field">
                                        <div style="display: inline-flex; align-items: center;">
                                            <label for="input"><b>Public key</b></label>
                                            <div class="copy-icon" style="color: #000; margin-left: 5px;" role="button" title="Copy public key to clipboard" aria-label="Copy public key to clipboard" data-copy-target="publickey"></div>
                                            <p id="publickey-copy-status" style="color: green;"></p>
                                        </div>
                                        <div class="control">
                                            <textarea class="textarea" rows="3" disabled="" name="publickey" id="publickey"></textarea>
                                        </div>
                                    </div>
                                    <div class="field">
                                        <div style="display: inline-flex; align-items: center;">
                                            <label for="input"><b>Private key</b></label>
                                            <div class="copy-icon" style="color: #000; margin-left: 5px;" role="button" title="Copy private key to clipboard" aria-label="Copy private key to clipboard" data-copy-target="privatekey"></div>
                                            <p id="privatekey-copy-status" style="color: green;"></p>
                                        </div>
                                        <div class="control">
                                            <textarea class="textarea" rows="9" disabled="" name="privatekey" id="privatekey"></textarea>
                                        </div>
                                    </div>
                                    <button type="button" class="button is-fullwidth is-primary" id="keypair">Generate</button>
                                </form>
                            </div>
                        </article>
                    </div>
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
                            <p class="title">Encrypt a config value</p>
                            <div class="content">
                                <form>
                                    <div class="field">
                                        <label class="label">Cleartext</label>
                                        <div class="control">
                                            <input type="text" class="input" id="cleartext" name="cleartext">
                                        </div>
                                    </div>
                                    <div class="field">
                                        <label class="label">Public key</label>
                                        <div class="control">
                                            <textarea class="textarea" rows="3" name="pubkey" id="pubkey"></textarea>
                                        </div>
                                    </div>
                                    <div class="field">
                                        <div style="display: inline-flex; align-items: center;">
                                            <label for="input"><b>Encrypted value</b></label>
                                            <div class="copy-icon" style="color: #000; margin-left: 5px;" role="button" title="Copy encrypted value to clipboard" aria-label="Copy encrypted value to clipboard" data-copy-target="encryptedvalue"></div>
                                            <p id="encryptedvalue-copy-status" style="color: green;"></p>
                                        </div>
                                        <div class="control">
                                            <textarea class="textarea" rows="5" id="encryptedvalue" name="encryptedvalue" disabled=""></textarea>
                                        </div>
                                    </div>
                                    <button type="button" class="button is-fullwidth is-primary" id="encrypt">Encrypt</button>
                                 </form>
                            </div>
                        </article>
                    </div>
                </div>
            </section>
        </div>
    </div>
    <#if qrcode?has_content && secret?has_content>
    <div class="columns">
        <div class="column">
            <section class="info-tiles">
                <div class="tile is-ancestor">
                    <div class="tile is-parent">
                        <article class="tile is-child is-primary box">
                            <p class="title">Two Factor Authentication</p>
                            <div class="content">
                                <form>
                                    <div class="field">
                                        <img src="data:image/png;base64,${qrcode}" alt="QR Code" />
                                    </div>
                                    <div class="field">
                                        <label class="label">Secret</label>
                                        <div class="control">
                                            <textarea class="textarea" rows="3" name="pubkey" id="pubkey">${secret}</textarea>
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
    </#if>
</div>
<#include "footer.ftl">