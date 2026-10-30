<#import "layout.ftl" as layout>
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
<@layout.page title="Dashboard" active="dashboard" subtitle="Overview of the running application">
<#setting locale="${mangooioAdminLocale}">
<#if warnings?has_content>
<div class="alert alert-warning" role="alert">
    <@layout.icon "warning"/>
    <div>
        <strong>Configuration warnings</strong>
        <ul>
            <#list warnings as warning><li>${warning}</li></#list>
        </ul>
    </div>
</div>
</#if>
<section>
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
</section>
<section>
    <h2 class="section-title">This instance</h2>
    <div class="stats">
        <@stat label="Application started" value=uptime hint=system.started/>
        <@stat "Java" system.java/>
        <@stat "Open connections" connections/>
    </div>
    <article class="meter">
        <div class="meter-label">
            <span>Heap memory</span>
            <span>${system.heapUsedReadable} of ${system.heapMaxReadable}</span>
        </div>
        <progress value="${system.heapUsed?c}" max="${system.heapMax?c}" aria-label="Heap memory usage"></progress>
    </article>
</section>
<#if enabled>
<section>
    <div class="section-header">
        <h2 class="section-title">Requests</h2>
        <form method="post" action="/@admin/metrics/reset" data-confirm="Reset all request metrics on ${scope}?">
            <@csrfform/>
            <button type="submit" class="secondary outline small">Reset metrics</button>
        </form>
    </div>
    <#assign avgTime = (totalRequests gt 0)?then(avgRequestTime, -1)>
    <div class="stats">
        <@stat label="Requests" value=recentRequests hint="${totalRequests} since start"/>
        <@stat label="Error rate" value=recentErrorRate unit="%" hint="${errorRate} % since start"/>
        <@time label="Avg processing time" value=recentAvgRequestTime hint=(avgTime gte 0)?then("${avgTime} ms since start", "– since start")/>
        <@time label="Max processing time" value=recentMaxRequestTime hint=(maxRequestTime gte 0)?then("${maxRequestTime} ms since start", "– since start")/>
    </div>
    <p class="section-note">Values of the last five minutes on this instance.<#if otlp> The same requests are exported to OpenTelemetry as <code>http.server.request.duration</code> for history and alerting.<#else> For history and alerting enable OpenTelemetry with <code>otlp.enable</code>.</#if></p>
</section>
<section>
    <h2 class="section-title">Responses by status since start</h2>
    <article class="status">
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
    </article>
</section>
<#else>
<div class="alert alert-info" role="status"><@layout.icon "info"/><span>Request metrics are disabled. Set <code>metrics.enable: true</code> in your configuration to collect them.</span></div>
</#if>
<section>
    <h2 class="section-title">Events</h2>
    <div class="stats">
        <@stat "Handled events" events/>
        <@stat label="Failed events" value=failedEvents tone=(failedEvents gt 0)?then("stat-danger", "")/>
    </div>
    <#if queues?has_content>
    <article class="table-card queues">
        <div class="overflow-auto">
            <table class="striped">
                <thead>
                <tr>
                    <th scope="col">Queue</th>
                    <th scope="col">Subscribers</th>
                </tr>
                </thead>
                <tbody>
                <#list queues as queue, classes>
                    <tr>
                        <td><code>${queue}</code></td>
                        <td><#list classes as subscriber><code>${subscriber}</code><#sep><br></#sep></#list></td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>
    </article>
    </#if>
</section>
<section>
    <h2 class="section-title">Features</h2>
    <article class="features">
        <ul>
            <#list features as name, active>
            <li class="feature-<#if active>on<#else>off</#if>"><span class="dot" aria-hidden="true"></span>${name}<span class="visually-hidden"><#if active> enabled<#else> disabled</#if></span></li>
            </#list>
        </ul>
    </article>
</section>
</@layout.page>
