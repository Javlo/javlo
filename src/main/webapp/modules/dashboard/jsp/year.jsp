<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="year" class="content">
	<c:set var="options" value="" />
	<c:forEach var="y" begin="2010" end="${info.currentYear}">		
		<c:set var="options" value="<option>${y}</option>${options}" />
	</c:forEach>	
	<c:set var="options" value="<option value=''>${i18n.edit['global.select']}</option>${options}" />
<select class="form-input pull-right" onchange="updateYear(this.value)">
${options}
</select>
<br /><br />
<div id="year-chart"></div>
</div>

<script type="text/javascript">
function updateYear(year) {
	jQuery('#year-chart').html("");
	if (year == "") {
		jQuery('#year-chart').removeClass("pie");		
		return;
	}
	jQuery('#year-chart').addClass("pie");	
	jQuery.ajax({
	url : "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=year&y="+year,
	cache : true,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {	 
	jQuery.jqplot.config.enablePlugins = true;
	    plot1 = jQuery.jqplot('year-chart', [jsonObj.datas], {        
	        animate: !jQuery.jqplot.use_excanvas,
	        seriesDefaults:{
	            renderer:jQuery.jqplot.BarRenderer,
	            pointLabels: { show: true }
	        },
	        axes: {
	            xaxis: {
	                renderer: jQuery.jqplot.CategoryAxisRenderer,	                
	            }
	        },
	        highlighter: { show: false }
	    }); 
	    
});
}
//updateYear(2016);
</script>
