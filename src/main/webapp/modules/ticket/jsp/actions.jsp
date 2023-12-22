<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<c:url var="createURL" value="${info.currentURL}" context="/">
	<c:param name="id" value="new" />
</c:url>
<c:if test="${not empty globalContext.mainHelpURL}">
	<a class="action-button" href="${globalContext.mainHelpURL}" target="_blank">
		<i class="bi bi-question-circle"></i>
		<span>${i18n.edit['ticket.support-link']}</span>
	</a>
</c:if>
<a class="action-button more page btn-primary" href="${createURL}">
	<i class="bi bi-file-earmark-plus"></i>
	<span>${i18n.edit['global.create']}</span>
</a>