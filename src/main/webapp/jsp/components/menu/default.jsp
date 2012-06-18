<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
&nbsp;
<c:if test="${fn:length(page.children) gt 0}">
<c:if test="${page.info.depth >= start}"> 
<ul class="menu">	
	<c:forEach var="page" items="${page.children}" varStatus="status">
		<c:if test="${page.info.visible}">
  				<c:if test="${page.info.depth <= end}">				
				<li class="depth-${page.info.depth} ${page.lastSelected ? "current" : "not-current" } ${page.name}">
					<a href="${page.url}" title="${page.info.title}" ><span>${page.info.label}</span></a>				
    				<c:set var="page" value="${page}" scope="request" />		 
		        	<jsp:include page="default.jsp"/>
		        </li>
		 	</c:if>	
	</c:if>				 			
	</c:forEach>
</ul>
</c:if>
<c:if test="${page.info.depth < start}">
	<c:forEach var="page" items="${page.children}" varStatus="status">
		<c:if test="${page.selected}">
			<c:set var="page" value="${page}" scope="request" />
			<jsp:include page="default.jsp"/>
		</c:if>
	</c:forEach>
</c:if>
</c:if>