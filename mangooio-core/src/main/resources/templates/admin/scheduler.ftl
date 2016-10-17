<#include "header.ftl">
<section class="content-header">
	<h1>Scheduler</h1>
</section>
<section class="content">
<#if jobs?size gt 0>
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
									<a href="/@admin/scheduler/state/${job.name}" onclick="return confirm('Are you sure you want to change the state of the job?');" class="simpletooltip">
										<#if job.active>
											<span class="simpletooltiptext">Deactivate job</span>
											<i class="fa fa-stop" aria-hidden="true"></i>
										<#else>
											<span class="simpletooltiptext">Activate job</span>
											<i class="fa fa-play" aria-hidden="true"></i>
										</#if>
									</a>
									&nbsp;&nbsp;
									<a href="/@admin/scheduler/execute/${job.name}" onclick="return confirm('Are you sure you want to exectue the job?');" class="simpletooltip">
										<span class="simpletooltiptext">Execute job</span>
										<i class="fa fa-fire" aria-hidden="true"></i>
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
<#else>
<div class="row">
	<div class="col-xs-12">
    	<div class="box">
	    	<div class="box-header">
	        	No jobs are scheduled in the application.
	        </div>
    	</div>
	</div>
</div>
</#if>
</section>
<#include "footer.ftl">