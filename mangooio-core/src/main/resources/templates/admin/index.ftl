<#include "header.ftl">
<div class="content-wrapper" style="min-height: 666px;">
    <section class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-12">
            <h1>Dashboard</h1>
          </div>
        </div>
      </div>
    </section>
    <section class="content">
      <div class="container-fluid">
        <div class="row">
          <div class="col-md-4 col-sm-6 col-12">
            <div class="info-box">
              <span class="info-box-icon bg-info"><i class="fa fa-clock-o"></i></span>
              <div class="info-box-content">
                <span class="info-box-text">Application started</span>
                <span class="info-box-number">${prettytime(uptime)}</span>
              </div>
            </div>
          </div>
          <div class="col-md-4 col-sm-6 col-12">
            <div class="info-box">
              <span class="info-box-icon bg-success"><i class="fa fa-battery-half"></i></span>
              <div class="info-box-content">
                <span class="info-box-text">Free memory</span>
                <span class="info-box-number">${freeMemory}</span>
              </div>
            </div>
          </div>
          <div class="col-md-4 col-sm-6 col-12">
            <div class="info-box">
              <span class="info-box-icon bg-warning"><i class="fa fa-battery-full"></i></span>
              <div class="info-box-content">
                <span class="info-box-text">Allocated memory</span>
                <span class="info-box-number">${allocatedMemory}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</div>
<#include "footer.ftl">