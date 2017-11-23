      </section>
      </div>
      <footer class="main-footer">
        <div class="pull-right">
          <b>mangoo I/O version:</b> ${version}
        </div>
        <strong>&nbsp;</strong>
      </footer>
    <div class="control-sidebar-bg"></div>
    </div>
	<script><#include "js/jquery.min.js"></script>
    <script><#include "js/bootstrap.min.js"></script>
    <script><#include "js/app.min.js"></script>
	<script type="text/javascript"><#include "js/stupidtable.min.js"></script>
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
	          $('#additionalfilter').keyup(function () {
	              var rex = new RegExp($(this).val(), 'i');
	              $('.additionalsearchable tr').hide();
	              $('.additionalsearchable tr').filter(function () {
	                  return rex.test($(this).text());
	              }).show();
	          })
	      }(jQuery));
	      
	      $("#hash").click(function() {
			$.ajax({
	  			type: "POST",
	  			processData: false,
	  			contentType : 'application/json',
	  			url: "/@admin/tools/ajax",
	  			data: JSON.stringify({ "function": "hash", "cleartext" : $("#hash_cleartext").val(), "key" : "" }),
	  			dataType: "json",
	  			success: function(data){
					$("#hashedvalue").val(data);
	  			}
			});
		  });
		  
		  $("#encrypt").click(function() {
		  	var cleartext = $("#encrypt_cleartext").val();
		  	var key = $("#encrypt_key").val();
		  	
		  	if (key.length > 0 && key.length != 32) {
		  		$("#key").addClass("has-error");
		  		$("#key-help").text("Encryption key must be exactly 32 characters. Currently " + key.length + " characters.");
		  		$("#key-help").show();
		  	}
		  	
		  	if (key.length == 0 || key.length == 32) {
		  		$("#key").removeClass("has-error");
		  		$("#key-help").hide();
				$.ajax({
		  			type: "POST",
		  			processData: false,
		  			contentType : 'application/json',
		  			url: "/@admin/tools/ajax",
		  			data: JSON.stringify({ "function": "encrypt", "cleartext" : cleartext, "key" : key }),
		  			dataType: "json",
		  			success: function(data){
						$("#encryptedvalue").val(data);
		  			}
				});
		  	}
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
		  			success: function(data){
						$this.parent().addClass("has-success");
		  			}
			});
	    }); 
	   
		});
	  </script>
  </body>
</html>