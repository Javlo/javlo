<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<c:if test="${not empty param.component}">
<c:url var="addFileUrl" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="components.createfile" />
	<c:param name="component" value="${param.component}" />
</c:url>
<c:url var="deleteFileUrl" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="components.deletefile" />
	<c:param name="component" value="${param.component}" />
</c:url>
<c:url var="commitTemplate" value="${info.currentURL}" context="/">
	<c:param name="webaction" value="template.commit" />
	<c:param name="templateid" value="${info.template.id}" />
	<c:param name="module" value="components" />
	<c:param name="component" value="${param.component}" />
</c:url>

<c:if test="${!cssExist}"><a class="action-button more" href="${addFileUrl}&type=css"><span><i class="bi bi-plus-circle"></i> CSS</span></a></c:if>
<c:if test="${cssExist}"><a class="action-button more needconfirm" href="${deleteFileUrl}&type=css"><span><i class="bi bi-trash"></i> CSS</span></a></c:if>
<c:if test="${!cssExist}"><a class="action-button more" href="${addFileUrl}&type=scss"><span><i class="bi bi-plus-circle"></i> SCSS</span></a></c:if>
<c:if test="${!propertiesExist}"><a class="action-button more" href="${addFileUrl}&type=properties"><span><i class="bi bi-plus-circle"></i> properties</span></a></c:if>
<c:if test="${propertiesExist}"><a class="action-button more" href="${deleteFileUrl}&type=properties"><span><i class="bi bi-trash"></i> properties</span></a></c:if>
<a class="action-button" href="${commitTemplate}"><span><i class="fa fa-upload" aria-hidden="true"></i> commit</span></a>

</c:if>

<div class="clear">&nbsp;</div>
