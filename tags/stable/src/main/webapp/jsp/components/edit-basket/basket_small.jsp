<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:if test="${not empty basket.products}">
<div class="small">
	<div class="products">
	<c:forEach items="${basket.products}" var="product">
		<div class="product">
			<a href="${product.url}" title="${product.name}"><span class="name">${product.name}</span></a>
			<div class="product-image">
				<img src="${product.imageURL}" alt="${product.shortDescription}" />
			</div>
			<div class="product-info">
				<span class="quantity">${product.quantity} x ${product.price * (1 - product.reduction)}&nbsp;${product.currencyCode}</span>
				<span class="amount">Total: ${product.quantity * product.price * (1 - product.reduction)}&nbsp;${product.currencyCode}</span>
			</div>
		<c:if test="${not basket.valid}">
			<div class="product-delete">
				<form action="">
					<input type="hidden" name="webaction" value="ecom.deletebasket">
					<input type="hidden" name="id" value="${product.id}">
					<input type="submit" class="action" value=" " />
				</form>
			</div>
		</c:if>
		</div>
	</c:forEach>
	</div>
	<div class="basket-summary">
	<c:if test="${not empty basket.deliveryZone}">
		<span class="zone">Zone: ${basket.deliveryZone.name}</span>
		<span class="delivery">Delivery: ${basket.deliveryIncludingVAT}&nbsp;${basket.currencyCode}</span>
	</c:if>
		<span class="total">TOTAL: ${basket.totalIncludingVAT}&nbsp;${basket.currencyCode}</span>
	</div>
</div>
</c:if>
