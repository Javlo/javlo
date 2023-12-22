<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<ul class="navigation">			
	<li class="${page eq 'main'?'current':''}"><a href="${info.currentURL}?webaction=mainPage">taxonomy</a></li>	
	<li class="${page eq 'text'?'current':''}"><a href="${info.currentURL}?webaction=text">text version</a></li>
</ul>