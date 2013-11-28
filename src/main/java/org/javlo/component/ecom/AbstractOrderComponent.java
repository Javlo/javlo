package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.Product;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.ReadOnlyPropertiesMap;

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
	public String getSpecificClass() {
		return "order";
	}
	
	protected String getConfirmationEmail(Basket basket) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println(getData().get("mail.body"));
		out.println("");
		out.println(getData().get("order.title"));
		if (getData().get("order.account") != null && getData().get("order.account").toString().trim().length() > 0) {
			out.println(getData().get("order.account"));		
		}
		out.println("");
		out.println(getData().get("order.total")+basket.getTotalIncludingVATString());
		if (getData().get("order.communication") != null && getData().get("order.communication").toString().trim().length() > 0) {
			out.println(getData().get("order.communication")+basket.getStructutedCommunication());
		}
		out.println("");
		out.println(getData().get("mail.signature"));		
		out.println("");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	protected static String renderBasket(ContentContext ctx, Basket basket) throws FileNotFoundException, IOException {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		
		out.println("<table id=\"basket-details\">");
		out.println("<thead>");
		out.println("<tr>");
		out.println("<td>"+i18nAccess.getViewText("ecom.product")+"</td>");
		out.println("<td>"+i18nAccess.getViewText("ecom.quantity")+"</td>");
		out.println("<td>"+i18nAccess.getViewText("ecom.total_evat")+"</td>");
		out.println("<td>"+i18nAccess.getViewText("ecom.total_vat")+"</td>");
		out.println("</tr>");
		out.println("</thead>");		
		
		for (Product product : basket.getProducts()) {
			out.println("<tr>");
			out.println("<td>"+product.getName()+"</td>");
			out.println("<td>"+product.getQuantity()+"</td>");
			double total = product.getPrice()*product.getQuantity();
			out.println("<td>"+Basket.renderPrice(ctx,total-(total*product.getVAT()),product.getCurrencyCode())+"</td>");
			out.println("<td>"+Basket.renderPrice(ctx,total,product.getCurrencyCode())+"</td>");			
			out.println("</tr>");
		}
		
		out.println("</table>");
		
		out.close();
		return new String(outStream.toByteArray());
	}
	
	protected void sendConfirmationEmail(ContentContext ctx, Basket basket) throws Exception {
		/** send email **/
		String subject = getData().getProperty("mail.subject");		
		if (subject == null) {
			subject = "Transaction confirmed : "+ctx.getGlobalContext().getContextKey();
		}
		
		String email = null;
		String mailingPage = getData().getProperty("mail.page");
		String pageURL="error:no link.";
		if (mailingPage != null) {
			MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromName(mailingPage);	
			if (page == null) {
				logger.warning("page not found : "+mailingPage);
			}
			Map<String,String> params = new HashMap<String,String>();
			params.put("body", getConfirmationEmail(basket));
			params.put("total", basket.getTotalIncludingVATString());
			params.put("communication", basket.getStructutedCommunication());
			params.put("firstName", StringHelper.toHTMLAttribute(basket.getFirstName()));
			params.put("lastName", StringHelper.toHTMLAttribute(basket.getLastName()));
			params.put("basketSize", ""+basket.getSize());
			params.put("basketId", ""+basket.getId());
			String basketTable = URLEncoder.encode(renderBasket(ctx, basket), ContentContext.CHARACTER_ENCODING);			
			params.put("basketTable", basketTable);
			params.put("address", StringHelper.toHTMLAttribute(basket.getAddress()));
			params.put("city", StringHelper.toHTMLAttribute(basket.getCity()));
			params.put("zip", basket.getZip());
			params.put("country", StringHelper.toHTMLAttribute(new Locale(ctx.getRequestContentLanguage(), basket.getCountry()).getDisplayCountry(ctx.getLocale())));
			params.put("currencyCode", ""+basket.getCurrencyCode());
			if (basket.getOrganization() != null && basket.getOrganization().trim().length() > 0) {
				params.put("organization",basket.getOrganization());
			}
			
			double delivery = basket.getDeliveryIncludingVAT();			
			params.put("delivery", Basket.renderPrice(ctx, delivery, basket.getCurrencyCode()));
			
			if (basket.getVATNumber() != null && basket.getVATNumber().trim().length() > 0) {
				params.put("vat",basket.getVATNumber());
			}
			params.putAll(new ReadOnlyPropertiesMap(getData()));
			pageURL = URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PAGE_MODE), page.getPath(), params);
			try {
				email = NetHelper.readPage(new URL(pageURL));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		InternetAddress bcc = null;
		String bccString = getData().getProperty("mail.bcc");
		if(bccString  != null && StringHelper.isMail(bccString)) {
			bcc = new InternetAddress(getData().getProperty("mail.bcc"));
		}
		InternetAddress from;
		String fromString = getData().getProperty("mail.from");
		if( fromString != null && StringHelper.isMail(fromString)) {
			from = new InternetAddress(fromString);
		} else {
			from = new InternetAddress(ctx.getGlobalContext().getAdministratorEmail());
		}	
		InternetAddress to = new InternetAddress(basket.getContactEmail());		
		if (email == null) {
			email = getConfirmationEmail(basket);
			NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, null, false);
		} else {					
			NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, getConfirmationEmail(basket), true);
		}
		
	}
	
	

}
