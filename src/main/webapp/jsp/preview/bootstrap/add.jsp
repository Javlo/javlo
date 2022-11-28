<%@page import="org.javlo.module.content.Edit"%>
<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
//if (ctx.getGlobalContext().getStaticConfig().isAddButton()) {
%><c:set var="logged" value="${not empty info.editUser}" />
<c:if test="${not empty info.editUser}">
	<div class="add">
		<div class="macros hiddenCollapse bloc-background" id="addMacros">
			<c:forEach var="macro" items="${info.addMacro}">
				<c:set var="key" value="macro.${macro.name}" />
				<c:if test="${!macro.interative}">
					<c:set var="url" value="${macro.url}" />
					<c:if test="${empty url}">
						<c:url var="url" value="${info.currentURL}" context="/">
							<c:param name="module" value="macro" />
							<c:param name="webaction" value="macro.executeMacro" />
							<c:param name="mode" value="3" />
							<c:param name="macro" value="${macro.name}" />
							<c:param name="macro-${macro.name}" value="${macro.name}" />
							<c:param name="previewEdit" value="true" />
						</c:url>
					</c:if>
					<a class="btn btn-default" aria-label="Left Align" href="${url}" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}"> <c:set var="descriptionKey" value="macro.description.${macro.name}" /> <span class="button-group-addon"><i class="${macro.icon}" aria-hidden="true"></i></span> ${i18n.edit[key] != key?i18n.edit[key]:macro.name}
					</a>
				</c:if>
				<c:if test="${macro.interative}">
					<c:set var="url" value="${macro.url}" />
					<c:if test="${empty url}">
						<c:url var="url" value="<%=URLHelper.createURL(editCtx)%>" context="/">
							<c:param name="module" value="macro" />
							<c:param name="webaction" value="macro.executeInteractiveMacro" />
							<c:param name="mode" value="3" />
							<c:param name="macro" value="${macro.name}" />
							<c:param name="macro-${macro.name}" value="${macro.name}" />
							<c:param name="previewEdit" value="true" />
						</c:url>
					</c:if>
					<c:set var="descriptionKey" value="macro.description.${macro.name}" />
					<button type="button" class="btn btn-default" aria-label="Left Align" onclick="editPreview.openModal('${i18n.edit[key] != key?i18n.edit[key]:macro.name}', '${url}', 'jv-modal-${macro.modalSize}');" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:''}">
						<c:set var="key" value="macro.${macro.name}" />
						<span class="button-group-addon"><i class="${macro.icon}" aria-hidden="true"></i></span> ${i18n.edit[key] != key?i18n.edit[key]:macro.name}
					</button>
				</c:if>
			</c:forEach>
		</div>

		<a role="button" class="action prvCollapse" href="#addMacros" aria-expanded="false" aria-controls="addMacros"><i class="bi bi-wrench-adjustable-circle"></i></a>

<div class="direct-action">
	<%-- 	<form id="pc_form" action="${info.currentURL}" method="post"> --%>
	<!-- 		<div class="pc_line"> -->
	<!-- 			<input type="hidden" name="webaction" value="edit.previewedit" /> -->
	<%-- 			<c:if test='${!editPreview}'> --%>
	<%-- 				<button class="action btn-wait-loading" type="submit" title="${i18n.edit['preview.label.edit-page']}"> --%>
	<!-- 					<i class="bi bi-lock"></i></button> -->
	<%-- 			</c:if> --%>
	<%-- 			<c:if test='${editPreview}'> --%>
	<%-- 				<button class="action btn-wait-loading" type="submit" title="${i18n.edit['preview.label.not-edit-page']}"> --%>
	<!-- 					<i class="bi bi-unlock"></i></button> -->
	<%-- 			</c:if> --%>
	<!-- 		</div> -->
	<!-- 	</form> -->


	<c:if test="${not empty integrities && !globalContext.collaborativeMode && !globalContext.mailingPlatform && logged}">
		<c:if test="${fn:length(integrities.checker)==0}">
			<li><a class="btn btn-default btn-sm btn-integrity btn-color alert-success btn-notext" data-toggle="collapse" data-target="#integrity-list" href="#integrity-list" aria-expanded="false" aria-controls="integrity-list"> <span class="glyphicon glyphicon-check" aria-hidden="true"></span>
			</a>
				<div class="integrity-message collapse${integrities.error && contentContext.previewEdit?' in':''}" id="integrity-list">
					<ul class="list-group">
						<li class="list-group-item list-group-item-success">${i18n.edit['integrity.no_error']}</li>
					</ul>
				</div></li>
		</c:if>
	</c:if>

	<c:url var="logoutURL" value="<%=URLHelper.createURL(ctx)%>" context="/">
		<c:param name="edit-logout" value="true" />
	</c:url>
	<c:if test="${userInterface.minimalInterface}">
		<a class="action btn-logout" title="${i18n.edit['global.logout']}" href="${logoutURL}"><i class="fa fa-sign-out" aria-hidden="true"></i></a>
	</c:if>
</div>
</div>
</c:if>
<c:if test="${empty info.editUser}">
	<div class="add">
		<form id="pc_form" method="post" action="<%=URLHelper.createURL(editCtx)%>">
			<c:if test='${!editPreview}'>
				<button class="action btn-login" type="submit" title="${i18n.edit['global.login']}">
					<i class="bi bi-pencil-square"></i>
				</button>
			</c:if>
			<input type="hidden" name="backPreview" value="true" />
		</form>
	</div>
</c:if>
<%
//}
%>