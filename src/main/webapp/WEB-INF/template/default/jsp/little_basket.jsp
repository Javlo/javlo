<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:if test="${not empty basket}">
<div class="basket">
<h3>${i18n.view['ecom.basket']}</h3>
<span class="label">${i18n.view['ecom.quantity']}</span> : ${basket.productCount}
</div> 
</c:if>