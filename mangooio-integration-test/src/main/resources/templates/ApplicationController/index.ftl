<#import "../layout.ftl" as layout> 
<@layout.myLayout "Layout">This is a test!</@layout.myLayout>
<script>
var source = new EventSource("http://localhost:50000/sseauth");
source.onmessage = function(event) {
	console.log(event.data)
};
</script>