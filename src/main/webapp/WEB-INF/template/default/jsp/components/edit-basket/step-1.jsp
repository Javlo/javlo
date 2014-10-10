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
		<c:set var="colspan" value="3" />	
		<c:if test="${reduction}"><c:set var="colspan" value="${colspan+1}" /><td class="reduction">${i18n.view["ecom.reduction"]}</td></c:if>		
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
<c:if test="${not empty param.nobutton && not empty userReduction}">
<div class="line">
<th colspan="${colspan}">${i18n.view["ecom.promo"]}</td>
<td>${userReduction}</td>
</div>
</c:if>
<c:if test="${not empty deliveryStr}">
	<tr>
		<th colspan="${colspan}">${i18n.view['ecom.shipping']}</th>
		<td>${deliveryStr}</td>
	</tr>
</c:if>
	<tr>
		<th colspan="${colspan}">${i18n.view['ecom.total_evat']}</th>
		<td>${totalNoVAT}</td>
	</tr>
	<tr>
		<th colspan="${colspan}">${i18n.view['ecom.total_vat']}</th>
		<td>${total}</td>
	</tr>
</tfoot>

</table>

<c:if test="${empty param.nobutton}">
<form id="validate-basket-form" action="${info.currentURL}" method="post">
	<c:if test="${promo}">
	<div class="line">
		<label for="promo-code"><span>${i18n.view['ecom.promo-code']}</span></label>
		<div class="input"><input id="promo-code" name="promo-code" /></div>
	</div>
	</c:if>
	<div class="line">
		<input type="hidden" name="comp-id" value="${compid}" />
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

</c:if>
<c:if test="${fn:length(basket.products) == 0}">
<p>${i18n.view['ecom.basket-empty']}</p>
</c:if>