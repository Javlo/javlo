package org.javlo.component.ecom;

import jakarta.mail.internet.InternetAddress;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.Product;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.ReadOnlyPropertiesMap;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractOrderComponent extends AbstractVisualComponent {

	@Override
	protected void init() throws ResourceNotFoundException {
		super.init();
		if (getValue().isEmpty()) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("button=");
			out.println("mail.from=");
			out.println("mail.bcc=");
			out.println("mail.subject=");
			out.println("mail.body=");
			out.println("mail.signature=");
			out.println("order.title=");
			out.println("order.account=");
			out.println("order.total=");
			out.println("order.communication=");
			out.close();
			setValue(new String(outStream.toByteArray()));
		}
	}

	protected Properties getData() {
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(getValue()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop;
	}

	@Override
	public String getSpecificClass(ContentContext ctx) {
		return "order";
	}

	protected String getConfirmationEmail(ContentContext ctx, Basket basket) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(getData().get("mail.body"));
		out.println("");
		out.println(getData().get("order.title"));
		if (getData().get("order.account") != null && getData().get("order.account").toString().trim().length() > 0) {
			out.println(getData().get("order.account"));
		}
		out.println("");
		try {
			out.println(getData().get("order.total") + basket.getTotalString(ctx, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (getData().get("order.communication") != null && getData().get("order.communication").toString().trim().length() > 0) {
			out.println(getData().get("order.communication") + basket.getStructutedCommunication());
		}
		out.println("");
		out.println(getData().get("mail.signature"));
		out.println("");
		out.close();
		return new String(outStream.toByteArray());
	}

	protected static String renderBasket(ContentContext ctx, Basket basket) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);

		String cellStyle = "text-align: left; font-size: 1rem;";

		out.println("<table class=\"table\" id=\"basket-details\" style=\"width: 100%\">");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.product") + "</th>");
		out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.quantity") + "</th>");
		out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.total_evat") + "</th>");
		
		boolean tva = false;
		for (Product product : basket.getProducts()) {
			if (product.getVAT()>0.01) {
				tva = true;
			}
		}
		
		
		if (tva) {
			out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.total_vat") + "</th>");
		}
		out.println("</tr>");
		out.println("</thead>");
		for (Product product : basket.getProducts()) {
			out.println("<tr>");
			//out.println("<td style=\"" + cellStyle + "\"><a href=\"" + product.getUrl() + "\">" + product.getName() + "</a></td>");
			out.println("<td style=\"" + cellStyle + "\">" + product.getName() + "</td>");
			out.println("<td style=\"" + cellStyle + "\">" + product.getQuantity() + "</td>");
			double total = product.getPrice() * product.getQuantity();
			out.println("<td style=\"" + cellStyle + "\">" + Basket.renderPrice(ctx, total / (1 + product.getVAT()), product.getCurrencyCode()) + "</td>");
			if (tva) {
				out.println("<td style=\"" + cellStyle + "\">" + Basket.renderPrice(ctx, total, product.getCurrencyCode()) + "</td>");
			}
			out.println("</tr>");
		}
		out.println("<tr><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>"+(tva?"<td>&nbsp;</td>":"")+"</tr>");
		if (basket.getUserReduction() > 0.01) {
			out.println("<tr><td>&nbsp;</td>"+(tva?"<td>&nbsp;</td>":""));
			out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.promo") + "</th>");
			out.println("<td style=\"" + cellStyle + "\">" + StringHelper.renderDoubleAsPercentage(basket.getUserReduction()) + "</td>");
			out.println("</tr>");
		}
		if (basket.getDelivery(ctx, true) > 0.01) {
			out.println("<tr><td>&nbsp;</td>"+(tva?"<td>&nbsp;</td>":""));
			out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.shipping") + "</th>");
			out.println("<td style=\"" + cellStyle + "\">" + Basket.renderPrice(ctx, basket.getDelivery(ctx, true), basket.getCurrencyCode()) + "</td>");
			out.println("</tr>");
		}
		out.println("<tr><td>&nbsp;</td>"+(tva?"<td>&nbsp;</td>":""));
		out.println("<th style=\"" + cellStyle + "\">" + i18nAccess.getViewText("ecom.total") + "</th>");
		out.println("<td style=\"" + cellStyle + "\">" + Basket.renderPrice(ctx, basket.getTotal(ctx, true), basket.getCurrencyCode()) + "</td>");
		out.println("</tr>");
		out.println("</table>");

		out.close();
		return new String(outStream.toByteArray());
	}

	protected void sendConfirmationEmail(ContentContext ctx, Basket basket) throws Exception {
		/** send email **/
		String subject = getData().getProperty("mail.subject");
		if (subject == null) {
			subject = "Transaction confirmed : " + ctx.getGlobalContext().getContextKey();
		}

		String email = null;
		String mailingPage = getData().getProperty("mail.page");
		String pageURL = "error:no link.";
		if (mailingPage != null) {
			MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromName(mailingPage);
			if (page == null) {
				logger.warning("page not found : " + mailingPage);
			}
			Map<String, String> params = new HashMap<String, String>();
			params.put("body", getConfirmationEmail(ctx, basket));
			params.put("total", basket.getTotalString(ctx, true));
			params.put("totalHVAT", basket.getTotalString(ctx, false));
			params.put("VAT", basket.getVAT(ctx));
			params.put("userReduction", StringHelper.renderDoubleAsPercentage(basket.getUserReduction()).replace("%", "&#37;"));
			params.put("communication", basket.getStructutedCommunication());
			params.put("firstName", StringHelper.toHTMLAttribute(basket.getFirstName()));
			params.put("lastName", StringHelper.toHTMLAttribute(basket.getLastName()));
			params.put("basketSize", "" + basket.getSize());
			params.put("basketId", "" + basket.getId());
			params.put("invoiceHash", basket.getInvoiceHash());

			/** customer **/
			params.put("customerFirstName", StringHelper.toHTMLAttribute(basket.getCustomerFirstName()));
			params.put("customerLastName", StringHelper.toHTMLAttribute(basket.getCustomerLastName()));
			params.put("customerEmail", StringHelper.toHTMLAttribute(basket.getCustomerEmail()));
			params.put("customerPhone", StringHelper.toHTMLAttribute(basket.getCustomerPhone()));

			params.put("deliveryDate", StringHelper.renderDate(basket.getDeliveryDate()));
			if (basket.getDeliveryDate() != null) {
				params.put("deliveryDay", StringHelper.renderDay(basket.getDeliveryDate(), ctx.getRequestContentLanguage()));
			}
			params.put("deliveryTime", StringHelper.renderTime(basket.getDeliveryDate()));
			params.put("deliveryInstructions", basket.getDeliveryInstructions());

			params.put("giftMessage", StringHelper.toHTMLAttribute(basket.getGiftMessage()));
			params.put("giftSender", StringHelper.toHTMLAttribute(basket.getGiftSender()));
			params.put("giftReceiver", StringHelper.toHTMLAttribute(basket.getGiftReceiver()));

			String basketTable = URLEncoder.encode(renderBasket(ctx, basket), ContentContext.CHARACTER_ENCODING);
			params.put("basketTable", basketTable);
			params.put("address", StringHelper.toHTMLAttribute(basket.getAddress()));
			params.put("city", StringHelper.toHTMLAttribute(basket.getCity()));
			params.put("zip", basket.getZip());
			params.put("postcode", basket.getZip());
			params.put("phone", basket.getContactPhone());
			params.put("country", StringHelper.toHTMLAttribute(new Locale(ctx.getRequestContentLanguage(), basket.getCountry()).getDisplayCountry(ctx.getLocale())));
			params.put("currencyCode", "" + basket.getCurrencyCode());
			if (basket.getOrganization() != null && basket.getOrganization().trim().length() > 0) {
				params.put("organization", basket.getOrganization());
			}

			double delivery = basket.getDelivery(ctx, true);
			params.put("delivery", Basket.renderPrice(ctx, delivery, basket.getCurrencyCode()));
			double deliveryHVAT = basket.getDelivery(ctx, false);
			params.put("deliveryHVAT", Basket.renderPrice(ctx, deliveryHVAT, basket.getCurrencyCode()));

			if (basket.getVATNumber() != null && basket.getVATNumber().trim().length() > 0) {
				params.put("vat", basket.getVATNumber());
			}

			if (page != null) {
				try {
					params.putAll(new ReadOnlyPropertiesMap(getData()));
					pageURL = URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PAGE_MODE), page.getPath(), params);
					logger.info("read page for mail : "+pageURL);
					email = NetHelper.readPageForMailing(new URL(pageURL));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		InternetAddress bcc = null;
		String bccString = getData().getProperty("mail.bcc");
		if (bccString != null && StringHelper.isMail(bccString)) {
			bcc = new InternetAddress(bccString);
		}
		// InternetAddress from;
		// String fromString = getData().getProperty("mail.from");
		// if( fromString != null && StringHelper.isMail(fromString)) {
		// from = new InternetAddress(fromString);
		// } else {
		// from = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
		// }
		InternetAddress from = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
		InternetAddress to = new InternetAddress(basket.getRealEmail());
		if (email == null) {
			email = getConfirmationEmail(ctx, basket);
			NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, null, false);
		} else {
			NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, getConfirmationEmail(ctx, basket), true);
		}

	}

	public static void main(String[] args) throws Exception {
		String html = renderBasket(null, new Basket());
		ResourceHelper.writeStringToFile(new File("c:/trans/out.html"), html);
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

}
