<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<ul class="languages">
<c:forEach var="lg" items="${languagesList}">
    <li class="${lg.language}"><a href="${lg.url}">${lg.language}<span class="details"> - ${lg.label} (${lg.translatedLabel})</span></a></li>    
</c:forEach>
</ul>