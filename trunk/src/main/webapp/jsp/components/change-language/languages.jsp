<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<ul class="languages">
<c:forEach var="lg" items="${languagesList}">
    <li><a href="${lg.url}">${lg.language} - ${lg.label} (${lg.translatedLabel})</a></li>    
</c:forEach>
</ul>