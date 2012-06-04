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
});