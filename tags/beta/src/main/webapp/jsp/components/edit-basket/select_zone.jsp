<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html"
        import="org.javlo.config.GlobalContext, org.javlo.I18nAccess" %><%

// TODO make i18n accessible as a bundle to avoid scriptlets
GlobalContext globalContext = GlobalContext.getInstance(request);
I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());

%><div class="ecom">
<form action="${actionURL}">
<input type="hidden" name="webaction" value="ecom.selectzone">
<div class="select_zone">
	<p><%= i18nAccess.getViewText("ecom.zone.select", "Select the region closest to yours") %></p>
    <select name="ecom_zone">
<c:forEach items="${zones}" var="zone">
		<option value="${zone.name}">${zone.label}</option>
</c:forEach>
		<option value="other">Other</option>
	</select>
	<input type="submit" value="Select" />
</div>

</form>
</div>
