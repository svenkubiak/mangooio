<#if subject.authenticated>
	Hello ${subject.username}!
	//Display navigation for authenticated user
<#else>
	Hello Guest!
	//Display navigation for not authenticated user
</#if>