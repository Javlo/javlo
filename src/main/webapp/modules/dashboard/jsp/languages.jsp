<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="languages" class="content">
<div id="languages-chart" class="pie" style="height: ${100 + (10 * fn:length(info.contentLanguages))}px;"></div>
</div>
<script type="text/javascript">
var url = "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=languages";
jQuery.ajax({
	url : url,
	cache : false,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {	

	jQuery.jqplot('languages-chart', [jsonObj.datas], {
		seriesDefaults: {
			renderer:jQuery.jqplot.BarRenderer,
			shadow : false,
			pointLabels: {
				show: true,
				location: 'e',
				edgeTolerance: -15
			},
			rendererOptions: {
				barDirection: 'horizontal'
			}
		},
		axes: {
			yaxis: {
				renderer: jQuery.jqplot.CategoryAxisRenderer,
				ticks: jsonObj.labels
			}
		}
	});

});	

</script>