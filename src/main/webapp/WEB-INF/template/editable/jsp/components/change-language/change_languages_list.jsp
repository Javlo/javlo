<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" 
%><ul class="content-language-selector">
<c:forEach var="lg" items="${languagesList}">
	<li class="${lg.language}"><a href="${lg.url}" hreflang="${lg.language}">${lg.language}</a></li>
</c:forEach></ul>
