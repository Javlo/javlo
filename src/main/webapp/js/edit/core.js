var mouseX = 0;
var mouseY = 0;

initFocusPoint = function() {
	/** * focus point ** */
	jQuery(".focus-point").each(
			function() {
				var point = jQuery(this);				
				point.parent().find("img").click(function() {				
					var img = jQuery(this);					
					var posx = Math.round(mouseX-img.offset().left-point.outerWidth()/2);
					var posy = Math.round(mouseY-img.offset().top-point.outerHeight()/2);
					point.css("position", "absolute");
					point.css("display", "block");
					point.css("left", posx);
					point.css("top", posy);
					sendFocus(point);
					return false;
				});
				point.css("display", "none");		
				var image = point.parent().find("img");
				image.mouseenter(function() {					
					if (!(this._init == 'done')) {
						this._init = "done";
						focusPosition(point);
					}
				});				
				point.draggable({
					containment : image
				});
				point.bind("dragstop", function(event, ui) {
					sendFocus(point);
				});
			});
}

focusPosition = function(point) {	
	var container = point.parent();
	var image = point.parent().find("img");
	var posx = parseInt(point.parent().find(".posx").val());
	var posy = parseInt(point.parent().find(".posy").val());
	posx = image.width() * posx / 1000;
	posy = image.height() * posy / 1000;
	var focusImgX = (image.offset().left - container.offset().left)	+ posx - point.outerWidth() / 2;
	var focusImgY = (image.offset().top - container.offset().top) + posy - point.outerWidth() / 2;
	point.css("position", "absolute");
	point.css("display", "block");
	point.css("left", Math.round(focusImgX));
	point.css("top", Math.round(focusImgY));
}

sendFocus = function(point) {	
	var image = point.parent().find("img");
	var container = point.parent();
	var focusRealX = (point.offset().left + point.outerWidth() / 2 - image.offset().left) * 1000 / image.width();
	var focusRealY = (point.offset().top + point.outerHeight() / 2 - image.offset().top) * 1000 / image.height();
	point.parent().find(".posx").val(focusRealX);
	point.parent().find(".posy").val(focusRealY);
	var path = "";
	if (point.parent().find(".path").length > 0) {
		path = "&image_path="+ point.parent().find(".path").val();
	}
	var url = point.closest("form").attr("action");
	if (url.indexOf("?") >= 0) {
		url = url + "&";
	} else {
		url = url + "?";
	}
	url = url + "webaction=file.updateFocus&"
			+ point.parent().find(".posx").attr("name") + "="
			+ focusRealX + "&"
			+ point.parent().find(".posy").attr("name") + "="
			+ focusRealY + path;
	ajaxRequest(url)
}

function setInputColor(input) {
	var color = jQuery(input);
	if (color.val().length == 7) {
		color.css("background-color", color.val());
		var colorPower = parseInt("0x" + color.val().substring(1, 3))
				+ parseInt("0x" + color.val().substring(3, 5))
				+ parseInt("0x" + color.val().substring(5, 7));
		if (colorPower > 128 * 3) {
			color.css("color", "#000000");
		} else {
			color.css("color", "#ffffff");
		}
	}
	color.change(function() {
		if (color.val().length == 7) {
			color.css("background-color", color.val());
			var colorPower = parseInt("0x" + color.val().substring(1, 3))
					+ parseInt("0x" + color.val().substring(3, 5))
					+ parseInt("0x" + color.val().substring(5, 7));
			if (colorPower > 128 * 3) {
				color.css("color", "#000000");
			} else {
				color.css("color", "#ffffff");
			}
		}
	});
	color.keyup(function() {
		if (color.val().length == 7) {
			color.css("background-color", color.val());
			var colorPower = parseInt("0x" + color.val().substring(1, 3))
					+ parseInt("0x" + color.val().substring(3, 5))
					+ parseInt("0x" + color.val().substring(5, 7));
			if (colorPower > 128 * 3) {
				color.css("color", "#000000");
			} else {
				color.css("color", "#ffffff");
			}
		}
	});
	color.focus(function() {
		if (color.val().length == 7) {
			color.css("background-color", color.val());
			var colorPower = parseInt("0x" + color.val().substring(1, 3))
					+ parseInt("0x" + color.val().substring(3, 5))
					+ parseInt("0x" + color.val().substring(5, 7));
			if (colorPower > 128 * 3) {
				color.css("color", "#000000");
			} else {
				color.css("color", "#ffffff");
			}
		}
	});
}

