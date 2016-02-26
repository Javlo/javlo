var PREVIEWLOG = false;

var editPreview = editPreview||{};

+function($,jQuery,pjq) {

function handleDragStart(e) {
  this.style.opacity = '0.4';  // this / e.target is the source node.
}

editPreview.layerOver = function(item, title, drop) {	
	var layer = pjq("#preview-layer");
	pjq("._ep_new-component-zone").remove();
	
	
	var insideLayer = pjq("#preview-layer span");	
	if (item == null) {		
		layer.css("z-index", -1);
		layer.hide();		
		layer.data("compType", null);
		layer.data("sharedContent", null);
		layer.attr("title", " ");		
	} else {		
		var comp = pjq(item);		
		var parent = comp.parent();
		var area = "";
		c = 1;
		while (parent.attr("id") != "top" && c < 20) {
			c++;
			if (parent.hasClass("_area")) {
				area = parent.attr("id");
			}
			parent = parent.parent();
		}
		pjq('#area-name').text(area);
		
		if (drop) {
			comp.after('<div class="_ep_new-component-zone"></div>');
		} else  {
			if (item.getAttribute("data-name") != null && item.getAttribute("data-name").length > 0) {
				pjq("#preview-layer h4").html(item.getAttribute("data-name"));
				pjq("#preview-layer").removeClass("nocommand");
			} else {
				pjq("#preview-layer h4").html(i18n_first_component);
				pjq("#preview-layer").addClass("nocommand");
			}
			layer.css("z-index", 10010);
			layer.show();		
		
			layer.css("top", comp.offset().top);
			layer.css("left", comp.offset().left);
			
			if (comp.offset().top<80) {
				layer.addClass('bottom');
			} else {
				layer.removeClass('bottom');
			}
		
			var width = comp.outerWidth(false);
			layer.css("width", width);
			if (width < 350) {
				layer.addClass("small");
			} else {
				layer.removeClass("small");
			}
			layer.css("height", comp.outerHeight(false));		
			layer.data("comp", comp);
		}		
	}
}

editPreview.openModal = function (title, url) {	
	editPreview.layerOver(null);	
	pjq('#preview-modal-frame').attr("src", url);	
	pjq('#previewModalTitle').html(title);	
	pjq('#preview-modal').modal('show');
	pjq('#preview-modal').on('hidden.bs.modal', function (e) {
		pjq('#previewModalTitle').html('');
		pjq('#preview-modal-frame').attr("src", pjq('#preview-modal-frame').data("wait"));
	});
	var modalMargin = parseInt(pjq('#preview-modal .modal-dialog').css("margin-top").replace("px", ""))*2;
	var bodyPadding = parseInt(pjq('#preview-modal .modal-body').css("padding-top").replace("px", ""))+parseInt(pjq('#preview-modal .modal-body').css("padding-bottom").replace("px", ""));
	pjq('#preview-modal .modal-body iframe').height(pjq(window).height()-(pjq('#preview-modal .modal-header').outerHeight(true)+modalMargin+bodyPadding));
}

editPreview.updatePDFPosition = function() {
	pjq("._pdf_page_limit").remove();
	if (pjq('body').hasClass('pdf')) {		
		setTimeout(function () {	        
			editPreview.realUpdatePDFPosition();	        
		}, 1500);
	}
}

editPreview.realUpdatePDFPosition = function() {
	if (pjq('body').hasClass('pdf')) {
		var pdfHeight = parseInt(pjq(".page_association_fake_body").data("pdfheight"));	
		var previousBreak = null;	
		pjq("._page_associate").each(function() {
			var page = pjq(this);
			var currentTop = page.position().top;
			pjq(".page-break").each(function() {	
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
}

editPreview.scrollToItem = function(container) {
	pjq("body").append('<div id="_fake_footer" style="font-size: 0; position: fixed; bottom: 0;">&nbsp;</div>');
	var fakeFolder = pjq("#_fake_footer");
	var footerOffset = fakeFolder.offset();
	fakeFolder.remove();
	if (typeof pjq(container).offset() == 'undefined') {
		return;
	}
	var top = pjq(container).offset().top;	
	var body = $("html, body");	
	if (top > (footerOffset.top/2+footerOffset.top/4)) {
		body.animate({scrollTop:(top-150)}, '100', 'swing');
	}
}

editPreview.isScrollBottom = function(container) {
	var item = pjq(container);
	var scrollTop = item.scrollTop();
	item.scrollTop(99999);
	var outScrollBottom = (scrollTop == item.scrollTop());
	item.scrollTop(scrollTop);
	return outScrollBottom;	
}

editPreview.searchPageId = function(node) {	
	var parent = pjq(node).parent();	
	while (parent !== undefined && !parent.hasClass("_page_associate") && parent.prop("tagName") !== undefined) {			
		parent = pjq(parent).parent();
	}	
	if (parent.prop("tagName") !== undefined) {			
		return pjq(parent).attr("id").replace("page_","");
	} else {
		return null;
	}	
	return null;
}

editPreview.heightToBottom = function(inItem) {
	pjq("body").append('<div id="_fake_footer" style="font-size: 0; position: fixed; bottom: 0;">&nbsp;</div>');
	var fakeFolder = pjq("#_fake_footer");
	var footerOffset = fakeFolder.offset();
	fakeFolder.remove();
	pjq(inItem).each(function() {
		var item = pjq(this);
		var saveHeight = item.height();
		var itemOffset = item.offset();		
		item.css("height", footerOffset.top-itemOffset.top);	
		if (saveHeight > item.height()) {
			item.addClass("has-scrollbar");
		} else {
			item.removeClass("has-scrollbar");
		}
	});	
}

editPreview.searchArea = function(item) {	
	var parent = pjq(item);
	while (pjq(parent).get(0).tagName.toLowerCase() != "body" && !parent.hasClass("_area")) {									
		parent = pjq(parent).parent();									
	}
	if (parent.hasClass("_area")) {
		return pjq(parent).attr("id");
	} else {
		return null;
	}
}

editPreview.initPreview = function() {
	
	//pjq('#preview_command a').attr('draggable', 'false');  
	
	pjq('a.as-modal').on('click', function() {
		var text = $(this).attr("title");
		if (text == null || text.length == 0) {
			text = $(this).text();
		}
		editPreview.openModal(text, $(this).attr('href'));		
		return false;
	});
	
	pjq('.slow-hide').on('load', function() {
		pjq(this).delay(5000).fadeOut(500);
	});
	
	pjq('.slow-hide').delay(5000).fadeOut(500);
	
	/**********************/
	/** prepare preview * */
	/**********************/
	
	if (pjq("#preview-layer").length == 0) {
		pjq("body").append('<div id="preview-layer"><div class="commands btn-group btn-group-sm area-actions" role="group"><span class=\"btn area-name\"><span class="glyphicon glyphicon-th-large" aria-hidden="true"></span><span id=\"area-name\"></span></span>'+
				'<button class="btn-edit btn btn-primary"><span class="glyphicon glyphicon-edit" aria-hidden="true"></span><span class="text">edit</span></button>'+
				'<button class="btn-copy btn btn-primary"><span class="glyphicon glyphicon-copy" aria-hidden="true"></span><span class="text">copy</span></button>'+
				'<button class="btn-delete btn btn-primary"><span class="glyphicon glyphicon-trash" aria-hidden="true"></span><span class="text">delete</span></button>'+
				'</div><h4></h4><div class="main">&nbsp;</span></div>');
		pjq("#preview-layer").css("position", "absolute");
		pjq("#preview-layer").on('mouseleave', function (event) {	    	
			editPreview.layerOver(null);
	    });
		pjq("#preview-layer").hide();
		pjq("#preview-layer").on('click', function (event) {
			var compId = pjq(this).data("comp").attr("id").substring(3);
			var editURL = editPreviewURL + "&comp_id=" + compId;
			editPreview.openModal(i18n_preview_edit, editURL);	
			
	    });
		pjq('#preview-layer .btn-delete').on('click', function (e) {
			editPreview.layerOver(null);
			var subComp = pjq(this).parent().parent().data("comp");
			var compId = subComp.attr("id").substring(3);
			var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.delete&id=" + compId);
			if (editPreview.searchPageId(subComp) != null) {					
				ajaxURL = ajaxURL +'&pageCompID='+ editPreview.searchPageId(subComp);
			}				
			editPreview.ajaxPreviewRequest(ajaxURL, function() {editPreview.layerOver(null);}, null);			
			return false;
		});
		pjq('#preview-layer .btn-copy').on('click', function (e) {
			editPreview.layerOver(null);
			var subComp = pjq(this).parent().parent().data("comp");
			var compId = subComp.attr("id").substring(3);
			var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.copy&id=" + compId);
			if (editPreview.searchPageId(subComp) != null) {					
				ajaxURL = ajaxURL +'&pageCompID='+ editPreview.searchPageId(subComp);
			}				
			editPreview.ajaxPreviewRequest(ajaxURL, function() {editPreview.layerOver(null);editPreview.heightToBottom(pjq(".height-to-bottom"));}, null);
			return false;
		});
		/*************************/
		/** drag and drop layer **/
		/*************************/
		var drop = document.querySelectorAll('#preview-layer'), el = null;
		el = drop[0];	
		el.addEventListener('dragover', function (event) {
	    	event.preventDefault();    	
	    	return false;
	    });
		el.setAttribute('draggable', 'true');
		el.addEventListener('dragstart', function (event) {			
			var subComp = pjq(this).data("comp");
			event.dataTransfer.setData("text", ","+subComp.attr("id").substring(3));			
			pjq(".free-edit-zone").addClass("open");
		});
		el.addEventListener('drop', function (event) {
			event.preventDefault();    	
	    	return false;
		});
		el.addEventListener('dragend', function (event) {
			pjq(".free-edit-zone").removeClass("open");
		});		
	}	
	/******************************/	
	/** drag and drop component * */
	/******************************/	
	var drag = document.querySelectorAll('#preview_command .component'), el = null;
	for (var i = 0; i < drag.length; i++) {
		el = drag[i];    
		el.setAttribute('draggable', 'true');  
		el.addEventListener('dragstart', function (event) {	
			var scrollBottom = editPreview.isScrollBottom(pjq('html'));
			event.dataTransfer.setData("text", this.getAttribute("data-type"));
			pjq(".free-edit-zone").addClass("open");	
			if (scrollBottom) {
				pjq('html').scrollTop(99999);
			}
		});
		el.addEventListener('dragend', function (event)  {
			pjq(".free-edit-zone").removeClass("open");
		});
		el.addEventListener('drop', function (event) {
			event.preventDefault();	
		})   
	}
	var drag = document.querySelectorAll('#preview_command .shared-content-item'), el = null;
	for (var i = 0; i < drag.length; i++) {
		el = drag[i];    
		el.setAttribute('draggable', 'true');  
		el.addEventListener('dragstart', function (event) {
			var scrollBottom = editPreview.isScrollBottom(pjq('html'));
			var sharedId = this.getAttribute("data-shared");			
			event.dataTransfer.setData('text', ",,"+sharedId);
			pjq(".free-edit-zone").addClass("open");
			if (scrollBottom) {
				pjq('html').scrollTop(99999);
			}
		});
		el.addEventListener('dragend', function (event) {
			pjq(".free-edit-zone").removeClass("open");
		});
		el.addEventListener('drop', function (event) { 
			event.preventDefault();    	
		});    
	}	
	var drop = document.querySelectorAll('.editable-component'), el = null;
	var countDrop = 0;
	for (var i = 0; i < drop.length; i++) {		
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;			
		    el.addEventListener('dragover', function (event) {
		    	var rowData = event.dataTransfer.getData("text").split(",");
				var compType = rowData[0];
				if (compType.indexOf("page:") == 0) {
		    		return;
				}
		    	event.preventDefault();
		    	editPreview.layerOver(this, null, true);
		    	return false;
		    });
		    el.addEventListener('dragleave', function (event) {
		    	event.preventDefault();
		    	editPreview.layerOver(null, null, true);
		    	return false;
		    });
		    el.addEventListener('mouseover', function (event) {
		    	if (!event.ctrlKey) {
		    		editPreview.layerOver(this, null, false);
		    	}
		    });		    
		    el.addEventListener('drop', function (event) {
		    	
		    	if (PREVIEWLOG) {
		    		console.log("*** DROP COMPONENT ***");
		    	}
		    	
				event.preventDefault();
				var rowData = event.dataTransfer.getData("text").split(",");
				if (PREVIEWLOG) {
		    		console.log("rowData   = ",rowData);
		    	}
				var compType = rowData[0];
				if (compType.indexOf("page:") == 0) {
		    		return;
				}
				if (rowData.length > 1) {
					var compId = rowData[1];
					if (rowData.length > 2) {
						var sharedId = rowData[2];
					}
				}			
				var subComp = pjq(this);
				
				countDrop++;
				if (PREVIEWLOG) {
		    		console.log("compId    = ",compId);
		    		console.log("compType  = ",compType);
		    		console.log("area      = ",area);
		    		console.log("sharedId  = ",sharedId);
		    		console.log("countDrop = ",countDrop);
		    	}				
				if (countDrop>1) {
					countDrop=0;
					return false;
				}			
				
				if (sharedId != null && sharedId.length > 0) {				
					var previewId = subComp.attr("id").substring(3);		
					var area = editPreview.searchArea(subComp);
					var ajaxURL = editPreview.addParam(currentURL, "webaction=edit.insertShared&sharedContent="
					+ sharedId + "&previous=" + previewId
					+ "&area=" + area+ "&render-mode=3&init=true");
					if (editPreview.searchPageId(subComp) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(subComp);
					}	
					editPreview.ajaxPreviewRequest(ajaxURL, null, null);
				} else if (compType != null && compType.length > 0) { // insert new component
					pjq(this).removeClass("drop-selected");
					var previewId = subComp.attr("id").substring(3);		
					var area = editPreview.searchArea(subComp);		
					var url = "webaction=edit.insert&type=" + compType + "&previous=" + previewId + "&area=" + area+ "&render-mode=3&init=true";
					if (editPreview.searchPageId(subComp) != null) {
						url = url +'&pageContainerID='+ editPreview.searchPageId(subComp);
					}		
					var ajaxURL = editPreview.addParam(currentURL,url);
					if (editPreview.searchPageId(subComp) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(subComp);
					}					
					editPreview.ajaxPreviewRequest(ajaxURL, function() {
						if (pjq(".edit-component").length > 0) {
							var compId = pjq(".edit-component").attr("id").substring(3);						
							var editURL = editPreviewURL + "&comp_id=" + compId;
							editPreview.openModal(i18n_preview_edit, editURL);
						}
					}, null);
				} else if (compId != null && compId.length > 0) { // move component				
					var previewId = subComp.attr("id").substring(3);				
					var area = editPreview.searchArea(subComp);		
					var ajaxURL = editPreview.addParam(currentURL,"webaction=edit.moveComponent&comp-id=" + compId + "&previous=" + previewId + "&area=" + area+ "&render-mode=3&init=true");
					if (editPreview.searchPageId(subComp) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(subComp);
					}	
					editPreview.ajaxPreviewRequest(ajaxURL, null, null);
				} else if (event.dataTransfer.files.length > 0) {					
					var previewId = subComp.attr("id").substring(3);	
					var ajaxURL = editPreview.addParam(currentURL,"webaction=data.upload&content=true&previous=" + previewId);
					if (editPreview.searchPageId(subComp) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(subComp);
					}
					var fd=new FormData();
					var fieldName = pjq(this).data("fieldname");
					if (fieldName == null) {
						fieldName = "files";
					}
					var i = 0;
					jQuery.each( event.dataTransfer.files, function(index, file) {
						if (i==0) {
							fd.append(fieldName,file);
						} else {
							fd.append(fieldName+"_"+i,file);
						}
						i++;			
					});					
					editPreview.ajaxPreviewRequest(ajaxURL, null, fd);					
				}				
				return false;	
		    })   
		}		
	}
	/*************************/	
	/** drag and drop area * */
	/********************** ***/
	var drop = document.querySelectorAll('._empty_area'), el = null;	
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;			
		    el.addEventListener('dragover', function (event) {
		    	event.preventDefault();
		    	pjq(this).addClass("drop-selected");
		    	return false;
		    });
		    el.addEventListener('dragleave', function (event) {
		    	event.preventDefault();
		    	pjq(this).removeClass("drop-selected");
		    	return false;
		    });	    
		    el.addEventListener('drop', function (event) {		    	
		    	if (PREVIEWLOG) {
		    		console.log("*** DROP AREA ***");
		    	}		    	
		    	event.preventDefault();
				var rowData = event.dataTransfer.getData("text").split(",");
				if (PREVIEWLOG) {
		    		console.log("rowData   = ",rowData);
		    	}
				var compType = rowData[0];				
				if (compType != null && compType.length > 0 && compType.indexOf("page:") == 0) {
					pjq(this).removeClass("drop-selected");
		    		return false;
				}
				if (rowData.length > 1) {
					var compId = rowData[1];
					if (rowData.length > 2) {
						var sharedId = rowData[2];
					}
				}
				if (PREVIEWLOG) {
		    		console.log("compId   = ",compId);
		    		console.log("compType = ",compType);
		    		console.log("area     = ",area);
		    		console.log("sharedId = ",sharedId);
		    	}
				var area = editPreview.searchArea(pjq(this).parent());			
				if (sharedId != null && sharedId.length > 0) {											
					var ajaxURL = editPreview.addParam(currentURL, "webaction=edit.insertShared&sharedContent="
					+ sharedId + "&previous=0"
					+ "&area=" + area+ "&render-mode=3&init=true");				
					if (editPreview.searchPageId(this) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(this);
					}
					editPreview.ajaxPreviewRequest(ajaxURL, null, null);
				} else if (compType != null && compType.length > 0) {
					pjq(this).removeClass("drop-selected");					
					var url = "previewEdit=true&webaction=edit.insert&type=" + compType + "&previous=0&area=" + area+ "&render-mode=3&init=true";
					if (editPreview.searchPageId(this) != null) {					
						url = url +'&pageContainerID='+ editPreview.searchPageId(this);
					}
					var ajaxURL = editPreview.addParam(currentURL,url);					
					editPreview.ajaxPreviewRequest(ajaxURL, function() {
						if (pjq(".edit-component").length > 0) {
							var compId = pjq(".edit-component").attr("id").substring(3);						
							var editURL = editPreviewURL + "&comp_id=" + compId;
							editPreview.openModal(i18n_preview_edit, editURL);
						}
					}, null);
				} else if (compId != null && event.dataTransfer.files.length == 0) { // move component															
					var ajaxURL = editPreview.addParam(currentURL,"previewEdit=true&webaction=edit.moveComponent&comp-id=" + compId + "&previous=0&area=" + area+ "&render-mode=3&init=true");				
					if (editPreview.searchPageId(this) != null) {					
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(this);
					}
					editPreview.ajaxPreviewRequest(ajaxURL, null, null);
				} else if (event.dataTransfer.files.length > 0) {						
					var ajaxURL = editPreview.addParam(currentURL,"webaction=data.upload&content=true&previous=0&area=" + area);
					if (editPreview.searchPageId(this) != null) {
						ajaxURL = ajaxURL +'&pageContainerID='+ editPreview.searchPageId(this);
					}
					var fd=new FormData();
					var fieldName = pjq(this).data("fieldname");
					if (fieldName == null) {
						filedName = "files";
					}
					var i = 0;
					jQuery.each( event.dataTransfer.files, function(index, file) {
						if (i==0) {
							fd.append(fieldName,file);
						} else {
							fd.append(fieldName+"_"+i,file);
						}
						i++;			
					});					
					editPreview.ajaxPreviewRequest(ajaxURL, null, fd);					
				}
				return false;	
		    });
		}		
	}
	
	/*************************/	
	/** drag and drop page * */
	/*************************/
	var drop = document.querySelectorAll('#preview_command ul.navigation a.draggable'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		el.setAttribute('draggable', 'true');  
		if (!el.eventsAdded) {
			el.eventsAdded = true;			
		    el.addEventListener('dragover', function (event) {
		    	event.preventDefault();
		    	pjq("._ep_new-component-zone").remove();
		    	pjq(this).after('<div class="_ep_new-component-zone"></div>');
		    });
		    el.addEventListener('dragleave', function (event) {		    	
		    	pjq("._ep_new-component-zone").remove();		    	
		    });	    
		    el.addEventListener('dragstart', function (event) {
		    	var targetPageName = pjq(this).attr("id");		    	
		    	event.dataTransfer.setData('text', "page:"+targetPageName);
		    });
		    el.addEventListener('drop', function (event) {
		    	event.preventDefault();		    	
		    	pjq("._ep_new-component-zone").remove();
		    	var item = pjq(this);
		    	var targetPageName = item.attr("id");
		    	var insertAsChild = false;
		    	if (item.parent().hasClass("title") || item.parent().parent().hasClass("title")) {
		    		insertAsChild = true;
		    	}
		    	var pageName = event.dataTransfer.getData('text');		    	
		    	if (pageName.indexOf("page:") == 0) {
		    		pageName = pageName.substring(5);		    		
		    		var ajaxURL = editPreview.addParam(currentURL,"previewEdit=true&webaction=edit.movePage&page=" + pageName + "&previous=" + targetPageName + "&render-mode=3&init=true&as-child="+insertAsChild);
					editPreview.ajaxPreviewRequest(ajaxURL, null, null);
		    	}
				return false;	
		    });
		}		
	}
	
	/************/
	/** upload **/
	/************/
	var drop = document.querySelectorAll('#preview_command .no-upload'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;
			 el.addEventListener('dragover', function (event) {
			    	event.preventDefault();
			    	pjq(this).addClass("no-drop");
			    	return false;
			    });
		}
	}
	var drop = document.querySelectorAll('#preview_command .upload-zone'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;
			 el.addEventListener('dragover', function (event) {
			    	event.preventDefault();
			    	pjq(this).addClass("drop-selected");
			    	return false;
			    });
			    el.addEventListener('dragleave', function (event) {
			    	event.preventDefault();
			    	pjq(this).removeClass("drop-selected");
			    	return false;
			    });
			    el.addEventListener('drop', function(e) {
			    	e.preventDefault();
					pjq(this).removeClass("dragover");	
					var url  = pjq(this).data("url");		
					if (url.indexOf("/edit-")>=0) {
						url = url.replace("/edit-", "/ajax-");
					} else {
						url = url.replace("/edit/", "/ajax/");
						if (url.indexOf("/preview-")>=0) {
							url = url.replace("/preview-", "/ajax-");
						} else {
							url = url.replace("/preview/", "/ajax/");
						}
					}	
					var fieldName = pjq(this).data("fieldname");
					if (fieldName == null) {
						filedName = "files";
					}							
					var i = 0;					
					var fd=new FormData();
					jQuery.each( e.dataTransfer.files, function(index, file) {
						if (i==0) {
							fd.append(fieldName,file);
						} else {
							fd.append(fieldName+"_"+i,file);
						}
						i++;			
					});	
					editPreview.startAjax();
					pjq.ajax({
						url : url,
						cache : false,
						data: fd,
						type : "post",
						dataType : "json",
						processData: false,
						contentType: false
					}).done(function(jsonObj) {						
						if (jsonObj.data != null) {
							pjq.each(jsonObj.data, function(key, value) {
								if (key == "need-refresh" && value) {						
									window.location.href=window.location.href;
								}
							});
						}
						jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
							var item = jQuery("#" + xhtmlId);			
							if (item != null) {
								pjq("#" + xhtmlId).replaceWith(xhtml);
							} else {
								if (console) {
									console.log("warning : component "+xhtmlId+" not found for zone.");
								}
							}
						});
						pjq.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
							var item = jQuery("#" + xhtmlId);
							if (item != null) {
								item.html(xhtml);	
							} else {
								if (console) {
									console.log("warning : component "+xhtmlId+" not found for insideZone.");
								}
							}

						});				
						pjq(document).trigger("ajaxUpdate");
						try {
							editPreview.initPreview();					
						} catch (ex) {
							if (console) {
								console.log(ex);
							}
						}
						editPreview.stopAjax();
					});
			    });
		}
		editPreview.updatePDFPosition();
	}
	
	editPreview.splitHtml = function(text,cutPos) {
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
	
	editPreview.floatZone = function(source, zone1, zone2, image){
		var zone1 = pjq(zone1);	
		var zone2 = jQuery(zone2);	
		var image = pjq(image);
		
		var html = pjq(source).html();
		var sep = html.length;		
		zone1.html(html);
		zone2.html('');
		while (sep > 0 && zone1.height() > image.height()) {
			sep = sep-1;			
			while (sep > 0 && html[sep] != ' ') {
				sep = sep - 1;			
			}			
			var outText = editPreview.splitHtml(html, sep);			
			zone1.html(outText[0]);
			zone2.html(outText[1]);
		}	
		return sep;
	};
	
	editPreview.reloadPreviewPage = function() {
		var doc = document.documentElement, body = document.body;
		var topScroll = (doc && doc.scrollTop || body && body.scrollTop || 0);
		var currentURL = window.location.href;
		if (currentURL.indexOf("_scrollTo") >= 1) {
			currentURL = currentURL.substring(0,
					currentURL.indexOf("_scrollTo") - 1);
		}
		if (currentURL.indexOf("?") < 0) {
			currentURL = currentURL + "?" + "_scrollTo=" + topScroll;
		} else {
			currentURL = currentURL + "&" + "_scrollTo=" + topScroll;
		}
		window.location.href = currentURL;
	}
	
	editPreview.ajaxPreviewRequest = function(url, doneFunction, data) {		
		editPreview.startAjax();
		if (url.indexOf("/edit-")>=0) {
			url = url.replace("/edit-", "/ajax-");
		} else {
			url = url.replace("/edit/", "/ajax/");
			if (url.indexOf("/preview-")>=0) {
				url = url.replace("/preview-", "/ajax-");
			} else {
				url = url.replace("/preview/", "/ajax/");
			}
		}
		jQuery.ajax({
			url : url,
			cache : false,
			data : data,
			type : "post",
			dataType : "json",
			processData: false,
			contentType: false
		}).done(function(jsonObj) {			
			if (jsonObj.data != null) {
				if (jsonObj.data["need-refresh"]) {
					editPreview.reloadPreviewPage();
				}
			}
			jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
				/* if allready select don't add '#' */
				if (xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf(" ") < 0 ) { 
					xhtmlId = "#"+xhtmlId;
				}
				var item = jQuery(xhtmlId);			
				if (item != null) {					
					jQuery(xhtmlId).replaceWith(xhtml);					
				} else {
					jQuery.each(jsonObj.data, function(key, value) {				
				});
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for zone.");
					}
				}
			});
			jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
				/* if allready select don't add '#' */
				if (xhtmlId.indexOf("#") < 0 && xhtmlId.indexOf(".") < 0 && xhtmlId.indexOf(" ") < 0 ) { 
					xhtmlId = "#"+xhtmlId;
				}			
				var item = jQuery(xhtmlId);
				if (item != null) {
					item.html(xhtml);
				} else {
					if (console) {
						console.log("warning : component "+xhtmlId+" not found for insideZone.");
					}
				}
			});			
			jQuery(document).trigger("ajaxUpdate");		
			try {			
				editPreview.initPreview();			
			} catch (ex) {
				if (console) {
					console.log("Exception when calling initPreview()", ex);
				}
			}			
			if (doneFunction != null) {				
				doneFunction();
			}
			editPreview.stopAjax();
		});	
	}
	function scrollToMe() {
		editPreview.scrollToItem(pjq(".scroll-to-me"));
	}
	window.setTimeout(scrollToMe, 250);
}

