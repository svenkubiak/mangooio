<#include "header.ftl">








<div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">Responsive Hover Table</h3>

                <div class="card-tools">
                  <div class="input-group input-group-sm" style="width: 150px;">
                    <input type="text" name="table_search" class="form-control float-right" placeholder="Search">

                    <div class="input-group-append">
                      <button type="submit" class="btn btn-default"><i class="fa fa-search"></i></button>
                    </div>
                  </div>
                </div>
              </div>
              <!-- /.card-header -->
              <div class="card-body table-responsive p-0">
                <table class="table table-hover">
                  <tbody><tr>
                    <th>ID</th>
                    <th>User</th>
                    <th>Date</th>
                    <th>Status</th>
                    <th>Reason</th>
                  </tr>
                  <tr>
                    <td>183</td>
                    <td>John Doe</td>
                    <td>11-7-2014</td>
                    <td><span class="tag tag-success">Approved</span></td>
                    <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
                  </tr>
                  <tr>
                    <td>219</td>
                    <td>Alexander Pierce</td>
                    <td>11-7-2014</td>
                    <td><span class="tag tag-warning">Pending</span></td>
                    <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
                  </tr>
                  <tr>
                    <td>657</td>
                    <td>Bob Doe</td>
                    <td>11-7-2014</td>
                    <td><span class="tag tag-primary">Approved</span></td>
                    <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
                  </tr>
                  <tr>
                    <td>175</td>
                    <td>Mike Doe</td>
                    <td>11-7-2014</td>
                    <td><span class="tag tag-danger">Denied</span></td>
                    <td>Bacon ipsum dolor sit amet salami venison chicken flank fatback doner.</td>
                  </tr>
                </tbody></table>
              </div>
              <!-- /.card-body -->
            </div>
            <!-- /.card -->
          </div>
        </div>















<section class="content-header">
	<h1>Logger</h1>
</section>
<section class="content">
<div class="row">
	<div class="col-md-12">
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
							<th data-sort="string"><b>Class</b></th>
							<th data-sort="string"><b>Log level</b></th>
						</tr>
					</thead>
					<tbody class="searchable">
  				    	<#list loggers as logger>
							<tr>
								<td class="col-md-9">${logger.name}</td>
								<td class="col-md-3">
									<div class="form-group">
									<select name="level" class="form-control loglevel" data-class="${logger.name}">
										<option value="ALL"<#if logger.level == "ALL"> selected</#if>>ALL</option>
										<option value="TRACE"<#if logger.level == "TRACE"> selected</#if>>TRACE</option>
										<option value="DEBUG"<#if logger.level == "DEBUG"> selected</#if>>DEBUG</option>
										<option value="INFO"<#if logger.level == "INFO"> selected</#if>>INFO</option>
										<option value="WARN"<#if logger.level == "WARN"> selected</#if>>WARN</option>
										<option value="ERROR"<#if logger.level == "ERROR"> selected</#if>>ERROR</option>
										<option value="FATAL"<#if logger.level == "FATAL"> selected</#if>>FATAL</option>
										<option value="OFF"<#if logger.level == "OFF"> selected</#if>>OFF</option>
									</select>
									</div>
								</td>
							</tr>
						</#list>
                	</tbody>
                </table>
        	</div>
    	</div>
	</div>
</div>
</section>
<#include "footer.ftl">