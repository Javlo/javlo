<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<form action="http://www.moneybookers.com/app/test_payment.pl" method="post">
<fieldset>
<legend>Money Bookers</legend>

<input type="hidden" name="pay_to_mail" value="pvandermaesen@immanence.be" />
<input type="hidden" name="language" value="${info.language}" />
<input type="hidden" name="transaction_id" value="${payementDetails.id}" />
<input type="hidden" name="amount" value="${payementDetails.amount}" />
<input type="hidden" name="currency" value="${payementDetails.currency}" />
<input type="hidden" name="return_url" value="${info.currentAbsoluteURL}?payement_status=ok" />
<input type="hidden" name="cancel_url" value="${info.currentAbsoluteURL}?payement_status=cancel" />

<input type="hidden" name="detail1_description" value="${payementDetails.description}" />
<input type="hidden" name="detail1_text" value="${payementDetails.text}" />

<div class="line">
</div>

<input type="submit" />
</fieldset>
</form>