editPreview.addParam  = function(url, params) {
	if (url.indexOf("?") < 0) {
		url = url + "?" + params;
	} else {
		url = url + "&" + params;
	}
	return url;
}

editPreview.readCookie  = function(key) {
	var cookie = document.cookie;
	var pos = cookie.indexOf(key+"=");
	if (pos < 0) {
		return null;
	} else {
		var value = cookie.substring(pos+key.length+1);
		pos = value.indexOf(";");
		if (pos>0) {
			value = value.substring(0,pos);  
		}
		return value;
	}
}

editPreview.getParam = function(url, name) {
	if (url.indexOf("?") < 0) {
		url = '?' + url;
	}
	name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
	var regexS = "[\\?&]" + name + "=([^&#]*)";
	var regex = new RegExp(regexS);
	var results = regex.exec(url);
	if (results == null)
		return "";
	else
		return results[1];
}

editPreview.startAjax = function() {
	pjq('body').addClass("_preview_ajax-loading");	
}

editPreview.stopAjax = function() {
	pjq('body').removeClass("_preview_ajax-loading");
}

pjq(document).ready(function() {
	editPreview.onReadyFunction();
	pjq('.btn-wait-loading').attr("disabled", "disabled");
});

pjq(window).load(function() {
	pjq('.btn-wait-loading').removeAttr("disabled");
});


