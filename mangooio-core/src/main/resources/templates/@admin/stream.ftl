<#import "sections.ftl" as sections>
<#setting number_format=",##0">
<#setting locale="${mangooioAdminLocale}">
<#switch section>
<#case "instances"><@sections.instancesSection/><#break>
<#case "instance"><@sections.instanceSection/><#break>
<#case "requests"><@sections.requestsSection/><#break>
<#case "status"><@sections.statusSection/><#break>
<#case "events"><@sections.eventsSection/><#break>
</#switch>
