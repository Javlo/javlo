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
});