function updateColorInput() {	
	if (jQuery('.color').length > 0 && jQuery.isFunction(jQuery('.color').ColorPicker)) {		
		jQuery('.color').ColorPicker({
			onSubmit : function(hsb, hex, rgb, el) {
				jQuery(el).val('#' + hex);
				jQuery(el).ColorPickerHide();
				setInputColor(el);
			},
			onBeforeShow : function() {
				jQuery(this).ColorPickerSetColor(this.value);
			}
		});
		jQuery('.color').each(function() {
			setInputColor(this);
		});
	}
}

jQuery(window).load(function() {
	/** scrol to latest position after refresh **/
	var scrollTo = getParam(window.location.href, "_scrollTo");	
	if (scrollTo != "") {
		window.scrollTo(0, scrollTo);
	}
});

var openSelect = function(selector){
    var element = jQuery(selector)[0], worked = false;
   if (document.createEvent) { // all browsers
       var e = document.createEvent("MouseEvents");
       e.initMouseEvent("mousedown", true, true, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);
       worked = element.dispatchEvent(e);
   } else if (element.fireEvent) { // ie
       worked = element.fireEvent("onmousedown");
   }
}

jQuery(document).ready(function() {
	
	
	jQuery( window ).mousemove(function( event ) {
		mouseX = event.pageX;
		mouseY = event.pageY;
		
		/*focus = jQuery("body .test-mouse");
		if (focus.length == 0) {
			jQuery('body').append('<div class="test-mouse" style="border: 1px red solid; position: fixed;">C</div>');
			focus = jQuery("body .test-mouse");
		}
		focus.css("top",mouseY);
		focus.css("left",mouseX);*/
		
	});	
	
	updateColorInput();

	jQuery("body").addClass("js");
	
	jQuery('input.filter').keydown( function() {
		var input = jQuery(this);
		var targetId = input.data("filtered");
		var target = jQuery('#'+targetId);
		target.find('option').each(function() {
			var option = jQuery(this);
			var text = option.val() + ' ' + option.attr('title');
			if (text.indexOf(input.val()) < 0) {
				option.addClass('hidden');
			} else {
				option.removeClass('hidden');
			}
		})
	});	
	
	closableFieldSet(jQuery(".closable"));

	jQuery("input.label-inside").each(function() {
		var input = jQuery(this);
		input.data.storedName = input.attr("name");
		input.attr("name", "");
		input.focus(function() {
			var input = jQuery(this);
			if (input.hasClass("label")) {
				input.data.storedValue = input.val();
				input.val("");
				input.removeClass("label");
			}
			input.attr("name", input.data.storedName);
		});
		input.blur(function() {
			var input = jQuery(this);
			if (!input.hasClass("label")) {
				if (input.val().length == 0) {
					input.val(input.data.storedValue);
					input.addClass("label");
					input.attr("name", "");
				}
			}
		});
	});

	jQuery(".js-hidden").each(function() {
		var item = jQuery(this);
		item.css("display", "none");
	});

	jQuery(".js-change-submit select").each(function() {
		var item = jQuery(this);
		if (!item.hasClass('no-submit')) {
			item.live("change", function() {
				jQuery(this.form).trigger("submit");
			});
		}
	});

	jQuery(".js-submit select").each(function() {
		var item = jQuery(this);
		item.live("change", function() {
			jQuery(this.form).trigger("submit");
		});
	});

	jQuery(".js-submit input[type='submit']").each(function() {
		var item = jQuery(this);
		item.css("display", "none");
	});

	jQuery(".submit_on_change").each(function() {
		jQuery(this).live("change", function() {
			jQuery(this.form).trigger("submit");
		});
	});

	initFocusPoint();
	
	var scrollTarget = jQuery(".scroll-to-me");
	if (scrollTarget.length > 0) {
		window.scrollTo(0,scrollTarget.position().top);
	}
	
	/************/
	/** upload **/
	/************/
	var drop = document.querySelectorAll('.no-upload'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;
			 el.addEventListener('dragover', function (event) {
			    	event.preventDefault();
			    	jQuery(this).addClass("no-drop");
			    	return false;
			    });
		}
	}
	var drop = document.querySelectorAll('.upload-zone'), el = null;
	for (var i = 0; i < drop.length; i++) {
		el = drop[i];
		if (!el.eventsAdded) {
			el.eventsAdded = true;
			 el.addEventListener('dragover', function (event) {
			    	event.preventDefault();
			    	jQuery(this).addClass("drop-selected");
			    	return false;
			    });
			    el.addEventListener('dragleave', function (event) {
			    	event.preventDefault();
			    	jQuery(this).removeClass("drop-selected");
			    	return false;
			    });
			    el.addEventListener('drop', function(e) {
			    	e.preventDefault();
					jQuery(this).removeClass("dragover");	
					var url  = jQuery(this).data("url");		
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
					var fieldName = jQuery(this).data("fieldname");
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
					jQuery.ajax({
						url : url,
						cache : false,
						data: fd,
						type : "post",
						dataType : "json",
						processData: false,
						contentType: false
					}).done(function(jsonObj) {						
						if (jsonObj.data != null) {
							jQuery.each(jsonObj.data, function(key, value) {
								if (key == "need-refresh" && value) {						
									window.location.href=window.location.href;
								}
							});
						}
						jQuery.each(jsonObj.zone, function(xhtmlId, xhtml) {
							var item = jQuery("#" + xhtmlId);			
							if (item != null) {
								jQuery("#" + xhtmlId).replaceWith(xhtml);
							} else {
								if (console) {
									console.log("warning : component "+xhtmlId+" not found for zone.");
								}
							}
						});
						jQuery.each(jsonObj.insideZone, function(xhtmlId, xhtml) {
							var item = jQuery("#" + xhtmlId);
							if (item != null) {
								item.html(xhtml);	
							} else {
								if (console) {
									console.log("warning : component "+xhtmlId+" not found for insideZone.");
								}
							}

						});				
						jQuery(document).trigger("ajaxUpdate");
					});
			    });
		}
	}
	/* tooltips */
	jQuery("[data-tooltip]").hover(function() {
		var item = jQuery(this);
		item.append("<div class=\"tool-tip\">"+item.data("tooltip")+"</div>");
	});
});

