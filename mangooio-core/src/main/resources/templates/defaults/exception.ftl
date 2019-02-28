<!DOCTYPE html>
<html lang="en">
	<!-- This is a refactored version of the dev error template of the Play Framework (https://www.playframework.com)
		 The original source can be found here:
		 https://github.com/playframework/playframework/blob/master/framework/src/play/src/main/scala/views/defaultpages/devError.scala.html	
	 -->
	<head>
		<title>500 Internal Server Error</title>
	    <meta charset="utf-8">
    	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    	<meta name="viewport" content="width=device-width, initial-scale=1">
	    <link rel="stylesheet" href="/@admin/assets/css/exception.css">
	</head>
	<body id="play-error-page">
		<h1><#if templateException>Template<#else>Application</#if> exception</h1>
		<p id="detail"><#if cause??>${cause}<#else>The application has encountered a template exception</#if></p>
		<h2><#if sourceCodePath??>In ${sourceCodePath}</#if></h2>
        <div id="source-code">
	        <#if sources?has_content>
				<p><#list sources as source><pre class="<#if source.cause>error</#if>"><span class="line">${source.line}:</span><span class="code marker">${source.content}</span></pre></#list></p>
		  	</#if>
		</div>
<#if !templateException>
<br /><br />
<h2 class="stacktrace">Stacktrace</h2>
<br />
<pre>
 ${causeSource}
<#list stackTraces as stackTrace>
    at ${stackTrace}
</#list>
<#else>
</pre>
<pre class="exception">
<#if exceptions?has_content>
<#list exceptions as exception>${exception}<br/></#list>
</#if>
</pre>
</#if>
	</body>
</html>