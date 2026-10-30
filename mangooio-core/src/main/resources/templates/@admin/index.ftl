<#import "layout.ftl" as layout>
<#import "sections.ftl" as sections>
<@layout.page title="Dashboard" active="dashboard" subtitle="Overview of the running application" live=true>
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
<section data-live="instances">
<@sections.instancesSection/>
</section>
<section>
    <h2 class="section-title">This instance</h2>
    <div data-live="instance">
    <@sections.instanceSection/>
    </div>
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
    <div data-live="requests">
    <@sections.requestsSection/>
    </div>
    <p class="section-note">Values of the last five minutes on this instance.<#if otlp> The same requests are exported to OpenTelemetry as <code>http.server.request.duration</code> for history and alerting.<#else> For history and alerting enable OpenTelemetry with <code>otlp.enable</code>.</#if></p>
</section>
<section>
    <h2 class="section-title">Responses by status since start</h2>
    <article class="status" data-live="status">
        <@sections.statusSection/>
    </article>
</section>
<#else>
<div class="alert alert-info" role="status"><@layout.icon "info"/><span>Request metrics are disabled. Set <code>metrics.enable: true</code> in your configuration to collect them.</span></div>
</#if>
<section>
    <h2 class="section-title">Events</h2>
    <div data-live="events">
    <@sections.eventsSection/>
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
    <div class="feature-groups">
        <#list features as group, entries>
        <article class="features">
            <h3>${group}</h3>
            <ul>
                <#list entries as feature>
                <li class="tone-${feature.tone}"><span class="dot" aria-hidden="true"></span><span class="feature-label">${feature.label}</span><span class="feature-value" title="${feature.value}">${feature.value}</span><#if feature.tone == "warn"><span class="visually-hidden"> (warning)</span></#if></li>
                </#list>
            </ul>
        </article>
        </#list>
    </div>
</section>
</@layout.page>
