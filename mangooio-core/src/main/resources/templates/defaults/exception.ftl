<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Application Exception</title>
    <style><#include "css/bootstrap.min.css"></style>
	<style>pre{padding:0;font-family:Monaco,'Lucida Console',monospace;background:#ECECEC;margin:0;border-bottom:1px solid #DDD;position:relative;font-size:12px}pre span.line{text-align:right;display:inline-block;padding:5px;width:30px;background:#D6D6D6;color:#8B8B8B;font-weight:700}pre span.line.error{background:#C21600;color:#fff}pre span.stacktrace{padding:10px}pre span.route{padding:5px;position:absolute;right:0;left:40px}pre span.route.error{background:#C21600;color:#fff}pre span.route span.verb{display:inline-block;width:5%;min-width:50px;overflow:hidden;margin-right:10px}pre span.route span.path{display:inline-block;width:30%;min-width:200px;overflow:hidden;margin-right:10px}pre span.route span.call{display:inline-block;width:50%;overflow:hidden;margin-right:10px}pre:first-child span.route{border-top:4px solid #CDCDCD}pre:first-child span.line{border-top:4px solid #B6B6B6}pre.error span.line{background:#A31012;color:#fff;text-shadow:1px 1px 1px rgba(0,0,0,.3)}</style>
  <body>
    <div class="jumbotron">
      <div class="container">
        <h1>500 Internal Server Error</h1>
        <p>${method} ${url}</p>
      </div>
    </div>
    <div class="container">
      <div class="row">
        <div class="col-md-12">
          <h2>${cause}</h2>
          <h4>In ${sourceCodePath}</h4>
		  <p><#list sources as source><pre><span class="line <#if source.cause>error<#else>info</#if>">${source.line}</span><span class="route <#if source.cause>error</#if>">${source.content}</span></pre></#list></p>
        </div>
       </div>
	</div>
  </body>
</html>