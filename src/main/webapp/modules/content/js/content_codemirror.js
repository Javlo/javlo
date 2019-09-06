 jQuery(window).load(function() {
	jQuery(".text-editor").each(function(index ) {
		CodeMirror.fromTextArea(this, {
			lineNumbers: true,
			mode: jQuery(this).data("mode"),
			foldGutter: true,
			matchBrackets: true,
			extraKeys: {"Ctrl-Space": "autocomplete"},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	});
});
