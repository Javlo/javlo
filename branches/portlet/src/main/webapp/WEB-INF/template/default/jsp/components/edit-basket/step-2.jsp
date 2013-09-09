<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<div class="total">
	<span class="label">${i18n.view['ecom.totalvta']}</span>
	<span class="value">${basket.totalIncludingVATString}</span>
</div>
<c:forEach var="service" items="${basket.services}">
<form class="service-${service.name}" id="validate-basket-form" action="${service.URL}" method="post">
	<div>
		<input type="hidden" name="webaction" value="basket.pay" />
		<input type="hidden" name="service" value="${service.name}" />
		<input type="submit" value="${i18n.view['ecom.pay']} : ${service.name}" /> 
	</div>
</form>
</c:forEach>