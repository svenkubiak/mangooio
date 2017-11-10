<#include "header.ftl">
<section class="content-header">
	<h1>Metrics</h1>
</section>
<section class="content">
<div class="row">
    <div class="col-lg-3 col-xs-6">
    	<div class="small-box bg-green">
        	<div class="inner">
            	<h3>${totalRequests}</h3>
            	<p>Total requests </p>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-xs-6">
    	<div class="small-box bg-red">
        	<div class="inner">
            	<h3>${errorRate} %</h3>
            	<p>Error rate</p>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-lg-3 col-xs-6">
    	<div class="small-box bg-aqua">
        	<div class="inner">
            	<h3>${minRequestTime} ms</h3>
            	<p>Min process time</p>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-xs-6">
    	<div class="small-box bg-aqua">
        	<div class="inner">
            	<h3>${maxRequestTime} ms</h3>
            	<p>Max process time</p>
            </div>
        </div>
    </div>
    <div class="col-lg-3 col-xs-6">
    	<div class="small-box bg-aqua">
        	<div class="inner">
            	<h3>${avgRequestTime} ms</h3>
            	<p>Avg process time</p>
            </div>
        </div>
    </div>    
</div>
<#if metrics?has_content>
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
							<th data-sort="string"><b>HTTP status</b></th>
							<th data-sort="string"><b>Count</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
						<#list metrics as key, value>
							<tr>
								<td>${key}</td>
								<td>${value}</td>
							</tr>
						</#list>
                	</tbody>
                </table>
        	</div>
    	</div>
	</div>
</div>
</#if>
<div class="row">
	<div class="col-xs-12">
		<a href="/@admin/metrics/reset" class="btn btn-primary btn-block" onclick="return confirm('Are you sure you want to reset the metrics?')">Reset metrics</a>
	</div>
</div>
</section>
<#include "footer.ftl">