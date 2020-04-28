#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<${symbol_pound}import "../layout.ftl" as layout>
<@layout.myLayout "Layout">
<ul>
<#list persons as person>
<li>${person.firstname} ${person.lastname}, ${person.age} years old</li>
</#list>
</ul>
</@layout.myLayout>
