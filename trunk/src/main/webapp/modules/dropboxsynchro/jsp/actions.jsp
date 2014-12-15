<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="special">
<c:if test="${empty dropboxThread}">
<c:if test="${empty config || !config.valid}">
	<p>unvalid config.</p>
</c:if>
<c:if test="${not empty config && config.valid}">
<form id="dropbox-to-local-form" action="${info.currentURL}" method="post">
	<div class="form-group">
		<input type="hidden" name="webaction" value="toLocal" />
		<input class="btn btn-default action-button" type="submit" value="copy dropbox to local" /> 
	</div>
</form>
<form id="dropbox-to-local-form" action="${info.currentURL}" method="post">
	<div class="form-group">
		<input type="hidden" name="webaction" value="toDropbox" />
		<input class="btn btn-default action-button" type="submit" value="copy local to dropbox" /> 
	</div>
</form>
</c:if>
</c:if>
<c:if test="${not empty dropboxThread}">
	<p>running... (${dropboxThread.humanDownloadSize})</p>
</c:if>
</div>