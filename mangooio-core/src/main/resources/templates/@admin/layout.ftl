<#macro document title class>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="color-scheme" content="light dark">
    <meta name="robots" content="noindex, nofollow">
    <title>${title} · mangoo I/O Admin</title>
    <link rel="apple-touch-icon" sizes="180x180" href="/@admin/assets/favicons/apple-touch-icon.png">
    <link rel="icon" type="image/png" sizes="32x32" href="/@admin/assets/favicons/favicon-32x32.png">
    <link rel="icon" type="image/png" sizes="16x16" href="/@admin/assets/favicons/favicon-16x16.png">
    <link rel="manifest" href="/@admin/assets/favicons/site.webmanifest">
    <link rel="stylesheet" href="/@admin/assets/css/blades.min.css">
    <link rel="stylesheet" href="/@admin/assets/css/admin.min.css">
</head>
<body class="${class}">
<#nested>
<script src="/@admin/assets/js/admin.min.js" defer></script>
</body>
</html>
</#macro>

<#macro icon name>
<svg class="icon" viewBox="0 0 24 24" aria-hidden="true" focusable="false"><#switch name><#case "dashboard"><rect x="3" y="3" width="7" height="9" rx="1.5"/><rect x="14" y="3" width="7" height="5" rx="1.5"/><rect x="14" y="12" width="7" height="9" rx="1.5"/><rect x="3" y="16" width="7" height="5" rx="1.5"/><#break><#case "routes"><path d="M9 6h11"/><path d="M9 12h11"/><path d="M9 18h11"/><path d="M4 6h.01"/><path d="M4 12h.01"/><path d="M4 18h.01"/><#break><#case "scheduler"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/><#break><#case "cache"><ellipse cx="12" cy="5" rx="8" ry="3"/><path d="M4 5v14c0 1.7 3.6 3 8 3s8-1.3 8-3V5"/><path d="M4 12c0 1.7 3.6 3 8 3s8-1.3 8-3"/><#break><#case "security"><path d="M12 3l8 3v6c0 4.5-3.4 8.3-8 9-4.6-.7-8-4.5-8-9V6z"/><#break><#case "verified"><path d="M12 3l8 3v6c0 4.5-3.4 8.3-8 9-4.6-.7-8-4.5-8-9V6z"/><path d="m9 12 2 2 4-4"/><#break><#case "logout"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="m16 17 5-5-5-5"/><path d="M21 12H9"/><#break><#case "copy"><rect x="9" y="9" width="12" height="12" rx="2"/><path d="M5 15H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v1"/><#break><#case "warning"><path d="M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z"/><path d="M12 9v4"/><path d="M12 17h.01"/><#break><#case "info"><circle cx="12" cy="12" r="9"/><path d="M12 16v-4"/><path d="M12 8h.01"/><#break></#switch></svg>
</#macro>

<#macro brand>
<span class="brand"><span class="brand-mark" aria-hidden="true">m</span><span class="brand-name">mangoo I/O</span></span>
</#macro>

<#macro item label href key active>
<li><a href="${href}"<#if key == active> aria-current="page"</#if>><@icon key/>${label}</a></li>
</#macro>

<#macro page title active subtitle="">
<#setting number_format=",##0">
<#setting locale="en_US">
<@document title=title class="app">
<header class="topbar">
    <nav class="container" aria-label="Main">
        <ul class="topbar-brand">
            <li><a href="/@admin" aria-label="Dashboard"><@brand/></a></li>
            <#if mode??><li><span class="badge mode-${mode?lower_case}" title="Application mode">${mode}</span></li></#if>
        </ul>
        <ul class="menu">
            <@item "Dashboard" "/@admin" "dashboard" active/>
            <@item "Routes" "/@admin/routes" "routes" active/>
            <@item "Scheduler" "/@admin/scheduler" "scheduler" active/>
            <@item "Cache" "/@admin/cache" "cache" active/>
            <@item "Security" "/@admin/security" "security" active/>
        </ul>
        <ul class="topbar-actions">
            <li><a class="logout" href="/@admin/logout"><@icon "logout"/>Logout</a></li>
        </ul>
    </nav>
</header>
<main class="container">
    <hgroup class="page-header">
        <h1>${title}</h1>
        <#if subtitle?has_content><p>${subtitle}</p></#if>
    </hgroup>
    <#if (flash.success)?has_content><div class="alert alert-success" role="status"><@icon "verified"/><span>${flash.success}</span></div></#if>
    <#if (flash.error)?has_content><div class="alert alert-danger" role="alert"><@icon "warning"/><span>${flash.error}</span></div></#if>
    <#nested>
</main>
<footer class="container app-footer">
    <small>mangoo I/O ${version!}<#if instance??> · Instance <code>${instance}</code></#if></small>
</footer>
</@document>
</#macro>

<#macro auth title subtitle>
<@document title=title class="auth">
<main>
    <div class="auth-wrapper">
        <article class="auth-card">
            <hgroup>
                <h1>${title}</h1>
                <p>${subtitle}</p>
            </hgroup>
            <#nested>
        </article>
    </div>
</main>
</@document>
</#macro>
