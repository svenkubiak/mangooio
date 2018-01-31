<#setting number_format=",##0">
<#setting locale="en_US">
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>mangoo I/O | Control Panel</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <style><#include "css/bootstrap.min.css"></style>
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css">	
    <style><#include "css/AdminLTE.min.css"></style>
    <style><#include "css/skin-blue.min.css"></style>
  </head>
  <body class="hold-transition skin-blue sidebar-mini">
    <div class="wrapper">
      <header class="main-header">
        <a href="/" class="logo">
          <span class="logo-mini">I/O</span>
          <span class="logo-lg"><b>mangoo</b> I/O</span>
        </a>
        <nav class="navbar navbar-static-top" role="navigation">
        <a href="#" class="sidebar-toggle" data-toggle="offcanvas" role="button">
            <span class="sr-only">Toggle navigation</span>
          </a>
        </nav>
      </header>
      <aside class="main-sidebar">
        <section class="sidebar">
          <ul class="sidebar-menu">
            <li <#if !space??>class="active"</#if>><a href="/@admin"><i class="fa fa-dashboard"></i><span>Dashboard</span></a></li>
            <li <#if space?? && space == 'logger'>class="active"</#if>><a href="/@admin/logger"><i class="fa fa-file-o"></i><span>Logger</span></a></li>
            <li <#if space?? && space == 'routes'>class="active"</#if>><a href="/@admin/routes"><i class="fa fa-arrows"></i><span>Routes</span></a></li>
            <li <#if space?? && space == 'scheduler'>class="active"</#if>><a href="/@admin/scheduler"><i class="fa fa-calendar"></i><span>Scheduler</span></a></li>
            <li <#if space?? && space == 'metrics'>class="active"</#if>><a href="/@admin/metrics"><i class="fa fa-signal"></i><span>Metrics</span></a></li>
            <li <#if space?? && space == 'tools'>class="active"</#if>><a href="/@admin/tools"><i class="fa fa-wrench"></i><span>Tools</span></a></li>
            <li><a href="/@admin/health"><i class="fa fa-feed"></i><span>Health</span></a></li>            
          </ul>
        </section>
      </aside>
      <div class="content-wrapper">
        <section class="content">