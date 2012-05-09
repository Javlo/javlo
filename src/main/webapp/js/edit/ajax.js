jQuery(document).ready(function() {	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");
		var link = jQuery(this);		
		jQuery.ajax({
			url : link.attr('href').replace("/edit/", "/ajax/"),
			cache : false,
			dataType : "json"
		}).done(function(jsonObj) {			
			jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
				jQuery("#" + xhtmlId).html(xhtml);	
			});			
			jQuery(document).trigger("ajaxUpdate");
			jQuery("#ajax-loader").removeClass("active");
		});
	});	
	jQuery(document).trigger("ajaxUpdate");
});