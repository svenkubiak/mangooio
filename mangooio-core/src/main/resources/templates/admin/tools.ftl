<#include "header.ftl">
<section class="content-header">
	<h1>Tools</h1>
</section>
<section class="content">
<div class="row">
	<div class="col-xs-12">
		<div class="box box-primary">
			<div class="box-header with-border">
    			<h3 class="box-title">Hash a value</h3>
    		</div>
   			<div class="box-body">
   				<p>Hashes a given cleartext value using the same JBCrypt hash algorithm one would use when hashing a user password.</p>
	   			<div class="form-group">
		   			<input type="text" class="form-control input-lg" id="hash_cleartext" placeholder="Enter cleartext ...">
       			</div>
	   			<div class="form-group">
	   				<textarea class="form-control input-lg" id="hashedvalue" disabled></textarea>
       			</div>       		
   			</div>
		</div>
		<div class="row">
		<div class="col-xs-12">
			<div class="box box-primary">
				<div class="box-header with-border">
    				<h3 class="box-title">Encrypt a value</h3>
    			</div>
   				<div class="box-body">
   				<p>Encrypts a given cleartext value using the crypto functions one would use programtically. Please note that the optional key has to be exactly 32 (256 Bit) characters long. If no optional key is provided, the application secret will be used.</p>
	   			<div class="form-group">
		   			<input type="text" pattern=".{32,32}" required title="32 characters required" size="32" class="form-control encrypt input-lg" id="encrypt_cleartext" placeholder="Enter cleartext ...">
       			</div>
	   			<div class="form-group">
		   			<input type="text" class="form-control encrypt input-lg" id="encrypt_key" placeholder="Enter encryption key (optional) ">
       			</div>       		
	   			<div class="form-group">
	   				<textarea class="form-control input-lg" id="encryptedvalue" disabled></textarea>
       			</div>       		
   			</div>   		
		</div>
	</div>
</div>	
</section>
<#include "footer.ftl">