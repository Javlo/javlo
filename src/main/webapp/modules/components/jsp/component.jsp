<%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib
	prefix="fn" uri="jakarta.tags.functions"%>
<div class="content">

<div class="editor-wrapper">
<div class="editor">
<jsp:include page="navigation_components.jsp" />
<c:if test="${not empty param.component}">
<div class="tabs tabloading component ui-tabs ui-widget ui-widget-content ui-corner-all">
<form id="tabs-form" action="${info.currentURL}" method="post">
<input type="hidden" name="webaction" value="components.update" />
<input type="hidden" name="component" value="${param.component}" />
	<div class="header-tabs">
		<ul class="ui-tabs-nav ui-helper-reset ui-helper-clearfix ui-widget-header ui-corner-all">
			<c:if test="${xhtmlExist}">
				<li class="ui-state-default ui-corner-top ui-tabs-selected ui-state-active">
					<a class="link" href="#htmltab">xhtml</a>
				</li>
			</c:if>
			<c:if test="${cssExist}">
				<li class="enabled ui-state-default ui-corner-top">
					<a class="link" href="#csstab">css</a>
				</li>
			</c:if>
			<c:if test="${propertiesExist}">
				<li class="enabled ui-state-default ui-corner-top">
					<a class="link" href="#propertiestab">properties</a>
				</li>
			</c:if>
			<li class="enabled ui-state-default ui-corner-top">
				<a class="link" href="#previoustab">previous</a>
			</li>
		</ul>
	</div>
	<c:if test="${xhtmlExist}"><div id="htmltab" class="ui-tabs-panel ui-widget-content ui-corner-bottom">
		<textarea name="html" class="text-editor" rows="10" cols="10" data-ext="html"  data-mode="text/html">${fn:escapeXml(xhtml)}</textarea>
	</div></c:if>
	<c:if test="${cssExist}"><div id="csstab" class="ui-tabs-panel ui-widget-content ui-corner-bottom">
		<textarea class="form-control text-editor" data-ext="css" data-mode="text/x-scss" name="css">${fn:escapeXml(css)}</textarea>		
	</div></c:if>
	<c:if test="${propertiesExist}"><div id="propertiestab" class="ui-tabs-panel ui-widget-content ui-corner-bottom">
		<textarea class="form-control text-editor" data-mode="text/properties" name="properties">${properties}</textarea>		
	</div></c:if>
	<div id="previoustab" class="ui-tabs-panel ui-widget-content ui-corner-bottom">
		<iframe id="previousiframe" src="${previousUrl}"></iframe>
	</div>
<div class="action">
	<input value="${i18n.edit['global.save']}" type="submit">
</div>
</form>
</div>
<style type="text/css">
	/* Own the panel visibility with !important so jQuery UI's inline display can't override it */
	.tabs.component .ui-tabs-panel.jv-tab-hidden { display: none !important; }
	.tabs.component .ui-tabs-panel.jv-tab-shown { display: block !important; }
</style>
<script type="text/javascript">
jQuery(function() {
	var $tabs = jQuery('.tabs.component');
	if (!$tabs.length) { return; }
	var $links = $tabs.find('.header-tabs .link');
	var $panels = $tabs.find('.ui-tabs-panel');
	var currentHref = null;

	function apply() {
		if (!currentHref) { return; }
		var $panel = jQuery(currentHref);
		$panels.removeClass('jv-tab-shown').addClass('jv-tab-hidden');
		$panel.removeClass('jv-tab-hidden').addClass('jv-tab-shown');
		/* CodeMirror renders with wrong size inside a hidden panel; refresh once visible */
		$panel.find('.CodeMirror').each(function() {
			if (this.CodeMirror) { this.CodeMirror.refresh(); }
		});
	}

	function activate($link) {
		var href = $link.attr('href');
		if (!href || href.charAt(0) !== '#' || !jQuery(href).length) { return; }
		currentHref = href;
		$link.closest('ul').find('li')
			.removeClass('ui-tabs-selected ui-state-active ui-tabs-active');
		$link.closest('li').addClass('ui-tabs-selected ui-state-active ui-tabs-active');
		apply();
		/* keep the active tab selected after the form is submitted */
		var form = jQuery('#tabs-form');
		var action = form.attr('action') || '';
		if (action.indexOf('#') >= 0) { action = action.substring(0, action.indexOf('#')); }
		form.attr('action', action + href);
	}

	$links.on('click', function(e) {
		e.preventDefault();
		e.stopImmediatePropagation();
		activate(jQuery(this));
	});

	/* drop the loading state (originally removed by components_codemirror.js) */
	$tabs.removeClass('tabloading');

	/* initial tab: url hash, then the pre-selected tab, then the first one */
	var $initial = jQuery();
	if (window.location.hash && $panels.filter(window.location.hash).length) {
		$initial = $links.filter('[href="' + window.location.hash + '"]').first();
	}
	if (!$initial.length) {
		$initial = $tabs.find('li.ui-tabs-selected .link, li.ui-state-active .link').first();
	}
	if (!$initial.length) { $initial = $links.first(); }
	activate($initial);

	/* jQuery UI .tabs() may init later (on ajaxUpdate) and reset panel display;
	   re-assert our state after it runs so the class rules stay in control */
	setTimeout(apply, 100);
	setTimeout(apply, 500);
});
</script>
</div>
<div class="help-wrapper">

	<c:if test="${not empty detectedFields}">
	<div class="detected-fields">
		<fieldset>
			<legend>detected fields</legend>
			<ul>
				<c:forEach var="field" items="${detectedFields}" varStatus="status">
					<li class="_jv_flex-line ${status.index % 2 == 0?'fist':'last'}">
						<span>${field.key}</span>
						<span>${field.value}</span>
					</li>
				</c:forEach>
			</ul>
		</fieldset>
	</div>
	</c:if>

	<jsp:include page="help.html" />
</div>
</c:if>
</div>