package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.ImportHelper.ContentSelector;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
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
	
	protected void sendConfirmationEmail(ContentContext ctx, Basket basket) throws Exception {
		/** send email **/
		String subject = getData().getProperty("mail.subject");		
		if (subject == null) {
			subject = "Transaction confirmed : "+ctx.getGlobalContext().getContextKey();
		}
		
		String email = null;
		String mailingPage = getData().getProperty("mail.page");
		if (mailingPage != null) {
			MenuElement page = ContentService.getInstance(ctx.getGlobalContext()).getNavigation(ctx).searchChildFromName(mailingPage);			
			Map<String,String> params = new HashMap<String,String>();
			params.put("body", getConfirmationEmail(basket));
			params.put("total", basket.getTotalIncludingVATString());
			params.put("communication", basket.getStructutedCommunication());
			params.putAll(new ReadOnlyPropertiesMap(getData()));
			String pageURL = URLHelper.createURL(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.PAGE_MODE), page.getPath(), params);
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
		boolean isHTML = email != null;
		if (email == null) {
			email = getConfirmationEmail(basket);
		}
		NetHelper.sendMail(ctx.getGlobalContext(), from, to, null, bcc, subject, email, isHTML);
	}
	
	

}
