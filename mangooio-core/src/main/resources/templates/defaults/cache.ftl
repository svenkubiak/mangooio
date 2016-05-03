<#setting number_format=",##0">
<#setting locale="en_US">
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>mangoo I/O | Control Panel</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <style><#include "css/bootstrap.min.css" parse=false></style>
    <style><#include "css/AdminLTE.min.css"  parse=false></style>
    <style><#include "css/skin-blue.min.css" parse=false></style>
    <style><#include "css/blue.min.css" parse=false></style>
    <style><#include "css/jquery-jvectormap.min.css" parse=false></style>
  </head>
  <body class="hold-transition skin-blue sidebar-mini">
    <div class="wrapper">
      <header class="main-header">
        <a href="/" class="logo">
          <span class="logo-lg"><b>mangoo</b> I/O</span>
        </a>
        <nav class="navbar navbar-static-top" role="navigation">
        </nav>
      </header>
      <aside class="main-sidebar">
        <section class="sidebar">
          <ul class="sidebar-menu">
            <li <#if !space??>class="active"</#if>><a href="/@admin"><span>Dashboard</span></a></li>
            <li <#if space?? && space == 'configuration'>class="active"</#if>><a href="/@admin/configuration"><span>Configuration</span></a></li>
            <li <#if space?? && space == 'routes'>class="active"</#if>><a href="/@admin/routes"><span>Routes</span></a></li>
            <li <#if space?? && space == 'scheduler'>class="active"</#if>><a href="/@admin/scheduler"><span>Scheduler</span></a></li>
            <li <#if space?? && space == 'cache'>class="active"</#if>><a href="/@admin/cache"><span>Cache</span></a></li>
            <li <#if space?? && space == 'metrics'>class="active"</#if>><a href="/@admin/metrics"><span>Metrics</span></a></li>
          </ul>
        </section>
      </aside>
      <div class="content-wrapper">
        <section class="content">
<div class="row">
	<div class="col-xs-12">
    	<div class="box">
        	<div class="box-header">
            	<h3 class="box-title">Cache</h3>
                <div class="box-tools">
                	<div class="input-group">
                    	<input type="text" name="table_search" size="50" id="filter" class="form-control input-large" placeholder="Start typing what you are looking for...">
                    </div>
                </div>
			</div>
            <div class="box-body table-responsive no-padding">
            	<table class="table table-hover">
                	<thead>
						<tr>
							<th data-sort="string"><b>Key</b></th>
							<th data-sort="string"><b>Value</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
						<#list stats?keys as stat>
							<tr>
								<td>${stat}</td>
								<td>${stats[stat]}</td>
							</tr>
						</#list>
                	</tbody>
                </table>
        	</div>
    	</div>
	</div>
</div>
       </section>
      </div>
      <footer class="main-footer">
        <div class="pull-right hidden-xs">
          <b>Version</b> ${version}
        </div>
        <strong>&nbsp;</strong>
      </footer>
    <div class="control-sidebar-bg"></div>
    </div>
	<script><#include "js/jquery.min.js" parse=false></script>
	<script><#include "js/jquery-ui.min.js" parse=false></script>
    <script>
      $.widget.bridge('uibutton', $.ui.button);
    </script>
    <script><#include "js/bootstrap.min.js" parse=false></script>
    <script><#include "js/raphael-min.js" parse=false></script>
    <script><#include "js/jquery-jvectormap-1.2.2.min.js" parse=false></script>
    <script><#include "js/jquery-jvectormap-world-mill-en.js" parse=false></script>
    <script><#include "js/jquery.knob.js" parse=false></script>
    <script><#include "js/app.min.js" parse=false></script>
        <script>
      $(function () {
        /* jQueryKnob */

        $(".knob").knob({
          /*change : function (value) {
           //console.log("change : " + value);
           },
           release : function (value) {
           console.log("release : " + value);
           },
           cancel : function () {
           console.log("cancel : " + this.value);
           },*/
          draw: function () {
            if (this.$.data('skin') == 'tron') {

              var a = this.angle(this.cv)  // Angle
                      , sa = this.startAngle          // Previous start angle
                      , sat = this.startAngle         // Start angle
                      , ea                            // Previous end angle
                      , eat = sat + a                 // End angle
                      , r = true;

              this.g.lineWidth = this.lineWidth;

              this.o.cursor
                      && (sat = eat - 0.3)
                      && (eat = eat + 0.3);

              if (this.o.displayPrevious) {
                ea = this.startAngle + this.angle(this.value);
                this.o.cursor
                        && (sa = ea - 0.3)
                        && (ea = ea + 0.3);
                this.g.beginPath();
                this.g.strokeStyle = this.previousColor;
                this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sa, ea, false);
                this.g.stroke();
              }

              this.g.beginPath();
              this.g.strokeStyle = r ? this.o.fgColor : this.fgColor;
              this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sat, eat, false);
              this.g.stroke();

              this.g.lineWidth = 2;
              this.g.beginPath();
              this.g.strokeStyle = this.o.fgColor;
              this.g.arc(this.xy, this.xy, this.radius - this.lineWidth + 1 + this.lineWidth * 2 / 3, 0, 2 * Math.PI, false);
              this.g.stroke();

              return false;
            }
          }
        });
      });
    </script>
  <script type="text/javascript"><#include "js/stupidtable.min.js" parse=false></script>
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
  </body>
</html>