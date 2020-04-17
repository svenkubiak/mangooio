<#include "header.ftl">
<div class="container">
  <div class="columns">
      <div class="column is-12">
          <section class="hero is-info welcome is-small">
              <div class="hero-body">
                  <div class="container">
                      <h1 class="title">
                          Logger
                      </h1>
                  </div>
              </div>
          </section>
          <div class="field">
	      	<input id="filter" type="text" name="table_search" class="input" placeholder="Start typing what you are looking for...">
		  </div>
		  		  	<div class="table-container">
          <table class="table is-fullwidth">
              	<tbody class="searchable">
	                  <#list loggers as logger>
						<tr>
							<td><b>${logger.name}</b>
								<p>
									<div class="select is-fullwidth">
										<select name="level" class="loglevel" data-class="${logger.name}">
											<option value="ALL"<#if logger.level == "ALL"> selected</#if>>ALL</option>
											<option value="TRACE"<#if logger.level == "TRACE"> selected</#if>>TRACE</option>
											<option value="DEBUG"<#if logger.level == "DEBUG"> selected</#if>>DEBUG</option>
											<option value="INFO"<#if logger.level == "INFO"> selected</#if>>INFO</option>
											<option value="WARN"<#if logger.level == "WARN"> selected</#if>>WARN</option>
											<option value="ERROR"<#if logger.level == "ERROR"> selected</#if>>ERROR</option>
											<option value="FATAL"<#if logger.level == "FATAL"> selected</#if>>FATAL</option>
											<option value="OFF"<#if logger.level == "OFF"> selected</#if>>OFF</option>
										</select>
									</div>
								</p>
							</td>
						</tr>
					  </#list>
            	</tbody>
          </table>    
          </div>          
      </div>
  </div>
</div>
<#include "footer.ftl">