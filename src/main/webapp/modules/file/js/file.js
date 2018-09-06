function openEditor(textarea) {
	
	var container = jQuery(textarea).parent().parent();
	
	var title = container.find(".ui-dialog-titlebar").text();
	var ext = title.split(".")[title.split(".").length-1];
	
	container.children(".ui-resizable-se").mouseover(function(){
		var container = jQuery(textarea).parent();
		container.find("iframe").css("height", (container.height()-14));
		container.find("iframe").css("width", (container.width()));
	});
	
	changeFooter();
	
	var box = jQuery("#fileManager");	
	var ta = jQuery(textarea);
	
	var value = ta.val();
	ta.addClass("hidden");
	ta.after('<pre id="file-text-editor">'+escapeHTML(value)+'</pre>');
	
	var titleBar = jQuery(".ui-dialog-titlebar").text();
	
	CodeMirror.fromTextArea(textarea, {
		lineNumbers: true,
		mode: jQuery(this).data("mode"),
		foldGutter: true,
		extraKeys: {"Ctrl-Space": "autocomplete"},
		gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"],
	}).on('change', editor => {
		jQuery("#file-text-editor").val(editor.getValue());
	});;
	
	
	container.width(box.width()-200);
	container.css("left", box.offset().left-100 );	
	container.css("top", box.offset().top-150 );	

}

function closeEditor(textarea) {
}

function saveEditor(textarea) {
	textarea.value = jQuery("#file-text-editor").val();
}

function openMetaEditor(textarea, editorURL) {
	$.ajax({
		  url: 'editorURL',
		  success: function(data) {
		    $('.result').html(data);
		    alert('Load was performed.');
		  }
		});
}

function loadLocalisation(url,lat,long,lg,queryInput) {
	var sep="?";
	if (url.indexOf("?")>=0) {
		sep="&";
	}
	url = url + sep+"webaction=data.location&lat="+lat+"&long="+long+"&lg="+lg;	
	try {
		jQuery.ajax({
			  url: url,		  
			  dataType: "json"
			}).done(function(json) {
			  jQuery(queryInput).val(json.data.location);
			});
	} catch(e) {
		console.log(e);
	}	
}
