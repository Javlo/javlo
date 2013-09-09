jQuery(document).ready(function() {	

	var value = jQuery("#text-editor").val();
	
	jQuery("#text-editor").addClass("hidden");
	var ext = jQuery("#text-editor").data("ext"); 
	if (jQuery("#ace-text-editor").length == 0) {
		jQuery("#text-editor").after('<pre id="ace-text-editor">'+value+'</pre>');
	}
	if (jQuery("#ace-text-editor").length > 0) {
		fullHeight(jQuery("#ace-text-editor"));
		var editor = ace.edit("ace-text-editor");
		editor.setTheme("ace/theme/github");
		if (ext == "css") {
			editor.getSession().setMode("ace/mode/css");
		} else if (ext == "html") {
			editor.getSession().setMode("ace/mode/html");
		} else if (ext == "jsp") {
			editor.getSession().setMode("ace/mode/jsp");
		}
		editor.getSession().on('change', function(e) {
			var editor = ace.edit("ace-text-editor");		
			jQuery("#text-editor").val(editor.getValue());
		});
	}
});


function fullHeight(item) {	
	jQuery(item).each(function() {
		var footer = jQuery('#footer');
		if (footer.length > 0) {
			var height = footer.offset().top -  jQuery(this).offset().top - 95;		
			jQuery(this).height(height);
		}
	});	

}