editPreview.onReadyFunction = function() {
	
	editPreview.startAjax();
	
	pjq(document).on("change", ".js-change-submit select", function() {
		pjq(this.form).trigger("submit");
	});
	
	pjq(document).on("change", ".js-change-submit select", function() {
		pjq(this.form).trigger("submit");
	});
	
	pjq(document).on("change", ".js-submit select", function() {	
		pjq(this.form).trigger("submit");
	});

	pjq(".js-submit input[type='submit']").each(function() {
		var item = pjq(this);
		item.css("display", "none");
	});

	pjq(document).on("change", ".submit_on_change", function() {
		pjq(this.form).trigger("submit");		
	});	
	
	editPreview.heightToBottom(pjq(".height-to-bottom"));
	pjq( window ).resize(function() {
		editPreview.heightToBottom(pjq(".height-to-bottom"));
	});
	pjq('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {		
		editPreview.heightToBottom(pjq(".height-to-bottom"));
		document.cookie="preview_tab="+pjq(this).attr("href");		
	});
	
	editPreview.initPreview();
	
	var activeTab = editPreview.readCookie("preview_tab");	
	if (activeTab != null) {
		var tab = pjq(".nav-tabs a[href='"+activeTab+"']");	
		if (!tab.parent().hasClass("disabled")) {
			tab.tab("show");
		}
	}
}

