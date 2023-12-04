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
								<th data-sort="string"><b>Requests</b></th>
								<th data-sort="string"><b>Hits</b></th>
								<th data-sort="string"><b>Hit rate</b></th>
								<th data-sort="string"><b>Misses</b></th>
								<th data-sort="string"><b>Miss rate</b></th>
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
								</tr>
							</#list>
						</tbody>
                </table>   
          </div>          
      </div>
  </div>
</div>
<#include "footer.ftl">