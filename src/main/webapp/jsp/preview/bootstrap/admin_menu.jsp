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
	<span class="label">View mode</span>
</a>
<c:if test="${info.admin}"><a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="${info.currentEditURL}">
	<span class="button-group-addon"><i class="bi bi-arrow-repeat"></i></span>
	<span class="label">Edit mode</span>
</a>
<a id="pc_edit_mode_button" class="btn btn-default" title="${i18n.edit['global.exit']}" href="#" onclick="editPreview.openModal('Site properties','${info.currentEditURL}?module=admin&previewEdit=true'); return false;">
	<span class="button-group-addon"><i class="bi bi-gear"></i></span>
	<span class="label">Site properties</span>
</a></c:if>
</div>


