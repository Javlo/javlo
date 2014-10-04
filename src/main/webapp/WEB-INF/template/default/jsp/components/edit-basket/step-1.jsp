<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<c:if test="${fn:length(basket.products) > 0}">
<table>
<thead>
	<tr>
		<td class="name">${i18n.view["ecom.name"]}</td>
		<td class="price">${i18n.view["ecom.price"]}</td>
		<td class="quantity">${i18n.view["ecom.quantity"]}</td>		
		<c:if test="${reduction}"><td class="reduction">${i18n.view["ecom.reduction"]}</td></c:if>		
		<td class="total-vat">${i18n.view["ecom.total"]}</td>
	</tr>
</thead>
<c:forEach var="product" items="${basket.products}">
<tr>
		<td class="name">${product.name}</td>
		<td class="price">${product.priceString}</td>
		<td class="quantity">${product.quantity}</td>			
		<c:if test="${reduction}"><td class="reduction">${product.reductionString}</td></c:if>		
		<td class="total-vat">${product.totalString}</td>
</tr>
</c:forEach>

<tfoot>
<c:if test="${not empty deliveryStr}">
	<tr>
		<th colspan="${reduction?'4':'3'}">${i18n.view['ecom.shipping']}</th>
		<td>${deliveryStr}</td>
	</tr>
</c:if>
	<tr>
		<th colspan="${reduction?'4':'3'}">${i18n.view['ecom.total_evat']}</th>
		<td>${totalNoVAT}</td>
	</tr>
	<tr>
		<th colspan="${reduction?'4':'3'}">${i18n.view['ecom.total_vat']}</th>
		<td>${total}</td>
	</tr>
</tfoot>

</table>

<form id="validate-basket-form" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="basket.confirm" />
		<input type="submit" value="${i18n.view['ecom.confirm-basket']}" /> 
	</div>
</form>

<form id="reset-basket-form" action="${info.currentURL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="basket.reset" />
		<input type="submit" value="${i18n.view['ecom.reset-basket']}" /> 
	</div>
</form>

</c:if>
<c:if test="${fn:length(basket.products) == 0}">
<p>${i18n.view['ecom.basket-empty']}</p>
</c:if>