jQuery(document).ready(function() {
	var ajaxLinks = jQuery("a.ajax");
	for ( var i = 0; i < ajaxLinks.length; i++) {
		var link = ajaxLinks[i];
		var href = jQuery(link).attr("href");
		href = href.replace("/edit/", "/ajax/");
		jQuery(link).attr("href", "");
		jQuery(link).bind("click", function() {
			jQuery.ajax({
				url : href,
				cache : false,
				dataType : "json"
			}).done(function(jsonObj) {
				jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
					jQuery("#"+xhtmlId).html(xhtml);
				});
			});
			return false;
		});
	}
});