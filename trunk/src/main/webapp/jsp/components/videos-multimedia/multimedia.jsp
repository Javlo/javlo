<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="multimedia">
<div class="ep_block">
<div class="ep_element1col">
<div class="ep_elementimg_left">

<c:forEach var="resource" items="${resources}" varStatus="status">
<c:if test="${status.count == 1}">
<a href="${resource.URL}?epbox&amp;gallery=${resource.relation}" title="${resource.title}" rel="${resource.relation}">
	<img alt="${resource.title}" src="${resource.previewURL}" />
	<span class="layer">&nbsp;</span>
</a>
</c:if>
</c:forEach>

<c:if test="${fn:length(resources) gt 1}">
<ul class="ep-hidden-images">
<c:forEach var="resource" items="${resources}" varStatus="status">
<c:if test="${status.count > 1}">
<li>
	<a href="${resource.URL}?epbox&amp;gallery=${resource.relation}" title="${resource.title}" rel="${resource.relation}">
		download
	</a>
</li>
</c:if>	
</c:forEach>
</ul>
</c:if>

</div>
</div>
</div>
</div>
