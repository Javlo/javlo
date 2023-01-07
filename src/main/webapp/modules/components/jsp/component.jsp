<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib
	prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
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
</div>
<div class="help">
	<jsp:include page="help.html" />
</div>
</c:if>
</div>