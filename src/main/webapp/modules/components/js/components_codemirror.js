/*
 * Component editor: tab switching + CodeMirror init.
 *
 * Loaded persistently via modules/components/config.properties (js.import), so it
 * runs even though the component editor markup is injected through the edit AJAX
 * pipeline. Everything is bound with event delegation on `document` and the CSS is
 * injected from JS, so it does not depend on the injected fragment executing any
 * inline <script>/<style>.
 */
(function() {

	var CSS_ID = "jv-component-tabs-css";

	/* Own panel visibility with !important so it beats jQuery UI's inline display
	   and works even if the old jquery.ui.css (.ui-tabs-hide{display:none}) is gone. */
	function injectCss() {
		if (document.getElementById(CSS_ID)) { return; }
		var style = document.createElement("style");
		style.id = CSS_ID;
		style.type = "text/css";
		var css = ".tabs.component .ui-tabs-panel { display: none !important; }"
			+ " .tabs.component .ui-tabs-panel.jv-tab-shown { display: block !important; }";
		if (style.styleSheet) { style.styleSheet.cssText = css; } else { style.appendChild(document.createTextNode(css)); }
		(document.head || document.getElementsByTagName("head")[0]).appendChild(style);
	}

	function activate($link) {
		var href = $link.attr("href");
		if (!href || href.charAt(0) !== "#") { return; }
		var $tabs = $link.closest(".tabs.component");
		var $panel = $tabs.find(href);
		if (!$panel.length) { return; }

		$link.closest("ul").find("li")
			.removeClass("ui-tabs-selected ui-state-active ui-tabs-active");
		$link.closest("li").addClass("ui-tabs-selected ui-state-active ui-tabs-active");

		$tabs.find(".ui-tabs-panel").removeClass("jv-tab-shown");
		$panel.addClass("jv-tab-shown");

		/* CodeMirror renders with a wrong size while inside a hidden panel */
		$panel.find(".CodeMirror").each(function() {
			if (this.CodeMirror) { this.CodeMirror.refresh(); }
		});

		/* keep the active tab selected after the form is submitted */
		var $form = $tabs.find("#tabs-form");
		var action = $form.attr("action") || "";
		if (action.indexOf("#") >= 0) { action = action.substring(0, action.indexOf("#")); }
		$form.attr("action", action + href);
	}

	/* Delegated click: works for markup injected after page load too */
	jQuery(document).on("click", ".tabs.component .header-tabs .link", function(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		activate(jQuery(this));
	});

	function initTabs() {
		injectCss();
		jQuery(".tabs.component").each(function() {
			var $tabs = jQuery(this).removeClass("tabloading");
			/* select initial tab if none shown yet: url hash, pre-selected li, then first */
			if ($tabs.find(".ui-tabs-panel.jv-tab-shown").length) { return; }
			var $link = jQuery();
			if (window.location.hash) {
				$link = $tabs.find('.header-tabs .link[href="' + window.location.hash + '"]').first();
			}
			if (!$link.length) {
				$link = $tabs.find("li.ui-tabs-selected .link, li.ui-state-active .link").first();
			}
			if (!$link.length) {
				$link = $tabs.find(".header-tabs .link").first();
			}
			if ($link.length) { activate($link); }
		});
	}

	/* Run on DOM ready, on window load, and after every edit AJAX update
	   (the latter also runs after jQuery UI .tabs() so we stay in control). */
	jQuery(initTabs);
	jQuery(window).on("load", initTabs);
	jQuery(document).on("ajaxUpdate", function() { setTimeout(initTabs, 0); });

	/* CodeMirror on the editor textareas (once) */
	function initCodeMirror() {
		if (typeof CodeMirror === "undefined") { return; }
		jQuery(".text-editor").each(function() {
			if (this.__jvCmDone) { return; }
			this.__jvCmDone = true;
			CodeMirror.fromTextArea(this, {
				lineNumbers: true,
				mode: jQuery(this).data("mode"),
				foldGutter: true,
				extraKeys: { "Ctrl-Space": "autocomplete" },
				gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
			});
		});
		/* the freshly-created CodeMirror in the visible panel needs a refresh */
		jQuery(".tabs.component .ui-tabs-panel.jv-tab-shown .CodeMirror").each(function() {
			if (this.CodeMirror) { this.CodeMirror.refresh(); }
		});
	}
	jQuery(window).on("load", initCodeMirror);
	jQuery(document).on("ajaxUpdate", function() { setTimeout(initCodeMirror, 0); });

})();
