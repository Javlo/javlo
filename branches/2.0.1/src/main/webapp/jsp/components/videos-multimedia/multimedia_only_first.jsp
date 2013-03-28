<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<div class="multimedia">
<div class="ep_block">
<div class="ep_element1col">
<div class="ep_elementimg_left">

<c:forEach var="resource" items="${resources}" varStatus="status">
status.count = ${status.count}
<c:if test="${status.count == 1}">
<a href="${resource.URL}?epbox&gallery=${resource.relation}" title="${resource.title}" rel="${resource.relation}">
	<img alt="${resource.title}" src="${resource.previewURL}" />
</a>
</c:if>
</c:forEach>

<ul>
<c:forEach var="resource" items="${resources}" varStatus="status">
<c:if test="${status.count > 1}">
<li>
	<a href="${resource.URL}?epbox&gallery=${resource.relation}" title="${resource.title}" rel="${resource.relation}">
		<img alt="${resource.title}" src="${resource.previewURL}" />
	</a>
</li>
</c:if>	
</c:forEach>
</ul>
</div>
</div>
</div>
</div>
