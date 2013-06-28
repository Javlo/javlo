<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:if test="${not empty basket}">
<div class="basket">
<h3>${i18n.view['ecom.basket']}</h3>
<span class="label">${i18n.view['ecom.quantity']}</span> : ${basket.productCount}
</div> 
</c:if>