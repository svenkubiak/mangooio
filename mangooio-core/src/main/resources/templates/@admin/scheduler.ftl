<#include "header.ftl">
<div class="content-wrapper" style="min-height: 846px;">
<section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1>Scheduler</h1>
          </div>
        </div>
      </div>
    </section>
<section class="content">
<#if jobs?size gt 0>
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
								<th data-sort="string"><b>Job name</b></th>
								<th data-sort="string"><b>Active</b></th>
								<th data-sort="string"><b>Description</b></th>
								<th data-sort="string"><b>Last execution</b></th>
								<th data-sort="string"><b>Next execution</b></th>
								<th data-sort="string"><b>Actions</b></th>
							</tr>
						</thead>
						<tbody class="searchable">
							<#list jobs as job>
								<tr>
									<td>${job.name}</td>
									<td>${job.active?string('yes', 'no')}</td>
									<td>${job.description}</td>
									<td><#if job.previousFireTime??>${job.previousFireTime?string('dd.MM.yyyy HH:mm:ss')}<#else>-</#if></td>
									<td><#if job.nextFireTime??>${job.nextFireTime?string('dd.MM.yyyy HH:mm:ss')}<#else>-</#if></td>
									<td>
										<a href="/@admin/scheduler/state/${job.name}" class="btn btn-app" onclick="return confirm('Are you sure you want to change the state of the job?');" class="simpletooltip">
											<#if job.active>
												<i class="fa fa-pause"></i> Deactivate
											<#else>
												<i class="fa fa-play"></i> Activate
											</#if>
										</a>
										&nbsp;&nbsp;
										<a href="/@admin/scheduler/execute/${job.name}" class="btn btn-app"onclick="return confirm('Are you sure you want to exectue the job?');" class="simpletooltip">
											<i class="fa fa-fire"></i> Run now
										</a>
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
<#else>
      <div class="container-fluid">
		<div class="row">
	          <div class="col-12">
	            <div class="card">
	              <div class="card-header">
	                <h3 class="card-title">No jobs are scheduled in this application.</h3>
	              </div>
	            </div>
	          </div>
	        </div>
		</div>
</div>
</#if>
</section>
</div>
<#include "footer.ftl">