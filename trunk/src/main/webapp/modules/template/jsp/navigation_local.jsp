<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
<c:forEach var="link" items="${templateContext.localNavigation}">	
<li ${templateContext.currentLink eq link.url?'class="current"':''}><a href="${info.currentURL}?webaction=changeRenderer&list=${link.url}">${link.label}</a></li>
</c:forEach>	
</ul>