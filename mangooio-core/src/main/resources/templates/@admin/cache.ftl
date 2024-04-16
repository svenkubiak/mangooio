<#include "header.ftl">
<div class="container">
	<div class="columns">
		<div class="column">
			<section class="hero is-info is-small">
				<div class="hero-body">
					<p class="title">Cache</p>
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
						<th data-sort="string"><b>Cache name</b></th>
						<th data-sort="string"><b>Evictions</b></th>
						<th data-sort="string"><b>Requests</b></th>
						<th data-sort="string"><b>Hits</b></th>
						<th data-sort="string"><b>Hit rate</b></th>
						<th data-sort="string"><b>Misses</b></th>
						<th data-sort="string"><b>Miss rate</b></th>
						<th data-sort="string"><b>Load successes</b></th>
						<th data-sort="string"><b>Load failures</b></th>
					</tr>
					</thead>
					<tbody class="searchable">
					<#list statistics as name, statistic>
						<tr>
							<td>${name}</td>
							<td>${statistic.evictionCount()}</td>
							<td>${statistic.requestCount()}</td>
							<td>${statistic.hitCount()}</td>
							<td>${statistic.hitRate()?string["0.##"]}</td>
							<td>${statistic.missCount()}</td>
							<td>${statistic.missRate()?string["0.##"]}</td>
							<td>${statistic.loadSuccessCount()}</td>
							<td>${statistic.loadFailureCount()}</td>
						</tr>
					</#list>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<#include "footer.ftl">