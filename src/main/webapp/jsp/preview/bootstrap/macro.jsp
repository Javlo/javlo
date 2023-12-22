<%@page import="org.javlo.module.content.Edit"%>
<%@page import="org.javlo.context.ContentContext"%><%@page import="org.javlo.helper.URLHelper"%><%@ taglib uri="jakarta.tags.core" prefix="c"%><%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%
ContentContext ctx = ContentContext.getContentContext(request, response);
ContentContext editCtx = new ContentContext(ctx);
editCtx.setRenderMode(ContentContext.EDIT_MODE);
%>
<div class="macro-btn-list _jv_menu">
<c:forEach var="macro" items="${info.addMacro}">
	<c:if test="${macro.type == param.type}">
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
			<a class="btn btn-default" aria-label="Left Align" href="${url}" title="${i18n.edit[descriptionKey] != descriptionKey?i18n.edit[descriptionKey]:macro.name}">
			<c:set var="descriptionKey" value="macro.description.${macro.name}" />
			<span class="button-group-addon"><i class="${macro.icon}" aria-hidden="true"></i></span>
			<span class="label">${i18n.edit[key] != key?i18n.edit[key]:macro.name}</span>
			</a>
		</c:if>

	</c:if>
</c:forEach>

	<hr />

	<c:forEach var="macro" items="${info.addMacro}">
		<c:if test="${macro.type == param.type}">
			<c:set var="key" value="macro.${macro.name}" />
			
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
					<span class="button-group-addon"><i class="${macro.icon}" aria-hidden="true"></i></span>
					<span class="label">${i18n.edit[key] != key?i18n.edit[key]:macro.name}</span>
				</button>
			</c:if>
		</c:if>
	</c:forEach>
</div>