pjq(window).load(function() {
	/** scrol to latest position after refresh * */
	var scrollTo = editPreview.getParam(window.location.href, "_scrollTo");
	if (scrollTo != "") {
		var body = $("html, body");		
		body.animate({scrollTop:(scrollTo)}, '100', 'swing');		
	}
	pjq('[data-toggle="tooltip"]').tooltip();
	editPreview.stopAjax();
});


/** ************ */
/** bootstrap * */
/** ************ */

/** tab * */

/*
 * ========================================================================
 * Bootstrap: tab.js v3.3.2 http://getbootstrap.com/javascript/#tabs
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc. Licensed under MIT
 * (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ========================================================================
 */


+function ($) {
  'use strict';

  // TAB CLASS DEFINITION
  // ====================

  var Tab = function (element) {
    this.element = $(element)
  }

  Tab.VERSION = '3.3.2'

  Tab.TRANSITION_DURATION = 150

  Tab.prototype.show = function () {
    var $this    = this.element
    var $ul      = $this.closest('ul:not(.dropdown-menu)')
    var selector = $this.data('target')

    if (!selector) {
      selector = $this.attr('href')
      selector = selector && selector.replace(/.*(?=#[^\s]*$)/, '') // strip for
																	// ie7
    }

    if ($this.parent('li').hasClass('active')) return

    var $previous = $ul.find('.active:last a')
    var hideEvent = $.Event('hide.bs.tab', {
      relatedTarget: $this[0]
    })
    var showEvent = $.Event('show.bs.tab', {
      relatedTarget: $previous[0]
    })

    $previous.trigger(hideEvent)
    $this.trigger(showEvent)

    if (showEvent.isDefaultPrevented() || hideEvent.isDefaultPrevented()) return

    var $target = $(selector)

    this.activate($this.closest('li'), $ul)
    this.activate($target, $target.parent(), function () {
      $previous.trigger({
        type: 'hidden.bs.tab',
        relatedTarget: $this[0]
      })
      $this.trigger({
        type: 'shown.bs.tab',
        relatedTarget: $previous[0]
      })
    })
  }

  Tab.prototype.activate = function (element, container, callback) {
    var $active    = container.find('> .active')
    var transition = callback
      && $.support.transition
      && (($active.length && $active.hasClass('fade')) || !!container.find('> .fade').length)

    function next() {
      $active
        .removeClass('active')
        .find('> .dropdown-menu > .active')
          .removeClass('active')
        .end()
        .find('[data-toggle="tab"]')
          .attr('aria-expanded', false)

      element
        .addClass('active')
        .find('[data-toggle="tab"]')
          .attr('aria-expanded', true)

      if (transition) {
        element[0].offsetWidth // reflow for transition
        element.addClass('in')
      } else {
        element.removeClass('fade')
      }

      if (element.parent('.dropdown-menu')) {
        element
          .closest('li.dropdown')
            .addClass('active')
          .end()
          .find('[data-toggle="tab"]')
            .attr('aria-expanded', true)
      }

      callback && callback()
    }

    $active.length && transition ?
      $active
        .one('bsTransitionEnd', next)
        .emulateTransitionEnd(Tab.TRANSITION_DURATION) :
      next()

    $active.removeClass('in')
  }


  // TAB PLUGIN DEFINITION
  // =====================

  function Plugin(option) {
    return this.each(function () {
      var $this = $(this)
      var data  = $this.data('bs.tab')

      if (!data) $this.data('bs.tab', (data = new Tab(this)))
      if (typeof option == 'string') data[option]()
    })
  }

  var old = $.fn.tab

  $.fn.tab             = Plugin
  $.fn.tab.Constructor = Tab


  // TAB NO CONFLICT
  // ===============

  $.fn.tab.noConflict = function () {
    $.fn.tab = old
    return this
  }


  // TAB DATA-API
  // ============

  var clickHandler = function (e) {
    e.preventDefault()
    Plugin.call($(this), 'show')
  }

  $(document)
    .on('click.bs.tab.data-api', '[data-toggle="tab"]', clickHandler)
    .on('click.bs.tab.data-api', '[data-toggle="pill"]', clickHandler)

}(jQuery);

/*
 * ========================================================================
 * Bootstrap: modal.js v3.3.2 http://getbootstrap.com/javascript/#modals
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc. Licensed under MIT
 * (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ========================================================================
 */


+function ($) {
  'use strict';

  // MODAL CLASS DEFINITION
  // ======================

  var Modal = function (element, options) {
    this.options        = options
    this.$body          = $(document.body)
    this.$element       = $(element)
    this.$backdrop      =
    this.isShown        = null
    this.scrollbarWidth = 0

    if (this.options.remote) {
      this.$element
        .find('.modal-content')
        .load(this.options.remote, $.proxy(function () {
          this.$element.trigger('loaded.bs.modal')
        }, this))
    }
  }

  Modal.VERSION  = '3.3.2'

  Modal.TRANSITION_DURATION = 300
  Modal.BACKDROP_TRANSITION_DURATION = 150

  Modal.DEFAULTS = {
    backdrop: true,
    keyboard: true,
    show: true
  }

  Modal.prototype.toggle = function (_relatedTarget) {
    return this.isShown ? this.hide() : this.show(_relatedTarget)
  }

  Modal.prototype.show = function (_relatedTarget) {
    var that = this
    var e    = $.Event('show.bs.modal', { relatedTarget: _relatedTarget })

    this.$element.trigger(e)

    if (this.isShown || e.isDefaultPrevented()) return

    this.isShown = true

    this.checkScrollbar()
    this.setScrollbar()
    this.$body.addClass('modal-open')

    this.escape()
    this.resize()

    this.$element.on('click.dismiss.bs.modal', '[data-dismiss="modal"]', $.proxy(this.hide, this))

    this.backdrop(function () {
      var transition = $.support.transition && that.$element.hasClass('fade')

      if (!that.$element.parent().length) {
        that.$element.appendTo(that.$body) // don't move modals dom position
      }

      that.$element
        .show()
        .scrollTop(0)

      if (that.options.backdrop) that.adjustBackdrop()
      that.adjustDialog()

      if (transition) {
        that.$element[0].offsetWidth // force reflow
      }

      that.$element
        .addClass('in')
        .attr('aria-hidden', false)

      that.enforceFocus()

      var e = $.Event('shown.bs.modal', { relatedTarget: _relatedTarget })

      transition ?
        that.$element.find('.modal-dialog') // wait for modal to slide in
          .one('bsTransitionEnd', function () {
            that.$element.trigger('focus').trigger(e)
          })
          .emulateTransitionEnd(Modal.TRANSITION_DURATION) :
        that.$element.trigger('focus').trigger(e)
    })
  }

  Modal.prototype.hide = function (e) {
    if (e) e.preventDefault()

    e = $.Event('hide.bs.modal')

    this.$element.trigger(e)

    if (!this.isShown || e.isDefaultPrevented()) return

    this.isShown = false

    this.escape()
    this.resize()

    $(document).off('focusin.bs.modal')

    this.$element
      .removeClass('in')
      .attr('aria-hidden', true)
      .off('click.dismiss.bs.modal')

    $.support.transition && this.$element.hasClass('fade') ?
      this.$element
        .one('bsTransitionEnd', $.proxy(this.hideModal, this))
        .emulateTransitionEnd(Modal.TRANSITION_DURATION) :
      this.hideModal()
  }

  Modal.prototype.enforceFocus = function () {
    $(document)
      .off('focusin.bs.modal') // guard against infinite focus loop
      .on('focusin.bs.modal', $.proxy(function (e) {
        if (this.$element[0] !== e.target && !this.$element.has(e.target).length) {
          this.$element.trigger('focus')
        }
      }, this))
  }

  Modal.prototype.escape = function () {
    if (this.isShown && this.options.keyboard) {
      this.$element.on('keydown.dismiss.bs.modal', $.proxy(function (e) {
        e.which == 27 && this.hide()
      }, this))
    } else if (!this.isShown) {
      this.$element.off('keydown.dismiss.bs.modal')
    }
  }

  Modal.prototype.resize = function () {
    if (this.isShown) {
      $(window).on('resize.bs.modal', $.proxy(this.handleUpdate, this))
    } else {
      $(window).off('resize.bs.modal')
    }
  }

  Modal.prototype.hideModal = function () {
    var that = this
    this.$element.hide()
    this.backdrop(function () {
      that.$body.removeClass('modal-open')
      that.resetAdjustments()
      that.resetScrollbar()
      that.$element.trigger('hidden.bs.modal')
    })
  }

  Modal.prototype.removeBackdrop = function () {
    this.$backdrop && this.$backdrop.remove()
    this.$backdrop = null
  }

  Modal.prototype.backdrop = function (callback) {
    var that = this
    var animate = this.$element.hasClass('fade') ? 'fade' : ''

    if (this.isShown && this.options.backdrop) {
      var doAnimate = $.support.transition && animate

      this.$backdrop = $('<div class="modal-backdrop ' + animate + '" />')
        .prependTo(this.$element)
        .on('click.dismiss.bs.modal', $.proxy(function (e) {
          if (e.target !== e.currentTarget) return
          this.options.backdrop == 'static'
            ? this.$element[0].focus.call(this.$element[0])
            : this.hide.call(this)
        }, this))

      if (doAnimate) this.$backdrop[0].offsetWidth // force reflow

      this.$backdrop.addClass('in')

      if (!callback) return

      doAnimate ?
        this.$backdrop
          .one('bsTransitionEnd', callback)
          .emulateTransitionEnd(Modal.BACKDROP_TRANSITION_DURATION) :
        callback()

    } else if (!this.isShown && this.$backdrop) {
      this.$backdrop.removeClass('in')

      var callbackRemove = function () {
        that.removeBackdrop()
        callback && callback()
      }
      $.support.transition && this.$element.hasClass('fade') ?
        this.$backdrop
          .one('bsTransitionEnd', callbackRemove)
          .emulateTransitionEnd(Modal.BACKDROP_TRANSITION_DURATION) :
        callbackRemove()

    } else if (callback) {
      callback()
    }
  }

  // these following methods are used to handle overflowing modals

  Modal.prototype.handleUpdate = function () {
    if (this.options.backdrop) this.adjustBackdrop()
    this.adjustDialog()
  }

  Modal.prototype.adjustBackdrop = function () {
    this.$backdrop
      .css('height', 0)
      .css('height', this.$element[0].scrollHeight)
  }

  Modal.prototype.adjustDialog = function () {
    var modalIsOverflowing = this.$element[0].scrollHeight > document.documentElement.clientHeight

    this.$element.css({
      paddingLeft:  !this.bodyIsOverflowing && modalIsOverflowing ? this.scrollbarWidth : '',
      paddingRight: this.bodyIsOverflowing && !modalIsOverflowing ? this.scrollbarWidth : ''
    })
  }

  Modal.prototype.resetAdjustments = function () {
    this.$element.css({
      paddingLeft: '',
      paddingRight: ''
    })
  }

  Modal.prototype.checkScrollbar = function () {
    this.bodyIsOverflowing = document.body.scrollHeight > document.documentElement.clientHeight
    this.scrollbarWidth = this.measureScrollbar()
  }

  Modal.prototype.setScrollbar = function () {
    var bodyPad = parseInt((this.$body.css('padding-right') || 0), 10)
    if (this.bodyIsOverflowing) this.$body.css('padding-right', bodyPad + this.scrollbarWidth)
  }

  Modal.prototype.resetScrollbar = function () {
    this.$body.css('padding-right', '')
  }

  Modal.prototype.measureScrollbar = function () { // thx walsh
    var scrollDiv = document.createElement('div')
    scrollDiv.className = 'modal-scrollbar-measure'
    this.$body.append(scrollDiv)
    var scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth
    this.$body[0].removeChild(scrollDiv)
    return scrollbarWidth
  }


  // MODAL PLUGIN DEFINITION
  // =======================

  function Plugin(option, _relatedTarget) {
    return this.each(function () {
      var $this   = $(this)
      var data    = $this.data('bs.modal')
      var options = $.extend({}, Modal.DEFAULTS, $this.data(), typeof option == 'object' && option)

      if (!data) $this.data('bs.modal', (data = new Modal(this, options)))
      if (typeof option == 'string') data[option](_relatedTarget)
      else if (options.show) data.show(_relatedTarget)
    })
  }

  var old = $.fn.modal

  $.fn.modal             = Plugin
  $.fn.modal.Constructor = Modal


  // MODAL NO CONFLICT
  // =================

  $.fn.modal.noConflict = function () {
    $.fn.modal = old
    return this
  }


  // MODAL DATA-API
  // ==============

  $(document).on('click.bs.modal.data-api', '[data-toggle="modal"]', function (e) {
    var $this   = $(this)
    var href    = $this.attr('href')
    var $target = $($this.attr('data-target') || (href && href.replace(/.*(?=#[^\s]+$)/, ''))) // strip
																								// for
																								// ie7
    var option  = $target.data('bs.modal') ? 'toggle' : $.extend({ remote: !/#/.test(href) && href }, $target.data(), $this.data())

    if ($this.is('a')) e.preventDefault()

    $target.one('show.bs.modal', function (showEvent) {
      if (showEvent.isDefaultPrevented()) return // only register focus
													// restorer if modal will
													// actually get shown
      $target.one('hidden.bs.modal', function () {
        $this.is(':visible') && $this.trigger('focus')
      })
    })
    Plugin.call($target, option, this)
  })
  
  /* ========================================================================
 * Bootstrap: tooltip.js v3.3.2
 * http://getbootstrap.com/javascript/#tooltip
 * Inspired by the original jQuery.tipsy by Jason Frame
 * ========================================================================
 * Copyright 2011-2015 Twitter, Inc.
 * Licensed under MIT (https://github.com/twbs/bootstrap/blob/master/LICENSE)
 * ======================================================================== */


+function ($) {
  'use strict';

  // TOOLTIP PUBLIC CLASS DEFINITION
  // ===============================

  var Tooltip = function (element, options) {
    this.type       =
    this.options    =
    this.enabled    =
    this.timeout    =
    this.hoverState =
    this.$element   = null

    this.init('tooltip', element, options)
  }

  Tooltip.VERSION  = '3.3.2'

  Tooltip.TRANSITION_DURATION = 150

  Tooltip.DEFAULTS = {
    animation: true,
    placement: 'top',
    selector: false,
    template: '<div class="tooltip" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner"></div></div>',
    trigger: 'hover focus',
    title: '',
    delay: 0,
    html: false,
    container: false,
    viewport: {
      selector: 'body',
      padding: 0
    }
  }

  Tooltip.prototype.init = function (type, element, options) {
    this.enabled   = true
    this.type      = type
    this.$element  = $(element)
    this.options   = this.getOptions(options)
    this.$viewport = this.options.viewport && $(this.options.viewport.selector || this.options.viewport)

    var triggers = this.options.trigger.split(' ')

    for (var i = triggers.length; i--;) {
      var trigger = triggers[i]

      if (trigger == 'click') {
        this.$element.on('click.' + this.type, this.options.selector, $.proxy(this.toggle, this))
      } else if (trigger != 'manual') {
        var eventIn  = trigger == 'hover' ? 'mouseenter' : 'focusin'
        var eventOut = trigger == 'hover' ? 'mouseleave' : 'focusout'

        this.$element.on(eventIn  + '.' + this.type, this.options.selector, $.proxy(this.enter, this))
        this.$element.on(eventOut + '.' + this.type, this.options.selector, $.proxy(this.leave, this))
      }
    }

    this.options.selector ?
      (this._options = $.extend({}, this.options, { trigger: 'manual', selector: '' })) :
      this.fixTitle()
  }

  Tooltip.prototype.getDefaults = function () {
    return Tooltip.DEFAULTS
  }

  Tooltip.prototype.getOptions = function (options) {
    options = $.extend({}, this.getDefaults(), this.$element.data(), options)

    if (options.delay && typeof options.delay == 'number') {
      options.delay = {
        show: options.delay,
        hide: options.delay
      }
    }

    return options
  }

  Tooltip.prototype.getDelegateOptions = function () {
    var options  = {}
    var defaults = this.getDefaults()

    this._options && $.each(this._options, function (key, value) {
      if (defaults[key] != value) options[key] = value
    })

    return options
  }

  Tooltip.prototype.enter = function (obj) {
    var self = obj instanceof this.constructor ?
      obj : $(obj.currentTarget).data('bs.' + this.type)

    if (self && self.$tip && self.$tip.is(':visible')) {
      self.hoverState = 'in'
      return
    }

    if (!self) {
      self = new this.constructor(obj.currentTarget, this.getDelegateOptions())
      $(obj.currentTarget).data('bs.' + this.type, self)
    }

    clearTimeout(self.timeout)

    self.hoverState = 'in'

    if (!self.options.delay || !self.options.delay.show) return self.show()

    self.timeout = setTimeout(function () {
      if (self.hoverState == 'in') self.show()
    }, self.options.delay.show)
  }

  Tooltip.prototype.leave = function (obj) {
    var self = obj instanceof this.constructor ?
      obj : $(obj.currentTarget).data('bs.' + this.type)

    if (!self) {
      self = new this.constructor(obj.currentTarget, this.getDelegateOptions())
      $(obj.currentTarget).data('bs.' + this.type, self)
    }

    clearTimeout(self.timeout)

    self.hoverState = 'out'

    if (!self.options.delay || !self.options.delay.hide) return self.hide()

    self.timeout = setTimeout(function () {
      if (self.hoverState == 'out') self.hide()
    }, self.options.delay.hide)
  }

  Tooltip.prototype.show = function () {
    var e = $.Event('show.bs.' + this.type)

    if (this.hasContent() && this.enabled) {
      this.$element.trigger(e)

      var inDom = $.contains(this.$element[0].ownerDocument.documentElement, this.$element[0])
      if (e.isDefaultPrevented() || !inDom) return
      var that = this

      var $tip = this.tip()

      var tipId = this.getUID(this.type)

      this.setContent()
      $tip.attr('id', tipId)
      this.$element.attr('aria-describedby', tipId)

      if (this.options.animation) $tip.addClass('fade')

      var placement = typeof this.options.placement == 'function' ?
        this.options.placement.call(this, $tip[0], this.$element[0]) :
        this.options.placement

      var autoToken = /\s?auto?\s?/i
      var autoPlace = autoToken.test(placement)
      if (autoPlace) placement = placement.replace(autoToken, '') || 'top'

      $tip
        .detach()
        .css({ top: 0, left: 0, display: 'block' })
        .addClass(placement)
        .data('bs.' + this.type, this)

      this.options.container ? $tip.appendTo(this.options.container) : $tip.insertAfter(this.$element)

      var pos          = this.getPosition()
      var actualWidth  = $tip[0].offsetWidth
      var actualHeight = $tip[0].offsetHeight

      if (autoPlace) {
        var orgPlacement = placement
        var $container   = this.options.container ? $(this.options.container) : this.$element.parent()
        var containerDim = this.getPosition($container)

        placement = placement == 'bottom' && pos.bottom + actualHeight > containerDim.bottom ? 'top'    :
                    placement == 'top'    && pos.top    - actualHeight < containerDim.top    ? 'bottom' :
                    placement == 'right'  && pos.right  + actualWidth  > containerDim.width  ? 'left'   :
                    placement == 'left'   && pos.left   - actualWidth  < containerDim.left   ? 'right'  :
                    placement

        $tip
          .removeClass(orgPlacement)
          .addClass(placement)
      }

      var calculatedOffset = this.getCalculatedOffset(placement, pos, actualWidth, actualHeight)

      this.applyPlacement(calculatedOffset, placement)

      var complete = function () {
        var prevHoverState = that.hoverState
        that.$element.trigger('shown.bs.' + that.type)
        that.hoverState = null

        if (prevHoverState == 'out') that.leave(that)
      }

      $.support.transition && this.$tip.hasClass('fade') ?
        $tip
          .one('bsTransitionEnd', complete)
          .emulateTransitionEnd(Tooltip.TRANSITION_DURATION) :
        complete()
    }
  }

  Tooltip.prototype.applyPlacement = function (offset, placement) {
    var $tip   = this.tip()
    var width  = $tip[0].offsetWidth
    var height = $tip[0].offsetHeight

    // manually read margins because getBoundingClientRect includes difference
    var marginTop = parseInt($tip.css('margin-top'), 10)
    var marginLeft = parseInt($tip.css('margin-left'), 10)

    // we must check for NaN for ie 8/9
    if (isNaN(marginTop))  marginTop  = 0
    if (isNaN(marginLeft)) marginLeft = 0

    offset.top  = offset.top  + marginTop
    offset.left = offset.left + marginLeft

    // $.fn.offset doesn't round pixel values
    // so we use setOffset directly with our own function B-0
    $.offset.setOffset($tip[0], $.extend({
      using: function (props) {
        $tip.css({
          top: Math.round(props.top),
          left: Math.round(props.left)
        })
      }
    }, offset), 0)

    $tip.addClass('in')

    // check to see if placing tip in new offset caused the tip to resize itself
    var actualWidth  = $tip[0].offsetWidth
    var actualHeight = $tip[0].offsetHeight

    if (placement == 'top' && actualHeight != height) {
      offset.top = offset.top + height - actualHeight
    }

    var delta = this.getViewportAdjustedDelta(placement, offset, actualWidth, actualHeight)

    if (delta.left) offset.left += delta.left
    else offset.top += delta.top

    var isVertical          = /top|bottom/.test(placement)
    var arrowDelta          = isVertical ? delta.left * 2 - width + actualWidth : delta.top * 2 - height + actualHeight
    var arrowOffsetPosition = isVertical ? 'offsetWidth' : 'offsetHeight'

    $tip.offset(offset)
    this.replaceArrow(arrowDelta, $tip[0][arrowOffsetPosition], isVertical)
  }

  Tooltip.prototype.replaceArrow = function (delta, dimension, isHorizontal) {
    this.arrow()
      .css(isHorizontal ? 'left' : 'top', 50 * (1 - delta / dimension) + '%')
      .css(isHorizontal ? 'top' : 'left', '')
  }

  Tooltip.prototype.setContent = function () {
    var $tip  = this.tip()
    var title = this.getTitle()

    $tip.find('.tooltip-inner')[this.options.html ? 'html' : 'text'](title)
    $tip.removeClass('fade in top bottom left right')
  }

  Tooltip.prototype.hide = function (callback) {
    var that = this
    var $tip = this.tip()
    var e    = $.Event('hide.bs.' + this.type)

    function complete() {
      if (that.hoverState != 'in') $tip.detach()
      that.$element
        .removeAttr('aria-describedby')
        .trigger('hidden.bs.' + that.type)
      callback && callback()
    }

    this.$element.trigger(e)

    if (e.isDefaultPrevented()) return

    $tip.removeClass('in')

    $.support.transition && this.$tip.hasClass('fade') ?
      $tip
        .one('bsTransitionEnd', complete)
        .emulateTransitionEnd(Tooltip.TRANSITION_DURATION) :
      complete()

    this.hoverState = null

    return this
  }

  Tooltip.prototype.fixTitle = function () {
    var $e = this.$element
    if ($e.attr('title') || typeof ($e.attr('data-original-title')) != 'string') {
      $e.attr('data-original-title', $e.attr('title') || '').attr('title', '')
    }
  }

  Tooltip.prototype.hasContent = function () {
    return this.getTitle()
  }

  Tooltip.prototype.getPosition = function ($element) {
    $element   = $element || this.$element

    var el     = $element[0]
    var isBody = el.tagName == 'BODY'

    var elRect    = el.getBoundingClientRect()
    if (elRect.width == null) {
      // width and height are missing in IE8, so compute them manually; see https://github.com/twbs/bootstrap/issues/14093
      elRect = $.extend({}, elRect, { width: elRect.right - elRect.left, height: elRect.bottom - elRect.top })
    }
    var elOffset  = isBody ? { top: 0, left: 0 } : $element.offset()
    var scroll    = { scroll: isBody ? document.documentElement.scrollTop || document.body.scrollTop : $element.scrollTop() }
    var outerDims = isBody ? { width: $(window).width(), height: $(window).height() } : null

    return $.extend({}, elRect, scroll, outerDims, elOffset)
  }

  Tooltip.prototype.getCalculatedOffset = function (placement, pos, actualWidth, actualHeight) {
    return placement == 'bottom' ? { top: pos.top + pos.height,   left: pos.left + pos.width / 2 - actualWidth / 2 } :
           placement == 'top'    ? { top: pos.top - actualHeight, left: pos.left + pos.width / 2 - actualWidth / 2 } :
           placement == 'left'   ? { top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left - actualWidth } :
        /* placement == 'right' */ { top: pos.top + pos.height / 2 - actualHeight / 2, left: pos.left + pos.width }

  }

  Tooltip.prototype.getViewportAdjustedDelta = function (placement, pos, actualWidth, actualHeight) {
    var delta = { top: 0, left: 0 }
    if (!this.$viewport) return delta

    var viewportPadding = this.options.viewport && this.options.viewport.padding || 0
    var viewportDimensions = this.getPosition(this.$viewport)

    if (/right|left/.test(placement)) {
      var topEdgeOffset    = pos.top - viewportPadding - viewportDimensions.scroll
      var bottomEdgeOffset = pos.top + viewportPadding - viewportDimensions.scroll + actualHeight
      if (topEdgeOffset < viewportDimensions.top) { // top overflow
        delta.top = viewportDimensions.top - topEdgeOffset
      } else if (bottomEdgeOffset > viewportDimensions.top + viewportDimensions.height) { // bottom overflow
        delta.top = viewportDimensions.top + viewportDimensions.height - bottomEdgeOffset
      }
    } else {
      var leftEdgeOffset  = pos.left - viewportPadding
      var rightEdgeOffset = pos.left + viewportPadding + actualWidth
      if (leftEdgeOffset < viewportDimensions.left) { // left overflow
        delta.left = viewportDimensions.left - leftEdgeOffset
      } else if (rightEdgeOffset > viewportDimensions.width) { // right overflow
        delta.left = viewportDimensions.left + viewportDimensions.width - rightEdgeOffset
      }
    }

    return delta
  }

  Tooltip.prototype.getTitle = function () {
    var title
    var $e = this.$element
    var o  = this.options

    title = $e.attr('data-original-title')
      || (typeof o.title == 'function' ? o.title.call($e[0]) :  o.title)

    return title
  }

  Tooltip.prototype.getUID = function (prefix) {
    do prefix += ~~(Math.random() * 1000000)
    while (document.getElementById(prefix))
    return prefix
  }

  Tooltip.prototype.tip = function () {
    return (this.$tip = this.$tip || $(this.options.template))
  }

  Tooltip.prototype.arrow = function () {
    return (this.$arrow = this.$arrow || this.tip().find('.tooltip-arrow'))
  }

  Tooltip.prototype.enable = function () {
    this.enabled = true
  }

  Tooltip.prototype.disable = function () {
    this.enabled = false
  }

  Tooltip.prototype.toggleEnabled = function () {
    this.enabled = !this.enabled
  }

  Tooltip.prototype.toggle = function (e) {
    var self = this
    if (e) {
      self = $(e.currentTarget).data('bs.' + this.type)
      if (!self) {
        self = new this.constructor(e.currentTarget, this.getDelegateOptions())
        $(e.currentTarget).data('bs.' + this.type, self)
      }
    }

    self.tip().hasClass('in') ? self.leave(self) : self.enter(self)
  }

  Tooltip.prototype.destroy = function () {
    var that = this
    clearTimeout(this.timeout)
    this.hide(function () {
      that.$element.off('.' + that.type).removeData('bs.' + that.type)
    })
  }


  // TOOLTIP PLUGIN DEFINITION
  // =========================

  function Plugin(option) {
    return this.each(function () {
      var $this   = $(this)
      var data    = $this.data('bs.tooltip')
      var options = typeof option == 'object' && option

      if (!data && option == 'destroy') return
      if (!data) $this.data('bs.tooltip', (data = new Tooltip(this, options)))
      if (typeof option == 'string') data[option]()
    })
  }

  var old = $.fn.tooltip

  $.fn.tooltip             = Plugin
  $.fn.tooltip.Constructor = Tooltip


  // TOOLTIP NO CONFLICT
  // ===================

  $.fn.tooltip.noConflict = function () {
    $.fn.tooltip = old
    return this
  }

}(jQuery);


}(jQuery);

}(pjq, pjq, pjq);