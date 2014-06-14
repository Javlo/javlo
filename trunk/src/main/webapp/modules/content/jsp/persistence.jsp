<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><ul class="navigation">
	<li><a href="${download}">${i18n.edit['edit.link.download']}</a></li>	
	<li><a href="${downloadAll}">${i18n.edit['edit.link.download-static']}</a></li>
	<li><a class="popup" href="${currentModule.path}/jsp/upload.jsp?editTemplateURL=${info.editTemplateURL}&currentURL=${info.currentURL}" title="${i18n.edit['edit.action.upload']}"><span>${i18n.edit['edit.action.upload']}</span></a></li>		
</ul>
