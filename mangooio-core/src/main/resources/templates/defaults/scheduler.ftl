<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Application scheduler</title>
    <style><#include "css/skeleton.min.css"></style>
  <body>
    <div class="container">
      <div class="row">
      <div class="twelve columns">
          <h1>Scheduler</h1>
      </div>
      </div>
      <div class="row">
      <div class="twelve columns">
	      <div><input id="filter" type="text" class="u-full-width" placeholder="Start typing what you are looking for..."></div>
      </div>
      </div>
      <div class="row">
        <div class="twelve columns">
          	<table class="u-full-width">
			<thead>
			<tr>
				<th data-sort="string"><b>Name</b></th>
				<th data-sort="string"><b>Active</b></th>
				<th data-sort="string"><b>Description</b></th>
				<th data-sort="string"><b>Last execution</b></th>
				<th data-sort="string"><b>Next execution</b></th>
			</tr>
			</thead>
			<tbody class="searchable">
			<#list jobs as job>
				<td>${job.name}</td>
				<td>${job.active?string('yes', 'no')}</td>
				<td>${job.description}</td>
				<td><#if job.previousFireTime??>${job.previousFireTime?string('dd.MM.yyyy HH:mm:ss')}<#else>-</#if></td>
				<td><#if job.nextFireTime??>${job.nextFireTime?string('dd.MM.yyyy HH:mm:ss')}<#else>-</#if></td>
			</#list>
		  	</tbody>
		  </table>
        </div>
       </div>
	</div>
  </body>
  <script type="text/javascript"><#include "js/jquery.min.js"></script>
  <script type="text/javascript"><#include "js/stupidtable.min.js"></script>
  <script>
  $(document).ready(function(){
  	(function ($) {
          $('#filter').keyup(function () {
              var rex = new RegExp($(this).val(), 'i');
              $('.searchable tr').hide();
              $('.searchable tr').filter(function () {
                  return rex.test($(this).text());
              }).show();
          })
      }(jQuery));
	});
  </script>
</html>