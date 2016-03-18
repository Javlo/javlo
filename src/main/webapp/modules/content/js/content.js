jQuery(document).ready(function(){
	if (window.clipboardData == null) {
		jQuery('.clipboard').remove();
	} else {
		jQuery('.noclipboard').remove();
	}
	jQuery(".readonly input, .readonly textarea, .readonly select").attr("readonly", "readonly");
});

function loadWysiwyg(cssQuery, complexity, chooseFileURL) {
	var wysiwygCss = staticRootURL+"modules/content/js/tinymce.css";
	
	tinymce.init({
	    paste_as_text: true
	});
	
	if (complexity == "middle") {
		tinymce.init({
			textlang_langs : contentLanguage,			
		    selector: cssQuery,
		    convert_urls: false,
		    menubar : false,
		    nonbreaking_force_tab: true,
		    content_css: wysiwygCss,
		    plugins: [
		        "advlist autolink lists link image charmap print preview anchor",
		        "searchreplace visualblocks code fullscreen",
		        "insertdatetime media table paste textcolor colorpicker nonbreaking textlang"
		    ],
		    fontsize_formats: "10px 11px 12px 13px 14px 16px 18px 20px",
		    toolbar: "textlang | undo redo searchreplace | bold italic underline fontsizeselect forecolor backcolor | charmap nonbreaking | alignleft aligncenter alignright alignjustify | link | bullist numlist outdent indent"
		});
	} else if (complexity == "high") {		
		tinymce.init({
			textlang_langs : contentLanguage,
		    selector: cssQuery,		
		    convert_urls: false,
		    menubar : false,
		    nonbreaking_force_tab: true,
		    theme: "modern",
		    content_css: wysiwygCss,
		    textcolor_map : ["111111","MyBlack","993300","My Burnt orange"],
		    plugins: [
		        "advlist autolink lists link image charmap print preview hr anchor pagebreak",
		        "searchreplace wordcount visualblocks visualchars code fullscreen",
		        "insertdatetime media nonbreaking save table directionality",
		        "emoticons template paste textcolor colorpicker nonbreaking textlang"
		    ],
		    toolbar1: "textlang | styleselect bold italic underline strikethrough fontsizeselect | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | forecolor backcolor | table charmap nonbreaking code",		    
		    image_advtab: true,
		    fontsize_formats: "10px 11px 12px 13px 14px 16px 18px 20px",
		    //paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		    file_browser_callback: function(field_name, url, type, win) {
		    	
	    	 	jQuery("body").data("fieldName", field_name);
	    	 	
	    	 	
	    	 	var fileURL = chooseFileURL.replace("_TYPE_",type);
	    	 	
		    	tinyMCE.activeEditor.windowManager.open({
		            file : fileURL,
		            title : 'Select resouce',
		            width : jQuery(document).width()-100,  // Your dimensions may differ - toy around with them
		            height :  jQuery(document).height()-150,
		            resizable : "yes",
		            inline : "yes",  // This parameter only has an effect if you use the inlinepopups plugin!
		            close_previous : "no"
		        }, {
		            window : win,
		            input : field_name
		        });
		    	
		    },
		    templates: [
		        {title: 'Test template 1', content: 'Test 1'},
		        {title: 'Test template 2', content: 'Test 2'}
		    ]
		});	
	} else if (complexity == "soft") {
		tinyMCE.init({
		textlang_langs : contentLanguage,
		mode : "specific_textareas",
		paste_as_text: true,
		plugins: "textlang paste",
		theme : "modern",
		content_css: wysiwygCss,
		add_form_submit_trigger: true,	
		menubar : false,
		convert_urls: false,
		selector: cssQuery,    	
		toolbar: "textlang | undo redo | bold italic | pastetext"
		});	 
	} else {
		tinyMCE.init({
		content_css : 'tinymce.css',
		textlang_langs : contentLanguage,
		mode : "specific_textareas",
		theme : "modern",
		convert_urls: false,
		content_css: wysiwygCss,
		add_form_submit_trigger: true,	
		menubar : false,
		selector: cssQuery,
		plugins: "paste link nonbreaking textlang",
		fontsize_formats: "10px 11px 12px 13px 14px 16px 18px 20px",
	    nonbreaking_force_tab: true,
		//paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		toolbar: "textlang | undo redo | bold italic underline fontsizeselect | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link pastetext nonbreaking"
		});	 
	}
}

function clipboardCopy(text) {
	window.clipboardData.setData('Text', text);
}


function smartLinkAction(item) {
	var parent = jQuery(item).parent();		
	while (parent != null && !jQuery(parent).is("form")) {
		parent = jQuery(parent).parent();	
		parent.find(".waiting").css('display', 'block');
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
		if (urlPrefix.indexOf('webaction=smartlink.loadlink') < 0) {
			if (urlPrefix.indexOf('?') < 0) {
				var url = urlPrefix+"?webaction=smartlink.loadlink&url="+jQuery(item).val()+"&comp_id="+compId;
			} else {
				var url = urlPrefix+"&webaction=smartlink.loadlink&url="+jQuery(item).val()+"&comp_id="+compId;
			}
		}
		ajaxRequest(url, null, function() {jQuery(".waiting").css('display', 'none');});
	}
}

function initSmartLink() {
	jQuery(".smart-link .link").keydown (function(event) {
		smartLinkAction(this);		
	});
	jQuery(".smart-link .link").change (function(event) {
		smartLinkAction(this);		
	});
	jQuery(".smart-link .link").on('paste', function(event) {
		var element = this;
		setTimeout(function () {
			smartLinkAction(element);
		  }, 100); 		
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
	url = jQuery("#"+compId+" .image-field").val();
	jQuery("#"+compId+" .choice-images li").each(function() {
		if (firstElement && url.length == 0) {
			jQuery(this).addClass("active");
			firstElement = false;			
			displayFigureSize(jQuery(this).find("figure"));
		}		
		var item = jQuery(this);
		if (item.find("img").attr('src') == url) {
			jQuery(this).addClass("active");			
			displayFigureSize(jQuery(this).find("figure"));
		}
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

function switchClass (class1, class2) {
	var selector1 = "."+class1;
	var selector2 = "."+class2;
	var tempClass = "___temp_switch_class___";
	
	jQuery(selector2).addClass(tempClass);
	jQuery(selector1).addClass(class2);
	jQuery(selector1).removeClass(class1);	
	jQuery("."+tempClass).addClass(class1);
	jQuery("."+tempClass).removeClass(class2);
	jQuery("."+tempClass).removeClass(tempClass);
}

function filterPage(url, filter, cssSelector) {
	jQuery.ajax({
		url : url+'&filter='+filter,
		cache : false,		
		type : "post",
		dataType : "html",
		contentType : "text/html; charset=UTF-8"
	}).done(function(html) {
		jQuery(cssSelector).html(html);
	});
}