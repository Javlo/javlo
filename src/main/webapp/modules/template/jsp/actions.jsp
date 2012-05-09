<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty param.valid_all}"><a class="action-button valid-all" href="${info.currentURL}?webaction=validAll"><span>${i18n.edit['command.admin.template.all']}</span></a></c:if>

