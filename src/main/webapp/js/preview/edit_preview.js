jQuery(document).ready(function(){	
	jQuery("#preview_command").draggable({ handle: ".pc_header" });	
	jQuery(".editable-component").click(function() {
		var elems = jQuery(this);		
		var editURL = editPreviewURL+"&comp_id="+elems.attr("id");
		var param = "";
		if (jQuery("#search-result").length > 0) {
			param = "&noinsert=true";
		}
		jQuery.colorbox({href:editURL+param, opacity:0.6, iframe:true, width:"75%", height:"75%"});		
		return false;
	});
	jQuery("#pc_change_template").click(function() {
		var elems = jQuery(this);
		var editURL = jQuery("#change_template_form").attr("action");
		var param = "";
		jQuery.colorbox({href:editURL+param, opacity:0.6, iframe:true, width:"75%", height:"75%"});		
		return false;
	});
});
