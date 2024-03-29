<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"
%><%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%>
<c:if test="${not empty editUser && !globalContext.mailingPlatform}">
	<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
		<c:param name="module" value="admin"></c:param>
		<c:param name="previewEdit" value="true"></c:param>
	</c:url>
	<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('${i18n.edit['preview.label.properties']}','${url}'); return false;">
		<div class="label">${i18n.edit['preview.label.properties']}</div>
		<div class="value">${info.globalTitle}</div>
		<span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
	</button>
</c:if>
<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">	
	<c:param name="module" value="template"></c:param>
	<c:param name="webaction" value="template.changeFromPreview"></c:param>
	<c:param name="previewEdit" value="true"></c:param>
</c:url><div class="settings">
<h2><span class="glyphicon glyphicon-cog" aria-hidden="true"></span>Settings</h2>
<div class="height-to-bottom">
<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('Template', '${url}');">
  <div class="label">Template</div>
  <div class="value">${empty info.page.templateId?'<span class="glyphicon glyphicon-download"></span>':''} ${info.templateName}</div>
  <span class="glyphicon glyphicon-pencil" aria-hidden="true"></span>
</button>
<c:if test="${!userInterface.light}">
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
</div>
</div>