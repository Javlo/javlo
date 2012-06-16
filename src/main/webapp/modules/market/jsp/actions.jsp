<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:if test="${not empty viewAllButton}"><a class="action-button valid-all" href="${info.currentURL}?viewAll=true&list=${link.url}"><span>${i18n.edit['market.action.view-all']}</span></a></c:if>
<div class="clear">&nbsp;</div>