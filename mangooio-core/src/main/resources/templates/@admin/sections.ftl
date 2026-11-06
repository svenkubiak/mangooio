<#-- Sections of the dashboard that the AdminStream pushes again while the page is open -->
<#macro stat label value unit="" tone="" hint="">
<article class="stat<#if tone?has_content> ${tone}</#if>">
    <span class="stat-label">${label}</span>
    <span class="stat-value">${value}<#if unit?has_content><small>${unit}</small></#if></span>
    <#if hint?has_content><span class="stat-hint">${hint}</span></#if>
</article>
</#macro>
<#macro time label value hint="">
<#if value gte 0><@stat label=label value=value unit="ms" hint=hint/><#else><@stat label=label value="–" hint=hint/></#if>
</#macro>
<#macro instancesSection>
<h2 class="section-title">Instances (${instances?size})</h2>
<article class="table-card instances">
    <div class="overflow-auto">
        <table class="striped">
            <thead>
            <tr>
                <th scope="col">Instance</th>
                <th scope="col">Version</th>
                <th scope="col" class="num">Last seen</th>
                <th scope="col" class="num">Heap</th>
                <th scope="col" class="num">Connections</th>
                <th scope="col" class="num">Requests</th>
                <th scope="col" class="num">Errors</th>
                <th scope="col" class="num">Avg time</th>
                <th scope="col" class="num">Failed events</th>
            </tr>
            </thead>
            <tbody>
            <#list instances as instance>
                <tr<#if instance.stale> class="stale"</#if>>
                    <td><code title="Started ${instance.started}">${instance.id}</code><#if instance.current> <span class="badge badge-success">This instance</span></#if><#if instance.stale> <span class="badge badge-danger">No heartbeat</span></#if></td>
                    <td>${instance.version}</td>
                    <td class="num">${instance.seen} s</td>
                    <td class="num">${instance.heap}</td>
                    <td class="num">${instance.connections}</td>
                    <td class="num">${instance.requests}</td>
                    <td class="num<#if instance.errors gt 0> danger</#if>">${instance.errors}</td>
                    <td class="num"><#if instance.avgRequestTime gte 0>${instance.avgRequestTime} ms<#else>–</#if></td>
                    <td class="num<#if instance.failedEvents gt 0> danger</#if>">${instance.failedEvents}</td>
                </tr>
            </#list>
            </tbody>
            <#if instances?size gt 1>
            <tfoot>
            <tr>
                <th scope="row" colspan="4">Total</th>
                <td class="num">${instanceTotals.connections}</td>
                <td class="num">${instanceTotals.requests}</td>
                <td class="num">${instanceTotals.errors}</td>
                <td class="num">–</td>
                <td class="num">${instanceTotals.failedEvents}</td>
            </tr>
            </tfoot>
            </#if>
        </table>
    </div>
</article>
<p class="section-note">Requests, errors and average time cover the last five minutes. Every instance reports every ten seconds<#if !shared>; without persistence only this instance is known</#if>.</p>
</#macro>
<#macro instanceSection>
<div class="stats">
    <@stat label="Application started" value=uptime hint=system.started/>
    <@stat "Java" system.java/>
    <@stat label="Open connections" value=connections hint="Clients connected to your server sent event and WebSocket routes"/>
</div>
<article class="meter">
    <div class="meter-label">
        <span>Heap memory</span>
        <span>${system.heapUsedReadable} of ${system.heapMaxReadable}</span>
    </div>
    <progress value="${system.heapUsed?c}" max="${system.heapMax?c}" aria-label="Heap memory usage"></progress>
</article>
</#macro>
<#macro requestsSection>
<#assign avgTime = (totalRequests gt 0)?then(avgRequestTime, -1)>
<div class="stats">
    <@stat label="Requests" value=recentRequests hint="${totalRequests} since start"/>
    <@stat label="Error rate" value=recentErrorRate unit="%" hint="${errorRate} % since start"/>
    <@time label="Avg processing time" value=recentAvgRequestTime hint=(avgTime gte 0)?then("${avgTime} ms since start", "– since start")/>
    <@time label="Max processing time" value=recentMaxRequestTime hint=(maxRequestTime gte 0)?then("${maxRequestTime} ms since start", "– since start")/>
</div>
</#macro>
<#macro statusSection>
<#list statusClasses as name, count>
<div class="status-row status-${name}">
    <span class="status-name">${name}</span>
    <progress value="${count?c}" max="${(totalRequests gt 0)?then(totalRequests, 1)?c}" aria-label="${name} responses"></progress>
    <span class="status-count">${count}</span>
    <span class="status-share"><#if totalRequests gt 0>${(count * 100 / totalRequests)?string["0.0"]}<#else>0</#if> %</span>
</div>
</#list>
<#if metrics?has_content>
<footer class="status-codes">
    <#list metrics as code, count><span class="badge status-${code?substring(0, 1)}xx">${code} · ${count}</span></#list>
</footer>
</#if>
</#macro>
<#macro eventsSection>
<div class="stats">
    <@stat "Handled events" events/>
    <@stat label="Failed events" value=failedEvents tone=(failedEvents gt 0)?then("stat-danger", "")/>
</div>
</#macro>
<#macro routesSection>
<#if failedRoutes?has_content>
<div class="overflow-auto">
    <table class="striped">
        <thead>
        <tr>
            <th scope="col">Route</th>
            <th scope="col" class="num">Requests</th>
            <th scope="col" class="num">4xx</th>
            <th scope="col" class="num">5xx</th>
            <th scope="col">Status codes</th>
        </tr>
        </thead>
        <tbody>
        <#list failedRoutes as route>
            <tr>
                <td><code>${route.route}</code></td>
                <td class="num">${route.requests}</td>
                <td class="num">${route.clientErrors}</td>
                <td class="num<#if route.serverErrors gt 0> danger</#if>">${route.serverErrors}</td>
                <td><#list route.codes as code, count><span class="badge status-${code?substring(0, 1)}xx">${code} · ${count}</span><#sep> </#sep></#list></td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
<#else>
<p class="empty">No failed requests since start</p>
</#if>
</#macro>
<#macro failuresSection>
<#if failures?has_content>
<div class="overflow-auto">
    <table class="striped">
        <thead>
        <tr>
            <th scope="col">Time</th>
            <th scope="col">Route</th>
            <th scope="col" class="num">Status</th>
        </tr>
        </thead>
        <tbody>
        <#list failures as failure>
            <tr>
                <td>${failure.time}</td>
                <td><code>${failure.method} ${failure.route}</code></td>
                <td class="num danger">${failure.status?c}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>
<#else>
<p class="empty">No server errors since start</p>
</#if>
</#macro>
