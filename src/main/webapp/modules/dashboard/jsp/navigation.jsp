<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<ul class="navigation">			
	<li class="${page eq 'main'?'current':''}"><a href="${info.currentURL}?webaction=mainPage">main</a></li>	
	<c:if test="${info.admin}"><li class="${page eq 'tracker'?'current':''}"><a href="${info.currentURL}?webaction=trackerPage">tracker</a></li></c:if>
	<li class="${page eq 'report'?'current':''}"><a href="${info.currentURL}?webaction=reportPage">report</a></li>
	<li class="${page eq 'list'?'current':''}"><a href="${info.currentURL}?webaction=listPage">errors list</a></li>
	<li class="${page eq 'use'?'current':''}"><a href="${info.currentURL}?webaction=usePage">use</a></li>
</ul>