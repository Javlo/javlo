jQuery(document).ready(function(){
	jQuery("#preview_command").attr("name","ceci est un test");
	jQuery("#preview_command").draggable({ handle: ".pc_header" });	
	jQuery(".editable-component").click(function() {
		var elems = jQuery(this);		
		var editURL = editPreviewURL+"&comp_id="+elems.attr("id");		
		$.colorbox({href:editURL, opacity:0.6, iframe:true, width:"75%", height:"75%"});		
		return false;
	});
});
