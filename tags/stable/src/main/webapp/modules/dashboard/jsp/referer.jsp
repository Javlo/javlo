<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="content">
<div id="referer-chart" class="pie"></div>
</div>
<script type="text/javascript">
var url = "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=referer";
jQuery.ajax({
	url : url,
	cache : false,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {
	jQuery.jqplot ('referer-chart', [jsonObj.datas], {
		seriesDefaults: {
		renderer: jQuery.jqplot.PieRenderer,
			rendererOptions: {
				startAngle: -90,
				highlightMouseOver: false,
				padding: 10
			}
		},
		legend: {
			show:true,
			location: 'e',
			rowSpacing: 0
		},
		grid : {
			drawGridlines: false,
			background: 'transparent',
			drawBorder: false,
			shadow: false
		}
	});

});	

</script>