jQuery(document).ready(function() {	
	editAreaLoader.init({
		id : "text-editor"
		,start_highlight: true
		,allow_resize: false
		,syntax: jQuery("#text-editor").data("ext")
		,toolbar: "search, go_to_line, |, undo, redo, |, select_font, |, change_smooth_selection, highlight, reset_highlight, |, help"
	});
});