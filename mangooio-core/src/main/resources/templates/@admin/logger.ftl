<#include "header.ftl">
<div class="container">
	<div class="columns">
		<div class="column">
			<section class="hero is-info is-small">
				<div class="hero-body">
					<p class="title">Logger</p>
				</div>
			</section>
		</div>
	</div>
	<div class="logger-notification">
		<div class="columns">
			<div class="column">
				<div class="notification is-success">
					Logger settings have been updated!
				</div>
			</div>
		</div>
	</div>
	<div class="columns">
		<div class="column">
			<input id="filter" type="text" name="table_search" class="input" placeholder="Start typing what you are looking for...">
		</div>
	</div>
	<div class="columns">
		<div class="column">
			<div class="table-container">
				<table class="table is-fullwidth">
					<tbody class="searchable">
					<#list loggers as logger>
						<tr>
							<td><b>${logger.name}</b>
								<p>
								<div class="select is-fullwidth">
									<select name="level" class="loglevel" data-class="${logger.name}">
										<option value="ALL"<#if logger.level == "ALL"> selected</#if>>ALL</option>
										<option value="TRACE"<#if logger.level == "TRACE"> selected</#if>>TRACE</option>
										<option value="DEBUG"<#if logger.level == "DEBUG"> selected</#if>>DEBUG</option>
										<option value="INFO"<#if logger.level == "INFO"> selected</#if>>INFO</option>
										<option value="WARN"<#if logger.level == "WARN"> selected</#if>>WARN</option>
										<option value="ERROR"<#if logger.level == "ERROR"> selected</#if>>ERROR</option>
										<option value="FATAL"<#if logger.level == "FATAL"> selected</#if>>FATAL</option>
										<option value="OFF"<#if logger.level == "OFF"> selected</#if>>OFF</option>
									</select>
								</div>
								</p>
							</td>
						</tr>
					</#list>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<#include "footer.ftl">