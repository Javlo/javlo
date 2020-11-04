<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<div id="year">
<div class="content">
	<c:set var="options" value="" />
	<c:forEach var="y" begin="2014" end="${info.currentYear}">		
		<c:set var="options" value="<option>${y}</option>${options}" />
	</c:forEach>	
	<c:set var="options" value="<option value=''>${i18n.edit['global.select']}</option>${options}" />
<select class="form-input pull-right" onchange="generalUpdateYear(this.value); return false;">
${options}
</select>
</div>

<h3>${i18n.edit['dashboard.title.month']}</h3>

<div class="content">
<div id="year-chart"></div>
</div>

<script type="text/javascript">

function generalUpdateYear(year) {
	updateYear(year);
	updateYearForTime(year);
	updateDaysForTime(year);
	updateYearForCountries(year);
	updateYearForPages(year);
	updateYearForResources(year);
}

<c:url var="ajaxURL" value="${info.currentAjaxURL}" context="/">
<c:param name="webaction" value="dashboard.readTracker" />
<c:param name="type" value="year" />
</c:url>


function updateYear(year) {
	jQuery('#year-chart').html("");
	if (year == "") {
		jQuery('#year-chart').removeClass("pie");		
		return;
	}
	jQuery('#year-chart').addClass("pie");	
	jQuery.ajax({
	url : "${ajaxURL}&y="+year,
	cache : true,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : "+textStatus);
	}
}).done(function(jsonObj) {	 
	jQuery.jqplot.config.enablePlugins = true;
	    plot1 = jQuery.jqplot('year-chart', jsonObj.datas, {        
	        animate: !jQuery.jqplot.use_excanvas,
	        stackSeries: true,
	        seriesDefaults:{
	            renderer:jQuery.jqplot.BarRenderer,
	            pointLabels: { show: true }
	        },
	        legend: {
                show: true,
                location: 'ne',
                placement: 'inside',
				labels:['desktop', 'mobile']
            },
            axes: {            
                xaxis: {
                    renderer: jQuery.jqplot.CategoryAxisRenderer,
                    ticks: ['${info.months[0]}', '${info.months[1]}', '${info.months[2]}', '${info.months[3]}', '${info.months[4]}', '${info.months[5]}', '${info.months[6]}', '${info.months[7]}', '${info.months[8]}', '${info.months[9]}', '${info.months[10]}', '${info.months[11]}']
                }
            }	        
	    }); 
	    
});
}
updateYear(${info.currentYear});
</script>

<h3>${i18n.edit['dashboard.title.days']}</h3>

<div class="content"><div id="days-chart"></div></div>

<script type="text/javascript">

<c:url var="ajaxURL" value="${info.currentAjaxURL}" context="/">
	<c:param name="webaction" value="dashboard.readTracker" />
	<c:param name="type" value="days" />
</c:url>

function updateDaysForTime(year) {
	jQuery('#days-chart').html("");
	if (year == "") {
		jQuery('#days-chart').removeClass("pie");		
		return;
	}
	jQuery('#days-chart').addClass("pie");	
	jQuery.ajax({
	url : "${ajaxURL}&y="+year,
	cache : true,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : ",errorThrown);
	}
}).done(function(jsonObj) {	 
	jQuery.jqplot.config.enablePlugins = true;
	    plot1 = jQuery.jqplot('days-chart', [jsonObj.datas], {	    	
	        animate: !jQuery.jqplot.use_excanvas,
	        stackSeries: true,
	        seriesDefaults:{
	            renderer:jQuery.jqplot.BarRenderer
	        },
            axes: {            
                xaxis: {
                    renderer: jQuery.jqplot.CategoryAxisRenderer,
                    ticks: [
                    	<c:forEach var="d" begin="1" end="7">'${info.longDays[d]}'<c:if test="${d<7}">,</c:if></c:forEach>                    	
                    ]
                }
            }	        
	    });
});
}
updateDaysForTime(${info.currentYear});
</script>

<h3>${i18n.edit['dashboard.title.hours']}</h3>

<div class="content"><div id="time-chart"></div></div>

<script type="text/javascript">

