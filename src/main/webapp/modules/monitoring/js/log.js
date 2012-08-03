var REFRESH_LOG_INTERVAL = 2000;

(function($) {

	var timeout = null;
	var lastCount = -1;

	jQuery(document).live("ajaxUpdate", function () {
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		var content = jQuery(".log-content");
		var count = content.text().length;
		if (lastCount != count) {
			lastCount = count;
			content.scrollTop(MAX_SCROLL_HEIGHT);
		}
		timeout = setTimeout(refreshLogs, REFRESH_LOG_INTERVAL);
	});
	
	function refreshLogs() {
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
		if(!jQuery(".log-pause input").attr('checked')) {
			jQuery("#log-next-lines [type=submit]").click();
		} else {
			timeout = setTimeout(refreshLogs, REFRESH_LOG_INTERVAL);
		}
	}
	
	jQuery(function() {
		jQuery(".log-pause").show();
	});

})(jQuery);