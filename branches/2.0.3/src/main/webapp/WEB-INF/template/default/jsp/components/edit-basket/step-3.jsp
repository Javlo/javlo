<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<form method="post" action="${info.currentURL}">
<fieldset>	
	<legend>${i18n.view["ecom.delivery-address"]}</legend>
	<div class="one_half">
	<div class="line">
		<label for="firstName">${i18n.view["field.firstname"]}</label>
		<span class="value">${basket.firstName}</span>
	</div><div class="line">
		<label for="lastName">${i18n.view["field.lastname"]}</label>
		<span class="value">${basket.lastName}</span>
	</div><div class="line"><c:if test="${comp.style == 'company'}"><div class="line">
				<label for="organization">${i18n.view['form.company']}</label>
				<span class="value">${basket.organization}</span> 
			</div>
			<div class="line">
				<label for="vat">${i18n.view['form.vat']}</label>
				<span class="value">${basket.VATNumber}</span> 
			</div></c:if>
		<label for="email">${i18n.view["field.email"]}</label>
		<span class="value">${basket.contactEmail}</span>
	</div><c:if test="${comp.style == 'company'}">
			</div><div class="one_half last">
	</c:if><div class="line">
		<label for="phone">${i18n.view["form.address.phone"]}</label>
		<span class="value">${basket.contactPhone}</span>
	</div>
	<c:if test="${comp.style != 'company'}">
		</div><div class="one_half last">
	</c:if>
	<div class="line">
		<label for="country">${i18n.view["form.address.country"]}</label>
		<span class="value">${basket.country}</span>
	</div>
	<div class="line">
		<label for="address">${i18n.view["form.address.street"]}</label>
		<span class="value">${basket.address}</span>
	</div><div class="line">
		<label for="zip">${i18n.view["form.address.zip"]}</label>
		<span class="value">${basket.zip}</span>
	</div><div class="line">
		<label for="city">${i18n.view["form.address.city"]}</label>
		<span class="value">${basket.city}</span>
	</div>
	<div class="action">
		<input type="hidden" name="webaction" value="basket.back" />
		<input type="submit" value="${i18n.view["global.back"]}" />
	</div>
	</div>
</fieldset>
</form>

