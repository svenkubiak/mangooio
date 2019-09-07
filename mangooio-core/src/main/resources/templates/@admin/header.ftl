<#setting number_format=",##0">
<#setting locale="en_US">
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta http-equiv="x-ua-compatible" content="ie=edge">
  <title>mangoo I/O | Admin Dashboard</title>
  <link rel="stylesheet" href="/@admin/assets/css/font-awesome.min.css">
  <link rel="stylesheet" href="/@admin/assets/css/admin.min.css">
  <link rel="stylesheet" href="/@admin/assets/css/styles.css">
</head>
<body class="hold-transition sidebar-mini layout-fixed">
<div class="wrapper">
  <nav class="main-header navbar navbar-expand bg-white navbar-light border-bottom">
    <ul class="navbar-nav">
      <li class="nav-item">
        <a class="nav-link" data-widget="pushmenu" href="#"><i class="fa fa-bars"></i></a>
      </li>
    </ul>
    </nav>
  <aside class="main-sidebar sidebar-dark-primary elevation-4">
    <div class="sidebar">
      <nav class="mt-2">
        <ul class="nav nav-pills nav-sidebar flex-column" role="menu" data-accordion="false">
          <li class="nav-item">
            <a href="/@admin" class="nav-link<#if !space??> active</#if>">
              <i class="nav-icon fa fa-dashboard"></i>
              <p>Dashboard</p>
            </a>
          </li>  
          <li class="nav-item">
            <a href="/@admin/logger" class="nav-link<#if space?? && space == "logger"> active</#if>">
              <i class="nav-icon fa fa-file-text"></i>
              <p>Logger</p>
            </a> 
                      </li>
                      <li class="nav-item">
            <a href="/@admin/routes" class="nav-link<#if space?? && space == "routes"> active</#if>">
              <i class="nav-icon fa fa-arrows-alt"></i>
              <p>Routes</p>
            </a>  
                      </li>
                      <li class="nav-item">
            <a href="/@admin/scheduler" class="nav-link<#if space?? && space == "scheduler"> active</#if>">
              <i class="nav-icon fa fa-calendar"></i>
              <p>Scheduler</p>
            </a>  
                      </li>
                      <li class="nav-item">
            <a href="/@admin/metrics" class="nav-link<#if space?? && space == "metrics"> active</#if>">
              <i class="nav-icon fa fa-bar-chart"></i>
              <p>Metrics</p>
            </a>   
                      </li>
                      <li class="nav-item">
            <a href="/@admin/tools" class="nav-link<#if space?? && space == "tools"> active</#if>">
              <i class="nav-icon fa fa-wrench"></i>
              <p>Config tools</p>
            </a>   
                      </li>
                      <li class="nav-item">
            <a href="/@admin/health" target="_blank" class="nav-link">
              <i class="nav-icon fa fa-heartbeat"></i>
              <p>Health</p>
            </a>                                                             
          </li>
        </ul>
      </nav>
    </div>
  </aside>