<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<h2>use</h2>
<table class="table">
<tr>
	<th>#publish</th>
	<th>#save</th>
</tr>
<c:forEach var="day" items="${dayInfos}">
	<tr>
		<td>${day.publishCount}</td>
		<td>${day.saveCount}</td>
	</tr>
</c:forEach>
</table>