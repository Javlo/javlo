/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.ecom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.EcomActions;
import org.javlo.ecom.Product;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class EditBasketComponent extends AbstractPropertiesComponent {

	public void renderBasket(ContentContext ctx, PrintStream out, boolean interactive) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		Basket basket = Basket.getInstance(ctx);

		List<Product> products = basket.getProducts();
		if (products.size() == 0) {
			out.print("<p class=\"message\">");
			out.print(i18nAccess.getViewText("ecom.basket-empty"));
			out.println("</p>");
		} else {
			boolean hasReduction = false;
			for (Product product : products) {
				if (product.getQuantity() < 1) {
					basket.removeProduct(product.getId());
				} else {
					hasReduction = hasReduction || (product.getReduction() > 0);
				}
			}
			out.println("<table>");
			out.println("<tr>");
			out.println("<th>&nbsp;</th>");
			out.println("<th class=\"name\">" + i18nAccess.getViewText("ecom.name") + "</th>");
			out.println("<th class=\"price\">" + i18nAccess.getViewText("ecom.price") + "</th>");
			out.println("<th class=\"quantity\">" + i18nAccess.getViewText("ecom.quantity") + "</th>");
			if (hasReduction) {
				out.println("<th class=\"reduction\">" + i18nAccess.getViewText("ecom.reduction") + "</th>");
			}
//			out.println("<th class=\"total-evat\">" + i18nAccess.getViewText("ecom.total_evat") + "</th>");
//			out.println("<th class=\"vat\">" + i18nAccess.getViewText("ecom.vat") + "</th>");
			out.println("<th class=\"total-vat\" colspan=\"2\">" + i18nAccess.getViewText("ecom.total_vat") + "</th>");
			out.println("</tr>");
			int c = 0;
			double total = 0;
			for (Product product : products) {
				c++;
				if (c % 2 == 0) {
					out.println("<tr class=\"odd\">");
				} else {
					out.println("<tr class=\"even\">");
				}
				out.println("<td class=\"icone\">");
				if ((product.getImage() != null) && (product.getImage().getImageURL(ctx) != null)) {
					ctx.setAbsoluteURL(true);
					String url = URLHelper.createTransformURL(ctx, getPage(), product.getImage().getImageURL(ctx), "basket");
					out.println("<img src=\"" + url + "\" alt=\"" + product.getImage().getImageDescription(ctx) + "\" />");
				} else {
					out.println("&nbsp;");
				}
				out.println("</td>");
				out.println("<td class=\"name\">");
				out.println("<a href=\"" + product.getUrl() + "\"><span class=\"description\">" + product.getShortDescription() + "&nbsp;</span><span class=\"code\">" + product.getName() + "</span></a>");
				out.println("</td>");
				out.println("<td class=\"price\">");
				out.println(StringHelper.renderDouble(product.getPrice(), 2) + " " + basket.getCurrencyCode());
				out.println("</td>");
				out.println("<td class=\"quantity\">");
				out.println(product.getQuantity());
				out.println("</td>");
				if (hasReduction) {
					out.println("<td class=\"reduction\">");
					out.println(StringHelper.renderDoubleAsPercentage(product.getReduction()));
					out.println("</td>");
				}
//				out.println("<td class=\"total-evat\">");
//				out.println(StringHelper.renderDouble(product.getPrice() * product.getQuantity() * (1 - product.getReduction()) / (1 + product.getVAT()), 2)
//						+ " " + product.getCurrencyCode());
//				
//				out.println("</td>");
//				out.println("<td class=\"vat\">");
//				out.println(StringHelper.renderDoubleAsPercentage(product.getVAT()));
//				out.println("</td>");
				out.println("<td class=\"total-vat\">");
				double produtTotal = product.getPrice() * product.getQuantity() * (1 - product.getReduction());
				out.println(StringHelper.renderDouble(produtTotal, 2) + " " + product.getCurrencyCode());
				total = total + produtTotal;
				out.println("</td>");

				out.println("<td class=\"delete\">");
				if (interactive) {
					out.println("<form id=\"basket-" + getId() + " action=\"" + URLHelper.createURL(ctx) + "\">");
					out.println("<input type=\"hidden\" name=\"webaction\" value=\"ecom.deletebasket\">");
					out.println("<input type=\"hidden\" name=\"id\" value=\"" + product.getId() + "\">");
					out.println("<input type=\"submit\" name=\"valid-basket\" class=\"action\" value=\"" + i18nAccess.getViewText("ecom.delete-basket") + "\">");
					out.println("</form>");
				}
				out.println("</td>");
				out.println("</tr>");
			}

			double delivery = basket.getDeliveryIncludingVAT();
			if (delivery > 0) {
				out.println("<tr>");
				out.println("<td>&nbsp;</td>");
				out.println("<td>" + i18nAccess.getViewText("ecom.delivery") + "</td>");
				out.println("<td colspan=\"2\">&nbsp;</td>");
//				out.println("<td class=\"total-evat\">");
//				out.println(StringHelper.renderDouble(basket.getDeliveryExcludingVAT(), 2) + " " + basket.getCurrencyCode());
//				out.println("</td>");
//				out.println("<td class=\"vat\">");
//				out.println(StringHelper.renderDoubleAsPercentage(0.21));
//				out.println("</td>");

				out.println("<td colspan=\"2\">" + StringHelper.renderDouble(delivery, 2) + " " + basket.getCurrencyCode() + "</td>");
				out.println("</tr>");
			}
			total = total + delivery;

			out.println("<tr>");
			out.println("<th>&nbsp;</th>");
			out.println("<th colspan=\"5\">" + i18nAccess.getViewText("ecom.total_vat") + "</th>");
			out.println("<th colspan=\"2\">" + StringHelper.renderDouble(total, 2) + " " + basket.getCurrencyCode() + "</th>");
			out.println("</tr><tr>");
			out.println("<td colspan=\"9\" class=\"action\">");
			if (interactive) {
				if (isReception()) {
					out.println("<form id=\"basket-" + getId() + " action=\"" + URLHelper.createURL(ctx) + "\">");
					out.println("<input type=\"hidden\" name=\"webaction\" value=\"ecom.validbasket\">");
					out.println("<input type=\"hidden\" name=\"cid\" value=\""+getId()+"\">");
					out.println("<input type=\"hidden\" name=\"message\" value=\"" + i18nAccess.getViewText("ecom.reception-message") + "\">");
					out.println("<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.reception") + "\">");
					out.println("</form>");
				}
				if (isPayPal()) {
					if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
						out.println("<form action=\"https://www.paypal.com/cgi-bin/webscr\" method=\"post\">");
						out.println("<input type=\"hidden\" name=\"business\" value=\"" + getBusiness() + "\">");
					} else { // TODO: test values for business and PDT in edit
						out.println("<form action=\"https://www.sandbox.paypal.com/cgi-bin/webscr\" method=\"post\">");
						out.println("<input type=\"hidden\" name=\"business\" value=\"info@volpaiole.com\">");
					}
					out.println("<input type=\"hidden\" name=\"cmd\" value=\"_xclick\">");

//					out.println("<input type=\"hidden\" name=\"bn\" value=\"FuoriMondo_BuyNow_WPS_BE\">");
					out.println("<input type=\"hidden\" name=\"no_shipping\" value=\"1\">");
					out.println("<input type=\"hidden\" name=\"no_note\" value=\"1\">");

					out.println("<input type=\"hidden\" name=\"lc\" value=\"" + ctx.getLanguage() + "\">");
//					out.println("<input type=\"hidden\" name=\"charset\" value=\"" + ContentContext.CHARACTER_ENCODING + "\">");
					ContentContext tmpCtx = new ContentContext(ctx);
					tmpCtx.setAbsoluteURL(true);
					
					// TODO: parameterize or relative path, hard-coded for volpaiole
					out.println("<input type=\"hidden\" name=\"return\" value=\"" + URLHelper.createURL(tmpCtx, "/ecom/checkout/paypal-ok") + "\">");
					out.println("<input type=\"hidden\" name=\"cancel_return\" value=\"" + URLHelper.createURL(tmpCtx, "/ecom/checkout/reserve-ok") + "\">");
					out.println("<input type=\"hidden\" name=\"rm\" value=\"2\">");

					// ecom.basket-title=panier de ##website## avec
					// ##product_count## articles.

					GlobalContext globalCtx = GlobalContext.getInstance(ctx.getRequest());
					String[][] params = new String[][] { { "website", globalCtx.getGlobalTitle() }, { "product_count", "" + basket.getProductCount() } };
					String basketName = i18nAccess.getViewText("ecom.basket-title", params);

					out.println("<input type=\"hidden\" name=\"item_name\" value=\"" + basketName + "\">");
					out.println("<input type=\"hidden\" name=\"item_number\" value=\"" + basket.getId() + "\">");

					String amout = ("" + StringHelper.renderDouble(basket.getTotalExcludingVAT(), 2, '.'));
					out.println("<input type=\"hidden\" name=\"amount\" value=\"" + amout + "\">");
					String tax = ("" + (StringHelper.renderDouble(basket.getTotalIncludingVAT() - basket.getTotalExcludingVAT(), 2, '.')));
					out.println("<input type=\"hidden\" name=\"tax\" value=\"" + tax + "\">");
					String fee = ("" + StringHelper.renderDouble(basket.getTotalIncludingVAT() * 0.03, 2, '.'));
					out.println("<input type=\"hidden\" name=\"handling\" value=\"" + fee + "\">");

					out.println("<input type=\"hidden\" name=\"currency_code\" value=\"" + basket.getCurrencyCode() + "\">");
					out.println("<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.paypal") + " (+3%)\">");
					out.println("</form>");
				}
			}
			out.println("</td>");
			out.println("</tr>");
			out.println("</table>");
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String aTag = null;
		String paypalTX = requestService.getParameter("tx", null);
		if (!isDisplayOnly() && paypalTX != null) {
			logger.log(Level.INFO, "####### PAYPAL TX: " + paypalTX);
			
			URL pdtURL;
			String pdtParam = "&at=";
			if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				pdtURL = new URL("https://www.paypal.com/cgi-bin/webscr");
				pdtParam = pdtParam + getPDT();
			} else {
				pdtURL = new URL("https://www.sandbox.paypal.com/cgi-bin/webscr");
				
				// TODO: add test pdt in edit itf, hard-coded for volpaiole
				pdtParam = pdtParam + "b3xbzyPIcAzqnJ8_CHicSb3Uy15i4qo9ERkXl3-WuZfWS1G4xAmmf_GjbcK";
			}
			HttpsURLConnection conn = (HttpsURLConnection) pdtURL.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			
			String query = "cmd=_notify-synch" + pdtParam + "&tx=" + paypalTX;
			conn.getOutputStream().write(query.getBytes(ContentContext.CHARACTER_ENCODING));
			conn.getOutputStream().close();
			
			InputStream in = conn.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in, ContentContext.CHARACTER_ENCODING));
			String line = br.readLine();
			StringBuilder sb = new StringBuilder();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			in.close();
			String resp = sb.toString().toLowerCase();
			
			if (resp.contains("success")) {
				EcomActions.performValidbasket(ctx.getRequest(), ctx.getResponse(), true, paypalTX);
				aTag = "<p class=\"message\">payment succeeded</p>";
			} else if (resp.contains("fail")) {
				basket.setConfirm(false);
				aTag = "<p class=\"error\">payment failed</p>";
//				aTag = aTag + "<a href=\"" + URLHelper.createURL(ctx, "/ecom/checkout/reserve-ok") + "\">See the procedure to confirm your order...</a>";
			}
		}
		
		ctx.getRequest().setAttribute("basket", basket);

		String clazz = isDisplayOnly() ? "basket display-only" : "basket";
		out.println("<div class=\"" + clazz + "\">");

		if (isDisplayOnly()) {
			return executeJSP(ctx, "basket_small.jsp");

//		} else if (basket.getDeliveryZone() == null && !basket.getDeliveryZones(ctx).isEmpty()) {
//			ctx.getRequest().setAttribute("zones", basket.getDeliveryZones(ctx));
//			renderBasket(ctx, out, false);
//			return executeJSP(ctx, "select_zone.jsp");

		} else if (!basket.isConfirm()) {
			if (basket.getDeliveryZone() == null && !basket.getDeliveryZones(ctx).isEmpty()) {
				basket.setDeliveryZone(basket.getDeliveryZones(ctx).get(0));
			}
			return executeJSP(ctx, "delivery.jsp");

		} else if (basket.isValid()) {
			out.print("<div class=\"message\">");
			String msg = requestService.getParameter("message", i18nAccess.getViewText("ecom.basket-confirmed"));
			out.print("<p>" + msg + "</p>");
			if (aTag != null) {
				out.println(aTag);
			}
			if (!StringUtils.isEmpty(basket.getAddress())) {
				out.print("<p>" + i18nAccess.getViewText("ecom.delivery-confirm") + "</p>");
				out.print("<p>" + basket.getAddress() + "</p>");
			} else if (basket.getDeliveryZone() != null && basket.getDeliveryZone().getPickupURL() != null) {
				out.println("<p>" + i18nAccess.getViewText("ecom.pickup-confirm") + "</p>");
			}
			out.println("<form id=\"basket-" + getId() + " action=\"" + URLHelper.createURL(ctx, "/") + "\">");
			out.println("	<input type=\"hidden\" name=\"webaction\" value=\"ecom.initbasket\">");
			out.println("	<input type=\"submit\" name=\"valid-basket\" value=\"" + i18nAccess.getViewText("ecom.init-basket") + "\">");
			out.println("</form>");
			out.println("</div>");

			renderBasket(ctx, out, false);
		} else {
			if (aTag != null) {
				out.println(aTag);
			}
			renderBasket(ctx, out, true);
		}
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getType() {
		return "edit-basket";
	}

	protected String getPayPalInputName() {
		return "paypal_" + getId();
	}

	protected String getBusinessInputName() {
		return "business_" + getId();
	}

	protected String getReceptionInputName() {
		return "reception_" + getId();
	}

	protected String getPDTInputName() {
		return "pdt_" + getId();
	}

	protected String getEMailInputName() {
		return "email_" + getId();
	}

	protected String getDisplayInputName() {
		return "display_" + getId();
	}

	protected boolean isDisplayOnly() {
		return getCheckedString().contains("display");
	}

	public boolean isPayPal() {
		return getCheckedString().contains("paypal");
	}

	public boolean isReception() {
		return getCheckedString().contains("reception");
	}

	protected String getCheckedString() {
		return properties.getProperty("__special_value", "");
	}

	protected String getPDT() {
		return properties.getProperty("pdt", "");
	}

	protected void setPDT(String pdt) {
		properties.setProperty("pdt", pdt);
	}

	protected String getDisplayType() {
		return getFieldValue("display", "");
	}

	protected void setDisplayType(String displayType) {
		setFieldValue("display", displayType);
	}

	public String getEmail() {
		return properties.getProperty("email", "");
	}

	protected void setEmail(String email) {
		properties.setProperty("email", email);
	}

	public String getBusiness() {
		return properties.getProperty("business", "");
	}

	protected void setBusiness(String business) {
		properties.setProperty("business", business);
	}

	protected void setCheckedString(String value) {
		properties.setProperty("__special_value", value);
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);

		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getEMailInputName() + "\"> " + i18nAccess.getText("ecom.notification-email") + " : </label>");
		out.println("<input type=\"text\" name=\"" + getEMailInputName() + "\" value=\"" + getEmail() + "\" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getDisplayInputName() + "\"> " + i18nAccess.getText("ecom.display-only") + " : </label>");
		out.println(XHTMLHelper.getCheckbox(getDisplayInputName(), isDisplayOnly()));
		out.println("</div>");
		out.println("&nbsp;");

		out.println("<fieldset><legend>");
		out.println(XHTMLHelper.getCheckbox(getPayPalInputName(), isPayPal()));
		out.println("<label for=\"" + getPayPalInputName() + "\"> paypal</label>");
		out.println("</legend>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getPDTInputName() + "\"> PDT : </label>");
		out.println("<input type=\"text\" name=\"" + getPDTInputName() + "\" value=\"" + getPDT() + "\" />");
		out.println("</div>");
		out.println("<div class=\"line\">");
		out.println("<label for=\"" + getBusinessInputName() + "\"> business : </label>");
		out.println("<input type=\"text\" name=\"" + getBusinessInputName() + "\" value=\"" + getBusiness() + "\" />");
		out.println("</div>");

		out.println("</fieldset>&nbsp;");

		out.println("<fieldset><legend>");
		out.println(XHTMLHelper.getCheckbox(getReceptionInputName(), isReception()));
		out.println("<label for=\"" + getReceptionInputName() + "\"> " + i18nAccess.getText("ecom.reception") + "</label>");
		out.println("</legend>");
		out.println("</fieldset>&nbsp;");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getHexColor() {
		return ECOM_COLOR;
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String newValue = "";
		if (requestService.getParameter(getPayPalInputName(), null) != null) {
			newValue = newValue + "paypal;";
			if (!isPayPal()) {
				setModify();
			}
		} else {
			if (isPayPal()) {
				setModify();
			}
		}
		if (requestService.getParameter(getReceptionInputName(), null) != null) {
			newValue = newValue + "reception;";
			if (!isReception()) {
				setModify();
			}
		} else {
			if (isReception()) {
				setModify();
			}
		}
		if (requestService.getParameter(getDisplayInputName(), null) != null) {
			newValue = newValue + "display;";
			if (!isDisplayOnly()) {
				setModify();
			}
		} else {
			if (isDisplayOnly()) {
				setModify();
			}
		}

		String pdt = requestService.getParameter(getPDTInputName(), "");
		if (!pdt.equals(getPDT())) {
			setModify();
			setPDT(pdt);
		}

		String email = requestService.getParameter(getEMailInputName(), "");
		if (!email.equals(getEmail())) {
			setModify();
			setEmail(email);
		}

		String business = requestService.getParameter(getBusinessInputName(), "");
		if (!business.equals(getBusiness())) {
			setModify();
			setBusiness(business);
		}

		setCheckedString(newValue);
		storeProperties();
	}

	@Override
	public List<String> getFields(ContentContext ctx) {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getHeader() {
		return "basket component";
	}

}
