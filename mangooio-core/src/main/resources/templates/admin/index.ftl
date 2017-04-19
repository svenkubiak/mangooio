<#include "header.ftl">
<section class="content-header">
	<h1>Dashboard</h1>
</section>
<section class="content">
<div class="row">
	<div class="col-lg-12">
	    <div class="small-box bg-light-blue-active">
    	    <div class="inner">
              <h3>${prettytime(uptime)}</h3>
              <p>Application started</p>
            </div>
        </div>
    </div>
</div>
<div class="row">
	<div class="col-lg-6">
	    <div class="small-box bg-teal">
    	    <div class="inner">
              <h3>${allocatedMemory}</h3>
              <p>Allocated memory</p>
            </div>
        </div>
    </div>
    	<div class="col-lg-6">
	    <div class="small-box bg-teal">
    	    <div class="inner">
              <h3>${freeMemory}</h3>
              <p>Free memory</p>
            </div>
        </div>
    </div>
</div>
<div class="row">
	<div class="col-lg-6">
	    <div class="small-box bg-teal">
    	    <div class="inner">
              <h3>${maxMemory}</h3>
              <p>Max memory</p>
            </div>
        </div>
    </div>
    	<div class="col-lg-6">
	    <div class="small-box bg-teal">
    	    <div class="inner">
              <h3>${totalFreeMemory}</h3>
              <p>Total free memory</p>
            </div>
        </div>
    </div>
</div>
</section>
<#include "footer.ftl">