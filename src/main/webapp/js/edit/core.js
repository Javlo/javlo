jQuery(document).ready(function() {
	jQuery("input.label-inside").each(function(){				
		var input = jQuery(this);
		input.click(function() {			
			var input = jQuery(this);
			if (input.hasClass("label")) {
				input.data.storedValue = input.val();
				input.val("");
				input.removeClass("label");
			}
		});
		input.blur(function() {			
			var input = jQuery(this);
			if (!input.hasClass("label")) {
				if (input.val().length == 0) {
					input.val(input.data.storedValue);
					input.addClass("label");
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
		item.change(function() {
		  this.form.submit();
		});
	});
	jQuery(".js-submit select").each(function(){				
		var item = jQuery(this);
		item.change(function() {
		  this.form.submit();
		});
	});	
	jQuery(".js-submit input[type='submit']").each(function(){				
		var item = jQuery(this);
		item.css("display","none");
	});
});