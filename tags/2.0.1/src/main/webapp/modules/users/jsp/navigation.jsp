<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<ul class="navigation">	
	<c:forEach var="mode" items="${userContext.allModes}">	
		<c:set var="i18nKey" value="user.mode.${mode}" />
		<li class="${userContext.mode eq mode?'current':''}"><a href="${info.currentURL}?webaction=ChangeMode&mode=${mode}">${i18n.edit[i18nKey]}</a></li>
	</c:forEach>
</ul>