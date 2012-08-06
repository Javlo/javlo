var REFRESH_LOG_INTERVAL = 2000;

(function($) {

	var timeout = null;
	var lastCount = -1;

	jQuery(document).live("ajaxUpdate", function () {
		clearAutoRefresh();
		refreshFilter();
		var isActive = isAutoRefreshActive();
		refreshAutoRefreshState(isActive);
		var content = jQuery(".log-content");
		var count = content.find(".log-line:visible").length;
		if (lastCount != count) {
			lastCount = count;
			var maxScrollTop = (content.prop('scrollHeight') - content.height());
			if (maxScrollTop > 0) {
				content.animate({scrollTop: maxScrollTop}, 1000);
				//content.scrollTop(maxScrollTop);
			}
		}
		if (isActive) {
			timeout = setTimeout(refreshLogs, REFRESH_LOG_INTERVAL);
		}
	});

	function isAutoRefreshActive() {
		return jQuery("#log-auto-refresh").is('.active');
	}
	function clearAutoRefresh() {
		if (timeout) {
			clearTimeout(timeout);
			timeout = null;
		}
	}
	function refreshLogs() {
		jQuery("#log-next-lines [type=submit]").click();
	}
	jQuery("#log-next-lines [type=submit]").live('click', function() {
		jQuery("#log-wait").show();
	});

	jQuery("#log-auto-refresh").live('click', function() {
		var isActive = !isAutoRefreshActive();
		jQuery("#log-auto-refresh").toggleClass("active", isActive);
		refreshAutoRefreshState(isActive);
		if (isActive) {
			refreshLogs();
		} else {
			clearAutoRefresh();
		}
	});
	jQuery("#log-refresh").live('click', refreshLogs);
	
	function refreshAutoRefreshState(isActive) {
		jQuery("#log-wait").toggle(isActive);
		jQuery("#log-refresh").toggle(!isActive);
		jQuery("#log-next-lines form").toggle(false);
	}

	var filterTimeout = null;
	jQuery(function() {
		jQuery(".log-js-only").show();
	});
	jQuery("#log-filter").live('keyup', function(e) {
		e.preventDefault();
		if (filterTimeout!= null) {
			clearTimeout(filterTimeout);
			filterTimeout = null;
		}
		filterTimeout = setTimeout(function() {
			filterTimeout = null;
			refreshFilter(true);
		}, 50);
	});
	function refreshFilter(resetLastCount) {
		var filter = jQuery("#log-filter").val();
		if (filter) {
			filter = filter.toLowerCase();
		}
		jQuery(".log-content .log-line").each(function() {
			var line = jQuery(this);
			line.toggle(filter == null || filter == "" || (line.find(".log-text").text().toLowerCase().indexOf(filter) >= 0));
		});
		if (resetLastCount) {
			lastCount = jQuery(".log-content .log-line:visible").length;
		}
	}

})(jQuery);