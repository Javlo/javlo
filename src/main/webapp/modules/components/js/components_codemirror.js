jQuery(window).load(function() {	
	jQuery(".text-editor").each(function(index ) {
		CodeMirror.fromTextArea(this, {
			lineNumbers: true,
			mode: jQuery(this).data("mode"),
			foldGutter: true,
			extraKeys: {"Ctrl-Space": "autocomplete"},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	});	
	jQuery(".header-tabs .link").click(function() {
		if (jQuery(this).attr("href")[0]=='#') {
			var form = jQuery("#tabs-form");
			var action = form.attr("action");
			if (action.indexOf("#")>=0) {
				action = action.substring(0,action.indexOf("#"));
			}
			form.attr("action", action+jQuery(this).attr("href"));
		}
		return true;
	});
	jQuery(".tabloading").removeClass("tabloading");
});


