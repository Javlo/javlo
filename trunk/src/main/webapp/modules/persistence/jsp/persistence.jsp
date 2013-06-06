<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="content">
<fieldset>
<legend>${i18n.edit['persistence.title.load-save']}</legend>
<ul class="navigation">
	<li>
		<a href="${download}">${i18n.edit['edit.link.download']}</a>
		<c:if test="${not empty token}"><a class="token" href="${download}&j_token=${token}">[${i18n.edit['edit.link.token-download']}]</a></c:if>
	</li>	
	<li>
		<a href="${downloadAll}">${i18n.edit['edit.link.download-static']}</a>
		<c:if test="${not empty token}"><a class="token" href="${downloadAll}?j_token=${token}">[${i18n.edit['edit.link.token-download']}]</a></c:if>
	</li>
	<li>
		<a class="popup" href="${info.absoluteURLPrefix}${currentModule.path}/jsp/upload.jsp?editTemplateURL=${info.editTemplateURL}&currentURL=${info.currentURL}" title="${i18n.edit['edit.action.upload']}"><span>${i18n.edit['edit.action.upload']}</span></a>
	</li>		
</ul>
</fieldset>
<fieldset>
<legend>${i18n.edit['persistence.title.remote-import']}</legend>
<form action="${info.currentURL}" method="post">
<div class="line">
	<label for="static-url">${i18n.edit['persistence.title.static-import']}</label>
	<input type="hidden" name="webaction" value="importPage" />
	<input id="static-url" type="text" name="import-url" />
	<input type="submit" value="${i18n.edit['global.ok']}" />
</div>
</form>
</fieldset>

<c:if test="${fn:length(exportLinks) > 0}">
<fieldset>
<legend>data</legend>
<div class="accordion">
<c:forEach var="link" items="${exportLinks}">
<h3><a href="#">${link.label}</a></h3>
<div>
<ul>
<li><a href="${link.csvURL}">CSV</a></li>
<li><a href="${link.excelURL}">Excel</a></li>
</ul>
</div>
</c:forEach>
</div>
</fieldset>
</c:if>
</div>
