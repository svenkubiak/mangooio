<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta http-equiv="refresh" content="3; /@memory">
    <title>Application memory</title>
    <style><#include "css/skeleton.min.css"></style>
  <body>
    <div class="container">
      <div class="row">
      <div class="twelve columns">
          <h1>Memory usage</h1>
      </div>
      </div>
      <div class="row">
      <div class="twelve columns">
	      The page will refresh every 3 seconds with current values.
      </div>
      </div>
      <div class="row">
        <div class="twelve columns">
          	<table class="u-full-width">
			<thead>
			<tr>
				<th data-sort="string"><b>Key</b></th>
				<th data-sort="string"><b>Value</b></th>
			</tr>
			</thead>
			<tbody>
			<tr>
				<td>Used memory</td>
				<td>${usedMemory} MB</td>
			</tr>
			<tr>
				<td>Free memory</td>
				<td>${freeMemory} MB</td>
			</tr>	
			<tr>
				<td>Total memory</td>
				<td>${totalMemory} MB</td>
			</tr>
			<tr>
				<td>Max memory</td>
				<td>${maxMemory} MB</td>
			</tr>									
		  	</tbody>
		  </table>
        </div>
       </div>
	</div>
  </body>
</html>