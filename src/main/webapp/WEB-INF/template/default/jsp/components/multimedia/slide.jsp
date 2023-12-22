<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%@ taglib uri="/WEB-INF/javlo.tld" prefix="jv"%><div class="multimedia slides">
	<c:forEach var="resource" items="${resources}" varStatus="status">
		<jv:changeFilter var="newURL" url="${resource.previewURL}" filter="preview" newFilter="slide" />        
		<img alt="${resource.title}" src="${newURL}" />	
	</c:forEach>
</div>
