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
		<link rel="shortcut icon" href="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAlFJREFUeNqUU8tOFEEUPVVdNV3dPe8xYRBnjGhmBgKjKzCIiQvBoIaNbly5Z+PSv3Aj7DSiP2B0rwkLGVdGgxITSCRIJGSMEQWZR3eVt5sEFBgTb/dN1yvnnHtPNTPG4PqdHgCMXnPRSZrpSuH8vUJu4DE4rYHDGAZDX62BZttHqTiIayM3gGiXQsgYLEvATaqxU+dy1U13YXapXptpNHY8iwn8KyIAzm1KBdtRZWErpI5lEWTXp5Z/vHpZ3/wyKKwYGGOdAYwR0EZwoezTYApBEIObyELl/aE1/83cp40Pt5mxqCKrE4Ck+mVWKKcI5tA8BLEhRBKJLjez6a7MLq7XZtp+yyOawwCBtkiBVZDKzRk4NN7NQBMYPHiZDFhXY+p9ff7F961vVcnl4R5I2ykJ5XFN7Ab7Gc61VoipNBKF+PDyztu5lfrSLT/wIwCxq0CAGtXHZTzqR2jtwQiXONma6hHpj9sLT7YaPxfTXuZdBGA02Wi7FS48YiTfj+i2NhqtdhP5RC8mh2/Op7y0v6eAcWVLFT8D7kWX5S9mepp+C450MV6aWL1cGnvkxbwHtLW2B9AOkLeUd9KEDuh9fl/7CEj7YH5g+3r/lWfF9In7tPz6T4IIwBJOr1SJyIGQMZQbsh5P9uBq5VJtqHh2mo49pdw5WFoEwKWqWHacaWOjQXWGcifKo6vj5RGS6zykI587XeUIQDqJSmAp+lE4qt19W5P9o8+Lma5DcjsC8JiT607lMVkdqQ0Vyh3lHhmh52tfNy78ajXv0rgYzv8nfwswANuk+7sD/Q0aAAAAAElFTkSuQmCC">
	    <style>body,html,pre{margin:0;padding:0;font-family:Monaco,'Lucida Console',monospace;background:#ECECEC}h1{margin:0;background:#A31012;padding:20px 45px;color:#fff;text-shadow:1px 1px 1px rgba(0,0,0,.3);border-bottom:1px solid #690000;font-size:28px}p#detail{margin:0;padding:15px 45px;background:#F5A0A0;border-top:4px solid #D36D6D;color:#730000;text-shadow:1px 1px 1px rgba(255,255,255,.3);font-size:14px;border-bottom:1px solid #BA7A7A}p#detail input{background:-webkit-gradient(linear,0 0,0 100%,from(#AE1113),to(#A31012));border:1px solid #790000;padding:3px 10px;text-shadow:1px 1px 0 rgba(0,0,0,.5);color:#fff;border-radius:3px;cursor:pointer;font-family:Monaco,'Lucida Console';font-size:12px;margin:0 10px;display:inline-block;position:relative;top:-1px}h2,pre{margin:0;font-size:12px}h2{padding:5px 45px;background:#333;color:#fff;text-shadow:1px 1px 1px rgba(0,0,0,.3);border-top:4px solid #2a2a2a}pre,pre span.line{text-shadow:1px 1px 1px rgba(255,255,255,.5)}pre{border-bottom:1px solid #DDD;position:relative;overflow:hidden}pre span.line{text-align:right;display:inline-block;padding:5px;width:30px;background:#D6D6D6;color:#8B8B8B;font-weight:700}pre.error span.line,pre.error span.marker{background:#A31012;text-shadow:1px 1px 1px rgba(0,0,0,.3)}pre span.code{padding:5px;right:0;left:40px;position:absolute;}pre:first-child span.code{border-top:4px solid #CDCDCD}pre:first-child span.line{border-top:4px solid #B6B6B6}pre.error span.line{color:#fff}pre.error{color:#A31012}pre.error span.marker{color:#fff}</style>
	</head>
	<body id="play-error-page" style="word-wrap: break-word;">
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
<h2 style="padding-left:5px; padding-bottom: 5px">Stacktrace</h2>
<br />
<pre>
 ${causeSource}
<#list stackTraces as stackTrace>
    at ${stackTrace}
</#list>
<#else>
</pre>
<pre style="padding-top:15px; padding-left: 15px;padding-right: 15px;font-size: 13px; font-family:Monaco,'Lucida Console',monospace">
<#if exceptions?has_content>
<#list exceptions as exception>${exception}<br/></#list>
</#if>
</pre>
</#if>
	</body>
</html>