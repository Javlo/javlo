package org.javlo.component.ecom;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang.StringEscapeUtils;
import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.ecom.PaypalConnector;
import org.javlo.ecom.Product;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
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

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (!getValue().contains("url")) {
			if (!getValue().isEmpty()) {
				out.println(getValue());
			}
			out.println("button=Paypal");
			out.println("url=https://api.sandbox.paypal.com");
			out.println("user=###");
			out.println("password=###");
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

	protected String getUserEMail() {
		String user = getData().getProperty("email");
		return user;
	}

	protected String getPassword() {
		return getData().getProperty("password");
	}

	private String getDirectPayForm(ContentContext ctx) throws IOException {
		Basket basket = Basket.getInstance(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form id=\"paypal-form\" method=\"post\" action=\""+getBaseURL()+"/cgi-bin/webscr\" class=\"place\"><input type=\"hidden\" name=\"cmd\" id=\"cmd\" value=\"_cart\" />");
		out.println("<input type=\"hidden\" name=\"charset\" id=\"charset\" value=\"utf-8\" />");
		out.println("<input type=\"hidden\" name=\"upload\" id=\"upload\" value=\"1\" />");
		out.println("<input type=\"hidden\" name=\"currency_code\" id=\"currency_code\" value=\"" + basket.getCurrencyCode() + "\" />");
		out.println("<input type=\"hidden\" name=\"business\" id=\"business\" value=\"" + getUserEMail() + "\" />");
		String returnURL = URLHelper.createURL(ctx.getContextForAbsoluteURL());
		returnURL = URLHelper.addParam(returnURL, IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
		returnURL = URLHelper.addParam(returnURL, "basket", basket.getId());
		URL validReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.validDirect"));
		URL cancelReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.cancel"));
		URL notifyURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.notify"));
		out.println("<input type=\"hidden\" name=\"return\" value=\"" + validReturnURL + "\" />");
		out.println("<input type=\"hidden\" name=\"cancel_return\" value=\"" + cancelReturnURL + "\" />");
		out.println("<input type=\"hidden\" name=\"notify_url\" value=\"" + notifyURL + "\" />");
		if (basket.getDelivery(ctx,true) > 0) {
			out.println("<input type=\"hidden\" name=\"shipping\" value=\"" +  PaypalConnector.formatDouble(basket.getDelivery(ctx,true)) + "\" />");			
		}
		int index = 1;
		for (Product product : basket.getProducts()) {
			out.println("<input type=\"hidden\" name=\"item_name_" + index + "\" id=\"" + index + "\" value=\"" + product.getName() + "\" />");
			out.println("<input type=\"hidden\" name=\"amount_" + index + "\" id=\"amount_" + index + "\" value=\"" + PaypalConnector.formatDouble(product.getReductionPrice()*(1 - basket.getUserReduction())) + "\" type=\"number\" />");			
			out.println("<input type=\"hidden\" name=\"quantity_" + index + "\" id=\"quantity" + index + "\" value=\"" + product.getQuantity() + "\" />");
			index++;
		}
		if (basket.getDelivery(ctx,true) > 0) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<input type=\"hidden\" name=\"item_name_" + index + "\" id=\"" + index + "\" value=\"" + i18nAccess.getViewText("ecom.shipping") + "\" />");
			out.println("<input type=\"hidden\" name=\"amount_" + index + "\" id=\"amount_" + index + "\" value=\"" + PaypalConnector.formatDouble(basket.getDelivery(ctx,true))  + "\" type=\"number\" />");
			out.println("<input type=\"hidden\" name=\"quantity_" + index + "\" id=\"quantity" + index + "\" value=\"1\" />");
		}

		out.println("<input type=\"hidden\" name=\"invoice\" id=\"invoice\" value=\"" + basket.getId() + "\" />");
		out.println("<input type=\"hidden\" name=\"lc\" id=\"lc\" value=\"" + ctx.getRequestContentLanguage() + "\" />");
		out.println("<input type=\"hidden\" name=\"address_override\" id=\"address_override\" value=\"1\" />");
		out.println("<input type=\"hidden\" name=\"email\" id=\"email\" value=\"" + StringEscapeUtils.escapeHtml(basket.getContactEmail()) + "\" />");
		out.println("<input type=\"hidden\" name=\"first_name\" id=\"first_name\" value=\"" +StringEscapeUtils.escapeHtml(basket.getFirstName()) + "\" />");
		out.println("<input type=\"hidden\" name=\"last_name\" id=\"last_name\" value=\"" + StringEscapeUtils.escapeHtml(basket.getLastName()) + "\" />");
		out.println("<input type=\"hidden\" name=\"address1\" id=\"address1\" value=\""+StringEscapeUtils.escapeHtml(basket.getAddress())+"\" />");
		out.println("<input type=\"hidden\" name=\"city\" id=\"city\" value=\""+StringEscapeUtils.escapeHtml(basket.getCity())+"\" />");
		out.println("<input type=\"hidden\" name=\"zip\" id=\"zip\" value=\""+basket.getZip()+"\" />");
		out.println("<input type=\"hidden\" name=\"country\" id=\"country\" value=\""+basket.getCountry().toUpperCase()+"\" />");		 
		out.println("<a href=\"#\" onclick=\"document.getElementById('paypal-form').submit(); return false;\"><img  src=\"https://www.paypal.com/fr_XC/i/btn/btn_xpressCheckout.gif\" align=\"left\" style=\"margin-right:7px;\"></a>");
		out.close();
		BasketPersistenceService basketPersitenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		basketPersitenceService.storeBasket(basket);
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		if (basket.getStep() != Basket.ORDER_STEP) {
			return "";
		}
		return getDirectPayForm(ctx);
		//return getTransactionalPayement(ctx);
	}

	private String getTransactionalPayement(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form id=\"paypal-form\" class=\"paypal\" mehtod=\"action\"" + URLHelper.createURL(ctx) + "\">");
		out.println("<fieldset>");
		out.println("<input type=\"hidden\" name=\"" + IContentVisualComponent.COMP_ID_REQUEST_PARAM + "\" value=\"" + getId() + "\" />");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"paypal.pay\" />");
		out.println("<input style=\"display: none;\" type=\"submit\" value=\"" + getData().getProperty("button") + "\" />");
		out.println("<a href=\"#\" onclick=\"document.getElementById('paypal-form').submit(); return false;\"><img  src=\"https://www.paypal.com/fr_XC/i/btn/btn_xpressCheckout.gif\" align=\"left\" style=\"margin-right:7px;\"></a>");
		out.println("</fieldset>");
		out.println("</form>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String performPay(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		PaypalOrderComponent comp = (PaypalOrderComponent) ComponentHelper.getComponentFromRequest(ctx);

		Basket basket = Basket.getInstance(ctx);

		String returnURL = URLHelper.createURL(ctx.getContextForAbsoluteURL());
		returnURL = URLHelper.addParam(returnURL, IContentVisualComponent.COMP_ID_REQUEST_PARAM, comp.getId());
		returnURL = URLHelper.addParam(returnURL, "basket", basket.getId());
		URL validReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.valid"));
		URL cancelReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.cancel"));

		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		PaypalConnector connector = new PaypalConnector(comp.getBaseURL(), comp.getUser(), comp.getPassword());
		basket.setTransactionManager(connector);
		// String url = connector.createPayment(basket.getTotalIncludingVAT(),
		// basket.getCurrencyCode(), ctx.getGlobalContext().getGlobalTitle(),
		// validReturnURL, cancelReturnURL);
		String url = connector.createPaypalPayment(ctx, basket, validReturnURL, cancelReturnURL);
		basketPersistenceService.storeBasket(basket);

		NetHelper.sendRedirectTemporarily(ctx.getResponse(), url);

		return null;
	}

	public static String performValid(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		PaypalOrderComponent comp = (PaypalOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		String token = rs.getParameter("token", null);
		String payerID = rs.getParameter("PayerID", null);
		if (basket == null || comp == null) {
			String error;
			if (basket != null) {
				error = "comp not found : token=" + token + "  -  payerID=" + payerID;
			} else {
				error = "basket not found : token=" + token + "  -  payerID=" + payerID;
			}
			logger.severe(error);
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ecom error on paypal payement (performValid) : " + ctx.getGlobalContext().getContextKey(), error);
			return "Ecom datal error : please try again.";
		} else {
			PaypalConnector connector = (PaypalConnector) basket.getTransactionManager();
			if (connector == null) {
				String error = "no transaction manager found : token=" + token + "  -  payerID=" + payerID + " - context:" + ctx.getGlobalContext().getContextKey();
				logger.severe(error);
				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ecom error on paypal payement (performValid) : " + ctx.getGlobalContext().getContextKey(), error);
				return "Fatal error : dead transaction, please try again.";
			}
			logger.info("basket:" + basket.getId() + "total tvac:" + basket.getTotalString(ctx,true) + " token:" + token + " payerID=" + payerID);
			try {
				String url = connector.executePaypalPayment(token, payerID);

				String baskResponse = NetHelper.readPage(new URL(url));

				logger.info(" validInfo:" + baskResponse);
				basket.setToken(token);
				basket.setPayerID(payerID);
				basket.setStatus(Basket.STATUS_VALIDED);
				basket.setValidationInfo(baskResponse);
				basket.setTransactionManager(null);

				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("ecom.message.final"), GenericMessage.INFO));

				basket.setStep(Basket.FINAL_STEP);
				basketPersistenceService.storeBasket(basket);
				Basket.setInstance(ctx, basket);

				comp.sendConfirmationEmail(ctx, basket);

				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "basket confirmed on " + ctx.getGlobalContext().getContextKey(), "" + basket);
				
				basket.payAll(ctx);
			} catch (Exception e) {
				e.printStackTrace();
				return i18nAccess.getViewText("ecom.message.error");
			}
		}
		return null;
	}

	public static String performValidDirect(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		PaypalOrderComponent comp = (PaypalOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		if (basket == null || comp == null) {
			String error;
			if (basket != null) {
				error = "comp not found.";
			} else {
				error = "basket not found.";
			}
			logger.severe(error);
			return "Ecom datal error : please try again.";
		} else {
			logger.info("basket returned :" + basket.getId() + " - total tvac:" + basket.getTotalString(ctx,true));
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("ecom.message.final"), GenericMessage.INFO));

			basket.setStep(Basket.FINAL_STEP);
			basketPersistenceService.storeBasket(basket);
			Basket.setInstance(ctx, basket);
			
		}		
		return null;
	}

	public static String performCancel(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));		
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("ecom.message.error"), GenericMessage.ERROR));
		basket.setStep(Basket.ERROR_STEP);
		basket.setValidationInfo("unvalid.");
		basketPersistenceService.storeBasket(basket);
		return null;
	}

	public static String performNotify(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {		
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		PaypalOrderComponent comp = (PaypalOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
		
		Enumeration en = ctx.getRequest().getParameterNames();
		String str = "cmd=_notify-validate";
		
		String fraud = null;
		String ipnTrackID = null;		
		
		while (en.hasMoreElements()) {
			String paramName = (String) en.nextElement();			
			String paramValue = ctx.getRequest().getParameter(paramName);
			if (paramName.equals("receiver_email")) {
				if (!paramValue.equals(comp.getUserEMail())) {
					fraud = "bad receiver email : "+paramName.equals("receiver_email");
				}
			}
			if (paramName.equals("ipn_track_id")) {
				ipnTrackID = paramValue;
			}
			if (paramName.equals("mc_gross")) {
				double total = Double.parseDouble(paramValue);
				if (total != basket.getTotal(ctx,true)) {
					fraud = "mc_gross != TotalIncludingVAT  ("+paramValue+" != "+PaypalConnector.formatDouble(basket.getTotal(ctx,true))+")";
				}
			}
			str = str + "&" + paramName + "=" + URLEncoder.encode(paramValue);
		}
		
		// DEV https://www.sandbox.paypal.com/cgi-bin/webscr
		// PROD https://www.paypal.com/cgi-bin/webscr
		String payPalUrl = "https://www.paypal.com/cgi-bin/webscr";
		logger.info("Sending request to PayPal IPN: " + payPalUrl + '?' + str);
		
		String res = null;
		try {
			// Send request to PayPal for validation
			URL u = new URL(payPalUrl);
			URLConnection uc = u.openConnection();
			uc.setDoOutput(true);
			uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			PrintWriter pw = new PrintWriter(uc.getOutputStream());
			pw.println(str);
			pw.close();
			
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			res = in.readLine();
			in.close();
			
			logger.info("paypal return : "+res+" [bakset:"+rs.getParameter("basket", null)+" fraud:"+fraud+"]");
			
			basket.setInfo("paypal return : "+res);
			basket.setPresumptiveFraud(fraud != null);
			if ("VERIFIED".equals(res)) {
				basket.setStatus(Basket.STATUS_VALIDED);
			} else  {
				basket.setStatus(Basket.STATUS_TO_BE_VERIFIED);
			}
			basketPersistenceService.storeBasket(basket);
			
			comp.sendConfirmationEmail(ctx, basket);
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("paypal return     : "+res);
			out.println("paypal ipnTrackID : "+ipnTrackID);			
			out.println("presumptive fraud : "+(fraud != null));
			if (fraud != null) {
				out.println("fraud msg : "+fraud);
			}
			out.println("");
			out.println(basket);
			out.close();
			String subject = "basket confirmed on " + ctx.getGlobalContext().getContextKey();
			if (fraud != null) {
				subject = subject + " - Presumptive Fraud -";
			}
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), subject, new String(outStream.toByteArray()));

		} catch (MalformedURLException ex) {
			ex.printStackTrace();			
		} catch (IOException ex) {
			ex.printStackTrace();			
		}
		
		/*
		 * BasketPersistenceService basketPersistenceService =
		 * BasketPersistenceService.getInstance(ctx.getGlobalContext()); Basket
		 * basket = basketPersistenceService.getBasket(rs.getParameter("basket",
		 * null));
		 * System.out.println("***** PaypalOrderComponent.performNotify : basket = "
		 * +basket.getId()); //TODO: remove debug trace
		 */
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "paypal";
	}

}
