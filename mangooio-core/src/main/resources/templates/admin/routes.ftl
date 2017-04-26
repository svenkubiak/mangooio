<#include "header.ftl">
<section class="content-header">
	<h1>Routes</h1>
</section>
<section class="content">
<div class="row">
	<div class="col-xs-12">
    	<div class="box">
	    	<div class="box-header">
				<div class="form-group">
	            	<input type="text" name="table_search" id="filter" class="form-control" placeholder="Start typing what you are looking for...">
	            </div>
	        </div>
            <div class="box-body table-responsive no-padding">
            	<table class="table table-hover">
                	<thead>
						<tr>
							<th data-sort="string"><b>Method</b></th>
							<th data-sort="string"><b>URL</b></th>
							<th data-sort="string"><b>Controller class</b></th>
							<th data-sort="string"><b>Controller method</b></th>
							<th data-sort="string"><b>Rate limit</b></th>
							<th data-sort="string"><b>Authentication</b></th>
							<th data-sort="string"><b>Blocking</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
  				    	<#list routes as route>
							<tr>
								<td><#if route.requestMethod??>${route.requestMethod}</#if></td>
								<td><#if route.url??>${route.url}</#if></td>
								<td><#if route.controllerClass??>${route.controllerClass.name}</#if></td>
								<td><#if route.controllerMethod??>${route.controllerMethod}</#if></td>
								<td><#if route.url??>${route.limit}</#if></td>
								<td><#if route.url??>${route.authenticationRequired?string('yes', 'no')}</#if></td>
								<td><#if route.url??>${route.blockingAllowed?string('yes', 'no')}</#if></td>
							</tr>
						</#list>
                	</tbody>
                </table>
        	</div>
    	</div>
	</div>
</div>
</section>
<#include "footer.ftl">