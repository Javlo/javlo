jQuery(document).ready(function() {	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
		jQuery("#ajax-loader").addClass("active");				
		ajaxRequest(jQuery(this).attr('href'));	
	});	
	var AJAX_SUBMIT_SHADOW_DATA = "_AjaxSubmitShadow"
	jQuery('form.ajax :submit').live("click", function(event) {		
		var submit = jQuery(this);
		if (submit.attr("name") != null) {
			var shadow = submit.data(AJAX_SUBMIT_SHADOW_DATA);
			if (shadow == null) {
				shadow = jQuery("<input/>");
				submit.data(AJAX_SUBMIT_SHADOW_DATA, shadow);
				shadow.attr("type", "hidden");
				shadow.insertAfter(submit);
			}
			shadow.attr("name", submit.attr("name"));
			shadow.val(submit.val());
			submit.removeAttr("name");
		}
	});
	jQuery('form.ajax :submit').live("blur", function(event) {
		var submit = jQuery(this);
		var shadow = submit.data(AJAX_SUBMIT_SHADOW_DATA);
		if (shadow != null) {
			if (shadow.attr("name") != null) {
				submit.attr("name", shadow.attr("name"));
				shadow.removeAttr("name");
			}
		}
	});
	jQuery('form.ajax').live("submit", function(event) {
		console.log("ajax form");
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
	if (url.indexOf("/edit-")>=0) {
		url = url.replace("/edit-", "/ajax-");
	} else {
		url = url.replace("/edit/", "/ajax/");
		if (url.indexOf("/preview-")>=0) {
			url = url.replace("/preview-", "/ajax-");
		} else {
			url = url.replace("/preview/", "/ajax/");
		}
	}	
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
		jQuery(form).trigger("ajaxUpdate");
		jQuery(document).trigger("ajaxUpdate");
		jQuery("#ajax-loader").removeClass("active");
		try {
			initPreview();
		} catch (ex) {
			if (console) {
				console.log(ex);
			}
		}
	});	
}


