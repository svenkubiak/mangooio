<#setting number_format=",##0">
<#setting locale="en_US">
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>mangoo I/O | Control Panel</title>
    <meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
    <style><#include "../defaults/css/bootstrap.min.css"></style>
    <style><#include "../defaults/css/AdminLTE.min.css"></style>
    <style><#include "../defaults/css/skin-blue.min.css"></style>
    <style><#include "../defaults/css/blue.min.css"></style>
    <style><#include "../defaults/css/jquery-jvectormap.min.css"></style>
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