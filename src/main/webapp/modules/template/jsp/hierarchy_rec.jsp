<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>
<ul>
<c:forEach var="template" items="${htemps}">
	<c:url value="${info.currentURL}" var="editURL" context="/">
      	<c:param name="webaction" value="template.goEditTemplate" />    
      	<c:param name="templateid" value="${template.template.name}" />   	
    </c:url>
    <c:url value="${info.currentURL}" var="selectURL" context="/">
      	<c:param name="webaction" value="template.changeRenderer" />
      	<c:param name="list" value="hierarchy" />
       	<c:param name="templateh" value="${template.template.name}" />
    </c:url>  
	<li class="open">
	<c:if test="${fn:length(template.children)>0}"><a class="open" href="#" onclick="var grp = jQuery(this).parent(); if (grp.hasClass('open')) {grp.addClass('close'); jQuery(this).removeClass('open'); grp.removeClass('open');} else {grp.addClass('open'); grp.removeClass('close'); jQuery(this).addClass('open');}"><span class="glyphicon glyphicon-triangle-bottom open-icon"></span><span class="glyphicon glyphicon-triangle-right close-icon"></span></a></c:if>
	<a href="${editURL}"><span class="glyphicon glyphicon-pencil"></span></a> <a href="${selectURL}">${template.template.name}</a>
	<c:if test="${fn:length(template.children)>0}">
		<c:set var="htemps" value="${template.children}" scope="request" />
		<jsp:include page="hierarchy_rec.jsp" />				
	</c:if></li>
</c:forEach>
</ul>