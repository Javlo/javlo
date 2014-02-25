<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty templateFactory && empty param.templateid && empty param.previewEdit}"><a class="action-button valid-all" href="${info.currentURL}?webaction=validate"><span>${i18n.edit['command.admin.template.all']}</span></a></c:if>
<c:url var="inheritedURL" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="selectTemplate"></c:param>
	<c:if test="${not empty param.previewEdit}">
		<c:param name="previewEdit" value="${param.previewEdit}"></c:param>
	</c:if>
</c:url> 
<c:if test="${not empty param.previewEdit}"><a class="action-button valid-all${empty info.page.templateId?' active':''}" href="${inheritedURL}"><span>${i18n.edit['global.inherited']}</span></a></c:if>
<c:if test="${not empty templateFactory && empty param.viewAll}"><a class="action-button valid-all" href="${info.currentURL}?viewAll=true&list=${link.url}"><span>${i18n.edit['template.action.view-all']}</span></a></c:if>
<c:if test="${not empty param.templateid}">
   <a class="action-button more" href="${fileURL}&templateid=${currentTemplate.name}"><span>${i18n.edit['template.action.browse']}...</span></a>
   <a class="action-button more" href="${info.currentURL}?filter=&templateid=${currentTemplate.name}"><span>${i18n.edit['template.action.filter-image']}</span></a>
   <c:if test="${fn:length(currentTemplate.CSS)>0}">
       <a class="action-button more" href="${info.currentURL}?css=${currentTemplate.CSS[0]}&templateid=${currentTemplate.name}&webaction=editCSS"><span>${i18n.edit['template.action.css']}</span></a>	
   </c:if>
   <c:if test="${fn:length(currentTemplate.htmls)>0}">
   	<a class="action-button more" href="${info.currentURL}?html=${currentTemplate.htmls[0]}&templateid=${currentTemplate.name}&webaction=editHTML"><span>${i18n.edit['template.action.html']}</span></a>
   </c:if>    
   
   <c:url var="commit" value="${info.currentURL}" context="/">	
       <c:param name="webaction" value="commit"></c:param>
       <c:param name="templateid" value="${currentTemplate.name}"></c:param>       
       <c:if test="${not empty param.css}">
           <c:param name="css" value="${param.css}"></c:param>       	
           <c:param name="webaction" value="editCSS"></c:param>
       </c:if>
       <c:if test="${not empty param.html}">
       		<c:param name="html" value="${param.html}"></c:param>
       		<c:param name="webaction" value="editHTML"></c:param>
       </c:if>
       <c:if test="${not empty param.file}">
       		<c:param name="file" value="${param.file}"></c:param>
       </c:if>
       <c:if test="${not empty param.filter}">
       		<c:param name="filter" value="${param.filter}"></c:param>
       </c:if>
   </c:url>
   
   <a class="action-button" href="${commit}"><span>${i18n.edit['template.action.commit']}</span></a>
   
   <c:url var="commitChildren" value="${info.currentURL}" context="/">	
       <c:param name="webaction" value="commitChildren"></c:param>
       <c:param name="templateid" value="${currentTemplate.name}"></c:param>       
       <c:if test="${not empty param.css}">
           <c:param name="css" value="${param.css}"></c:param>       	
           <c:param name="webaction" value="editCSS"></c:param>
       </c:if>
       <c:if test="${not empty param.html}">
       		<c:param name="html" value="${param.html}"></c:param>
       		<c:param name="webaction" value="editHTML"></c:param>
       </c:if>
       <c:if test="${not empty param.file}">
       		<c:param name="file" value="${param.file}"></c:param>
       </c:if>
       <c:if test="${not empty param.filter}">
       		<c:param name="filter" value="${param.filter}"></c:param>
       </c:if>
   </c:url>
   <a class="action-button" href="${commitChildren}"><span>${i18n.edit['template.action.commit-children']}</span></a>   
</c:if>
<c:if test="${empty param.templateid and empty nobrowse}">
<a href="${info.absoluteURLPrefix}${currentModule.path}/jsp/upload.jsp" class="popup cboxElement action-button"><span>${i18n.edit['action.add-template']}</span></a>
<a class="action-button more" href="${fileURL}"><span>${i18n.edit['template.action.browse']}...</span></a>
</c:if>

<div class="clear">&nbsp;</div>


