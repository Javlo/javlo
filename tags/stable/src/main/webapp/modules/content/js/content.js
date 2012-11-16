function loadWysiwyg(cssQuery) {
	tinyMCE.init({
	// General options
	mode : "specific_textareas",
	theme : "advanced",
	add_form_submit_trigger: true,
	editor_selector : 'tinymce-light',
	//editor_selector : cssQuery,
	plugins : "paste",
	// Theme options
	theme_advanced_buttons1 : "bold,italic,underline,strikethrough,|,justifyleft,justifycenter,justifyright,justifyfull,|,cut,copy,paste,pastetext,|,bullist,numlist,|,outdent,indent,blockquote,|,undo,redo,|",
	theme_advanced_buttons2 : "",
	theme_advanced_toolbar_location : "top",
	theme_advanced_toolbar_align : "center",
	theme_advanced_statusbar_location : "bottom",
	theme_advanced_resizing : true
	});	 
}