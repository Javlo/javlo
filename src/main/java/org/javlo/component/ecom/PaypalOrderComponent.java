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
import org.javlo.ecom.BasketPersistenceService;
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

	public String getDirectPayForm(ContentContext ctx) {
		Basket basket = Basket.getInstance(ctx);
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form method=\"post\" action=\"https://www.paypal.com/cgi-bin/webscr\" class=\"place\"><input type=\"hidden\" name=\"cmd\" id=\"cmd\" value=\"_cart\" />");
		out.println("<input type=\"hidden\" name=\"charset\" id=\"charset\" value=\"utf-8\" />");
		out.println("<input type=\"hidden\" name=\"upload\" id=\"upload\" value=\"1\" /><%-- We use a custom shopping cart - not paypal --%>");
		out.println("<input type=\"hidden\" name=\"currency_code\" id=\"currency_code\" value=\"EUR\" />");
		out.println("<input type=\"hidden\" name=\"business\" id=\"business\" value=\""+getUser()+"\" /> <%-- Merchant id --%>");
		out.println("<input type=\"hidden\" name=\"item_name_1\" id=\"item_name_1\" value=\"${pageContext.request.serverName} / ${model.orderId}\" /> <%-- website --%>");
		out.println("<input type=\"hidden\" name=\"invoice\" id=\"invoice\" value=\""+basket.getId()+"\" /> <%-- Order/invoice id --%>");
		out.println("<input type=\"hidden\" name=\"amount_1\" id=\"amount_1\" value=\"<fmt:setLocale value=\"en_US\" /><fmt:formatNumber value=\"${model.total + model.shipping}\" type=\"number\" /><fmt:setLocale value=\"${webContext.locale}\" />\" /> <%-- Total order amount --%>");
		out.println("<input type=\"hidden\" name=\"quantity\" id=\"quantity\" value=\"${model.quantity}\" /> <%-- Order quantity --%>");

		out.println("<input type=\"hidden\" name=\"lc\" id=\"lc\" value=\"${fn:toUpperCase(webContext.locale.language)}\" /><%-- Defines the buyers language for the login page --%>");

		out.println("<input type=\"hidden\" name=\"address_override\" id=\"address_override\" value=\"1\" />");
		out.println("<input type=\"hidden\" name=\"email\" id=\"email\" value=\"${model.personalData.email}\" />");
		out.println("<input type=\"hidden\" name=\"first_name\" id=\"first_name\" value=\"${model.personalData.firstname}\" />");
		out.println("<input type=\"hidden\" name=\"last_name\" id=\"last_name\" value=\"${model.personalData.lastname}\" />");
		out.println("<input type=\"hidden\" name=\"address1\" id=\"address1\" value=\"${model.personalData.street}\" />");
		out.println("<input type=\"hidden\" name=\"city\" id=\"city\" value=\"${model.personalData.city}\" />");
		out.println("<input type=\"hidden\" name=\"zip\" id=\"zip\" value=\"${model.personalData.zip}\" />");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		if (basket.getStep() != Basket.ORDER_STEP) {
			return "";
		}

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
		System.out.println("***** PaypalOrderComponent.performPay : basket.getId() = "+basket.getId()); //TODO: remove debug trace
		System.out.println("***** PaypalOrderComponent.performPay : basket = "+basket); //TODO: remove debug trace_
		URL validReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.valid"));
		URL cancelReturnURL = new URL(URLHelper.addParam(returnURL, "webaction", "paypal.cancel"));

		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		PaypalConnector connector = new PaypalConnector(comp.getBaseURL(), comp.getUser(), comp.getPassword());
		basket.setTransactionManager(connector);
		String url = connector.createPayment(basket.getTotalIncludingVAT(), basket.getCurrencyCode(), ctx.getGlobalContext().getGlobalTitle(), validReturnURL, cancelReturnURL);		
		basketPersistenceService.storeBasket(basket);

		NetHelper.sendRedirectTemporarily(ctx.getResponse(), url);

		return null;
	}

	public static String performValid(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		PaypalOrderComponent comp = (PaypalOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		System.out.println("***** PaypalOrderComponent.performValid : rs.getParameter('basket', null) = "+rs.getParameter("basket", null)); //TODO: remove debug trace
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));		
		System.out.println("***** PaypalOrderComponent.performValid : basket = "+basket); //TODO: remove debug trace
		String token = rs.getParameter("token", null);
		String payerID = rs.getParameter("PayerID", null);
		if (basket == null || comp == null) {
			String error;
			if (basket != null) {
				error = "comp not found : token="+token+"  -  payerID="+payerID;	
			} else {
				error = "basket not found : token="+token+"  -  payerID="+payerID;
			}			
			logger.severe(error);
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ecom error on paypal payement (performValid) : "+ctx.getGlobalContext().getContextKey(), error); 
			return "Ecom datal error : please try again.";
		} else {
			PaypalConnector connector = (PaypalConnector)basket.getTransactionManager();
			if (connector == null) {
				String error = "no transaction manager found : token="+token+"  -  payerID="+payerID+ " - context:"+ctx.getGlobalContext().getContextKey();
				logger.severe(error);
				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ecom error on paypal payement (performValid) : "+ctx.getGlobalContext().getContextKey(), error);
				return "Fatal error : dead transaction, please try again.";
			}
			logger.info("basket:"+basket.getId()+"total tvac:"+basket.getTotalIncludingVATString()+" token:"+token+" payerID="+payerID);			
			String validInfo = connector.executePayment(token, payerID);
			logger.info(" validInfo:"+validInfo);
			basket.setToken(token);
			basket.setPayerID(payerID);
			basket.setStatus(Basket.STATUS_VALIDED);
			basket.setValidationInfo(validInfo);			
			basket.setTransactionManager(null); // remove reference to PaypalConnector
			basket.setStep(Basket.FINAL_STEP);
			basketPersistenceService.storeBasket(basket);
			Basket.setInstance(ctx, basket);
		}		
		return null;
	}

	public static String performCancel(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		basket.setStep(Basket.ERROR_STEP);
		basket.setValidationInfo("unvalid.");
		basketPersistenceService.storeBasket(basket);
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "paypal";
	}

}