<c:url var="ajaxURL" value="${info.currentAjaxURL}" context="/">
	<c:param name="webaction" value="dashboard.readTracker" />
	<c:param name="type" value="time" />
</c:url>

function updateYearForTime(year) {
	jQuery('#time-chart').html("");
	if (year == "") {
		jQuery('#time-chart').removeClass("pie");		
		return;
	}
	jQuery('#time-chart').addClass("pie");	
	jQuery.ajax({
	url : "${ajaxURL}&y="+year,
	cache : true,		
	type : "post",
	dataType : "json",
	error : function(jqXHR, textStatus, errorThrown) {
		console.log("ajax error : ",errorThrown);
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


<c:url var="countryAjaxURL" value="${info.currentAjaxURL}" context="/">
<c:param name="webaction" value="dashboard.readTracker" />
<c:param name="type" value="language" />
</c:url>

<h3>${i18n.edit['dashboard.title.language']}</h3>

<div class="content ${not empty lightInterface?'light':''}">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe" id="countrytable">

</table>


<script type="text/javascript">

function updateYearForCountries(year) {
	jQuery.ajax({
	    url: '${countryAjaxURL}&y='+year,
	    success: function(ajaxData){	    	
	    	var countrytable = jQuery('#countrytable').DataTable({
	    		data: ajaxData.datas,
	    		columns: [
	    	      { title: "lang" },
	    	      { title: "visit" }
	    	    ],
	    	    order: [[ 1, "desc" ]],
				bDestroy: true,
				dom: 'Bfrtip',
				buttons: [
					'copyHtml5',
					'excelHtml5',
					'csvHtml5'
				]
	    	  });
	    }
	});
}

jQuery(document).ready(function() {	
	updateYearForCountries(${info.currentYear});	
});
</script>

</div>

<c:url var="pageAjaxURL" value="${info.currentAjaxURL}" context="/">
<c:param name="webaction" value="dashboard.readTracker" />
<c:param name="type" value="pages" />
</c:url>

<h3>${i18n.edit['dashboard.title.pages']}</h3>

<div class="content ${not empty lightInterface?'light':''}">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe" id="pageAjaxURL">

</table>

<script type="text/javascript">

function updateYearForPages(year) {
	jQuery.ajax({
	    url: '${pageAjaxURL}&y='+year,
	    success: function(ajaxData){	    	
	    	var countrytable = jQuery('#pageAjaxURL').DataTable({
	    		data: ajaxData.datas,
	    		columns: [
	    	      { title: "page" },
	    	      { title: "visit" }
	    	    ],
	    	    order: [[ 1, "desc" ]],
				bDestroy: true,
				dom: 'Bfrtip',
				buttons: [
					'copyHtml5',
					'excelHtml5',
					'csvHtml5'
				]
	    	  });
	    }
	});
}

jQuery(document).ready(function() {	
	updateYearForPages(${info.currentYear});	
});
</script>

</div>

<c:url var="resourcesAjaxURL" value="${info.currentAjaxURL}" context="/">
<c:param name="webaction" value="dashboard.readTracker" />
<c:param name="type" value="resources" />
</c:url>

<h3>${i18n.edit['dashboard.title.resources']}</h3>

<div class="content ${not empty lightInterface?'light':''}">

<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe" id="resourcesAjaxURL">

</table>

<script type="text/javascript">

function updateYearForResources(year) {
	jQuery.ajax({
	    url: '${resourcesAjaxURL}&y='+year,
	    success: function(ajaxData){	    	
	    	var countrytable = jQuery('#resourcesAjaxURL').DataTable({
	    		data: ajaxData.datas,
	    		columns: [
	    	      { title: "page" },
	    	      { title: "visit" }
	    	    ],
	    	    order: [[ 1, "desc" ]],
				bDestroy: true,
				dom: 'Bfrtip',
				buttons: [
					'copyHtml5',
					'excelHtml5',
					'csvHtml5'
				]
	    	  });
	    }
	});
}

jQuery(document).ready(function() {	
	updateYearForResources(${info.currentYear});	
});
</script>

</div>

</div>