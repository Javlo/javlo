jQuery(document).ready(function(){
	if (window.clipboardData == null) {
		jQuery('.clipboard').remove();
	} else {
		jQuery('.noclipboard').remove();
	}
});

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

function clipboardCopy(text) {
	window.clipboardData.setData('Text', text);
}


function smartLinkAction(item) {
	var parent = jQuery(item).parent();		
	while (parent != null && !jQuery(parent).is("form")) {
		parent = jQuery(parent).parent();		
	}
	var urlPrefix = jQuery(parent).attr("action");
	
	parent = jQuery(item).parent();		
	while (parent != null && !jQuery(parent).hasClass("smart-link")) {
		parent = jQuery(parent).parent();			
	}
	
	if (parent == null) {
		alert("smart-link not found.");
	} else {
		var compId = parent.attr("id").substring(5);			
		var url = urlPrefix+"?webaction=smartlink.loadlink&url="+jQuery(item).val()+"&comp_id="+compId;			
		ajaxRequest(url, null);
	}
}

function initSmartLink() {	
	jQuery(".smart-link .link").keydown (function(event) {
		smartLinkAction(this);		
	});
	jQuery(".smart-link .link").change (function(event) {
		smartLinkAction(this);		
	});
};

function selectNextItem(compId, index) {
	if (jQuery('#'+compId+' .item-' + (index+1) ).length>0) {
		jQuery('#'+compId+' .item-' + (index+1) ).addClass("active");
		jQuery('#'+compId+' .item-' + index ).removeClass("active");
		var url = jQuery('#'+compId+' .item-' + (index+1) ).find("img").attr("src");
		jQuery("#"+compId+" .image-field").val(url);
		displayFigureSize(jQuery('#'+compId+' .item-' + (index+1) ).find("figure"));		
	}
}

function selectPreviousItem(compId, index) {
	if (jQuery('#'+compId+' .item-' + (index-1) ).length>0) {
		jQuery('#'+compId+' .item-' + (index-1) ).addClass("active");
		jQuery('#'+compId+' .item-' + index ).removeClass("active");
		var url = jQuery('#'+compId+' .item-' + (index-1) ).find("img").attr("src");
		jQuery("#"+compId+" .image-field").val(url);
	}
}

function displayFigureSize(figure) {
	console.log("displayFigureSize : "+figure);
	var fakeImg = new Image();		
	fakeImg.caption = jQuery(figure).find("figcaption");		
	fakeImg.onload = function() {				
		var label = this.width + 'x' + this.height+"px";		
		jQuery(this.caption).html(label);
	}	
	fakeImg.src = jQuery(figure).find("img").attr("src");	
}


function imageChoice(compId) {
	var firstElement = true;
	var index = 1;
	jQuery("#"+compId+" .choice-images li").each(function() {
		if (firstElement) {
			jQuery(this).addClass("active");
			firstElement = false;			
			displayFigureSize(jQuery(this).find("figure"));
		}		
		
		var item = jQuery(this);
		item.addClass("item-"+index);		
		var js = 'selectNextItem("'+compId+'",'+index+')';
		js = js.replace(/"/g, "'");		
		item.append('<a class="next" href="#" onclick="'+js+'"><span>&gt;&gt;</span></a>');
		js = 'selectPreviousItem("'+compId+'",'+index+')';
		js = js.replace(/"/g, "'");
		item.prepend('<a class="previous" href="#" onclick="'+js+'"><span>&lt;&lt;</span></a>');
		index++;
	});
}