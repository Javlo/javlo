(function($) {
	var MAX_SCROLL_HEIGHT = 99999999;

	var lastMessageCount = -1;
	var timeout = null;
	jQuery(".cim-form").live("ajaxUpdate", onAjaxUpdate);
	jQuery(document).live("ajaxUpdate", onAjaxUpdate);
	function onAjaxUpdate() {
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		var messages = jQuery(".cim-messages");
		var msgCount = messages.find(".message").length;
		if (lastMessageCount != msgCount) {
			lastMessageCount = msgCount;
			messages.scrollTop(MAX_SCROLL_HEIGHT);
		}
		var form = jQuery(".cim-form");
		form.find("[name=lastMessageId]").val(
				jQuery("#cim-next-messages [name=lastMessageId]").val());
		if (jQuery(this).is(".cim-form")) {
			form.find("[name=message]").val("");
		}
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		timeout = setTimeout(function() {
			jQuery("#cim-next-messages [type=submit]").click();
		}, 2000);
	}

	jQuery(function() {

		jQuery(".cim-form [type=submit]").hide()

		jQuery("body").on("mouseenter", ".cim-messages [data-user]", function(e) {
			var $this = jQuery(this);
			var user = $this.data("user");
			var available = false;
			jQuery(".cim-form [name=receiver] option").each(function() {
				var option = jQuery(this);
				if (option.val() == user) {
					available = true;
					return false;
				}
			});
			$this.toggleClass("data-user-available", available);
		});
		jQuery("body").on("click", ".cim-messages [data-user].data-user-available", function(e) {
			e.preventDefault();
			jQuery(".cim-form [name=receiver]").val(jQuery(this).data("user"));
		});

	});

})(jQuery);