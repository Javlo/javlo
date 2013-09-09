package org.javlo.module.ecom;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.ecom.EditBasketComponent;
import org.javlo.component.ecom.ProductComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.DeliveryZone;
import org.javlo.ecom.EcomService;
import org.javlo.ecom.PayementExternalService;
import org.javlo.ecom.Product;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.MailService;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class EcomAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(EcomAction.class.getName());

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String outMsg = super.prepare(ctx, modulesContext);
		EcomService ecomService = EcomService.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		ctx.getRequest().setAttribute("ecomServices", ecomService.getExternalService());
		return outMsg;
	}

	@Override
	public String getActionGroupName() {
		return "ecom";
	}

	public static String performCreate(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String name = rs.getParameter("name", null);
		if (name == null) {
			return "bad request structure, need 'name' as parameter";
		} else {
			PayementExternalService newService = new PayementExternalService();
			newService.setName(name);
			EcomService ecomService = EcomService.getInstance(globalContext, ctx.getRequest().getSession());
			ecomService.storePayementService(newService);
		}
		return null;
	}

	public static String performUpdateService(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String name = rs.getParameter("name", "");
		if (name.trim().length() == 0) {
			return "bad request format name not found or empty.";
		} else {
			EcomService ecomService = EcomService.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
			if (rs.getParameter("delete", null) != null) {
				ecomService.deletePayementService(name);
				ctx.setNeedRefresh(true);
			} else {
				PayementExternalService service = ecomService.getExternalService(name);
				if (service != null) {
					service.setName(rs.getParameter("name", ""));
					service.setAppId(rs.getParameter("appId", ""));
					service.setSecretKey(rs.getParameter("secretKey", ""));
					service.setURL(rs.getParameter("url", ""));
					service.setReturnPage(rs.getParameter("page", ""));
					ecomService.storePayementService(service);
				}
			}
		}
		return null;
	}

	public static String performBuy(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.getInstance(request);
		MenuElement currentPage = ctx.getCurrentPage();

		/* information from product */
		String cid = requestService.getParameter("cid", null);
		if (cid != null) {
			IContentVisualComponent comp = content.getComponent(ctx, cid);
			if ((comp != null) && (comp instanceof ProductComponent)) {
				ProductComponent pComp = (ProductComponent) comp;
				Product product = new Product(pComp);

				/* information from page */
				product.setUrl(URLHelper.createURL(ctx, currentPage.getPath()));
				product.setShortDescription(currentPage.getTitle(ctx));
				product.setLongDescription(currentPage.getDescription(ctx));
				if (currentPage.getImage(ctx) != null) {
					product.setImage(ctx, currentPage.getImage(ctx));
				}

				String quantity = requestService.getParameter("quantity", null);
				if (quantity != null) {
					int quantityValue = Integer.parseInt(quantity);

					quantityValue = quantityValue - (quantityValue % (int) pComp.getOffset());
					product.setQuantity(quantityValue);

					Basket basket = Basket.getInstance(ctx);
					basket.addProduct(product);
				}
			}
		}

		return null;
	}

	public static String performPaypal(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentService content = ContentService.getInstance(request);

		String cid = requestService.getParameter("cid", null);
		if (cid != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			IContentVisualComponent comp = content.getComponent(ctx, cid);
			if (comp instanceof EditBasketComponent) {
				EditBasketComponent basketComp = (EditBasketComponent) comp;
				Basket basket = Basket.getInstance(ctx);

				String zoneName = requestService.getParameter("ecom_zone", null);
				if (basket.getDeliveryZone() == null || !basket.getDeliveryZone().getName().equals(zoneName)) {
					for (DeliveryZone zone : basket.getDeliveryZones(ctx)) {
						if (zone.getName().equals(zoneName)) {
							basket.setDeliveryZone(zone);
							if (zone.getURL() != null && zone.getURL().length() > 0) {
								response.sendRedirect(URLHelper.createURL(ctx, zone.getURL()));
							}
							break;
						}
					}
				}
				if (fillCoordinates(request, basket)) {
					basket.setConfirm(true);

					GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
					I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

					StringBuffer url = new StringBuffer();
					if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
						url.append("https://www.paypal.com/cgi-bin/webscr?business=" + basketComp.getBusiness());
					} else { // TODO: test values for business and PDT in edit
						url.append("https://www.sandbox.paypal.com/cgi-bin/webscr?business=info@volpaiole.com");
					}
					url.append("&cmd=_xclick&no_shipping=1&no_note=1&rm=2&lc=" + ctx.getLanguage());
					// url.append("<input type=\"hidden\" name=\"bn\" value=\"FuoriMondo_BuyNow_WPS_BE\">");
					// url.append("<input type=\"hidden\" name=\"charset\" value=\""
					// + ContentContext.CHARACTER_ENCODING + "\">");

					ContentContext tmpCtx = new ContentContext(ctx);
					tmpCtx.setAbsoluteURL(true);

					// TODO: parameterize or relative path, hard-coded for
					// volpaiole
					if (basket.isPickup() && basket.getDeliveryZone() != null && basket.getDeliveryZone().getPickupURL() != null) {
						url.append("&return=" + URLHelper.createURL(tmpCtx, basket.getDeliveryZone().getPickupURL()));
					} else {
						url.append("&return=" + URLHelper.createURL(tmpCtx, "/ecom/checkout/paypal-ok"));
					}
					url.append("&cancel_return=" + URLHelper.createURL(tmpCtx, "/ecom/checkout"));

					String[][] params = new String[][] { { "website", globalContext.getGlobalTitle() }, { "product_count", "" + basket.getProductCount() } };
					String basketName = i18nAccess.getViewText("ecom.basket-title", params);

					url.append("&item_name=" + basketName);
					url.append("&item_number=" + basket.getId());

					String amout = ("" + StringHelper.renderDouble(basket.getTotalExcludingVAT(), 2, '.'));
					url.append("&amount=" + amout);
					String tax = ("" + (StringHelper.renderDouble(basket.getTotalIncludingVAT() - basket.getTotalExcludingVAT(), 2, '.')));
					url.append("&tax=" + tax);
					String fee = ("" + StringHelper.renderDouble(basket.getTotalIncludingVAT() * 0.03, 2, '.'));
					url.append("&handling=" + fee);

					url.append("&currency_code=" + basket.getCurrencyCode());

					response.sendRedirect(url.toString());
				}
			}
		}
		return null;
	}

	public static String performSelectpickup(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);

		fillCoordinates(request, basket);

		return null;
	}

	public static String performSelectzone(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);

		String zoneName = requestService.getParameter("ecom_zone", null);
		for (DeliveryZone zone : basket.getDeliveryZones(ctx)) {
			if (zone.getName().equals(zoneName)) {
				basket.setDeliveryZone(zone);
				if (zone.getURL() != null && zone.getURL().length() > 0) {
					response.sendRedirect(URLHelper.createURL(ctx, zone.getURL()));
				}
				break;
			}
		}
		fillCoordinates(request, basket);

		return null;
	}

	private static boolean fillCoordinates(HttpServletRequest request, Basket basket) {
		RequestService requestService = RequestService.getInstance(request);

		String clientEmail = requestService.getParameter("email", "");
		String firstName = requestService.getParameter("firstname", "");
		String lastName = requestService.getParameter("lastname", "");
		String phone = requestService.getParameter("phone", "");
		// String organization = requestService.getParameter("organization",
		// "");
		// String vatNumber = requestService.getParameter("vatnumber", "");
		String address = requestService.getParameter("address", "");
		boolean pickup = Boolean.valueOf(requestService.getParameter("pickup", ""));

		basket.setContactEmail(clientEmail);
		basket.setFirstName(firstName);
		basket.setLastName(lastName);
		basket.setContactPhone(phone);
		// basket.setOrganization(organization);
		// basket.setVATNumber(vatNumber);
		basket.setAddress(address);
		basket.setPickup(pickup);

		// TODO
		boolean valid = (lastName.trim().length() > 0) && ((clientEmail.trim().length() > 0) || (phone.trim().length() > 0));
		if (valid) {
			if (clientEmail.trim().length() > 0) {
				if (!PatternHelper.MAIL_PATTERN.matcher(clientEmail).matches()) {
					valid = false;
				}
			}
		}
		return valid;
	}

	private static void sendMail(ContentContext ctx, EditBasketComponent comp) throws Exception {
		// TODO: improve mail content + i18n keys

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Basket basket = Basket.getInstance(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

		String adminEmail = comp.getEmail();
		MailService mailService = MailService.getInstance(StaticConfig.getInstance(globalContext.getServletContext()));
		InternetAddress from;
		if (adminEmail.trim().length() > 0) {
			from = new InternetAddress(comp.getEmail());
		} else {
			from = new InternetAddress(globalContext.getAdministratorEmail());
		}
		if (basket.getContactEmail().trim().length() > 0) {
			InternetAddress[] tos = InternetAddress.parse(basket.getContactEmail());

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);

			ContentContext tmpCtx = new ContentContext(ctx);
			tmpCtx.setAbsoluteURL(true);

			out.println("<p>basket id: " + basket.getId() + "</p>");
			if (!StringUtils.isEmpty(basket.getPaypalTX())) {
				out.println("<p>paypal transaction id: " + basket.getPaypalTX() + "</p>");
			}
			out.println("<p>");
			out.println(i18nAccess.getViewText("ecom.email") + ": " + basket.getContactEmail());
			out.println("<br />" + i18nAccess.getViewText("ecom.firstname") + ": " + basket.getFirstName());
			out.println("<br />" + i18nAccess.getViewText("ecom.lastname") + ": " + basket.getLastName());
			out.println("<br />" + i18nAccess.getViewText("ecom.phone") + ": " + basket.getContactPhone());
			if (!StringUtils.isEmpty(basket.getAddress())) {
				out.println("<br />" + i18nAccess.getViewText("ecom.address") + ": " + basket.getAddress());
				out.println("</p>");
			} else {
				out.println("</p>");
				out.println("<p>We will contact you when your order will be ready for pick-up</p>");
				String pickupURL = URLHelper.createURL(ctx, basket.getDeliveryZone().getPickupURL());
				String pickupLabel = i18nAccess.getViewText("ecom.pickup.page", "See pick-up locations for your country");
				out.println("<a href=\"" + pickupURL + "\" title=\"" + pickupLabel + "\">" + pickupLabel + "</a>");
			}

			comp.renderBasket(ctx, out, false);

			out.close();
			String mailBody = new String(outStream.toByteArray());

			for (InternetAddress to : tos) {
				mailService.sendMail(null, from, to, "basket validation from " + globalContext.getGlobalTitle() + " [" + basket.getProductCount() + " products]", mailBody, true);
			}

		}
	}

	public static String performConfirmbasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);

		String zoneName = requestService.getParameter("ecom_zone", null);
		for (DeliveryZone zone : basket.getDeliveryZones(ctx)) {
			if (zone.getName().equals(zoneName)) {
				basket.setDeliveryZone(zone);
				if (zone.getURL() != null && zone.getURL().length() > 0) {
					response.sendRedirect(URLHelper.createURL(ctx, zone.getURL()));
				}
				break;
			}
		}
		if (fillCoordinates(request, basket)) {
			basket.setConfirm(true);
		}
		return performValidbasket(request, response, false, null);
	}

	public static String performValidbasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return performValidbasket(request, response, false, null);
	}

	public static String performValidbasket(HttpServletRequest request, HttpServletResponse response, boolean isRendering, String paypalTX) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);

		basket.setValid(!StringUtils.isEmpty(basket.getAddress()) || basket.isPickup());
		if (basket.isValid()) {
			RequestService requestService = RequestService.getInstance(request);
			ContentService content = ContentService.getInstance(request);
			GlobalContext globalContext = GlobalContext.getInstance(request);

			EcomService ecomService = EcomService.getInstance(globalContext, request.getSession());
			ecomService.storeBasket(basket);

			if (ctx.getRenderMode() == ContentContext.VIEW_MODE) {
				if (!StringUtils.isEmpty(paypalTX)) {
					basket.pay(ctx, paypalTX);
				} else {
					basket.reserve(ctx);
				}
			} else {
				logger.log(Level.INFO, "*** test basket validated, no payment or reservation made for basket: " + basket.getId());
			}

			/* send email */
			String cid = requestService.getParameter("cid", null);
			if (cid != null) {
				IContentVisualComponent comp = content.getComponent(ctx, cid);
				if (comp instanceof EditBasketComponent) {
					sendMail(ctx, (EditBasketComponent) comp);
				}
			}

			for (Product product : basket.getProducts()) {
				if (product.getQuantity() < 1) {
					MailService mailer = MailService.getInstance(StaticConfig.getInstance(ctx.getRequest().getSession()));
					/*
					 * InternetAddress sender =
					 * InternetAddress.parse("info@fuorimondo.com");
					 * InternetAddress recipient =
					 * InternetAddress.parse("info@fuorimondo.com");
					 * mailer.send(null, sender[0], recipient[0],
					 * (InternetAddress[]) null, "[WARNIG] Stock out of bound",
					 * basket.getId(), false);
					 */
					break;
				}
			}

			if (!isRendering) {
				boolean hasPickup = basket.getDeliveryZone() != null && !StringUtils.isEmpty(basket.getDeliveryZone().getPickupURL());
				if (basket.isPickup() && hasPickup) {
					response.sendRedirect(URLHelper.createURL(ctx, basket.getDeliveryZone().getPickupURL()));
				} else {

					// TODO: parameterize or relative path, hard-coded for
					// volpaiole
					response.sendRedirect(URLHelper.createURL(ctx, "/ecom/checkout/reserve-ok"));
				}
			}
		} else {
			// TODO: add appropriate error message somehow...
			basket.setConfirm(false);
		}
		return null;
	}

	public static String performInitbasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);
		basket.init(ctx);

		// TODO: parameterize or relative path, hard-coded for volpaiole
		response.sendRedirect(URLHelper.createURL(ctx, "/ecom/product"));
		return null;
	}

	public static String performDeletebasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String productId = requestService.getParameter("id", null);
		if (productId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			Basket basket = Basket.getInstance(ctx);
			basket.removeProduct(productId);
		}
		return null;
	}

	public static void main(String[] args) {
		// URL url = new URL("https://api.sandbox.paypal.com/v1/oauth2/token");

		// HttpRequest request = new BasicHttpRequest("GET", "/",
		// HttpVersion.HTTP_1_1);

	//	HttpPost post = new HttpPost("https://api.sandbox.paypal.com/v1/oauth2/token?grant_type=client_credentials");
		// HttpPost post = new HttpPost("https://www.google.com");
		/*
		 * post.addHeader("Accept", "application/json");
		 * post.addHeader("Accept-Language", "en_US"); String encoding =
		 * Base64.encodeBase64URLSafeString(
		 * "AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca:EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3"
		 * .getBytes()); post.addHeader("Authorization", "Basic " + encoding);
		 */

		/*CloseableHttpClient httpclient = HttpClients.createMinimal();
		HttpResponse response;
		try {
			response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();

			System.out.println("Login form get: " + response.getStatusLine());
			if (entity != null) {
				entity.consumeContent();
				InputStream in = entity.getContent();
				String content = ResourceHelper.writeStreamToString(in, ContentContext.CHARACTER_ENCODING);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}*/

		try {
			
			String paypalURL = new String("https://api-3t.sandbox.paypal.com/nvp");
			StringBuffer params = new StringBuffer();
//			params.append('?');
			params.append("USER=AZ1_hxCGaj9fSng1Xik5Egr03NqTWMzeiiUDK99dSJ5j6qsEVx1n_rjbu_lm");
			params.append('&');
			params.append("PWD=EAPINRCdAaPCf7NmGNDOwsv-bcCWbl1BJS3OYo4eQhv-M5-wmxN22PU0Ds7w");
			params.append('&');
			params.append("SIGNATURE=pvandermaesen-facilitator@immanence.be");
			params.append('&');
			params.append("METHOD=SetExpressCheckout");
			params.append('&');
			params.append("VERSION=93");
			params.append('&');
			params.append("PAYMENTREQUEST_0_PAYMENTACTION=SALE");
			params.append('&');
			params.append("PAYMENTREQUEST_0_AMT=19.95");
			params.append('&');
			params.append("PAYMENTREQUEST_0_CURRENCYCODE=EUR");
			params.append('&');
			params.append("RETURNURL=http://www.example.com/success.html");
			params.append('&');
			params.append("CANCELURL=http://www.example.com/cancel.html");
			
			//HttpPost post = new HttpPost(paypalURL);
			
			/*HttpParams httpParams = new SyncBasicHttpParams();
			httpParams.setParameter("USER", "AZ1_hxCGaj9fSng1Xik5Egr03NqTWMzeiiUDK99dSJ5j6qsEVx1n_rjbu_lm");
			httpParams.setParameter("PWD", "EAPINRCdAaPCf7NmGNDOwsv-bcCWbl1BJS3OYo4eQhv-M5-wmxN22PU0Ds7w");
			httpParams.setParameter("SIGNATURE", "pvandermaesen-facilitator@immanence.be");
			httpParams.setParameter("METHOD", "SetExpressCheckout");
			httpParams.setParameter("VERSION", "93");
			httpParams.setParameter("PAYMENTREQUEST_0_PAYMENTACTION", "SALE");
			httpParams.setParameter("PAYMENTREQUEST_0_AMT", "19.95");
			httpParams.setParameter("PAYMENTREQUEST_0_CURRENCYCODE", "EUR");
			httpParams.setParameter("RETURNURL", "http://www.example.com/success.html");
			httpParams.setParameter("CANCELURL", "http://www.example.com/cancel.htm");
			post.setParams(httpParams);
			
			BasicHttpEntityEnclosingRequest request = new BasicHttpEntityEnclosingRequest("POST","https://api-3t.sandbox.paypal.com/nvp");
			*/
			
			//post.addHeader("Accept", "application/json");
			//post.addHeader("Accept-Language", "en_US");
			/*CloseableHttpClient httpclient = HttpClients.createMinimal();			
			HttpResponse response;
			try {
				response = httpclient.execute(post);
				HttpEntity entity = response.getEntity();

				System.out.println("Login form get: " + response.getStatusLine());
				if (entity != null) {
					entity.consumeContent();
					InputStream in = entity.getContent();
					String content = ResourceHelper.writeStreamToString(in, ContentContext.CHARACTER_ENCODING);
					System.out.println(content);
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			
			//String content = NetHelper.readPage(new URL(paypalURL.toString()));
			
			//String content = ResourceHelper.excutePost("https://api.sandbox.paypal.com/v1/oauth2/token","grant_type=client_credentials","application/json","en_US","AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca","EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3");
			// 
			
			
			
			
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		

	}
	
}
