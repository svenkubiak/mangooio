<#import "../layout.ftl" as layout>   
<@layout.myLayout "Layout">This is a test!</@layout.myLayout>
<script>

var source = new EventSource("/sse");

source.addEventListener('message', function(e) {
  console.log(e.data);
}, false);

</script>