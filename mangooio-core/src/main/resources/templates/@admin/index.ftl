<#include "header.ftl">
<#setting locale="${mangooioAdminLocale}">
<div class="container">
    <div class="columns">
        <div class="column">
            <section class="hero is-info is-small">
                <div class="hero-body">
                    <p class="title">Dashboard</p>
                </div>
            </section>
            <br>
            <#if warnings?has_content>
                <#list warnings as warning>
                    <div class="notification is-warning">
                        ${warning}
                    </div>
                </#list>
            </#if>
        </div>
    </div>
    <div class="columns">
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${uptime}</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Application started</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${events}</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Handled events</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${subscribers}</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Total subscribers</span></p>
                </footer>
            </div>
        </div>
    </div>
    <#if enabled>
    <div class="columns">
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${totalRequests}</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Total requests</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${dataSend}</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Data send</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${errorRate} %</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Error rate</span></p>
                </footer>
            </div>
        </div>
    </div>
    <div class="columns">
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${minRequestTime} ms</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Min processing time</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${avgRequestTime} ms</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Avg processing time</span></p>
                </footer>
            </div>
        </div>
        <div class="column">
            <div class="card">
                <div class="card-content">
                    <p class="title">${maxRequestTime} ms</p>
                </div>
                <footer class="card-footer">
                    <p class="card-footer-item"><span>Max processing time</span></p>
                </footer>
            </div>
        </div>
    </div>
    </#if>
    <div class="columns">
        <div class="column">
            <a href="/@admin" aria-current="page">Running on mangoo I/O ${version}</a>
        </div>
    </div>
</div>
<#include "footer.ftl">