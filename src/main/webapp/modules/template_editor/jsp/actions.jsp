<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="line">
	<label for="template">choose template</label>
	<select id="template" name="template">
	<c:forEach var="template" items="${templates}">
		<option>${template}</option>
	</c:forEach>
	</select>
</div>
<div class="clear">&nbsp;</div>


