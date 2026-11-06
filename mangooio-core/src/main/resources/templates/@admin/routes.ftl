<#import "layout.ftl" as layout>
<#macro connections title routes>
<section>
    <h2 class="section-title">${title}</h2>
    <article class="table-card">
        <div class="overflow-auto">
            <table class="striped">
                <thead>
                <tr>
                    <th scope="col">URL</th>
                    <th scope="col" class="num">Open connections</th>
                </tr>
                </thead>
                <tbody>
                <#list routes as url, count>
                    <tr>
                        <td><code>${url}</code></td>
                        <td class="num">${count}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </article>
</section>
</#macro>
<@layout.page title="Routes" active="routes" subtitle="Routes of the application on this instance, without the admin dashboard">
<section>
    <div class="section-header">
        <h2 class="section-title">Requests (${requests?size})</h2>
        <input type="search" class="filter" placeholder="Filter routes" aria-label="Filter routes" data-filter="request-routes">
    </div>
    <article class="table-card">
        <div class="overflow-auto">
            <table class="striped" id="request-routes">
                <thead>
                <tr>
                    <th scope="col">Method</th>
                    <th scope="col">URL</th>
                    <th scope="col">Controller</th>
                    <th scope="col">Options</th>
                </tr>
                </thead>
                <tbody>
                <#list requests as route>
                    <tr>
                        <td><#list route.methods?split(", ") as method><span class="badge method-${method?lower_case}">${method}</span><#sep> </#sep></#list></td>
                        <td><code>${route.url}</code></td>
                        <td><code title="${route.controllerName}">${route.controller}.${route.action}</code></td>
                        <td><#if route.authentication><span class="badge">Authentication</span> </#if><#if route.nonBlocking><span class="badge">Non-blocking</span></#if></td>
                    </tr>
                <#else>
                    <tr><td colspan="4" class="empty">No request routes</td></tr>
                </#list>
                </tbody>
            </table>
        </div>
    </article>
</section>
<#if serverSentEvents?has_content><@connections "Server-sent events" serverSentEvents/></#if>
<#if webSockets?has_content><@connections "WebSockets" webSockets/></#if>
<#if files?has_content>
<section>
    <h2 class="section-title">Static files</h2>
    <article class="table-card">
        <div class="overflow-auto">
            <table class="striped">
                <thead>
                <tr>
                    <th scope="col">URL</th>
                </tr>
                </thead>
                <tbody>
                <#list files as file>
                    <tr><td><code>${file}</code></td></tr>
                </#list>
                </tbody>
            </table>
        </div>
    </article>
</section>
</#if>
</@layout.page>
