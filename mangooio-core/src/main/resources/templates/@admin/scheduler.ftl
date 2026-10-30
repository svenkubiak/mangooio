<#import "layout.ftl" as layout>
<@layout.page title="Scheduler" active="scheduler" subtitle="Scheduled tasks and their current state">
<article class="table-card">
    <div class="overflow-auto">
        <table class="striped">
            <thead>
            <tr>
                <th scope="col">Class</th>
                <th scope="col">Method</th>
                <th scope="col">Run at</th>
                <th scope="col">Next</th>
                <th scope="col">State</th>
            </tr>
            </thead>
            <tbody>
            <#list scheduler.schedules as schedule>
                <#assign state = schedule.scheduledFuture.state().name()>
                <tr>
                    <td><code>${schedule.clazz}</code></td>
                    <td><code>${schedule.method}</code></td>
                    <td>${schedule.runAt}</td>
                    <td><#assign next = schedule.next()!><#if next?has_content>${next.format('dd.MM.yyyy HH:mm:ss')}<#else>-</#if></td>
                    <td><#if state == "SUCCESS" || state == "RUNNING"><span class="badge badge-success">RUNNING</span><#elseif state == "FAILED"><span class="badge badge-danger">${state}</span><#else><span class="badge">${state}</span></#if></td>
                </tr>
            <#else>
                <tr><td colspan="5" class="empty">No scheduled tasks</td></tr>
            </#list>
            </tbody>
        </table>
    </div>
</article>
</@layout.page>
