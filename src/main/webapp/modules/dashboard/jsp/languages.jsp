<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
<div id="piechart" class="pie"></div>
</div>
<script type="text/javascript">
var url = "${info.currentURL}".replace("/edit/", "/ajax/")+"?webaction=dashboard.readTracker&type=languages";
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
			data[i] = v;
			console.log(""+i+" = "+v);
		});	
	jQuery.plot(jQuery("#piechart"), data, {
			series: {
				pie: {
					show: true
				}
			}
	});
	
});	

</script>