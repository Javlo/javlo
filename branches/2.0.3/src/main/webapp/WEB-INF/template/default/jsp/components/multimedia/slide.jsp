<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><div class="multimedia slides">
	<c:forEach var="resource" items="${resources}" varStatus="status">
		<img alt="${resource.title}" src="${fn:replace(resource.previewURL,'/preview/','/slide/')}" />	
	</c:forEach>
</div>