<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">			
	<li class="${page eq 'main'?'current':''}"><a href="${info.currentURL}?webaction=mainPage">main</a></li>	
	<li class="${page eq 'tracker'?'current':''}"><a href="${info.currentURL}?webaction=trackerPage">tracker</a></li>
	<li class="${page eq 'report'?'current':''}"><a href="${info.currentURL}?webaction=reportPage">report</a></li>
</ul>