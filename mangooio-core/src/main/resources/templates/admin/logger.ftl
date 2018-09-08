<#include "header.ftl">
<div class="content-wrapper" style="min-height: 846px;">
<section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>Logger</h1>
          </div>
        </div>
      </div><!-- /.container-fluid -->
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
		                    <th data-sort="string">Class</th>
		                  </tr>
	                  </thead>
                  	  <tbody class="searchable">
	                  <#list loggers as logger>
						<tr>
							<td>${logger.name}</td>
							<td>
								<div class="form-group">
								<select name="level" class="form-control loglevel" data-class="${logger.name}">
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
							</td>
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