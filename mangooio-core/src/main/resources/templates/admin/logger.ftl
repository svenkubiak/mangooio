<#include "header.ftl">
<section class="content-header">
	<h1>Logger</h1>
</section>
<section class="content">
<div class="row">
	<div class="col-md-12">
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
							<th data-sort="string"><b>Class</b></th>
							<th data-sort="string"><b>Log level</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
  				    	<#list loggers as logger>
							<tr>
								<td class="col-md-9">${logger.name}</td>
								<td class="col-md-3">
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
									<span class="help-block has-success" style="display:none;">Updated!</span>
								</td>
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