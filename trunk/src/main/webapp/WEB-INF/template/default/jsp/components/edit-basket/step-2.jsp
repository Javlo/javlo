<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"
%><%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<form>
	<fieldset>
		<c:if test="${empty contentContext.currentUser}">
			<legend>Login</legend>
			<jsp:include page="/jsp/view/login.jsp" />
			<c:set var="registrationPage" value="${info.pageByName.registration}" />
			<c:if test="${not empty registrationPage}">
				<a href="${registrationPage.url}">${registrationPage.info.title}</a>
			</c:if>
		</c:if>
		<c:if test="${not empty contentContext.currentUser}">
			<c:set var="user" value="${contentContext.currentUser}" />
			<input type="hidden" name="webaction" value="basket.registration" />
			<div class="user">${user.name}</div>
			<div class="one_half">
			<div class="line">
				<label for="firstName">${i18n.view["field.firstname"]}</label>
				<input type="text" id="firstName" name="firstName" value="${user.userInfo.firstName}" />
			</div><div class="line">
				<label for="lastName">${i18n.view["field.lastname"]}</label>
				<input type="text" id="lastName" name="lastName" value="${user.userInfo.lastName}" />
			</div><div class="line">
				<label for="email">${i18n.view["field.email"]}</label>
				<input type="text" id="email" name="email" value="${user.userInfo.email}" />
			</div><div class="line">
				<label for="phone">${i18n.view["form.address.phone"]}</label>
				<input type="text" id="phone" name="phone" value="${user.userInfo.phone}" />
			</div>
			</div><div class="one_half last">
			<div class="line">
				<label for="country">${i18n.view["form.address.country"]}</label>
				<c:if test="${empty list.countries}">
					<input type="text" id="country" name="country" value="${user.userInfo.country}" />
				</c:if>
				 <c:if test="${not empty list.countries}">
				 	<select id="country" name="country">
				 		<option></option>
				 		<c:forEach var="country" items="${list.countries}">
				 			<option value="${country.key}"${user.userInfo.country == country.key?' selected="selected"':''}>${country.value}</option>
				 		</c:forEach>		 		
				 	</select>
				 </c:if>
			</div>
			<div class="line">
				<label for="address">${i18n.view["form.address.street"]}</label>
				<input type="text" id="address" name="address" value="${user.userInfo.address}" />
			</div><div class="line">
				<label for="zip">${i18n.view["form.address.zip"]}</label>
				<input type="text" id="zip" name="zip" value="${user.userInfo.postCode}" />
			</div><div class="line">
				<label for="city">${i18n.view["form.address.city"]}</label>
				<input type="text" id="city" name="city" value="${user.userInfo.city}" />
			</div>
			</div>
			<div class="action">			
				<input type="submit" value="next" />
			</div>
		</c:if>
	</fieldset>
</form>