//jQuery(window).load(function() {	
//
//	jQuery(".text-editor").each(function(index ) {
//		console.log("text editor : "+index);
//		var textEdit = jQuery(this);
//		var value = textEdit.val();
//		console.log("#value = "+value.length);
//		textEdit.addClass("hidden");
//		var ext = textEdit.data("ext"); 
//		console.log("#children = "+textEdit.parent().find(".ace-text-editor").length);
//		var editorId = "ace-text-editor-"+index;
//		if (textEdit.parent().find("#"+editorId).length == 0) {
//			textEdit.after('<pre id="'+editorId+'">'+value+'</pre>');
//		}
//		if (textEdit.parent().find("#"+editorId).length > 0) {
//			//fullHeight(jQuery("#ace-text-editor"));
//			var editor = ace.edit(editorId);
//			editor.setTheme("ace/theme/github");
//			if (ext == "css") {
//				editor.getSession().setMode("ace/mode/css");
//			} else if (ext == "html") {
//				editor.getSession().setMode("ace/mode/html");
//			} else if (ext == "jsp") {
//				editor.getSession().setMode("ace/mode/jsp");
//			}
//			editor.textarea=textEdit;
//			editor.getSession().on('change', function(e) {
//				
//				// copie tout les éditeurs dans toutes les textarea
//				
//				console.log(e);
//				textEdit.val(e.data.text);
//			});
//		}
//	});
//});
//
//
//function fullHeight(item) {	
//	jQuery(item).each(function() {
//		item.hide();		
//		var footer = jQuery('#footer');
//		if (footer.length > 0) {
//			var height = footer.offset().top -  jQuery(this).offset().top;
//			jQuery(this).height(height);
//		}
//		item.show();
//	});	
//
//}