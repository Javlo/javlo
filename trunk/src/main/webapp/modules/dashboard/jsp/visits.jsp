<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="visits" class="content">
<div id="visits-chart" class="pie" style="height: 300px;"></div>
<button class="dashboard-visits-load" data-range="WEEK">${i18n.edit['dashboard.action.load-week']}</button>
<button class="dashboard-visits-load" data-range="MONTH">${i18n.edit['dashboard.action.load-month']}</button>
<button class="dashboard-visits-load" data-range="YEAR">${i18n.edit['dashboard.action.load-year']}</button>
<span>${i18n.edit['dashboard.label.visits-tips']}</span>
</div>
<script type="text/javascript">
function loadVisits(range) {
	jQuery('#visits-chart').html("");
	var url = "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=visits&range=" + range;
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
}
jQuery(".dashboard-visits-load").click(function(){
	loadVisits(jQuery(this).data("range"));
});
loadVisits("WEEK")
</script>