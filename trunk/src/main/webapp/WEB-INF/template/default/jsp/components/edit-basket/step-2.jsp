<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<form>
	<fieldset>
		<legend>Registration</legend>
		<input type="hidden" name="webaction" value="basket.registration" />
		<div class="line">
			<label for="name">${i18n.view["field.firstname"]}</label>
			<input type="text" name="firstName" value="${user.firstname}" />
		</div><div class="line">
			<label for="name">${i18n.view["field.lastname"]}</label>
			<input type="text" name="lastName" value="${user.lastname}" />
		</div><div class="line">
			<label for="name">${i18n.view["form.address.city"]}</label>
			<input type="text" name="country" value="${user.country}" />
		</div><div class="line">
			<label for="address">${i18n.view["form.address.street"]}</label>
			<input type="text" name="address" value="${user.address}" />
		</div><div class="line">
			<label for="zip">${i18n.view["form.address.zip"]}</label>
			<input type="text" name="zip" value="${user.zip}" />
		</div><div class="line">
			<label for="name">${i18n.view["form.address.city"]}</label>
			<input type="text" name="city" value="${user.city}" />
		</div><div class="line">
			<label for="phone">${i18n.view["form.address.phone"]}</label>
			<input type="text" name="phone" value="${user.phone}" />
		</div>
		<div class="actions">			
			<input type="submit" value="next" />
		</div>
	</fieldset>
</form>