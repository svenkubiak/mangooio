<#include "header.ftl">
<div class="container">
  <div class="columns">
      <div class="column is-12">
          <section class="hero is-info welcome is-small">
              <div class="hero-body">
                  <div class="container">
                      <h1 class="title">
                          Cache
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
								<th data-sort="string"><b>Cache name</b></th>
								<th data-sort="string"><b>Evictions</b></th>
								<th data-sort="string"><b>Expirations</b></th>
								<th data-sort="string"><b>Gets</b></th>
								<th data-sort="string"><b>Puts</b></th>
								<th data-sort="string"><b>Removals</b></th>
								<th data-sort="string"><b>Hits</b></th>
								<th data-sort="string"><b>Hit percentage</b></th>
								<th data-sort="string"><b>Misses</b></th>
								<th data-sort="string"><b>Miss percentage</b></th>
							</tr>
						</thead>
						<tbody class="searchable">
							<#list statistics as name, statistic>
								<tr>
									<td>${name}</td>
									<td>${statistic.cacheEvictions}</td>
									<td>${statistic.cacheExpirations}</td>
									<td>${statistic.cacheGets}</td>
									<td>${statistic.cachePuts}</td>
									<td>${statistic.cacheRemovals}</td>
									<td>${statistic.cacheHits}</td>
									<td>${statistic.cacheHitPercentage} %</td>
									<td>${statistic.cacheMisses}</td>
									<td>${statistic.cacheMissPercentage} %</td>
								</tr>
							</#list>
						</tbody>
                </table>   
          </div>          
      </div>
  </div>
</div>
<#include "footer.ftl">