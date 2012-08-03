(function($) {
	//AIM
	var MAX_SCROLL_HEIGHT = 99999999;

	var lastMessageCount = -1;
	var timeout = null;
	jQuery(".aim-form").live("ajaxUpdate", onAjaxUpdate);
	jQuery(document).live("ajaxUpdate", onAjaxUpdate);
	function onAjaxUpdate() {
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		var messages = jQuery(".aim-messages");
		var msgCount = messages.find(".message").length;
		if (lastMessageCount != msgCount) {
			lastMessageCount = msgCount;
			messages.scrollTop(MAX_SCROLL_HEIGHT);
		}
		var form = jQuery(".aim-form");
		form.find("[name=lastMessageId]").val(jQuery("#aim-next-messages [name=lastMessageId]").val());
		if (jQuery(this).is(".aim-form")) {
			form.find("[name=message]").val("");
		}
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		timeout = setTimeout(function() {
			jQuery("#aim-next-messages [type=submit]").click();
		}, 2000);
	}
	
	jQuery(function() {
		jQuery(".aim-form [type=submit]").hide()
	});

	jQuery(".aim-messages [data-user]").live("click", function(e) {
		e.preventDefault();
		jQuery(".aim-form [name=receiver]").val(jQuery(this).data("user"));
	});

})(jQuery);