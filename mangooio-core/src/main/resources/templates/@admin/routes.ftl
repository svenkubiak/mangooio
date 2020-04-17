<#include "header.ftl">
<div class="container">
  <div class="columns">
      <div class="column is-12">
          <section class="hero is-info welcome is-small">
              <div class="hero-body">
                  <div class="container">
                      <h1 class="title">
                          Routes
                      </h1>
                  </div>
              </div>
          </section>
          <div class="field">
	      	<input id="filter" type="text" name="table_search" class="input" placeholder="Start typing what you are looking for...">
		  </div>
		  	<div class="table-container">
			<table class="table is-fullwidth">
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
<#include "footer.ftl">