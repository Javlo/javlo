<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<div class="registration">
	<form action="${info.currentURL}" method="post">
		<div class="line">
			<input type="hidden" name="webaction" value="gform-registering.submit" />
			<input type="hidden" name="comp_id" value="${comp.id}" />
			
			<select type="text" name="rolesRaw">
				<option value="all">All</option>
				<c:forEach var="role" items="${info.roles}">
				<c:set var="key" value="country.${role}" />
				<option value="${role}">${i18n.view[key]}</option>
				</c:forEach>
			</select>
		</div>
		<input type="text" name="email" />
		<input type="submit" value="ok" />
	</form>
</div>