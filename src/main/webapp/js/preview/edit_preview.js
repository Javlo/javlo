var dragging = false;

var mouseX = 0;
var mouseY = 0;

function removeAnchorFromURL(url) {
	if (url.indexOf("#") < 0) {
		return url;
	} else {
		return url.substring(0,url.indexOf("#"));
	}
}

function splitHtml(text,cutPos) {
	var stackTags = [];
	var insideTag = false;
	var currentTag = "";
	var textPos = 0;
	
	for (var pos=0; pos<text.length && textPos<cutPos; pos++) {			
		if (!insideTag && text[pos] == '<') {
			insideTag = true;				
		}
		if (insideTag && text[pos] == '>') {				
			if (currentTag[0] == '/') {				
				stackTags.pop();
			} else {				
				stackTags.push(currentTag);
			}
			currentTag = "";
			insideTag = false;	
		}
		if (insideTag && text[pos] != '<') {
			currentTag = currentTag + text[pos];
		}
		if (!insideTag && text[pos] != '<' && text[pos] != '>') {
			textPos++;
		}
	}
	
	while (textPos > 0 && text[pos] != ' ' && text[pos] != '>') {
		pos--;
	}
	pos++;
	
	var firstText = text.substring(0,pos);
	var secondText = text.substring(pos,text.length);
	
	for (i=stackTags.length-1; i>=0; i--) {
		firstText = firstText+"</"+stackTags[i]+">";
		secondText = "<"+stackTags[i]+">"+secondText;
	}
	
	var outText = new Array();
	outText[0] = firstText;
	outText[1] = secondText;
	return outText;
}	

var floatZone = function(source, zone1, zone2, image){
	var zone1 = jQuery(zone1);	
	var zone2 = jQuery(zone2);	
	var image = jQuery(image);
	
	var html = jQuery(source).html();
	var sep = html.length;
	zone1.html(html);
	zone2.html('');
	while (sep > 0 && zone1.height() > image.height()) {
		sep = sep-1;
		while (sep > 0 && html[sep] != ' ') {
			sep = sep - 1;			
		}
		
		var outText = splitHtml(html, sep);
		
		zone1.html(outText[0]);
		zone2.html(outText[1]);
	}	
	//image.css("height", zone1.height()+"px");
	
	return sep;
};

function onreadyPreview() {
	
	closableFieldSet(jQuery(".closable"));
	
	specialScroll();
	
	jQuery( window ).mousemove(function( event ) {
		mouseX = event.pageX;
		mouseY = event.pageY;
	});
	
	jQuery("#preview_command .pc_command .slide").click(function() {
		var slide = jQuery(this);
		var command = jQuery("#preview_command .pc_command");
		if (command.hasClass("reduce")) {
			command.animate({marginLeft: 0},300);
			command.removeClass("reduce");					
		} else {
			command.animate({marginLeft: -(jQuery("#preview_command .pc_command").width()-200)+"px"},300);
			command.addClass("reduce");					
			command.addClass("reduce");
		}
		return false;
	});
	
	updateImagePreview();
	
	try {
		jQuery(".floating-preview #preview_command").draggable({
			handle : ".pc_header"
		});
		if (jQuery("#preview-layer").length == 0) {
			jQuery("body").append('<div id="preview-layer"><span>&nbsp;</span></div>');
		}		
		updatePDFPosition();				
	
		jQuery( document ).tooltip({
			position: {
			my: "center bottom-20",
			at: "center top",					
			using: function( position, feedback ) {
				 jQuery( this ).css( position );
				 jQuery( "<div>" )
				 .addClass( "arrow" )
				 .addClass( feedback.vertical )
				 .addClass( feedback.horizontal )
				 .appendTo( this );
			  }
			},
			items: "#preview-layer",
			show: {delay:500}
		});
		 
	} catch (err) {
		if (typeof console != 'undefined') {
			console.log("error on : " + err);
		}
	}
	
	jQuery(".reverse-link-preview").mouseover(function() {
		var link = jQuery(this);
		var popup = jQuery("#box-"+link.attr("id"));				
		popup.css("display", "block");
		popup.mouseleave(function() {
			jQuery(this).css("display", "none");
		});
	});

	initPreview();

	jQuery("body").mouseover(function() {				
		return true;
	});
	jQuery(window).scroll(function() {				
		if (!dragging) {					
			layerOver(null, false);
		}
		return true;
	});
	jQuery("#preview-layer").click(
			function() {						
				layerOver(null);
				jQuery(this).data("subItem").trigger("click",
						jQuery(this).data("subItem"));
				return true;
			});		
	jQuery("#preview-layer").mouseout(
			function() {						
				if (!dragging) {					
					layerOver(null, false);
				}
			});		
	jQuery("#preview-layer").scroll( function() {
		alert("scroll");
	});
	jQuery(".preview-edit").click(function() {
		
		if (jQuery(this).hasClass("no-access")) {
			return false;
		}
		var elems = jQuery(this);
		var editURL = elems.attr("action");
		var param = "";
		jQuery.colorbox({
			href : editURL + param,
			opacity : 0.6,
			iframe : true,
			width : "95%",
			height : "95%"
		});
		return false;
	});
	
	/////////// SORTABLE /////////////
	jQuery(".sortable").sortable({
		   placeholder: "sortable-target"
		   ,stop: function(event, ui) {
			   var url = jQuery("#children_list").attr("action");
			   url=addParam(url,"webaction=edit.movePage&page="+jQuery(ui.item).attr("id")+"&previous="+jQuery(ui.item).prev().attr("id"));
			   ajaxRequest(url);
		   }
	});
	
	jQuery(".editable-component.ui-droppable").each(function() {
		if (jQuery(this).height() == 0 && jQuery(this).children().height() > 0) { // don't contains block item
			jQuery(this).css("float", "left"); // out of the flow
			jQuery(this).height(jQuery(this).children().height());
		}				
	});
}



