<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="petition-links">
<ul>
<c:forEach var="link" items="${links}">
	<li>
		<a href="${link.link}">${link.label} (${link.count})</a>
	</li>
</c:forEach>
</ul>
</div>