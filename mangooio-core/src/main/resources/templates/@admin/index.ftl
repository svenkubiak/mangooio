<#include "header.ftl">
		<div class="container">
	        <div class="columns">
	            <div class="column is-12">
	                <section class="hero is-info welcome is-small">
	                    <div class="hero-body">
	                        <div class="container">
	                            <h1 class="title">
	                                Dashboard
	                            </h1>
	                        </div>
	                    </div>
	                </section>
	                <section class="info-tiles">
	                    <div class="tile is-ancestor has-text-centered">
	                        <div class="tile is-parent">
	                            <article class="tile is-child is-primary box">
	                                <p class="title">${totalRequests}</p>
	                                <p class="subtitle">Total requests</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${dataSend}</p>
	                                <p class="subtitle">Data send</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${errorRate} %</p>
	                                <p class="subtitle">Error rate</p>
	                            </article>
	                        </div>
	                    </div>
	                </section>
	                <section class="info-tiles">
	                    <div class="tile is-ancestor has-text-centered">
	                        <div class="tile is-parent is-primary">
	                            <article class="tile is-child box">
	                                <p class="title">${minRequestTime} ms</p>
	                                <p class="subtitle">Min process time</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${avgRequestTime} ms</p>
	                                <p class="subtitle">Avg process time</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${maxRequestTime} ms</p>
	                                <p class="subtitle">Max process time</p>
	                            </article>
	                        </div>
	                    </div>
	                </section>
	                <section class="info-tiles">
	                    <div class="tile is-ancestor has-text-centered">
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${prettytime(uptime)}</p>
	                                <p class="subtitle">Application started</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${events}</p>
	                                <p class="subtitle">EventBus events</p>
	                            </article>
	                        </div>
	                        <div class="tile is-parent">
	                            <article class="tile is-child box">
	                                <p class="title">${listeners}</p>
	                                <p class="subtitle">EventBus listeners</p>
	                            </article>
	                        </div>
	                    </div>
	                </section>    
	                <#if warnings?has_content>
		                <#list warnings as warning>
							<div class="notification is-warning">
						  		${warning}
							</div>
						</#list>  
					</#if>    
					<nav class="breadcrumb" aria-label="breadcrumbs">
					  <ul>
					    <li><a href="/@admin" aria-current="page">Running on mangoo I/O ${version}</a></li>
					  </ul>
					</nav>
	            </div>
	        </div>
	    </div>
<#include "footer.ftl">