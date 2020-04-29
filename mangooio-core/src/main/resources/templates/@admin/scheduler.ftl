<#include "header.ftl">
<div class="container">
  <div class="columns">
      <div class="column is-12">
          <section class="hero is-info welcome is-small">
              <div class="hero-body">
                  <div class="container">
                      <h1 class="title">
                          Scheduler
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
										<a href="/@admin/scheduler/state/${job.name}" class="btn btn-app confirmation" confirm-data="${job.active?string('deactivate', 'activate')} job '${job.name}'">
											<#if job.active>
												Deactivate
											<#else>
												Activate
											</#if>
										</a>
										&nbsp;&nbsp;
										<a href="/@admin/scheduler/execute/${job.name}" class="btn btn-app confirmation" confirm-data="execute job '${job.name}'">
											Run now
										</a>
									</td>
								</tr>
							</#list>
                		</tbody>
                </table>
                		  	<div class="table-container">             
      </div>
  </div>
</div>
<#include "footer.ftl">