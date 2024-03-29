<%@ taglib uri="jakarta.tags.core" prefix="c" %><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:if test="${info.god && empty context}">
<a class="action-button more edit-static-config" href="${info.currentURL}?webaction=EditStaticConfig"><span>${i18n.edit['admin.edit-static-config']}</span></a>
</c:if>
<c:if test="${empty context}">
<div class="special">
	<form id="form-create-site" action="${info.currentURL}" method="post">
		<input type="hidden" name="webaction" value="createSite" />
		<input class="label-inside label" type="text" name="context" value="${i18n.edit['admin.button.createsite']}..." />
		<input type="submit" value="${i18n.edit['global.ok']}" />		
	</form>
</div>
</c:if>
<c:if test="${info.admin}">
<c:if test="${not empty context}">
<div class="link"><a href="${info.contextDownloadURL}/${context}.properties">${context}.properties</a></div>
<a class="popup cboxElement" title="upload" href="${info.absoluteLocalURLPrefix}${currentModule.path}/jsp/upload.jsp?currentURL=${info.currentURL}&context=${context}">
<span>${i18n.edit['admin.link.uploadsite']}</span>
</a>
</c:if>
<c:set var="paramContext" value="&context=${context}" />
<a class="action-button clear-cache" href="${info.currentURL}?webaction=clearcache${not empty context?paramContext:''}"><span>${i18n.edit['admin.clear-cache']}</span></a>
<a class="action-button clear-cache" href="${info.currentURL}?webaction=clearimagecache${not empty context?paramContext:''}"><span>${i18n.edit['admin.clear-image-cache']}</span></a>
<c:if test="${fn:endsWith(currentModule.renderer, 'components.jsp')}"><jsp:include page="components_actions.jsp" /></c:if>
</c:if>
<div class="clear">&nbsp;</div>

