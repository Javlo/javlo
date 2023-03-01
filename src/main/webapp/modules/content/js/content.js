jQuery(document).ready(function(){
	if (window.clipboardData == null) {
		jQuery('.clipboard').remove();
	} else {
		jQuery('.noclipboard').remove();
	}
	jQuery(".readonly input, .readonly textarea, .readonly select").attr("readonly", "readonly");
});

var selectFile=null;
function updateOrder() {
	jQuery(".manual-order .order-item .btn-move, .manual-order img").on( "click", function() {
		jQuery(".manual-order").removeClass("move-mode");
		jQuery(".manual-order .current-item").removeClass("current-item");
		if (selectFile != jQuery(this).data("name")) {
			jQuery(this).parent().parent().parent().parent().parent().addClass("move-mode");
			jQuery(this).parent().parent().parent().addClass("current-item");
			selectFile = jQuery(this).data("name");
		} else {
			selectFile=null;
		}
		return false;
	});
	jQuery(".manual-order .order-item .btn-first").on( "click", function() {
		var ajaxURL = jQuery(this).data("url");
		ajaxURL = addParam(ajaxURL, "position=0&file="+jQuery(this).data("name"));
		ajaxRequest(ajaxURL);
		selectFile=null;
		return false;
	});
	jQuery(".manual-order .order-drop").on( "click", function() {
		if (selectFile != null) {
			var ajaxURL = jQuery(this).attr("href");
			ajaxURL = addParam(ajaxURL, "position="+jQuery(this).data("pos")+"&file="+selectFile);
			ajaxRequest(ajaxURL);
			selectFile=null;
		}
		return false;
	});
}

var searchTimeoutThread = null;

function updateSearch(query, resultId, selectMethod) {
	if (searchTimeoutThread != null) {
		clearTimeout(searchTimeoutThread); 
	}
	searchTimeoutThread = setTimeout(function() {updateSearchThread(query, resultId, selectMethod)}, 500);
}

function updateSearchThread(query, resultId, selectMethod) {
	var searchURL = currentAjaxURL;
	searchURL = addParam(searchURL, "webaction=search.searchdefaultresulthtml");
	searchURL = addParam(searchURL, "q="+query);
	searchURL = addParam(searchURL, "id="+resultId);
	searchURL = addParam(searchURL, "method="+selectMethod);
	searchURL = addParam(searchURL, "max=50");
	searchURL = addParam(searchURL, "sort=relevance");
	ajaxRequest(searchURL);
}

