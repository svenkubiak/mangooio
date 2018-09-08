<#include "header.ftl">
<#if enabled>
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
        	<div class="col-lg-4 col-6">
	            <div class="small-box bg-info">
                	<div class="inner">
                		<h3>${totalRequests}</h3>
                		<p>Total requests</p>
              		</div>
              		<div class="icon">
                		<i class="ion ion-checkmark-circled"></i>
              		</div>
            	</div>
          </div>
          <div class="col-lg-4 col-6">
          	<div class="small-box bg-info">
				<div class="inner">
					<h3>${dataSend}</h3>
              		<p>Data send</p>
				</div>
              	<div class="icon">
                	<i class="ion ion-stats-bars"></i>
              	</div>
			</div>
          </div>
          <div class="col-lg-4 col-6">
          	<div class="small-box bg-danger">
            	<div class="inner">
                	<h3>${errorRate} <sup style="font-size: 20px">%</sup></h3>
                	<p>Error rate</p>
              	</div>
              	<div class="icon">
                	<i class="ion ion-close-circled"></i>
              	</div>
            </div>
          </div>
		</div>
		<div class="row">
          <div class="col-lg-4 col-6">
            <div class="small-box bg-success">
              <div class="inner">
                <h3>${minRequestTime} ms</h3>
                <p>Min process time</p>
              </div>
              <div class="icon">
                <i class="ion ion-arrow-graph-up-right"></i>
              </div>
            </div>
          </div>
          <div class="col-lg-4 col-6">
            <div class="small-box bg-success">
              <div class="inner">
                <h3>${avgRequestTime} ms</h3>
                <p>Avg process time</p>
              </div>
              <div class="icon">
                <i class="ion ion-log-in"></i>
              </div>
            </div>
          </div>
          <div class="col-lg-4 col-6">
            <div class="small-box bg-danger">
              <div class="inner">
                <h3>${maxRequestTime} ms</h3>
                <p>Max process time</p>
              </div>
              <div class="icon">
                <i class="ion ion-arrow-graph-down-right"></i>
              </div>
            </div>
          </div>
        </div>
        <#if metrics?has_content>
        <div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title"><input type="text" name="table_search" class="form-control float-left" placeholder="Start typing what you are looking for...">
              </div>
              <div class="card-body table-responsive p-0">
                <table class="table table-hover">
                  <tbody>		<tr>
							<th data-sort="string"><b>Status</b></th>
							<th data-sort="string"><b>Count</b></th>
						</tr>
						<#list metrics as key, value>
							<tr>
								<td>${key}</td>
								<td>${value}</td>
							</tr>
						</#list>
                </tbody></table>
              </div>
            </div>
          </div>
        </div>
        </#if>
</div>
</div>
</section>
</div>
<#else>
<section class="content-header">
	<h1>Metrics are not enabled. Set metrics to true in your application.yaml file</a> in order to collect metrics.</h1>
</section>
</#if>
</section>

<#include "footer.ftl">