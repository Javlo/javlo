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
		<a class="popup" href="${info.absoluteLocalURLPrefix}${currentModule.path}/jsp/upload.jsp?editTemplateURL=${info.editTemplateURL}&currentURL=${info.currentURL}" title="${i18n.edit['edit.action.upload']}"><span>${i18n.edit['edit.action.upload']}</span></a>
	</li>		
</ul>
</fieldset>
<fieldset>
<legend>${i18n.edit['persistence.title.page-import']}</legend>
<form action="${info.currentURL}" method="post" enctype="multipart/form-data">
<label for="static-url">${i18n.edit['persistence.title.static-import']}</label>
<div class="row"><div class="col-sm-10">
<div class="form-group">	
	<input type="hidden" name="webaction" value="persistence.importPage" />
	<input class="form-control" id="static-url" type="text" name="import-url" />	
</div>
</div><div class="col-sm-2">
<input class="btn btn-default btn-xs pull-right" type="submit" value="${i18n.edit['global.ok']}" />
</div></div>
<label for="static-url">${i18n.edit['persistence.title.static-import-file']}</label>
<div class="row"><div class="col-sm-10">
<div class="form-group">	
	<input class="form-control" id="static-file" type="file" name="import-file" />	
</div>
</div><div class="col-sm-2">
<input type="submit" class="btn btn-default btn-xs pull-right" value="${i18n.edit['global.ok']}" />
</div></div>
</form>

<h4>${i18n.edit['persistence.title.download-page']}</h4>
<div class="form-group">
<a href="${info.currentAbsoluteURLXML}" class="btn btn-default btn-xs">XML</a>
<a href="${info.currentAbsoluteURLZIP}" class="btn btn-default btn-xs">ZIP</a>
</div>
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
