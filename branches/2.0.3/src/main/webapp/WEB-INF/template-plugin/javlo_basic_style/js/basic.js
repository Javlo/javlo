jQuery(document).ready(function() {
	jQuery('.sort').each(function() {		
		jQuery(this).find('option').sort(function(a, b){
			var vala = jQuery(a).text();
			if (jQuery(a).attr("data-sort") != null) {
				vala = jQuery(a).attr("data-sort");
			}
			var valb = jQuery(b).text();
			if (jQuery(b).attr("data-sort") != null) {
				valb = jQuery(b).attr("data-sort");
			}
	        return  vala > valb ? 1 : -1;
	    });
	});
});
