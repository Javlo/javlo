jQuery(document).ready(function() {	
	jQuery("a.ajax").live("click", function(event) {
		event.preventDefault();
						
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
			jQuery(".ajax-loader").addClass("active");
			var queryString = jQuery(this).attr("action"); 
			ajaxRequest(queryString, this);
			return false;
		} else {
			return true;
		}
	});
	
	initDropFile();	
	
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
	startAjaxLoading();
	jQuery.ajax({
		url : url,
		cache : false,
		data : data,
		type : "post",
		dataType : "json"
	}).done(function(jsonObj) {
		endAjaxLoading();
		if (jsonObj.data != null) {
			jQuery.each(jsonObj.data, function(key, value) {
				/** need test **/
				if (key == "need-refresh" && value) {
					window.location.href=window.location.href;
				}
			});
		}
		jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
			var item = jQuery("#" + xhtmlId);			
			if (item != null) {
				jQuery("#" + xhtmlId).replaceWith(xhtml);
			} else {
				jQuery.each(jsonObj.data, function(key, value) {				
			});
				if (console) {
					console.log("warning : component "+xhtmlId+" not found for zone.");
				}
			}
		});
		jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
			var item = jQuery("#" + xhtmlId);
			if (item != null) {
				item.html(xhtml);	
			} else {
				if (console) {
					console.log("warning : component "+xhtmlId+" not found for insideZone.");
				}
			}

		});
		jQuery(form).trigger("ajaxUpdate");
		jQuery(document).trigger("ajaxUpdate");		
		try {			
			initPreview();			
		} catch (ex) {
			if (console) {
				console.log(ex);
			}
		}		
	});	
}

function initDropFile() {
	jQuery.event.props.push('dataTransfer');
	jQuery(".drop-files").on('dragover', function(e) {		
		jQuery(this).addClass("dragover");
		doNothing(e);
	});
	jQuery(".drop-files").on('dragout', function(ev, drag) {		
		jQuery(this).removeClass("dragover");		
	 });
	jQuery(".drop-files").on('dragenter', function(e) {
		doNothing(e);
	});
	jQuery(".drop-files").on('drop', function(e) {		
		doNothing(e);
		jQuery(this).removeClass("dragover");	
		var url  = jQuery(this).data("url");		
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
		var fieldName = jQuery(this).data("fieldname");
		if (fieldName == null) {
			filedName = "files";
		}
				
		var i = 0;
		
		var fd=new FormData;
		jQuery.each( e.dataTransfer.files, function(index, file) {
			startAjaxLoading();
			if (i==0) {
				fd.append(fieldName,file);
			} else {
				fd.append(fieldName+"_"+i,file);
			}
			i++;			
		});
		
		jQuery.ajax({
			url : url,
			cache : false,
			data: fd,
			type : "post",
			dataType : "json",
			processData: false,
			contentType: false
		}).done(function(jsonObj) {
			if (jsonObj.data != null) {
				jQuery.each(jsonObj.data, function(key, value) {
					if (key == "need-refresh" && value) {						
						window.location.href=window.location.href;
					}
				});
			}
			jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
				var item = jQuery("#" + xhtmlId);			
				if (item != null) {
					jQuery("#" + xhtmlId).replaceWith(xhtml);
				} else {
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for zone.");
					}
				}
			});
			jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
				var item = jQuery("#" + xhtmlId);
				if (item != null) {
					item.html(xhtml);	
				} else {
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for insideZone.");
					}
				}

			});				
			jQuery(document).trigger("ajaxUpdate");
			endAjaxLoading();
			initDropFile();
			try {
				initPreview();					
			} catch (ex) {
				if (console) {
					console.log(ex);
				}
			}
		});
		 
	});
}

function doNothing(evt) {
	evt.stopPropagation();
	evt.preventDefault();
}

var ajaxLoading = 0;

function startAjaxLoading() {
	ajaxLoading++;
	jQuery("#ajax-loader").addClass("active");
	jQuery(".ajax-loader").addClass("active");
	jQuery("#upload-zone").addClass("hidden");
}

function endAjaxLoading() {
	ajaxLoading--;	
	if (ajaxLoading == 0) {
		jQuery("#ajax-loader").removeClass("active");
		jQuery(".ajax-loader").removeClass("active");
		jQuery("#upload-zone").removeClass("hidden");
	}
}




