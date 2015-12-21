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
	ta.after('<pre id="ace-text-editor">'+escapeHTML(value)+'</pre>');
	
	var titleBar = jQuery(".ui-dialog-titlebar").text();
	container.width(box.width()-200);
	ta.height(box.height()-250);
	container.css("left", box.offset().left-100 );
	container.css("top", box.offset().top-150 );
	var editor = ace.edit("ace-text-editor");
	editor.setTheme("ace/theme/github");
	if (titleBar.indexOf(".html")>0) {
		editor.getSession().setMode("ace/mode/html");
	} else if (titleBar.indexOf(".css")>0 || titleBar.indexOf(".less")>0) {
		editor.getSession().setMode("ace/mode/css");
	} else if (titleBar.indexOf(".js")>0&&titleBar.indexOf(".jsp")<0) {
		editor.getSession().setMode("ace/mode/javacript");
	} else if (titleBar.indexOf(".properties")>0) {
		editor.getSession().setMode("ace/mode/properties");
	} else if (titleBar.indexOf(".jsp")>0) {
		editor.getSession().setMode("ace/mode/jsp");
	} else if (titleBar.indexOf(".json")>0) {
		editor.getSession().setMode("ace/mode/json");
	} else if (titleBar.indexOf(".java")>0) {
	    editor.getSession().setMode("ace/mode/java");
	} else if (titleBar.indexOf(".txt")>0) {
	    editor.getSession().setMode("ace/mode/text");
	} else if (titleBar.indexOf(".sql")>0) {
	    editor.getSession().setMode("ace/mode/sql");
	}
	editor.getSession().on('change', function(e) {
		jQuery("#ace-text-editor").val(editor.getValue())
	});	
	
	jQuery("#ace-text-editor").height(box.height()/2);
	jQuery("#ace-text-editor").val(editor.getValue());
	
}

function closeEditor(textarea) {
}

function saveEditor(textarea) {
	textarea.value = jQuery("#ace-text-editor").val();
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
