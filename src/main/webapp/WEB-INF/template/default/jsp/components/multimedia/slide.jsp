<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><div class="multimedia slides">
	<c:forEach var="resource" items="${resources}" varStatus="status">
		<jv:changeFilter var="newURL" url="${image.url}" filter="preview" newFilter="slide" />
		<img alt="${resource.title}" src="${newURL}" />	
	</c:forEach>
</div>