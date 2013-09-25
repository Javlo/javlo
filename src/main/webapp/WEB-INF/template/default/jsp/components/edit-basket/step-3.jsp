<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<div class="total">
	<span class="label">${i18n.view['ecom.totalvta']}</span>
	<span class="value">${basket.totalIncludingVATString}</span>
</div>
