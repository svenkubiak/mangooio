<#include "header.ftl">
<div class="content-wrapper" style="min-height: 846px;">
<section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>Routes</h1>
          </div>
        </div>
      </div>
    </section>
<section class="content">
      <div class="container-fluid">
<div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title"><input id="filter" type="text" name="table_search" class="form-control float-left" placeholder="Start typing what you are looking for...">
              </div>
              <div class="card-body table-responsive p-0">
                <table class="table table-hover">
                  		<thead>
	                      	<tr>
								<th data-sort="string"><b>Method</b></th>
								<th data-sort="string"><b>URL</b></th>
								<th data-sort="string"><b>Controller class</b></th>
								<th data-sort="string"><b>Controller method</b></th>
								<th data-sort="string"><b>Rate limit</b></th>
								<th data-sort="string"><b>Authentication</b></th>
								<th data-sort="string"><b>Authorization</b></th>
								<th data-sort="string"><b>Basic Authentication</b></th>
								<th data-sort="string"><b>Blocking</b></th>
							</tr>
						</thead>
						<tbody class="searchable">
					    	<#list routes as route>
								<tr>
									<td><#if route.method??>${route.method?upper_case}</#if></td>
									<td><#if route.url??>${route.url}</#if></td>
									<td><#if route.controllerClass??>${route.controllerClass}</#if></td>
									<td><#if route.controllerMethod??>${route.controllerMethod}</#if></td>
									<td><#if route.limit??>${route.limit}</#if></td>
									<td><#if route.authentication??>${route.authentication?string('yes', 'no')}</#if></td>
									<td><#if route.authorization??>${route.authorization?string('yes', 'no')}</#if></td>
									<td><#if route.basicAuthentication??>${route.basicAuthentication?string('yes', 'no')}</#if></td>
									<td><#if route.blocking??>${route.blocking?string('yes', 'no')}</#if></td>
								</tr>
							</#list>
						</tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
</div>
</section>
</div>
<#include "footer.ftl">