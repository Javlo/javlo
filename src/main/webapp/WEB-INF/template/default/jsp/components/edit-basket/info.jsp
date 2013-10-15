<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"
%><c:set var="basketPage" value="${info.pageByName.basket}" />
<c:if test="${not empty basketPage && empty param.basketLink}"><a href="${basketPage.url}"></c:if>
<h3>${i18n.view['ecom.basket']}</h3>
<c:if test="${basket.productCount > 0}">
<div class="total">
	<div class="line">
		<span class="value">${basket.productCount}</span>
		<span class="label">${i18n.view['ecom.product.total']}</span>		
	</div>
	<div class="line">
		<span class="label">${i18n.view['ecom.total_vat']}</span>
		<span class="value">${basket.totalIncludingVATString}</span>
	</div>
</div>
</c:if>
<c:if test="${basket.productCount == 0}">
<p>${i18n.view['ecom.basket-empty']}</p>
</c:if>
<c:if test="${not empty basketPage && empty param.basketLink}"><span class="link">${i18n.view['ecom.confirm']}</span></a></c:if>