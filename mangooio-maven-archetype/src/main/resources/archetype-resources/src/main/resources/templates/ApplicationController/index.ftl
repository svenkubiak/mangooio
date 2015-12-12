#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<${symbol_pound}import "../layout.ftl" as layout>
<@layout.myLayout "Layout">
${hello}
</@layout.myLayout>