function updateImagePreview() {
	jQuery(".image-preview-loading").each(function() {
		var image = jQuery(this);
		image.load(function() {
			if (jQuery(this).attr("src") == jQuery(this).data("src")) {
				jQuery(this).removeClass("image-preview-loading");
			}
		});
		image.attr("src", image.data("src"));
	});
}

function updatePDFPosition() {
		jQuery("._pdf_page_limit").remove();
		setTimeout(function () {	        
	        realUpdatePDFPosition();	        
	    }, 1500);
}

function realUpdatePDFPosition() {	
	var pdfHeight = parseInt(jQuery(".page_association_fake_body").data("pdfheight"));	
	var previousBreak = null;	
	jQuery("._page_associate").each(function() {
		var page = jQuery(this);
		var currentTop = page.position().top;
		jQuery(".page-break").each(function() {	
			var currentBreak = jQuery(this);
			if (currentTop + currentBreak.position().top < pdfHeight) {
				currentTop = currentBreak.position().top;
			}
		});
		if ((page.position().top+page.height())-currentTop > pdfHeight) {			
			page.prepend('<div class="_pdf_page_limit"><span>&nbsp;</span></div>');
			var pdfLimit = jQuery(page.children()[0]);				
			pdfLimit.css('top',(currentTop+pdfHeight)+'px');
		}
	});	
}

function __OLD___updatePDFPosition() {	
	var pdfHeight = parseInt(jQuery(".page_association_fake_body").data("pdfheight"));	
	var previousBreak = null;	
	jQuery("._pdf_page_limit").remove();
	console.log("-----------------------------");
	jQuery(".page-break, ._page_associate").each(function() {	
		var currentBreak = jQuery(this);
		if (currentBreak.hasClass("page-break")) {
			console.log("pageBreak top="+currentBreak.position().top);
		} else {
			console.log("_page_associate top="+currentBreak.position().top);
		}
		if (previousBreak != null) {
			console.log("currentBreak.position().top - previousBreak.position().top = "+(currentBreak.position().top - previousBreak.position().top) );
			console.log("pdfHeight = "+pdfHeight);
			if ((currentBreak.position().top - previousBreak.position().top) > pdfHeight) {
				previousBreak.prepend('<div class="_pdf_page_limit"><span>&nbsp;</span></div>');
				var pdfLimit = jQuery(previousBreak.children()[0]);				
				pdfLimit.css('top',(previousBreak.position().top+pdfHeight)+'px');				
			}
		}		
		previousBreak = currentBreak;
	});
	//latest page
	if (previousBreak != null  && previousBreak.height() > pdfHeight) {
		//previousBreak.css("position", "relative");
		previousBreak.prepend('<div class="_pdf_page_limit"><span>&nbsp;</span></div>');
		var pdfLimit = jQuery(previousBreak.children()[0]);				
		pdfLimit.css('top',pdfHeight+'px');
	}
}

