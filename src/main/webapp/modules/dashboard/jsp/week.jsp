<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
<div id="weekchart" class="week"></div>
</div>
<script type="text/javascript">
var url = "${info.currentURL}".replace("/edit/", "/ajax/")+"?webaction=dashboard.readTracker&type=week";
jQuery.ajax({
	url : url,
	cache : false,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {	
	var data = [];
	jQuery.each(jsonObj, function(i, v) {				
			data[i] = [i,v];	
		});	
	/*data = [[0, 3], [1, 8], [2, 5], [3, 13]];
	for( var i = 0; i<20; i++) {
		//data[i] = { label: "Series"+(i+1), data: Math.floor(Math.random()*100)+1 }
		data[i] = [label:"s"+i,data:Math.floor(Math.random()*100)+1 ];
	}*/
	jQuery.plot(jQuery("#weekchart"), [	                          
	                           {
	                               data: data,
	                               bars: { show: true }
	                           }
	                           ]);
	
});	

</script>