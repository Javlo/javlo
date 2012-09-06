<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div class="content">
<div id="visits-chart" class="pie" style="height: 300px;"></div>
</div>
<script type="text/javascript">
var url = "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=visits";
jQuery.ajax({
	url : url,
	cache : false,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {
	jQuery.jqplot('visits-chart', [jsonObj.datas], {
		seriesDefaults: {showMarker:false}, 
		axes : {
			xaxis : {
				renderer : jQuery.jqplot.DateAxisRenderer
			}, 
			yaxis : {
				min: 0
			}
		},
		cursor : {
			show : true,
			zoom : true,
			showTooltip : false,
			constrainZoomTo: 'x'
		}
	});
});
</script>