<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%>

<ul class="menu-file">
	<a href="${info.currentUrl}?webaction=changeRenderer&page=explorer&path=/static/file/legals" class="${fn:contains(currentModule.renderer, 'file.jsp')?'active':'unactive'}">
		<i class="bi bi-folder2-open"></i> ${i18n.edit['file.navigation.explorer']}
	</a>
	<a href="${info.currentUrl}?webaction=changeRenderer&page=meta" class="${fn:contains(currentModule.renderer, 'meta.jsp')?'active':'unactive'}">
		<i class="bi bi-card-checklist"></i> ${i18n.edit['file.navigation.meta']}
	</a>
</ul>

