<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%><%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>

<form target="paypal" action="${ecomService.URL}" method="post">
<input type="hidden" name="cmd" value="_xclick" />
<input type="hidden" name="currency_code" value="${basket.currencyCode}" />
<input type="image" src="https://www.sandbox.paypal.com/en_US/i/btn/btn_cart_LG.gif" border="0" name="submit" alt="">
<img alt="" border="0" src="https://www.sandbox.paypal.com/en_US/i/scr/pixel.gif" width="1" height="1">
</form>