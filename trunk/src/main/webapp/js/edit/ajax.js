jQuery(document).ready(function() {	
	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");				
		ajaxRequest(jQuery(this).attr('href'));	
	});
	
	jQuery('form.ajax').submit(function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");
		var queryString = jQuery(this).attr("action")+'?'+jQuery(this).formSerialize(); 
		ajaxRequest(queryString);
		return false;
	});
	
	jQuery(document).trigger("ajaxUpdate"); 	
	
});

function ajaxRequest(url) {
	url = url.replace("/edit/", "/ajax/");
	console.log("url : "+url);
	jQuery.ajax({
		url : url,
		cache : false,
		dataType : "json"
	}).done(function(jsonObj) {			
		jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {				
			jQuery("#" + xhtmlId).replaceWith(xhtml);	
		});
		jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {				
			jQuery("#" + xhtmlId).html(xhtml);	
		});
		jQuery(document).trigger("ajaxUpdate");
		jQuery("#ajax-loader").removeClass("active");
	});	
}

