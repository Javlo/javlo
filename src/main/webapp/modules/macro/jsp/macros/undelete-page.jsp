<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><form action="${info.currentURL}">
<input type="hidden" name="webaction" value="undelete-page.action" />
<c:forEach var="page" items="${pages}">
<div class="row form-group">
	<div class="col-sm-1"><button class="btn btn-default" name="delete" value="${page.id}"><span class="glyphicon glyphicon-trash"></span></button></div>
	<div class="col-sm-4">${page.humanName}  (${page.info.label})</div>
	<div class="col-sm-2">${page.modificationDate}</div>
	<div class="col-sm-3">${page.creator}</div>
	<div class="col-sm-2"><button class="btn btn-primary btn-color" name="restore" value="${page.id}">restore</button></div>	
</div>
</c:forEach></form>
<form action="${info.currentURL}">
<input type="hidden" name="webaction" value="undelete-page.deleteAll" />
<button class="btn btn-primary btn-color confirm" name="restore" value="${page.id}" onclick="if (!confirm('Are you sure you want delete definitely all pages ?')) return false;">delete all pages</button>
</form>