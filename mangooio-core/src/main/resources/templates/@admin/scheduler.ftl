<#include "header.ftl">
<div class="container">
	<div class="columns">
		<div class="column">
			<section class="hero is-info is-small">
				<div class="hero-body">
					<p class="title">Scheduler</p>
				</div>
			</section>
		</div>
	</div>
	<div class="columns">
		<div class="column">
			<input id="filter" type="text" name="table_search" class="input" placeholder="Start typing what you are looking for...">
		</div>
	</div>
	<div class="columns">
		<div class="column">
			<div class="table-container">
				<table class="table is-fullwidth">
					<thead>
					<tr>
						<th data-sort="string"><b>Class</b></th>
						<th data-sort="string"><b>Method</b></th>
						<th data-sort="string"><b>Run at</b></th>
						<th data-sort="string"><b>Next</b></th>
						<th data-sort="string"><b>State</b></th>
					</tr>
					</thead>
					<tbody class="searchable">
					<#list scheduler.schedules as schedule>
						<tr>
							<td>${schedule.clazz}</td>
							<td>${schedule.method}</td>
							<td>${schedule.runAt}</td>
							<td>${schedule.next().format('dd.MM.yyyy HH:mm:ss')}</td>
							<td><#if schedule.scheduledFuture.state().name() == "SUCCESS">RUNNING<#else>${schedule.scheduledFuture.state().name()}</#if></td>
						</tr>
					</#list>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<#include "footer.ftl">