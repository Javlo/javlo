<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">			
	<li class="${page eq '404'?'current':''}"><a href="${info.currentURL}?webaction=error.page404">404</a></li>
	<li class="${page eq 'forward'?'current':''}"><a href="${info.currentURL}?webaction=error.pageForward">forward</a></li>
</ul>
