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
	
	container.width(box.width()-200);
	ta.height(box.height()-250);
	container.css("left", box.offset().left-100 );
	container.css("top", box.offset().top-150 );
	editAreaLoader.init({
		id : textarea.id
		,start_highlight: true
		,allow_resize: false
		,syntax: ext
		,toolbar: "search, go_to_line, |, undo, redo, |, select_font, |, change_smooth_selection, highlight, reset_highlight, |, help"
	});
}

function closeEditor(textarea) {
}

function saveEditor(textarea) {
	textarea.value = editAreaLoader.getValue(textarea.id);
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
