<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
 %><c:set var="titleCount" value="0" />
<div class="component-list height-to-bottom">
<div id="paste">
	<c:if test="${not empty clipboard.copied}">
		<c:url var="url" value="${info.currentURL}">
			<c:param name="webaction" value="edit.clearClipboard" />
		</c:url>
		<h4><a href="${url}" class="ajax close">X</a><span>${i18n.edit['global.clipboard']}</span></h4>
		<div class="component" data-type="clipboard" data-deletable="true"><span>${clipboard.label}</span></div>
	</c:if>
</div>
<c:set var="cat" value="" />
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">
<c:set var="cat" value="${i18n.edit[comp.value]}" />
</c:if><c:if test="${!comp.metaTitle}"
><div class="component${comp.selected?' selected':''}" data-type="${comp.type}">
<div class="wrapper-in">
<div class="figure"></div>
<span>${comp.label}</span>
<div class="category">(${i18n.edit[cat]})</div>
</div>
</div>
</c:if></c:forEach>
<div style="clear: both;"><span></span></div>
</div>