mouseInLayer = function() {
	var layer = jQuery("#preview-layer");
	if (mouseX < layer.position().left || mouseY < layer.position().top) {	
		return false;
	} else if (mouseX > layer.position().left+layer.width() || mouseY > layer.position().top+layer.height()) {
	
		return false;
	}	
	return true;
}

layerOver = function(item, deletable) {	
	var layer = jQuery("#preview-layer");	
	layer.data("deletable", deletable);
	
	var insideLayer = jQuery("#preview-layer span");	
	if (item == null) {		
		layer.css("z-index", -1);
		layer.css("display", "none");		
		layer.data("compType", null);
		layer.data("sharedContent", null);
		layer.attr("title", "");
	} else {		
		var comp = jQuery(item);
		if (layer.width() > 0) {
			layer.css("z-index", 10010);
			layer.css("display", "block");
		}
		
		if (comp.hasClass("editable-component")) {			
			if (comp.data("hint")) {
				layer.attr("title", comp.data("hint"));
			} else {
				layer.attr("title", "move with drag and drop or click to edit.");
			}
		} else {
			if (comp.data("hint")) {
				layer.attr("title", comp.data("hint"));
			} else {
				layer.attr("title", "move with drag and drop.");
			}
		}
		
		layer.css("top", comp.offset().top);
		layer.css("left", comp.offset().left);
		
		layer.css("width", comp.outerWidth(false));			
		layer.css("height", comp.outerHeight(false));
		
		layer.data("subItem", comp);
	}
}

var initPreviewDone = false;

