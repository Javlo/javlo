<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<div class="basket">
<table>
<thead>
	<tr>
		<td class="name">${i18n.view["ecom.name"]}</td>
		<td class="price">${i18n.view["ecom.price"]}</td>
		<td class="quantity">${i18n.view["ecom.quantity"]}</td>		
		<td class="reduction">${i18n.view["ecom.reduction"]}</td>
		<td class="total-vat">${i18n.view["ecom.total_vat"]}</td>
	</tr>
</thead>
<c:forEach var="product" items="${basket.products}">
<tr>
		<td class="name">${product.name}</td>
		<td class="price"><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="${basket.currencyCode}"/> </td>
		<td class="quantity">${product.quantity}</td>			
		<td class="reduction"><fmt:formatNumber value="${product.reduction}" type="precent" /></td>
		<td class="total-vat"><fmt:formatNumber value="${prodct.total}" type="currency" currencySymbol="${basket.currencyCode}"/></td>
</tr>
</c:forEach>
</table>
</div>