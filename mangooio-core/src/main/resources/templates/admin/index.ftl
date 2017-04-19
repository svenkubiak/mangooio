<#include "header.ftl">
<section class="content-header">
	<h1>Dashboard</h1>
</section>
<section class="content">
<div class="row">
    <div class="col-lg-12">
    	<div class="info-box">
        	<span class="info-box-icon bg-green"><i class="fa fa-rocket"></i></span>
            <div class="info-box-content">
            	<span class="info-box-text">Application started</span>
            	<span class="info-box-number">${started}<br/>${prettytime(uptime)}</span>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-lg-6">
    	<div class="info-box">
        	<span class="info-box-icon bg-aqua"><i class="fa fa-battery-1"></i></span>
            <div class="info-box-content">
            	<span class="info-box-text">Allocated memory</span>
            	<span class="info-box-number">${allocatedMemory}</span>
            </div>
        </div>
    </div>
    <div class="col-lg-6">
    	<div class="info-box">
        	<span class="info-box-icon bg-aqua"><i class="fa fa-battery-2"></i></span>
            <div class="info-box-content">
            	<span class="info-box-text">Free memory</span>
            	<span class="info-box-number">${freeMemory}</span>
            </div>
        </div>
    </div>
</div>
<div class="row">
    <div class="col-lg-6">
    	<div class="info-box">
        	<span class="info-box-icon bg-aqua"><i class="fa fa-battery-three-quarters"></i></span>
            <div class="info-box-content">
            	<span class="info-box-text">Max memory</span>
            	<span class="info-box-number">${maxMemory}</span>
            </div>
        </div>
    </div>
    <div class="col-lg-6">
    	<div class="info-box">
        	<span class="info-box-icon bg-aqua"><i class="fa fa-battery-full"></i></span>
            <div class="info-box-content">
            	<span class="info-box-text">Total free memory</span>
            	<span class="info-box-number">${totalFreeMemory}</span>
            </div>
        </div>
    </div>    
</div>
</section>
<#include "footer.ftl">