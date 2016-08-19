<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<div class="redirection">
<form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Create redirection for all languages (replace language by #lg# in the url)</legend>
<input type="hidden" name="webaction" value="create-redirection-all-lg.create" />
<div class="line">
<label for="source">source</label>
<input class="btn btn-default" name="source" id="source" type="text" value="${param.source}" />
</div>
	<br />
<div class="line">
	<label for="target">target</label>
	<input class="btn btn-default" type="text" id="target" name="target" value="${param.target}" />
</div>
<div class="action">
	<input class="btn btn-primary pull-right" type="submit" value="create" />
</div>
</fieldset>
	<c:if test="${not empty redirect}">
	<br />
	<fieldset>
		<legend>Redirection file</legend>
		<textarea rows="10" class="btn btn-default">${redirect}</textarea>
	</fieldset>
	</c:if>

</form></div>