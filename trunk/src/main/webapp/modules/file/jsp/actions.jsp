<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${empty param.name}"><a class="action-button save" href="#save" onclick="jQuery('#form-meta').submit(); return false;"><span>${i18n.edit['action.update']}</span></a></c:if>
<c:if test="${not empty param.name}"><a class="action-button" href="${info.currentURL}?webaction=template.commit&webaction=browse&name=${param.name}&from-module=template"><span>${i18n.edit['template.action.commit']}</span></a></c:if>