initPreview = function() {
	
	jQuery(".editable-component").click(function() {
		
		if (initPreviewDone) {
			return false;
		}
		initPreviewDone=true;
		
		layerOver(null);
		var elems = jQuery(this);
		var editURL = editPreviewURL + "&comp_id=" + elems.attr("id");
		var param = "";
		if (jQuery("#search-result").length > 0) {
			param = "&noinsert=true";
		}
		jQuery.colorbox({
			href : editURL + param,
			opacity : 0.6,
			iframe : true,
			width : "95%",
			height : "95%",
			onComplete : function() {
				initPreviewDone = false;
			}
		});
		return false;
	});

	jQuery(".editable-component").mouseover(function() {
		if (!dragging) {			
			layerOver(this, true);
		}
		return false;
	});
	
	jQuery(".component-list .component, .shared-content .content").mouseover(function() {
		if (!dragging) {		
			layerOver(this, false);
			var layer = jQuery("#preview-layer");
			layer.data("compType", jQuery(this).data("type"));			
			layer.data("sharedContent", jQuery(this).data("shared"));
		}
		return false;
	});
	
	jQuery(".component-list .component,  .shared-content .content").mouseout(function() {
		if (!dragging) {			
			layerOver(this, false);
		}		
		return false;
	});

	jQuery("#preview-layer").live('dragover', function(e) {
		e.preventDefault();
		e.stopPropagation();
		layer = jQuery(this);
		var height = layer.height();
		if (height < 40) {
			height = 40;
		}					
		layer.css("height", height);	
	});
	/*jQuery("#preview-layer").on('mouseout', function(e) {		
		if (!dragging) {			
			layerOver(null);
		}
	});*/	
	jQuery("#preview-layer").live('dragenter', function(e) {		
		e.preventDefault();
		e.stopPropagation();
	});
	jQuery("#preview-layer").live('drop', function(e) {
		layerOver(null,false);
		jQuery(this).data("compType", null);
		e.preventDefault();
		e.stopPropagation();
	});
	
	searchPageId = function(node) {
		
		var parent = jQuery(node).parent();
		
		while (parent !== undefined && !parent.hasClass("_page_associate") && parent.prop("tagName") !== undefined) {			
			parent = jQuery(parent).parent();
		}		
		
		if (parent.prop("tagName") !== undefined) {			
			return jQuery(parent).attr("id").replace("page_","");
		} else {
			return null;
		}
		
		return null;
	}

	jQuery(".editable-component, .not-editable-component, #preview-delete-zone,#preview-copy-zone, ._empty_area")
			.droppable(
					{						
						cursor : 'move',
						greedy : true,
						tolerance : 'pointer',
						drop : function(event, ui) {
							var currentURL = addParam(removeAnchorFromURL(window.location.href),"previewEdit=true");
							var layer = jQuery("#preview-layer");
							var comp = layer.data("subItem");
							
							var pageId = searchPageId(comp);
							var pageIdParam = "";
							if (pageId != null) {								
								pageIdParam = "&pageCompID="+pageId;
							}
							var containerId = searchPageId(this);							
							if (containerId != null) {								
								pageIdParam = pageIdParam+"&pageContainerID="+containerId;
							}
							
							var compType = layer.data("compType");
							var sharedContent = layer.data("sharedContent");
							var area = null;
							
							if (compType !== undefined && compType != null) {
								var previewId = "0";
								if (jQuery(this).attr("id")) {
									var previewId = jQuery(this).attr("id").replace("cp_", "");
								}
								var parent = jQuery(this).parent();
								while (jQuery(parent).get(0).tagName.toLowerCase() != "body" && !parent.hasClass("_area")) {									
									parent = jQuery(parent).parent();									
								}
								area = jQuery(parent).attr("id");
								var ajaxURL = addParam(currentURL,"webaction=edit.insert&type="
								+ compType + "&previous=" + previewId
								+ "&area=" + area+ "&render-mode=3&init=true"+pageIdParam);
								ajaxRequest(ajaxURL,null,updatePDFPosition);
							} else if (sharedContent != null && sharedContent !== undefined) {								
								var previewId = "0";
								if (jQuery(this).attr("id")) {
									var previewId = jQuery(this).attr("id").replace("cp_", "");
								}
								var parent = jQuery(this).parent();
								while (jQuery(parent).get(0).tagName.toLowerCase() != "body" && !parent.hasClass("_area")) {									
									parent = jQuery(parent).parent();									
								}
								area = jQuery(parent).attr("id");
								var ajaxURL = addParam(currentURL, "webaction=edit.insertShared&sharedContent="
								+ sharedContent + "&previous=" + previewId
								+ "&area=" + area+ "&render-mode=3&init=true"+pageIdParam);
								ajaxRequest(ajaxURL,null,updatePDFPosition);
							} else if (comp !== undefined && jQuery(comp).attr("id") != null && jQuery(comp).attr('id') != jQuery(this).attr("id")) {								
								var compId = jQuery(comp).attr("id").replace("cp_", "");
								if (jQuery(this).attr("id") == "preview-delete-zone") {									
									var ajaxURL = addParam(currentURL,"webaction=edit.delete&id=" + compId + "&render-mode=3"+pageIdParam);
									jQuery(comp).remove();
								} else if (jQuery(this).attr("id") == "preview-copy-zone") {									
									var ajaxURL = addParam(currentURL,"webaction=edit.copy&id=" + compId + "&render-mode=3"+pageIdParam);									
								} else if (comp !== undefined) {
									jQuery(comp).insertAfter(jQuery(this));
									var previewId = "0";
									if (jQuery(this).attr("id")) {
										var previewId = jQuery(this).attr("id").replace("cp_", "");
									}								
									var parent = jQuery(comp).parent();
									while (parent !== undefined && !parent.hasClass("_area")) {
										parent = jQuery(parent).parent();
									}
									area = jQuery(parent).attr("id");
									
									var ajaxURL = addParam(currentURL,"webaction=edit.moveComponent&comp-id="
											+ compId + "&previous=" + previewId
											+ "&area=" + area + "&render-mode=3" + pageIdParam);
								}
								ajaxRequest(ajaxURL,null,updatePDFPosition);
							}							
							layerOver(null);
							jQuery(this).find(".drop-zone").remove();
							jQuery(this).removeClass("drop-selected");	
							jQuery(this).parent().data("drop-selected", false);
						},
						over : function(event, ui) {	
							dragging = true;
							var dropLayer = jQuery("#droppable-layer");
							var layer = jQuery("#preview-layer");
							var target = jQuery(this);
							
							if (!target.hasClass("free-edit-zone") && !target.hasClass("_empty_area")) {
								if (target.find(".drop-zone").length == 0 && !target.parent().data("drop-selected") && !target.parent().data("drop-selected")) {									
									target.append('<div class="drop-zone"><span>&nbsp;</span></div>');
								}
							} else {
								target.parent().data("drop-selected", true);
								target.addClass("drop-selected");
								target.addClass("drop-ones");
							}							
							
							if (dropLayer.offset.top < 0) {								
								dropLayer.css("top", "" + target.offset().top
										+ "px");
								dropLayer.css("left", "" + target.offset().left
										+ "px");
							}
							jQuery("#preview-layer").addClass("drop-up");	
						},
						out : function(event,ui) {
							jQuery(this).find(".drop-zone").remove();
							jQuery(this).removeClass("drop-selected");							
							jQuery(this).parent().data("drop-selected", false);
							jQuery("#preview-layer").removeClass("drop-up");
						},
						deactivate : function(event,ui) {
							jQuery(this).removeClass("drop-ones");
						}
					});

	dragging = false;
	
	jQuery("#preview-layer").draggable({
		cursor : "move",
		tolerance: "pointer",
		start : function(event, ui) {
			dragging = true;
			var layer = jQuery("#preview-layer");
			
			/* stable scroll */
			var height = jQuery("body").height();
			var scroll = jQuery(window).scrollTop();
			
			jQuery(".free-edit-zone").addClass("droppable");
			
			jQuery("._pdf_page_limit").hide();
			
			if (jQuery(this).data("deletable") !== undefined && jQuery(this).data("deletable")) {
				jQuery("#preview-delete-zone").removeClass("hidden");
				jQuery("#preview-copy-zone").removeClass("hidden");
				jQuery("#preview-copy-zone").parent().css("position", "fixed");
				//jQuery("#preview_command .pc_body").addClass("hidden");
			}
			if (jQuery(this).height() < 40) {			
				jQuery(this).height(40);
			};
			/*jQuery(this).height(40);
			jQuery(this).width(40);			
			console.log("mouseX = "+mouseX);
			jQuery(this).offset({ top: mouseY, left: mouseX});*/
			
			/* stable scroll */			
			jQuery(window).scrollTop(scroll*jQuery("body").height()/height);

		},
		stop : function(event, ui) {
			
			jQuery("._pdf_page_limit").show();
			
			/* stable scroll */
			var height = jQuery("body").height();
			var scroll = jQuery(window).scrollTop();
			
			var dropLayer = jQuery("#droppable-layer");
			dropLayer.css("top", "-9999px");
			dragging = false;
			jQuery(".free-edit-zone").removeClass("droppable");
			//if (jQuery(this).data("deletable") !== undefined && jQuery(this).data("deletable")) {
				//jQuery("#preview_command .pc_body").removeClass("hidden");
				jQuery("#preview-delete-zone").addClass("hidden");
				jQuery("#preview-copy-zone").addClass("hidden");
				jQuery("#preview-copy-zone").parent().css("position", "static");
			//}
			layerOver(null);
			
			/* stable scroll */			
			jQuery(window).scrollTop(scroll*jQuery("body").height()/height);

		}
	});
}

