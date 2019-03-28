<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="year" class="content">
	<c:set var="options" value="" />
	<c:forEach var="y" begin="2010" end="${info.currentYear}">		
		<c:set var="options" value="<option>${y}</option>${options}" />
	</c:forEach>	
	<c:set var="options" value="<option value=''>${i18n.edit['global.select']}</option>${options}" />
<select class="form-input pull-right" onchange="updateYearForTime(this.value)">
${options}
</select>
<br /><br />
<div id="time-chart"></div>
<!-- <div class="export"><a href="#" onclick="downloadImage('#year-chart', 'year-${globalContext.contextKey}'); return false;"><span class="glyphicon glyphicon-cloud-download"></span></a></div>  -->
</div>

<script type="text/javascript">

var s1 = [2, 6, 7, 10];

function updateYearForTime(year) {
	jQuery('#time-chart').html("");
	if (year == "") {
		jQuery('#time-chart').removeClass("pie");		
		return;
	}
	jQuery('#time-chart').addClass("pie");	
	jQuery.ajax({
	url : "${info.currentURL}".replace("/edit", "/ajax")+"?webaction=dashboard.readTracker&type=time&y="+year,
	cache : true,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {	 
	jQuery.jqplot.config.enablePlugins = true;
	    plot1 = jQuery.jqplot('time-chart', [jsonObj.datas], {	    	
	        animate: !jQuery.jqplot.use_excanvas,
	        stackSeries: true,
	        seriesDefaults:{
	            renderer:jQuery.jqplot.BarRenderer
	        },
            axes: {            
                xaxis: {
                    renderer: jQuery.jqplot.CategoryAxisRenderer,
                    ticks: [
                    	<c:forEach var="d" begin="0" end="23">'${d}'<c:if test="${d<23}">,</c:if></c:forEach>                    	
                    ]
                }
            }	        
	    }); 
	    
});
}
updateYearForTime(${info.currentYear});
</script>
