jQuery(document).ready(function() {	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");				
		ajaxRequest(jQuery(this).attr('href'));	
	});	
	jQuery('form.ajax').live("submit", function(event) {
		var form = jQuery(this);
		console.log("size : "+form.find("input[type='file']").length);
		var ajaxSubmit = true;
		jQuery.each(form.find("input[type='file']"), function() {
			console.log('input file : '+jQuery(this).val());
			if (jQuery(this).val().length > 0) {			
				ajaxSubmit = false;				
			}
		});
		if (ajaxSubmit) {
			console.log("not upload");
			event.preventDefault();
			jQuery("#ajax-loader").addClass("active");
			var queryString = jQuery(this).attr("action")+'?'+jQuery(this).formSerialize(); 
			ajaxRequest(queryString);
			return false;
		} else {
			console.log("upload");
			return true;
		}
	});
	jQuery(document).trigger("ajaxUpdate");
});

function ajaxRequest(url) {
	url = url.replace("/edit/", "/ajax/");	
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