jQuery.fn.extend({
	shadowInputs : function() {
		this.each(function() {
			var parent = jQuery(this);
			var shadowContainer = jQuery("<div></div>");
			shadowContainer.addClass("shadow-input-container");
			shadowContainer.hide();
			parent.before(shadowContainer);
			parent.find(":checkbox, :radio, :text, select").each(function() {
				var input = jQuery(this);
				var shadowInput = input.clone(false);
				shadowInput.addClass("shadow-input-clone");
				shadowInput.removeAttr("id");
				shadowInput.appendTo(shadowContainer);
				input.addClass("shadow-input-target");
				input.removeAttr("name");
				var refreshShadow = function(e) {
					if (input.is(":checkbox, :radio")) {
						shadowInput.prop("checked", input.prop("checked"));
					} else {
						shadowInput.val(input.val());
					}
				};
				input.click(refreshShadow);
				input.change(refreshShadow);
				input.keypress(refreshShadow);
				input.keydown(refreshShadow);
				input.keyup(refreshShadow);
			});

		});
		return this;
	}
});

filter = function(filterStr, selector) {
	// jQuery(selector).show();
	if (jQuery(selector).length == 0) {
		console.log("warning: no filtrable content.");
	}
	jQuery(selector).filter(function() {
		item = jQuery(this);
		if (item.text().toLowerCase().indexOf(filterStr.toLowerCase()) >= 0) {
			item.show(400);
			return false;
		} else {
			item.hide(400);
			return true;
		}
	});
	try {
		updateLayout();
	} catch (err) {
	}
}

scrollToCenter = function(container, target) {
	jQuery(container).animate(
			{
				scrollTop : ((jQuery(target).offset().top + jQuery(container)
						.scrollTop()) - jQuery(container).offset().top)
						- (jQuery(container).height() / 2 + jQuery(target)
								.height() / 2)
			}, 500);
}

scrollToFirstQuarter = function(container, target) {
	jQuery(container).animate(
			{
				scrollTop : ((jQuery(target).offset().top + jQuery(container)
						.scrollTop()) - jQuery(container).offset().top)
						- (jQuery(container).height() / 4 + jQuery(target)
								.height() / 2)
			}, 500);
}

function hashItem(item) {
	var path = "";
	while (jQuery(item).parent().length > 0) {
		path = path + "_"
				+ ((jQuery(item).parent().children().index(item)) + 1);
		item = jQuery(item).parent().get(0);
	}
	return path;
}

