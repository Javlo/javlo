<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"
 %><c:set var="titleCount" value="0" />
<div class="component-list auto-scroll">
<div id="paste">
	<c:if test="${not empty clipboard.copied}">
		<c:url var="url" value="${info.currentURL}">
			<c:param name="webaction" value="edit.clearClipboard" />
		</c:url>
		<h4><a href="${url}" class="ajax close">X</a><span>${i18n.edit['global.clipboard']}</span></h4>
		<div class="component" data-type="clipboard" data-deletable="true"><span>${clipboard.label}</span></div>
	</c:if>
</div>
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">
<c:set var="titleCount" value="${titleCount+1}" />
<h4><span style="color: #${comp.hexColor}">${i18n.edit[comp.value]}</span></h4>
</c:if><c:if test="${!comp.metaTitle}"
><div class="component${comp.selected?' selected':''}" data-type="${comp.type}"><span>${comp.label}</span></div>
</c:if></c:forEach>
<div style="clear: both;"><span></span></div>
</div>
