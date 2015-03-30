<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
 %><c:set var="titleCount" value="0" />
 
 <c:if test="${not empty clipboard.copied}">
<div id="_ep_clipboard" class="clipboard component-list">
	<c:url var="url" value="${info.currentURL}">
		<c:param name="webaction" value="edit.clearClipboard" />
	</c:url>
	<h2><span class="glyphicon glyphicon glyphicon-duplicate" aria-hidden="true"></span>${i18n.edit['global.clipboard']}<a href="${url}" class="ajax close">X</a></h2>
	<div class="body">
		<div class="component${comp.selected?' selected':''}" data-type="clipboard" data-deletable="true">
			<div class="wrapper-in">
				<div class="figure"></div>				
				<span>${clipboard.label}</span>
				<div class="category">(${i18n.edit['global.clipboard']})</div>
			</div>
		</div>
	</div>
</div>
</c:if>
 <h2><span class="glyphicon glyphicon-briefcase" aria-hidden="true"></span>Content</h2>
<div class="component-list height-to-bottom">
<c:set var="cat" value="" />
<c:forEach var="comp" items="${components}">
<c:if test="${comp.metaTitle}">
<c:set var="cat" value="${i18n.edit[comp.value]}" />
</c:if><c:if test="${!comp.metaTitle}"
><c:set var="toolTipKey" value="content.${comp.type}.description" /><c:set var="toolTip" value="data-toggle=\"tooltip\" data-placement=\"right\" title=\"${i18n.edit[toolTipKey]}\"" />
<div ${i18n.edit[toolTipKey] != toolTipKey?toolTip:''} class="component${comp.selected?' selected':''} component-${comp.type}" data-type="${comp.type}">
<div class="wrapper-in">
<div class="figure"></div>
<div class="text">
<span>${comp.label}</span>
<div class="category">(${i18n.edit[cat]})</div>
</div>
</div>
</div>
</c:if></c:forEach>
<div style="clear: both;"><span></span></div>
</div>
