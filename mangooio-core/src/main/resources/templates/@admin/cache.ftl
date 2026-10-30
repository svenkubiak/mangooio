<#import "layout.ftl" as layout>
<@layout.page title="Cache" active="cache" subtitle="Statistics of all registered caches on this instance">
<article class="table-card">
    <div class="overflow-auto">
        <table class="striped">
            <thead>
            <tr>
                <th scope="col">Cache name</th>
                <th scope="col" class="num">Evictions</th>
                <th scope="col" class="num">Requests</th>
                <th scope="col" class="num">Hits</th>
                <th scope="col" class="num">Hit rate</th>
                <th scope="col" class="num">Misses</th>
                <th scope="col" class="num">Miss rate</th>
                <th scope="col"><span class="visually-hidden">Actions</span></th>
            </tr>
            </thead>
            <tbody>
            <#list statistics as name, statistic>
                <tr>
                    <td><code>${name}</code></td>
                    <td class="num">${statistic.evictionCount()}</td>
                    <td class="num">${statistic.requestCount()}</td>
                    <td class="num">${statistic.hitCount()}</td>
                    <td class="num">${statistic.hitRate()?string["0.##"]}</td>
                    <td class="num">${statistic.missCount()}</td>
                    <td class="num">${statistic.missRate()?string["0.##"]}</td>
                    <td class="actions">
                        <form method="post" action="/@admin/cache/clear" data-confirm="Clear cache ${name} on ${scope}?">
                            <input type="hidden" name="name" value="${name}">
                            <@csrfform/>
                            <button type="submit" class="secondary outline small">Clear</button>
                        </form>
                    </td>
                </tr>
            <#else>
                <tr><td colspan="8" class="empty">No caches registered</td></tr>
            </#list>
            </tbody>
        </table>
    </div>
</article>
</@layout.page>
