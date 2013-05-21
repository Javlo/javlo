jQuery(document).ready(function() {	

	var value = jQuery("#text-editor").val();
	
	jQuery("#text-editor").addClass("hidden");
	jQuery("#text-editor").after('<pre id="ace-text-editor">'+value+'</pre>');
	fullHeight(jQuery("#ace-text-editor"));
	var editor = ace.edit("ace-text-editor");
	editor.setTheme("ace/theme/github");
	editor.getSession().setMode("ace/mode/css");
	editor.getSession().on('change', function(e) {
		var editor = ace.edit("ace-text-editor");		
		jQuery("#text-editor").val(editor.getValue());
	});
	
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