function loadWysiwyg(cssQuery, complexity, chooseFileURL, format, fontsize, wysiwygCss) {
	if (wysiwygCss == null) {
		wysiwygCss = staticRootURL+"modules/content/js/tinymce.css";
	}

	tinymce.init({
		paste_as_text: true
	});
	
	if (format == null) {
		format = [		    	
	    	{ title: 'h1', block: 'h1', classes: 'heading' },
	    	{ title: 'h2', block: 'h2', classes: 'heading' },
	    	{ title: 'h3', block: 'h3', classes: 'heading' },
	    	{ title: 'h4', block: 'h4', classes: 'heading' },
	    	{ title: 'h5', block: 'h5', classes: 'heading' },
	    	{ title: 'h6', block: 'h6', classes: 'heading' },
	        { title: 'highlight', inline: 'span', classes: 'text-highlight' },
			{ title: 'whisper', inline: 'span', classes: 'text-whisper' },
			{ title: 'left', block: 'div', classes: 'float-left mr-3 mb-1' },
			{ title: 'right', block: 'div', classes: 'float-right ml-3 mb-1' }
	      ];
	}
	
	if (fontsize == null) {
		fontsize = "10px 11px 12px 13px 14px 15px 16px 17px 18px 19px 20px 22px 24px 26px 28px 30px 32px 34px 36px 38px 40px 42px";
	}

	if (complexity == "middle") {
		tinymce.init({
			textlang_langs : contentLanguage,
		    selector: cssQuery,
		    convert_urls: false,
		    menubar : false,
		    nonbreaking_force_tab: true,
		    content_css: wysiwygCss,
		    height : "180",
		    plugins: [
		        "advlist autolink lists link image charmap print preview anchor",
		        "searchreplace visualblocks code fullscreen",
		        "insertdatetime media table paste textcolor colorpicker nonbreaking textlang"
		    ],
			fontsize_formats: fontsize,
			image_advtab: true,
		    fontsize_formats: fontsize,
		    //paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		    file_browser_callback: function(field_name, url, type, win) {
	    	 	jQuery("body").data("fieldName", field_name);
	    	 	var fileURL = chooseFileURL.replace("_TYPE_",type);
		    	tinyMCE.activeEditor.windowManager.open({
		            file : fileURL,
		            title : 'Select resource',
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
			toolbar: "undo redo searchreplace | bold italic underline fontsizeselect forecolor backcolor removeformat | charmap nonbreaking | alignleft aligncenter alignright alignjustify | link | bullist numlist outdent indent | code"
		    //toolbar: "textlang | undo redo searchreplace | bold italic underline fontsizeselect forecolor backcolor removeformat | charmap nonbreaking | alignleft aligncenter alignright alignjustify | link | bullist numlist outdent indent"
		});
	} if (complexity == "email") {
		tinymce.init({
			textlang_langs : contentLanguage,			
		    selector: cssQuery,
		    convert_urls: false,
		    menubar : false,
		    nonbreaking_force_tab: true,
		    content_css: wysiwygCss,
		    height : "180",
		    plugins: [
		        "advlist autolink lists link image charmap print preview anchor",
		        "searchreplace visualblocks code",
		        "insertdatetime table paste textcolor colorpicker nonbreaking textlang"
		    ],
			fontsize_formats: fontsize,
			image_advtab: true,
		    fontsize_formats: fontsize,
		    //paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		    file_browser_callback: function(field_name, url, type, win) {		    	
	    	 	jQuery("body").data("fieldName", field_name);	    	 	
	    	 	var fileURL = chooseFileURL.replace("_TYPE_",type);	    	 	
		    	tinyMCE.activeEditor.windowManager.open({
		            file : fileURL,
		            title : 'Select resource',
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
			toolbar: "undo redo searchreplace | bold italic underline strikethrough fontsizeselect removeformat | table charmap nonbreaking code | alignleft aligncenter alignright alignjustify | link | code"
		    //toolbar: "textlang | undo redo searchreplace | bold italic underline strikethrough fontsizeselect removeformat | table charmap nonbreaking code | alignleft aligncenter alignright alignjustify | link"
		});
	} else if (complexity == "high") {
		var tinyConfig = {textlang_langs : contentLanguage,
		    selector: cssQuery,		
		    convert_urls: false,
		    menubar : false,
		    nonbreaking_force_tab: true,
		    theme: "silver",
		    height : "280",
		    content_css: wysiwygCss,
		    plugins: [
		        "advlist autolink lists link image charmap print preview hr anchor pagebreak",
		        "searchreplace wordcount visualblocks visualchars code fullscreen",
		        "insertdatetime media nonbreaking save table directionality",
		        "emoticons template paste colorpicker nonbreaking textlang"
		    ],
			style_formats: format,
			toolbar1: "styleselect bold italic underline strikethrough fontsizeselect removeformat | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | table charmap nonbreaking code",
		    //toolbar1: "textlang | styleselect bold italic underline strikethrough fontsizeselect removeformat | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link image | table charmap nonbreaking code",
		    charmap_append: [
		        [0x2600, 'sun'],
		        [0x2601, 'cloud'],
		        [0x010C, 'C with caron'],
		        [0x010D, 'c with caron'],
		        [0x01C5, "Latin Capital Letter D With Small Letter Z With Caron"],
		        [0x01C6, "Latin Small Letter Dz With Caron"],
		        [0x0110, 'Latin Capital Letter D With Stroke'],
		        [0x0111, 'Latin Small Letter D With Stroke'],
		        [0x0160, 'Latin Capital Letter S Hacek'],
		        [0x0161, 'Latin Small Letter S Hacek']
		      ],
		    image_advtab: true,
		    fontsize_formats: fontsize,
		    //paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		    file_browser_callback: function(field_name, url, type, win) {
	    	 	jQuery("body").data("fieldName", field_name);
	    	 	var fileURL = chooseFileURL.replace("_TYPE_",type);
		    	tinyMCE.activeEditor.windowManager.open({
		            file : fileURL,
		            title : 'Select resource',
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
		}
		if (colorList != null) {
			tinyConfig.color_map=colorList;
		}
		tinymce.init(tinyConfig);	
	} else if (complexity == "soft") {
		tinyMCE.init({
		textlang_langs : contentLanguage,
		mode : "specific_textareas",
		paste_as_text: true,
		plugins: "textlang paste",
		theme : "silver",
		content_css: wysiwygCss,
		add_form_submit_trigger: true,	
		menubar : false,
		convert_urls: false,
		selector: cssQuery,    	
		toolbar: "textlang | undo redo | bold italic | pastetext | alignleft aligncenter alignright alignjustify | code"
		});	 
	} else {
		tinyMCE.init({
		content_css : 'tinymce.css',
		textlang_langs : contentLanguage,
		mode : "specific_textareas",
		theme : "silver",
		convert_urls: false,
		content_css: wysiwygCss,
		add_form_submit_trigger: true,	
		menubar : false,
		selector: cssQuery,
		plugins: "paste link nonbreaking textlang",
		fontsize_formats: fontsize,
	    nonbreaking_force_tab: true,
		//paste_word_valid_elements: "b,strong,i,em,h1,h2,h3,h4,h5,h6,table,tr,th,td,ul,ol,li,p,a,div",
		toolbar: "textlang | undo redo | bold italic underline fontsizeselect removeformat | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | link pastetext nonbreaking"
		});	 
	}
}

function clipboardCopy(text) {
	window.clipboardData.setData('Text', text);
}


function smartLinkAction(item) {
	url = encodeURIComponent(jQuery(item).val());
	if (url == null || !url.startsWith("http")) {
		return;
	}
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
				var url = urlPrefix+"?webaction=smartlink.loadlink&url="+url+"&comp_id="+compId;
			} else {
				var url = urlPrefix+"&webaction=smartlink.loadlink&url="+url+"&comp_id="+compId;
			}
		}
		ajaxRequest(url, null, function() {jQuery(".waiting").css('display', 'none');});
	}
}

function createSmartLink() {
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
		item.append('<a class="next tt2" href="#" onclick="'+js+'"><span><i class="fa fa-chevron-right" aria-hidden="true"></i></span></a>');
		js = 'selectPreviousItem("'+compId+'",'+index+')';
		js = js.replace(/"/g, "'");
		item.prepend('<a class="previous" href="#" onclick="'+js+'"><span><i class="fa fa-chevron-left" aria-hidden="true"></i></span></a>');
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

function filterPage(url, filter, cssSelector, lang) {
	jQuery.ajax({
		url : url+'&filter='+filter+'&lang='+lang,
		cache : false,		
		type : "post",
		dataType : "html",
		contentType : "text/html; charset=UTF-8"		
	}).done(function(html) {
		jQuery(cssSelector).html(html);
	});
}

