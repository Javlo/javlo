<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<ul class="navigation">			
	<li class="${page eq '404'?'current':''}"><a href="${info.currentURL}?webaction=error.page404">404</a></li>
	<li class="${page eq 'forward'?'current':''}"><a href="${info.currentURL}?webaction=error.pageForward">forward</a></li>
</ul>
