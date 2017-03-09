<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%><c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">	
	<c:param name="module" value="template"></c:param>
	<c:param name="webaction" value="template.changeFromPreview"></c:param>
	<c:param name="previewEdit" value="true"></c:param>
</c:url><div class="settings">
<h2><span class="glyphicon glyphicon-cog" aria-hidden="true"></span>Settings</h2>
<div class="height-to-bottom">
<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('Template', '${url}');">
  <div class="label">Template</div>
  <div class="value">${empty info.page.templateId?'<span class="glyphicon glyphicon-download normal"></span>':''} ${info.templateName}</div>
  <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
</button>
<c:if test="${!contentContext.currentTemplate.mailing || !userInterface.light}">
	<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
		<c:param name="module" value="content" />
		<c:param name="webaction" value="changeMode" />
		<c:param name="mode" value="3" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('${i18n.edit['global.page-properties']}', '${url}');">
	  <div class="label">${i18n.edit['global.page-properties']}</div>
	  <div class="value">${info.pageName}</div>
	  <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
	</button>
</c:if>

<c:if test="${fn:length(info.interactiveMacro)>0 || fn:length(info.macro)>0}">
<h2><span class="glyphicon glyphicon-wrench" aria-hidden="true"></span>${i18n.edit['command.macro']}</h2>
<c:forEach var="macro" items="${info.interactiveMacro}">
	<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
		<c:param name="module" value="macro" />
		<c:param name="webaction" value="macro.executeInteractiveMacro" />
		<c:param name="mode" value="3" />
		<c:param name="macro" value="${macro.name}" />
		<c:param name="macro-${macro.name}" value="${macro.name}" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('${i18n.edit[key] != key?i18n.edit[key]:macro.name}', '${url}');">
	  <c:set var="key" value="macro.${macro.name}" />
	  <c:set var="descriptionKey" value="macro.description.${macro.name}" />
	  <div class="label">${i18n.edit[key] != key?i18n.edit[key]:macro.name}</div>
	  <div class="value">${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}</div>	  
	  <span class="glyphicon glyphicon-modal-window" aria-hidden="true"></span>	  
	</button>	
</c:forEach>
<c:forEach var="macro" items="${info.macro}">
	<c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="module" value="macro" />
		<c:param name="webaction" value="macro.executeMacro" />
		<c:param name="mode" value="3" />
		<c:param name="macro" value="${macro.name}" />
		<c:param name="macro-${macro.name}" value="${macro.name}" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	<a class="btn btn-default" aria-label="Left Align" href="${url}">
	  <c:set var="key" value="macro.${macro.name}" />
	  <c:set var="descriptionKey" value="macro.description.${macro.name}" />
	  <div class="label">${i18n.edit[key] != key?i18n.edit[key]:macro.name}</div>
	  <div class="value">${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}</div>
	  <span class="glyphicon glyphicon-play-circle" aria-hidden="true"></span>	  	  
	</a>	
</c:forEach>
</c:if>
</div>
</div>
<c:if test="${contentContext.ajax}"><script>editPreview.onReadyFunction();</script></c:if>