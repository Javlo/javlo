<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><form method="post" action="${info.currentURL}" class="standard-form">
<fieldset>
<legend>Change image filter</legend>
<input type="hidden" name="webaction" value="change-image-filter.change" />
<select name="filter">
	<option></option>
	<c:forEach var="filter" items="${filters}"><option>${filter}</option></c:forEach>
</select>
<div class="action">
	<input class="btn btn-primary pull-right" type="submit" value="change" />
</div>
</fieldset>
</form>


