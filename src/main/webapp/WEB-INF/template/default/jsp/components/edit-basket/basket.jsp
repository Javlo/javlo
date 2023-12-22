<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%@ taglib uri="jakarta.tags.fmt" prefix="fmt"
%><div class="basket">
<c:if test="${not empty messages.globalMessage && messages.globalMessage.type > 0}">
<div class="message ${messages.globalMessage.typeLabel}"><span>${messages.globalMessage.message}</span></div>
</c:if>
<c:if test="${empty reset}">
<div class="step step-${basket.step}">
<jsp:include page="step-${basket.step}.jsp" />
</div>
<c:if test="${not empty shippingMessage}"><p class="step-${basket.step} shipping-message">${shippingMessage}</p></c:if>
</c:if>
</div>