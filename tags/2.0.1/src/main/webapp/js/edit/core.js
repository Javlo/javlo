initFocusPoint = function() {
	/*** focus point ***/
	jQuery(".focus-point").each(function(){
		var point = jQuery(this);
		point.css("display", "none");
		var container = point.parent();
		var image = point.parent().find("img");
		var posx = parseInt(point.parent().find(".posx").val());
		var posy = parseInt(point.parent().find(".posy").val());
		image.mouseenter(function() {
			var image = jQuery(this);
			if (!(this._init == 'done')) {
				this._init = "done";
				posx = image.width() * posx / 1000;
				posy = image.height() * posy / 1000;
				var focusImgX = (image.offset().left-container.offset().left) + posx - point.outerWidth()/2;
				var focusImgY = (image.offset().top-container.offset().top) + posy - point.outerWidth()/2;
				point.css("position", "absolute");
				point.css("display", "block");
				point.css("left", Math.round(focusImgX));
				point.css("top", Math.round(focusImgY));
			} 
		});	
		point.draggable({ containment: image });
		point.bind( "dragstop", function(event, ui) {
			var point = jQuery(this);
			var focusRealX = (point.offset().left+point.outerWidth()/2 - image.offset().left) * 1000 / image.width();
			var focusRealY = (point.offset().top+point.outerHeight()/2 - image.offset().top) * 1000 / image.height();
			point.parent().find(".posx").val(focusRealX);
			point.parent().find(".posy").val(focusRealY);
			var path = "";
			if (point.parent().find(".path").length > 0) {
				path = "&image_path="+point.parent().find(".path").val();
			}
			var url = point.closest("form").attr("action");
			if (url.indexOf("?") >= 0) {
				url = url + "&";
			} else {
				url = url + "?";
			}
			url = url + "webaction=file.updateFocus&"+point.parent().find(".posx").attr("name")+"="+focusRealX+"&"+point.parent().find(".posy").attr("name")+"="+focusRealY+path;
			ajaxRequest(url)
		});
	});
}

jQuery(document).ready(function() {
	jQuery("input.label-inside").each(function(){				
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
			input.attr("name",input.data.storedName);
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
	
	jQuery(".js-hidden").each(function(){				
		var item = jQuery(this);
		item.css("display","none");
	});
	
	jQuery(".js-change-submit select").each(function(){				
		var item = jQuery(this);
		item.live("change",function() {
			jQuery(this.form).trigger("submit");
		});
	});
	
	jQuery(".js-submit select").each(function(){				
		var item = jQuery(this);
		item.live("change", function() {
			jQuery(this.form).trigger("submit");
		});
	});
	
	jQuery(".js-submit input[type='submit']").each(function(){				
		var item = jQuery(this);
		item.css("display","none");
	});
	
	initFocusPoint();
	
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

filter = function(filterStr,selector) {
	jQuery(selector).removeClass("hidden");
	jQuery(selector).filter(function() {
		item = jQuery(this);		
		if (item.text().toLowerCase().indexOf(filterStr.toLowerCase()) >= 0) {			
			return false;
		} else {			
			return true;
		}
	}).addClass("hidden");
}

scrollToCenter = function(container, target) {
     jQuery(container).animate({    	 
         scrollTop: ((jQuery(target).offset().top+jQuery(container).scrollTop()) - jQuery(container).offset().top) - (jQuery(container).height()/2 + jQuery(target).height()/2) 
     }, 500);	
}

scrollToFirstQuarter = function(container, target) {
	jQuery(container).animate({    	 
        scrollTop: ((jQuery(target).offset().top+jQuery(container).scrollTop()) - jQuery(container).offset().top) - (jQuery(container).height()/4 + jQuery(target).height()/2) 
    }, 500);	
}

function getParam( url, name )
{
  if (url.indexOf("?") < 0) {
     url='?'+url;
  }
  name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
  var regexS = "[\\?&]"+name+"=([^&#]*)";
  var regex = new RegExp( regexS );
  var results = regex.exec( url );
  if( results == null )
    return "";
  else
    return results[1];
}