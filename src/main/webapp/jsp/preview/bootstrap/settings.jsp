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
<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('Template', '${url}');">
  <div class="label">Template</div>
  <div class="value">${info.templateName}</div>
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


</div>