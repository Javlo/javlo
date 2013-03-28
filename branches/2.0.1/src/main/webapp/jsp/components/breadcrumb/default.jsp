<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<ul>
<li class="title">${value}</li>
<c:forEach var="page" items="${pages}">
	<li class="arrow">&#x00bb;</li>
    <li><a href="${page.url}">${page.info.label}</a></li>    
</c:forEach>
</ul>
