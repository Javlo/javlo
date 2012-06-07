<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
<c:forEach var="link" items="${moduleContext.navigation}">	
<li ${moduleContext.currentLink eq link.name?'class="current"':''}><a href="${info.currentURL}${link.url}">${link.label}</a></li>
</c:forEach>	
</ul>