<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="content editor">
<c:if test="${fileFound}">
<form action="${info.currentURL}" method="post">
	<input type="hidden" name="webaction" value="file.modify" />
	<input type="hidden" name="webaction" value="file.changeRenderer" />
	<input type="hidden" name="page" value="meta" />
	<input type="hidden" name="file" value="${editFile}" />
	<div class="form-group">
		<textarea id="text-editor" name="content" class="form-control" rows="35" cols="100" class="form-control">${content}</textarea>
	</div><div class="form-group">
		<button type="submit" class="btn btn-default pull-right">${i18n.edit['save']}</button>
	</div>
</form>
</c:if><c:if test="${!fileFound}">
<div class="alert alert-danger" role="alert">
  <span class="glyphicon glyphicon-exclamation-sign" aria-hidden="true"></span>
  <span class="sr-only">Error:</span>
  File not found.
</div>
</c:if>
</div>