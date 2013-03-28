<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@page contentType="text/html"
        import="
	    org.javlo.ContentContext,
	    org.javlo.config.GlobalContext,
	    org.javlo.I18nAccess,
        org.javlo.helper.URLHelper,
        org.javlo.helper.StringHelper,
        org.javlo.component.AbstractVisualComponent,
        org.javlo.component.ecom.EditBasketComponent,
        org.javlo.ecom.Basket,
        org.javlo.ecom.DeliveryZone,
        org.javlo.service.RequestService"
%><%

GlobalContext globalContext = GlobalContext.getInstance(request);
I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, request.getSession());
ContentContext ctx = ContentContext.getContentContext(request, response);
RequestService requestService = RequestService.getInstance(request);
EditBasketComponent comp = (EditBasketComponent) AbstractVisualComponent.getRequestComponent (request);
Basket basket = Basket.getInstance(ctx);

boolean hasPickup = basket.getDeliveryZone() != null && basket.getDeliveryZone().getPickupURL() != null && basket.getDeliveryZone().getPickupURL().trim().length() > 0;
boolean hasDelivery = basket.getDeliveryZone() != null && basket.getDeliveryZone().getPrices() != null;

out.print("<div class=\"ecom-form\">");
out.print("<p>" + i18nAccess.getViewText("ecom.basket-info") + "</p>");

if (requestService.getParameter("webaction", "").equals("ecom.confirmbasket") || requestService.getParameter("webaction", "").equals("ecom.validbasket")) {
	out.print("<p class=\"error\"><span>" + i18nAccess.getViewText("ecom.basket-info-error") + "</span></p>");
}

out.println("<form class=\"confirm-basket\" id=\"basket-" + comp.getId() + " action=\"" + URLHelper.createURL(ctx, "/") + "\">");
out.println("	<input type=\"hidden\" name=\"webaction\" value=\"ecom.confirmbasket\">");
out.println("	<input type=\"hidden\" name=\"cid\" value=\""+ comp.getId() + "\">");

out.println("	<div class=\"line\">");
out.println("		<label for=\"ecom_zone\">" + i18nAccess.getViewText("ecom.zone.select", "Select the region closest to yours") + "</label>");
out.println("		<select id=\"ecom_zone\" name=\"ecom_zone\" onchange=\"this.form.webaction.value='ecom.selectzone';this.form.submit();\">");
for (DeliveryZone zone : basket.getDeliveryZones(ctx)) {
	if (zone.equals(basket.getDeliveryZone())) {
		out.println("			<option value=\"" + zone.getName() + "\" selected=\"selected\">" + zone.getLabel() + "</option>");
	} else {
		out.println("			<option value=\"" + zone.getName() + "\">" + zone.getLabel() + "</option>");
	}
}
out.println("		</select>");
out.println("	</div>");

out.println("	<div class=\"line\">");
out.println("	<label for=\"email\">"+i18nAccess.getViewText("ecom.email") + "</label>");
out.println("	<input type=\"text\" id=\"email\" name=\"email\" value=\"" + basket.getContactEmail() + "\" />");
out.println("	</div>");
out.println("	<div class=\"line\">");
out.println("	<label for=\"firstname\">" + i18nAccess.getViewText("ecom.firstname") + "</label>");
out.println("	<input type=\"text\" id=\"firstname\" name=\"firstname\" value=\"" + basket.getFirstName() + "\" />");
out.println("	</div>");
out.println("	<div class=\"line\">");
out.println("	<label for=\"lastname\">" + i18nAccess.getViewText("ecom.lastname") + "</label>");
out.println("	<input type=\"text\" id=\"lastname\" name=\"lastname\" value=\"" + basket.getLastName() + "\" />");
out.println("	</div>");
out.println("	<div class=\"line\">");
out.println("	<label for=\"phone\">" + i18nAccess.getViewText("ecom.phone") + "</label>");
out.println("	<input type=\"text\" id=\"phone\" name=\"phone\" value=\"" + basket.getContactPhone() + "\" />");
out.println("	</div>");
/*
out.println("	<div class=\"line\">");
out.println("	<label for=\"\">" + i18nAccess.getViewText("ecom.organization") + "</label>");
out.println("	<input type=\"text\" name=\"organization\" value=\"" + basket.getOrganization() + "\" />");
out.println("	</div>");
out.println("	<div class=\"line\">");
out.println("	<label for=\"\">"+i18nAccess.getViewText("ecom.vatnumber") + "</label>");
out.println("	<input type=\"text\" name=\"vatnumber\" value=\"" + basket.getVATNumber() + "\" />");
out.println("	</div>");
 */
 if (!basket.isPickup()) {
	out.println("	<div id=\"ecom_address_bloc\" class=\"line area\">");
	out.println("	<label for=\"address\">" + i18nAccess.getViewText("ecom.address") + "</label>");
	out.println("	<textarea id=\"address\" name=\"address\">" + basket.getAddress() + "</textarea>");
	out.println("	</div>");
}
if (hasPickup) {
	out.println("	<div class=\"line radio\">");
	out.println("	<input type=\"radio\" id=\"pickup-false\" name=\"pickup\" value=\"false\" checked=\"checked\" onclick=\"this.form.webaction.value='ecom.selectpickup';this.form.submit();\"");
	if (!basket.isPickup()) {
		out.print(" checked=\"checked\"");
	}
	out.println(" />");
	out.println("	<label for=\"pickup-false\">" + i18nAccess.getViewText("ecom.delivery.choose") + "</label>");
	out.println("	</div>");
	out.println("	<div class=\"line radio\">");
	out.print("	<input type=\"radio\" id=\"pickup-true\" name=\"pickup\" value=\"true\" onclick=\"this.form.webaction.value='ecom.selectpickup';this.form.submit();\"");
	if (basket.isPickup()) {
		out.print(" checked=\"checked\"");
	}
	out.println(" />");
	out.println("	<label for=\"pickup-true\">" + i18nAccess.getViewText("ecom.pickup.choose") + "</label>");
	out.println("	</div>");
}
out.println("	<div class=\"footer\">");

if (comp.isReception()) {
	out.println("<input type=\"hidden\" name=\"message\" value=\"" + i18nAccess.getViewText("ecom.reception-message") + "\">");
	out.println("<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.reception") + "\">");
}
if (comp.isPayPal() && hasDelivery) {
	out.println("<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.paypal") + " (+3%)\" onclick=\"this.form.webaction.value='ecom.paypal';this.form.submit()\">");
}

/*out.println("	<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.confirm-basket") + "\" />");*/
out.println("	</div>");
out.println("</form>");
out.println("</div>");

%>