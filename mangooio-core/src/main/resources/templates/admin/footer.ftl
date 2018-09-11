</div>
<script><#include "js/jquery.min.js"></script>
<script><#include "js/bootstrap.min.js"></script>
<script><#include "js/admin.min.js"></script>
<script><#include "js/stupidtable.min.js"></script>
	  <script>
	  $(document).ready(function(){
	  	var $table = $("table").stupidtable();
		var $th_to_sort = $table.find("thead th").eq(0);
		$th_to_sort.stupidsort();
	  	(function ($) {
	          $('#filter').keyup(function () {
	              var rex = new RegExp($(this).val(), 'i');
	              $('.searchable tr').hide();
	              $('.searchable tr').filter(function () {
	                  return rex.test($(this).text());
	              }).show();
	          })
	      }(jQuery));
	      
	      $("#keypair").click(function() {
			$.ajax({
	  			type: "POST",
	  			processData: false,
	  			contentType : 'application/json',
	  			url: "/@admin/tools/ajax",
	  			data: JSON.stringify({ "function": "keypair"}),
	  			dataType: "json",
	  			success: function(data){
					var keypair = jQuery.parseJSON(data);
					$("#publickey").val(keypair.publickey);
					$("#privatekey").val(keypair.privatekey);
	  			}
			});
		  });
		  
		  $("#encrypt").click(function() {
		  	var cleartext = $("#cleartext").val();
		  	var pubkey = $("#pubkey").val();
		  	
			$.ajax({
	  			type: "POST",
	  			processData: false,
	  			contentType : 'application/json',
	  			url: "/@admin/tools/ajax",
	  			data: JSON.stringify({ "function": "encrypt", "cleartext" : cleartext, "key" : pubkey }),
	  			dataType: "json",
	  			success: function(data){
					$("#encryptedvalue").val(data);
	  			}
			});
		  });	
	
		$( ".loglevel" ).change(function() {
			var $this = $(this);
			var level = $this.val();
			var clazz = $(this).data("class");
			
			$.ajax({
		  			type: "POST",
		  			processData: false,
		  			contentType : 'application/json',
		  			url: "/@admin/logger/ajax",
		  			data: JSON.stringify({ "class": clazz, "level" : level }),
		  			dataType: "text",
		  			success: function(data){}
			});
	    }); 
	    
		});
	  </script>
</body>
</html>