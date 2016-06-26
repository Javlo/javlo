<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">			
	<li class="${page eq 'main'?'current':''}"><a href="${info.currentURL}?webaction=mainPage">log</a></li>	
	<li class="${page eq 'performance'?'current':''}"><a href="${info.currentURL}?webaction=performancePage">performance</a></li>	
</ul>