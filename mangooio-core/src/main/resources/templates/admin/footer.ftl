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
	<script><#include "js/jquery-ui.min.js"></script>
    <script>$.widget.bridge('uibutton', $.ui.button);</script>
    <script><#include "js/bootstrap.min.js"></script>
    <script><#include "js/jquery-jvectormap-1.2.2.min.js"></script>
    <script><#include "js/jquery-jvectormap-world-mill-en.js"></script>
    <script><#include "js/jquery.knob.js"></script>
    <script><#include "js/app.min.js"></script>
    <script>
      $(function () {
        $(".knob").knob({
          draw: function () {
            if (this.$.data('skin') == 'tron') {
              var a = this.angle(this.cv)
                      , sa = this.startAngle
                      , sat = this.startAngle
                      , ea 
                      , eat = sat + a
                      , r = true;

              this.g.lineWidth = this.lineWidth;

              this.o.cursor
                      && (sat = eat - 0.3)
                      && (eat = eat + 0.3);

              if (this.o.displayPrevious) {
                ea = this.startAngle + this.angle(this.value);
                this.o.cursor
                        && (sa = ea - 0.3)
                        && (ea = ea + 0.3);
                this.g.beginPath();
                this.g.strokeStyle = this.previousColor;
                this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sa, ea, false);
                this.g.stroke();
              }

              this.g.beginPath();
              this.g.strokeStyle = r ? this.o.fgColor : this.fgColor;
              this.g.arc(this.xy, this.xy, this.radius - this.lineWidth, sat, eat, false);
              this.g.stroke();

              this.g.lineWidth = 2;
              this.g.beginPath();
              this.g.strokeStyle = this.o.fgColor;
              this.g.arc(this.xy, this.xy, this.radius - this.lineWidth + 1 + this.lineWidth * 2 / 3, 0, 2 * Math.PI, false);
              this.g.stroke();

              return false;
            }
          }
        });
      });
    </script>
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
					$this.parent().find(".help-block").show().delay(1000).fadeOut('slow');
	  			}
		});
    }); 
   
	});
  </script>
  </body>
</html>