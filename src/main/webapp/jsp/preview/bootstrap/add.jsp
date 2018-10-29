<%@page import="org.javlo.context.ContentContext"
%><%@page import="org.javlo.helper.URLHelper"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><c:if test="${fn:length(info.addMacro)>0 || fn:length(info.macro)>0}"><%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
if (ctx.getGlobalContext().getStaticConfig().isAddButton()) {
	
%>
<c:if test="${not empty info.editUser}">
<div class="add">	
<div class="macros hiddenCollapse" id="addMacros">
<c:forEach var="macro" items="${info.addMacro}">
<c:if test="${!macro.interative}">
  <c:url var="url" value="${info.currentURL}" context="/">
		<c:param name="module" value="macro" />
		<c:param name="webaction" value="macro.executeMacro" />
		<c:param name="mode" value="3" />
		<c:param name="macro" value="${macro.name}" />
		<c:param name="macro-${macro.name}" value="${macro.name}" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	<a class="btn btn-default" aria-label="Left Align" href="${url}" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}">
	  <c:set var="key" value="macro.${macro.name}" />
	  <c:set var="descriptionKey" value="macro.description.${macro.name}" />
	  <i class="${macro.icon}" aria-hidden="true"></i> ${i18n.edit[key] != key?i18n.edit[key]:macro.name}
	</a>
</c:if><c:if test="${macro.interative}">
	<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
		<c:param name="module" value="macro" />
		<c:param name="webaction" value="macro.executeInteractiveMacro" />
		<c:param name="mode" value="3" />
		<c:param name="macro" value="${macro.name}" />
		<c:param name="macro-${macro.name}" value="${macro.name}" />
		<c:param name="previewEdit" value="true" />
	</c:url>
	 <c:set var="descriptionKey" value="macro.description.${macro.name}" />
	<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('${i18n.edit[key] != key?i18n.edit[key]:macro.name}', '${url}', 'jv-modal-${macro.modalSize}');" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}">
	  <c:set var="key" value="macro.${macro.name}" />	 
	  <span class="button-group-addon"><i class="${macro.icon}" aria-hidden="true"></i></span> ${i18n.edit[key] != key?i18n.edit[key]:macro.name}
	</button>
</c:if>	
</c:forEach></div>

<a role="button" class="action prvCollapse" href="#addMacros" aria-expanded="false" aria-controls="addMacros"><i class="fa fa-pencil-square" aria-hidden="true"></i></a>
<div class="direct-action">
	<form id="pc_form" action="${info.currentURL}" method="post">
		<div class="pc_line">
			<input type="hidden" name="webaction" value="edit.previewedit" />
			<c:if test='${!editPreview}'>
				<button class="action btn-wait-loading" type="submit" title="${i18n.edit['preview.label.edit-page']}">
					<i class="fa fa-lock" aria-hidden="true"></i></button>
			</c:if>
			<c:if test='${editPreview}'>
				<button class="action btn-wait-loading" type="submit" title="${i18n.edit['preview.label.not-edit-page']}">
					<i class="fa fa-unlock" aria-hidden="true"></i></button>
			</c:if>
		</div>
	</form>
	<c:url var="logoutURL" value="<%=URLHelper.createURL(ctx)%>" context="/">
		<c:param name="edit-logout" value="true" />
	</c:url>
	<a class="action btn-logout" title="${i18n.edit['global.logout']}" href="${logoutURL}"><i class="fa fa-sign-out" aria-hidden="true"></i></a>
</div>
</div></c:if>
<c:if test="${empty info.editUser}">
<div class="add">
<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
		<c:if test='${!editPreview}'>
			<button class="action btn-login" type="submit" title="${i18n.edit['global.login']}">
				<i class="fa fa-sign-in" aria-hidden="true"></i></button>
		</c:if>
		<input type="hidden" name="backPreview" value="true" />
</form>
</div>
</c:if>
<%}%>
</c:if>