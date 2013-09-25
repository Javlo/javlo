package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Properties;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.PaypalConnector;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class PaypalOrderComponent extends AbstractOrderComponent implements IAction {

	@Override
	public String getType() {
		return "paypal-order";
	}
	
	@Override
	protected void init() throws ResourceNotFoundException { 
		super.init();
		if (getValue().isEmpty()) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);			
			out.println("button=Paypal");
			out.println("url=https://api.sandbox.paypal.com");
			out.println("user=AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca");
			out.println("password=EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3");
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
	
	protected String getBaseURL() {
		return getData().getProperty("url");
	}
	
	protected String getUser() {
		String user = getData().getProperty("user");		
		return user;
	}
	
	protected String getPassword() {
		return getData().getProperty("password");
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		if (basket.getStep() != Basket.ORDER_STEP) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form class=\"paypal\" action\""+URLHelper.createURL(ctx)+"\">");
		out.println("<fieldset>");		
		out.println("<input type=\"hidden\" name=\""+IContentVisualComponent.COMP_ID_REQUEST_PARAM+"\" value=\""+getId()+"\" />");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"paypal.pay\" />");
		out.println("<input type=\"submit\" value=\""+getData().getProperty("button")+"\" />");
		out.println("</fieldset>");
		out.println("</form>");
		out.close();
		return new String(outStream.toByteArray());	
	}
	
	public static String performPay(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		
		PaypalOrderComponent comp = (PaypalOrderComponent)ComponentHelper.getComponentFromRequest(ctx);
		
		Basket basket = Basket.getInstance(ctx);
		
		String returnURL = URLHelper.createURL(ctx.getContextForAbsoluteURL());
		returnURL = URLHelper.addParam(returnURL, IContentVisualComponent.COMP_ID_REQUEST_PARAM, comp.getId());		
		returnURL = URLHelper.addParam(returnURL, "basket", basket.getId());
		URL validReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.valid"));
		URL cancelReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.cancel"));
		
		PaypalConnector connector = new PaypalConnector(comp.getBaseURL(), comp.getUser(), comp.getPassword());
		String url = connector.createPayment(basket.getTotalIncludingVAT(), basket.getCurrencyCode(), ctx.getGlobalContext().getGlobalTitle(),validReturnURL,cancelReturnURL);
		
		NetHelper.sendRedirectTemporarily(ctx.getResponse(), url);

		return null;
	}
	
	public static String performValid(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		PaypalOrderComponent comp = (PaypalOrderComponent)ComponentHelper.getComponentFromRequest(ctx);
		System.out.println("***** PaypalOrderComponent.performValid : VALID"); //TODO: remove debug trace
		PaypalConnector connector = new PaypalConnector(comp.getBaseURL(), comp.getUser(), comp.getPassword());
		connector.executePayment(rs.getParameter("token", null), rs.getParameter("PayerID", null));
		return null;
	}
	
	public static String performCancel(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		System.out.println("***** PaypalOrderComponent.performCancel : CANCEL"); //TODO: remove debug trace
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "paypal";
	}

}

