<#include "header.ftl">
<div class="container">
    <div class="columns">
        <div class="column is-12">
            <section class="hero is-info welcome is-small">
                <div class="hero-body">
                    <div class="container">
                        <h1 class="title">
                            Config tools
                        </h1>
                    </div>
                </div>
            </section>
            <section class="info-tiles">
                <div class="tile is-ancestor">
                    <div class="tile is-parent">
                        <article class="tile is-child is-primary box">
                            <p class="title">Generate key pair</p>
                            <div class="content">
                                <form>
                                    <div class="field">
                                        <label class="label">Public key</label>
                                        <div class="control">
                                            <textarea class="textarea" rows="3" disabled="" name="publickey" id="publickey"></textarea>
                                        </div>
                                    </div>
                                    <div class="field">
                                        <label class="label">Private key</label>
                                        <div class="control">
                                            <textarea class="textarea" rows="9" disabled="" name="privatekey" id="privatekey"></textarea>
                                        </div>
                                    </div>
                                    <button type="button" class="button is-fullwidth is-primary" id="keypair">Generate
                                    </button>
                                </form>
                            </div>
                        </article>
                    </div>
                </div>
            </section>
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
                                        <label class="label">Encrypted value</label>
                                        <div class="control">
                                            <textarea class="textarea" rows="5" id="encryptedvalue" name="encryptedvalue" disabled=""></textarea>
                                        </div>
                                    </div>
                                    <button type="button" class="button is-fullwidth is-primary" id="encrypt">Encrypt
                                    </button>
                                </form>
                            </div>
                        </article>
                    </div>
                </div>
            </section>
            <#if qrcode?has_content && secret?has_content>
                <section class="info-tiles">
                    <div class="tile is-ancestor">
                        <div class="tile is-parent">
                            <article class="tile is-child is-primary box">
                                <p class="title">Two Factor Authentication</p>
                                <div class="content">
                                    <form>
                                        <div class="field">
                                            <img src="${qrcode}" alt="qrcode">
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
            </#if>
        </div>
    </div>
</div>
<#include "footer.ftl">