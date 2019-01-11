<#include "header.ftl">
<div class="content-wrapper" style="min-height: 846px;">
<section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>Config tools</h1>
          </div>
        </div>
      </div>
    </section>
<section class="content">
     <div class="container-fluid">
               <div class="row">
			<div class="col-md-12">
            <div class="card card-warning">
              <div class="card-header">
                <h3 class="card-title">Generate key pair</h3>
              </div>
              <form role="form">
                <div class="card-body">
                  <div class="form-group">
                  	<label>Public key</label>
                    <textarea class="form-control" rows="3" disabled="" name="publickey" id="publickey"></textarea>
                  </div>
                  <div class="form-group">
                    <label>Private key</label>
                    <textarea class="form-control" rows="9" disabled="" name="privatekey" id="privatekey"></textarea>
                  </div>
                <div class="card-footer">
                  <button type="button" class="btn btn-block btn-primary" id="keypair">Generate</button>
                </div>
              </form>
            </div>
          </div>
          <div class="row">
			<div class="col-md-12">
            <div class="card card-warning">
              <div class="card-header">
                <h3 class="card-title">Encrypt a config value</h3>
              </div>
              <form role="form">
                <div class="card-body">
                  <div class="form-group">
                    <label>Cleartext</label>
                    <input type="text" class="form-control" id="cleartext" name="cleartext">
                  </div>
                  <div class="form-group">
                  	<label>Public key</label>
                    <textarea class="form-control" rows="3" name="pubkey" id="pubkey"></textarea>
                  </div>
                   <div class="form-group">
                     <label>Encrypted value</label>
                    <textarea class="form-control" rows="5" id="encryptedvalue" name="encryptedvalue" disabled=""></textarea>
                  </div>
                <div class="card-footer">
                  <button type="button" class="btn btn-block btn-primary" id="encrypt">Encrypt</button>
                </div>
              </form>
            </div>
          </div>
       </div>
</section>
</div>
<#include "footer.ftl">