<#include "header.ftl">
<div class="row">
	<div class="col-xs-12">
    	<div class="box">
        	<div class="box-header">
            	<h3 class="box-title">Routes</h3>
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
  				    	<#list routes as route>
							<tr>
								<td><#if route.requestMethod??>${route.requestMethod}</#if></td>
								<td><#if route.url??>${route.url}</#if></td>
								<td><#if route.controllerClass??>${route.controllerClass.name}</#if></td>
								<td><#if route.controllerMethod??>${route.controllerMethod}</#if></td>
								<td><#if route.routeType??>${route.routeType}</#if></td>
							</tr>
						</#list>
                	</tbody>
                </table>
        	</div>
    	</div>
	</div>
</div>
<#include "footer.ftl">