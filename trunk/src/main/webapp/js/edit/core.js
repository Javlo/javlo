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
	
	/*** focus point ***/
	jQuery(".focus-point").each(function(){
		var point = jQuery(this);
		point.css("display", "none");
		var container = point.parent();
		var image = point.parent().find("img");
		var posx = parseInt(point.parent().find(".posx").val());
		var posy = parseInt(point.parent().find(".posy").val());		
		image.load(function() {
			var image = jQuery(this);
			posx = image.width() * posx / 1000;
			posy = image.height() * posy / 1000;
			var focusImgX = (image.offset().left-container.offset().left) + posx - point.outerWidth()/2;
			var focusImgY = (image.offset().top-container.offset().top) + posy - point.outerWidth()/2;
			point.css("position", "absolute");
			point.css("display", "block");
			point.css("left", Math.round(focusImgX));
			point.css("top", Math.round(focusImgY));
		});	
		point.draggable({ containment: image });
		point.bind( "dragstop", function(event, ui) {
			var point = jQuery(this);
			var focusRealX = (point.offset().left+point.outerWidth()/2 - image.offset().left) * 1000 / image.width();
			var focusRealY = (point.offset().top+point.outerHeight()/2 - image.offset().top) * 1000 / image.height();
			point.parent().find(".posx").val(focusRealX);
			point.parent().find(".posy").val(focusRealY);
			var url = point.closest("form").attr("action");
			url = url + "?webaction=updateFocus&"+point.parent().find(".posx").attr("name")+"="+focusRealX+"&"+point.parent().find(".posy").attr("name")+"="+focusRealY;
			ajaxRequest(url)
		});
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
			parent.find(":input").each(function() {
				var input = jQuery(this);
				var shadowInput = input.clone(false);
				shadowInput.addClass("shadow-input-clone");
				shadowInput.removeAttr("id");
				shadowInput.appendTo(shadowContainer);
				input.addClass("shadow-input-target");
				input.removeAttr("name");
				var refreshShadow = function(e) {
					if (input.is("[type=checkbox]") || input.is("[type=radio]")) {
						shadowInput.prop("checked", input.prop("checked"));
					//TODO } else if (input.is("select[multiple=multiple]")) {
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
