<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty templateFactory && empty param.templateid && empty param.previewEdit}"><a class="action-button valid-all" href="${info.currentURL}?webaction=validate"><span>${i18n.edit['command.admin.template.all']}</span></a></c:if>
<c:if test="${not empty param.previewEdit}"><a class="action-button valid-all" href="${info.currentURL}?webaction=selectTemplate"><span>${i18n.edit['global.inherited']}</span></a></c:if>
<c:if test="${not empty templateFactory && empty param.viewAll}"><a class="action-button valid-all" href="${info.currentURL}?viewAll=true&list=${link.url}"><span>${i18n.edit['template.action.view-all']}</span></a></c:if>
<c:if test="${not empty param.templateid}">
   <a class="action-button more" href="${fileURL}&templateid=${currentTemplate.name}"><span>${i18n.edit['template.action.browse']}...</span></a>
   <a class="action-button more" href="${info.currentURL}?filter=&templateid=${currentTemplate.name}"><span>${i18n.edit['template.action.filter-image']}</span></a>
   <c:if test="${fn:length(currentTemplate.CSS)>0}">
       <a class="action-button more" href="${info.currentURL}?css=${currentTemplate.CSS[0]}&templateid=${currentTemplate.name}&webaction=editCSS"><span>${i18n.edit['template.action.css']}</span></a>	
   </c:if> 
   <c:set var="css" value="&css=${param.css}&webaction=editCSS" />
   <a class="action-button" href="${info.currentURL}?webaction=commit&templateid=${currentTemplate.name}${not empty param.css?css:''}"><span>${i18n.edit['template.action.commit']}</span></a>
   <a class="action-button" href="${info.currentURL}?webaction=commitChildren&templateid=${currentTemplate.name}${not empty param.css?css:''}"><span>${i18n.edit['template.action.commit-children']}</span></a>   
</c:if>
<c:if test="${empty param.templateid and empty nobrowse}">
<a class="action-button more" href="${fileURL}"><span>${i18n.edit['template.action.browse']}...</span></a>
</c:if>

<div class="clear">&nbsp;</div>