function doNothing(evt) {
	evt.stopPropagation();
	evt.preventDefault();
}

function hidePreviewMessage() {
	jQuery("#pc_message").remove();
}

jQuery(window).resize(function() {
	specialScroll();
});

function specialScroll() {	
	fullHeight(jQuery(".full-height"));	
	fullHeight(jQuery(".auto-scroll"));
	if ($(".auto-scroll.js-scroll").length == 0) {
		$('.auto-scroll').addClass("js-scroll");
		$('.auto-scroll').enscroll({
			showOnHover: true,
		    verticalTrackClass: 'track',
		    verticalHandleClass: 'handle'
		});
	}
}

function fullHeight(inItem) {
	jQuery(inItem).each(function() {
		var item = jQuery(this);		
		var position = item.css("position");
		item.css("position", "absolute");
		var fullHeight = item.parent().height();
		var childHeight = item.parent().outerHeight(true)-item.parent().height();
		item.parent().children().each(function() {			
			var child = jQuery(this);			
			if (child.css("position") == "relative" || child.css("position") == "static" && child != item) {				
				childHeight = childHeight + child.outerHeight(false);
			}		
		});	
		item.css("position", position);
		item.css("height",(fullHeight-childHeight-(item.outerHeight(true)-item.height()))+"px");		
	});
}