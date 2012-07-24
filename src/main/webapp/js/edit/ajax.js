jQuery(document).ready(function() {	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");				
		ajaxRequest(jQuery(this).attr('href'));	
	});	
	jQuery('form.ajax').live("submit", function(event) {
		var form = jQuery(this);		
		var ajaxSubmit = true;
		jQuery.each(form.find("input[type='file']"), function() {			
			if (jQuery(this).val().length > 0) {			
				ajaxSubmit = false;				
			}
		});
		if (ajaxSubmit) {			
			event.preventDefault();
			jQuery("#ajax-loader").addClass("active");
			var queryString = jQuery(this).attr("action"); 
			ajaxRequest(queryString, this);
			return false;
		} else {
			return true;
		}
	});
	jQuery(document).trigger("ajaxUpdate");
});

function ajaxRequest(url, form) {
	url = url.replace("/edit/", "/ajax/");
	var data=null;
	if (form != null) {
		data = jQuery(form).serialize();
	}	
	jQuery.ajax({
		url : url,
		cache : false,
		data : data,
		type : "post",
		dataType : "json"
	}).done(function(jsonObj) {			
		jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
			var item = jQuery("#" + xhtmlId);
			if (item != null) {
				jQuery("#" + xhtmlId).replaceWith(xhtml);
			}
		});
		jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
			var item = jQuery("#" + xhtmlId);
			if (item != null) {
				item.html(xhtml);	
			}
		});
		jQuery(document).trigger("ajaxUpdate");
		jQuery("#ajax-loader").removeClass("active");
	});	
}


