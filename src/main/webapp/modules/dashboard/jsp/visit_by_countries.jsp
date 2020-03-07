<c:url var="countryAjaxURL" value="${info.currentAjaxURL}" context="/">
<c:param name="webaction" value="dashboard.readTracker" />
<c:param name="type" value="country" />
</c:url>

<h3>${i18n.edit['dashboard.title.country']}</h3>

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
	    	      { title: "country" },
	    	      { title: "visit" }
	    	    ],
	    	    order: [[ 1, "desc" ]],
	    	    bDestroy: true
	    	  });
	    }
	});
}

jQuery(document).ready(function() {	
	updateYearForCountries(${info.currentYear});	
});
</script>