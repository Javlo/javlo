<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">
<c:forEach var="link" items="${templateContext.localNavigation}">	
<c:url var="navURL" value="${info.currentURL}" context="/"><c:param name="webaction" value="changeRenderer" /><c:param name="list" value="${link.url}" /></c:url>
<li ${templateContext.currentLink eq link.url?'class="current"':''}><a href="${navURL}">${link.label}</a></li>
</c:forEach>	
</ul>