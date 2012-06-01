<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${empty templateFactory}"><a class="action-button valid-all" href="${info.currentURL}?webaction=validate"><span>${i18n.edit['command.admin.template.all']}</span></a></c:if>
<c:if test="${not empty templateFactory && empty param.viewAll}"><a class="action-button valid-all" href="${info.currentURL}?viewAll=true&list=${link.url}"><span>${i18n.edit['template.action.view-all']}</span></a></c:if>

