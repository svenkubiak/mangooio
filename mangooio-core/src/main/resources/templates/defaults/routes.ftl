<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Application routes</title>
    <style><#include "css/bootstrap.min.css"></style>
  <body>
    <div class="container">
      <div class="row">
      <div class="col-md-12">
          <h1>Configured routes</h1>
      </div>
      </div>
      <div class="row">
      <div class="col-md-12">
	      <div class="input-group"><span class="input-group-addon">Filter</span><input id="filter" type="text" class="form-control" placeholder="Start typing what you are looking for..."></div>
      </div>
      </div>
      <div class="row">
        <div class="col-md-12">
          	<table class="table table-hover">
			<thead>
			<tr>
				<th data-sort="string"><b>HTTP Method</b></th>
				<th data-sort="string"><b>URL</b></th>
				<th data-sort="string"><b>Controller class</b></th>
				<th data-sort="string"><b>Controller method</b></th>
				<th data-sort="string"><b>Type</b></th>
			</tr>
			</thead>
			<tbody class="searchable">
		  <#list routes as route>
			<tr>
				<td><#if route.requestMethod??>${route.requestMethod}</#if></td>
				<td><#if route.url??>${route.url}</#if></td>
				<td><#if route.controllerClass??>${route.controllerClass.name}</#if></td>
				<td><#if route.controllerMethod??>${route.controllerMethod}</#if></td>
				<td><#if route.routeType??>${route.routeType}</#if></td>
			</tr>
		  </#list>
		  	</tbody>
		  </table>
        </div>
       </div>
	</div>
  </body>
  <script type="text/javascript"><#include "js/jquery.min.js"></script>
  <script type="text/javascript"><#include "js/bootstrap.min.js"></script>
  <script type="text/javascript"><#include "js/stupidtable.min.js"></script>
  <script>
  $(document).ready(function(){
	$("table").stupidtable();
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