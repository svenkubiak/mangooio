<#include "header.ftl">
<div class="row">
	<div class="col-md-3 col-sm-6 col-xs-12">
    	<div class="info-box">
        	<span class="info-box-icon bg-aqua"></span>
			<div class="info-box-content">
            	<span class="info-box-text">Used memorey</span>
            	<span class="info-box-number">${usedMemory}<small> MB</small></span>
            </div>
		</div>
	</div>
    <div class="col-md-3 col-sm-6 col-xs-12">
    	<div class="info-box">
            <span class="info-box-icon bg-red"></span>
        	<div class="info-box-content">
            	<span class="info-box-text">Free memory</span>
            	<span class="info-box-number">${freeMemory}<small> MB</small></span>
            </div>
		</div>
	</div>
   	<div class="col-md-3 col-sm-6 col-xs-12">
    	<div class="info-box">
			<span class="info-box-icon bg-green"></span>
            <div class="info-box-content">
            	<span class="info-box-text">Max memory</span>
            	<span class="info-box-number">${maxMemory}<small> MB</small></span>
			</div>
		</div>
	</div>
</div> 
<div class="row">
	<div class="col-xs-12">
		<div class="box">
	    	<div class="box-header">
	        	<h3 class="box-title">System properties</h3>
	           	<div class="box-tools">
	            	<div class="input-group">
	                	<input type="text" name="table_search" size="50" id="filter" class="form-control input-large" placeholder="Start typing what you are looking for...">
	                </div>
	            </div>
	        </div>
	        <div class="box-body table-responsive no-padding">
	        	<table class="table table-hover">
	            	<thead>
						<tr>
							<th data-sort="string"><b>Key</b></th>
							<th data-sort="string"><b>Value</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
					<#list properties?keys as prop>
						<tr>
							<td>${prop}</td>
							<td>${properties[prop]}</td>
						</tr>
					</#list>
	                </tbody>
	            </table>
			</div>
		</div>
	</div>
</div>

<#include "footer.ftl">