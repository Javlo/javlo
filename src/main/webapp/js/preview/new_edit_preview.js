var dragging = false;

var mouseX = 0;
var mouseY = 0;

jQuery(document).ready(
		function() {
			
			jQuery( window ).mousemove(function( event ) {
				mouseX = event.pageX;
				mouseY = event.pageY;
				
				/*if (!mouseInLayer()) {
					console.log("RESET");
					layerOver(null,false);
				}*/
				
			});
			
			try {
				jQuery(".floating-preview #preview_command").draggable({
					handle : ".pc_header"
				});
				jQuery("body").append(
						'<div id="preview-layer"><span>&nbsp;</span></div>');
				
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
			jQuery(".preview-edit").click(function() {
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
			
			jQuery(document).on("ajaxUpdate", function() {
				updatePDFPosition();
			});
			
		});

function updatePDFPosition() {		
	var pdfHeight = parseInt(jQuery(".page_association_fake_body").data("pdfheight"));	
	var previousBreak = null;	
	jQuery(".page-break, ._page_associate").each(function() {		
		var currentBreak = jQuery(this);		
		if (previousBreak != null) {			
			if ((currentBreak.position().top - previousBreak.position().top) > pdfHeight) {				
				previousBreak.prepend('<div class="_pdf_page_limit"><span>&nbsp;</span></div>');
				var pdfLimit = jQuery(previousBreak.children()[0]);				
				pdfLimit.css('top',(previousBreak.position().top+pdfHeight)+'px');				
			}
		}		
		previousBreak = currentBreak;
	});
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
			layer.css("z-index", 10000);
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

initPreview = function() {
	
	jQuery(".editable-component").click(function() {		
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
			height : "95%"
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

	jQuery("#preview-layer").on('dragover', function(e) {
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
	jQuery("#preview-layer").on('dragenter', function(e) {		
		e.preventDefault();
		e.stopPropagation();
	});
	jQuery("#preview-layer").on('drop', function(e) {		
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

	jQuery(".editable-component, .not-editable-component, #preview-delete-zone, ._empty_area")
			.droppable(
					{						
						cursor : 'move',
						greedy : true,
						tolerance : 'pointer',
						drop : function(event, ui) {							
							var currentURL = addParam(window.location.href,"previewEdit=true");
							var layer = jQuery("#preview-layer");
							var comp = layer.data("subItem");
							
							var pageId = searchPageId(comp);
							var pageIdParam = "";
							if (pageId != null) {								
								pageIdParam = "&pageCompID="+pageId;
							}
							pageId = searchPageId(this);							
							if (pageId != null) {								
								pageIdParam = pageIdParam+"&pageContainerID="+pageId;
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
							
							if (!target.hasClass("free-edit-zone")) {
								if (target.find(".drop-zone").length == 0 && !target.hasClass("_empty_area") && !target.parent().data("drop-selected")) {
									target.append('<div class="drop-zone"><span>&nbsp;</span></div>');									
								}
							} else {
								target.parent().data("drop-selected", true);
								target.addClass("drop-selected");
								target.addClass("drop-ones");								
							}
							
							if (!target.hasClass("free-edit-zone") && !target.hasClass("_empty_area")) {
								if (target.find(".drop-zone").length == 0 && !target.parent().data("drop-selected")) {
									target.append('<div class="drop-zone"><span>&nbsp;</span></div>');
								}
							} else {
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