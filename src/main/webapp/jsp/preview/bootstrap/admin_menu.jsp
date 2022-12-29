<%@page import="org.javlo.module.content.Edit"%>
<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%>
<div class="_jv_menu">
<a id="pc_edit_mode_button" target="_blank" class="btn btn-default" title="${i18n.edit['global.exit']}" href="${info.currentViewURL}">
	<span class="button-group-addon"><i class="bi bi-cloud"></i></span>
	<span class="label">View site</span>
</a>
<c:if test="${contentContext.time}">
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="${info.currentPreviewURL}">
	<span class="button-group-addon"><i class="bi bi-pencil-square"></i></span>
	<span class="label">preview site</span>
</a>
</c:if>
<c:if test="${contentContext.preview}">
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="${info.currentTimeURL}">
	<span class="button-group-addon"><i class="bi bi-clock-history"></i></span>
	<span class="label">${i18n.edit['time.title']}</span>
</a>
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="#" onclick="editPreview.openModal('${i18n.edit['module.file']}','${info.currentEditURL}?module=file&previewEdit=true', true); return false;">
	<span class="button-group-addon"><i class="bi bi-file-earmark"></i></span>
	<span class="label">${i18n.edit['module.file']}</span>
</a>
<c:if test="${info.admin}">
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="#" onclick="editPreview.openModal('Site properties','${info.currentEditURL}?module=admin&previewEdit=true', true); return false;">
	<span class="button-group-addon"><i class="bi bi-gear"></i></span>
	<span class="label">Site properties</span>
</a>
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="${info.currentEditURL}">
	<span class="button-group-addon"><i class="bi bi-arrow-repeat"></i></span>
	<span class="label">Admin modules</span>
</a>
</c:if>
</c:if>
</div>