function closableFieldSet(items) {
	items.each(function() {
		
		if (jQuery(this).find(".closable_action").length > 0) {
			return;
		}
		
		jQuery(this).find("legend, .legend").wrapInner('<a class="title" href="#"/>');
		
		var hash = "closable_" + hashItem(this);
		var initVal = jQuery.cookie(hash);
		var item = jQuery(this);
		if (initVal == "close") {
			var legend = item.children("legend, .legend");						
			legend.prepend('<a class="closable_action close" href="#">#</a>');
			item.children().each(
					function() {
						var child = jQuery(this);
						if (!child.hasClass("closable_action") && (this.nodeName.toLowerCase() != "legend" && !child.hasClass("legend"))) {
							child.hide();
						}
					});
		} else {
			var legend = item.children("legend, .legend");		
			legend.prepend('<a class="closable_action open" href="#">)_</a>');
		}

		item.find(".closable_action").each(function() {
			jQuery(this).click(function() {
				clickFieldSet(this, jQuery(this).parent());
				return false;
			});
		});		
		item.find("legend .title, .legend .title").each(
				function() {
					jQuery(this).click(
							function() {
								clickFieldSet(jQuery(this).parent().parent().find(".closable_action"), jQuery(this).parent().parent());
								return false;
							});
				});
	});

}

function clickFieldSet(link, fieldset) {
	var hash = "closable_" + hashItem(fieldset);
	var link = jQuery(link);
	if (link.hasClass("open")) {
		$.cookie(hash, "close", {
			path : '/',
			expires : 365
		});
		jQuery.cookie(hash);
		link.removeClass("open");
		link.addClass("close");
		link.html("#");
		link.parent().parent().children().each(
				function() {
					var child = jQuery(this);
					if (!child.hasClass("closable_action")
							&& (this.nodeName.toLowerCase() != "legend" && !child.hasClass("legend"))) {
						child.hide();
					}
				});
	} else {
		$.cookie(hash, "open", {
			path : '/',
			expires : 365
		});
		link.removeClass("close");
		link.addClass("open");
		link.html("_");

		link.parent().parent().children().each(
				function() {
					var child = jQuery(this);
					if (!child.hasClass("closable_action")
							&& (this.nodeName.toLowerCase() != "legend" && !child.hasClass("legend"))) {
						child.show();
					}
				});
	}
}

function getParam(url, name) {
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

function addParam(url, params) {
	if (url.indexOf("?") < 0) {
		url = url + "?" + params;
	} else {
		url = url + "&" + params;
	}
	return url;
}

// List of HTML entities for escaping.
var htmlEscapes = {
	'&' : '&amp;',
	'<' : '&lt;',
	'>' : '&gt;',
	'"' : '&quot;',
	"'" : '&#x27;',
	'/' : '&#x2F;'
};

// Regex containing the keys listed immediately above.
var htmlEscaper = /[&<>"'\/]/g;

// Escape a string for HTML interpolation.
escapeHTML = function(string) {
	return ('' + string).replace(htmlEscaper, function(match) {
		return htmlEscapes[match];
	});
};

reloadPage = function() {
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

/** HELP * */

function showHelpButton() {
	jQuery(".notebutton")
			.append(
					'<li class="note"><a class="help"><span class="wrap"><span class="thicon helpicon">&nbsp;</span></span></a></li>');
	jQuery(".note .help").click(function() {
		showHelp();
	});
}

function addToolTips(selector, text, position) {
	if (jQuery(selector).length ) {
		if (position == null) {
			position = "top";
		}
		
		var url = i18nURL;
		if (i18nURL.indexOf("?")>=0) {
			url = url +"&key="+text;
		} else {
			url = url +"?key="+text;
		}
		
		if (!jQuery(selector).hasClass("show")) {
			jQuery("body").addClass("help");
			if (!jQuery(selector).hasClass("tooltipstered")) {
				jQuery.ajax({
					url : url,
					cache : true,
					type : "get",
					dataType : "text"
				}).done(function(data) {
					if (data != null && data != "" && data != "null") {						
						jQuery(selector).tooltipster({
							position : position,
							animation : 'grow',
							delay : 200,
							theme : 'tooltipster-default',
							touchDevices : false,
							trigger : 'none',
							content : data
						});
						jQuery(selector).tooltipster("show");
						jQuery(selector).addClass("show");
					} else {
						if(typeof console !== "undefined") {
							console.log("translation not found : "+text);	
						}						
					}
				});
			} else {
				jQuery(selector).tooltipster("show");
				jQuery(selector).addClass("show");
			}
		} else {
			jQuery("body").removeClass("help");
			jQuery(selector).removeClass("show");
			jQuery(selector).tooltipster("hide");
		}
	}
}

