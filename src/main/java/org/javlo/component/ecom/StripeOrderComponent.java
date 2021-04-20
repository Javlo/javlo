package org.javlo.component.ecom;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javlo.actions.EcomStatus;
import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.ecom.Product;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionCreateParams.LineItem;

public class StripeOrderComponent extends AbstractOrderComponent implements IAction {

	public static final String TYPE = "stripe-order";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	private static String getPublicKey(ContentContext ctx) {
		return (String) ctx.getGlobalContext().getSpecialConfig().get("stripe.key.public", null);
	}

	private static String getPrivateKey(ContentContext ctx) {
		return (String) ctx.getGlobalContext().getSpecialConfig().get("stripe.key.private", null);
	}
	
	private boolean isBancontact(ContentContext ctx) {
		return StringHelper.isTrue(ctx.getGlobalContext().getSpecialConfig().get("stripe.bancontact", null), true);
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("publicKey", getPublicKey(ctx));

		Map<String, String> urlParam = new HashMap<>();
		urlParam.put("webaction", TYPE + ".createSession");
		urlParam.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
		String url = URLHelper.createURL(ctx, urlParam);

		ctx.getRequest().setAttribute("createSessionUrl", url);
		ctx.getRequest().setAttribute("bancontact", isBancontact(ctx));
		
		Basket basket = Basket.getInstance(ctx);
		// bancontact
		if (isBancontact(ctx) && basket.getStep() == Basket.ORDER_STEP) {
			synchronized (Stripe.class) {
				// Set your secret key. Remember to switch to your live secret key in production.
				// See your keys here: https://dashboard.stripe.com/apikeys
				Stripe.apiKey =  getPrivateKey(ctx);
				PaymentIntentCreateParams params =
				  PaymentIntentCreateParams.builder()
				    .setAmount(Math.round(basket.getTotal(ctx, true)*100))
				    .setCurrency(basket.getCurrencyCode())
				    .addPaymentMethodType("bancontact")
				    .setPaymentMethodOptions(
				          PaymentIntentCreateParams.PaymentMethodOptions.builder()
				          .putExtraParam("bancontact[preferred_language]", ctx.getRequestContentLanguage())
				          .build())
				    .build();
				PaymentIntent paymentIntent = PaymentIntent.create(params);
				urlParam.clear();
				urlParam.put("webaction", TYPE + ".successBancontact");
				urlParam.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());
				String bancontactSuccessURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), urlParam)+"&id="+paymentIntent.getId();
				ctx.getRequest().setAttribute("PAYMENT_INTENT_CLIENT_SECRET", paymentIntent.getClientSecret());
				ctx.getRequest().setAttribute("bancontactSuccessURL", bancontactSuccessURL);
				ctx.getRequest().setAttribute("name", basket.getFirstName()+' '+basket.getLastName());
			}
		}

	}

	public static String performSuccess(ContentContext ctx, RequestService rs, I18nAccess i18nAccess) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		
		Session session = Session.retrieve(rs.getParameter("session_id"));

		if (session == null) {
			return "session not found.";
		}
		
		String bkey = rs.getParameter("bkey");
		if (!bkey.equals(basket.getSecurityKey())) {
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ERROR: security error bad security key : " + ctx.getGlobalContext().getContextKey(), basket.getAdministratorEmail(ctx));
			return "security error.";
		}

		if (basket.getCurrencyCode() == null) {
			logger.severe("currency not found.");
			return i18nAccess.getViewText("ecom.message.error");
		}

		basket.setStep(Basket.FINAL_STEP);
		basket.setStatus(Basket.STATUS_VALIDED);
		basket.setTransactionId(session.getId());
		basket.setPaymentType("cc");
		BasketPersistenceService.getInstance(ctx.getGlobalContext()).storeBasket(basket);

		EcomStatus status = basket.payAll(ctx);
		if (!status.isError()) {
			AbstractOrderComponent comp = (AbstractOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
			String msg = XHTMLHelper.textToXHTML(comp.getConfirmationEmail(ctx, basket));
			msg = "<p>" + i18nAccess.getViewText("ecom.basket-confirmed") + "</p><p>" + msg + "</p>";
			comp.sendConfirmationEmail(ctx, basket);
			ctx.getRequest().setAttribute("msg", msg);
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "basket confirmed with stripe : " + ctx.getGlobalContext().getContextKey(), basket.getAdministratorEmail(ctx));
			basket.reset(ctx);
		} else {
			NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ERROR: basket NOT confirmed with stripe : " + ctx.getGlobalContext().getContextKey(), basket.getAdministratorEmail(ctx));
		}

		MessageRepository messageRepository = MessageRepository.getInstance(ctx);
		messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("ecom.reception-message"), GenericMessage.INFO));

		return null;
	}

	public static String performError(ContentContext ctx, RequestService rs, I18nAccess i18n) throws Exception {
		/*Session session = Session.retrieve(rs.getParameter("session_id"));
		Customer customer = Customer.retrieve(session.getCustomer());*/
		
		Map<String,String> params = new HashMap<>();
		/*params.put("[stripe] session_id", session.getId());
		params.put("[stripe] client reference id", session.getClientReferenceId());
		params.put("[stripe] customer", session.getCustomer());
		params.put("[stripe] customer email", session.getCustomerEmail());*/
		params.put("[basket] id", Basket.getInstance(ctx).getId());
		params.put("[basket] contact email", Basket.getInstance(ctx).getContactEmail());
		
		String content = XHTMLHelper.createAdminMail("error on payment validation", null, params, "https://stripe.com", "stripe", null);
		
		NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "error payment validation : "+StringHelper.renderTime(new Date()), content);
		
		return i18n.getViewText("ecom.error.payment");
	}

	private static LineItem toLineItem(Product product) {
		return SessionCreateParams.LineItem.builder().setQuantity(1L)
				.setPriceData(SessionCreateParams.LineItem.PriceData.builder()
				.setCurrency(product.getCurrencyCode())
				.setUnitAmount(Math.round(product.getPrice() * 100))
				.setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
						.setName(product.getName())
						.build()).build()).build();
	}

	public static String performCreateSession(ContentContext ctx, RequestService rs) throws Exception {
		final GsonBuilder builder = new GsonBuilder();
		final Gson gson = builder.create();
		ctx.getResponse().setContentType("application/json");		
		
		IContentVisualComponent comp = ComponentHelper.getComponentFromRequest(ctx);
		Basket basket = Basket.getInstance(ctx);

		Map<String, String> urlParam = new HashMap<>();
		urlParam.put("webaction", TYPE + ".success");
		urlParam.put(IContentVisualComponent.COMP_ID_REQUEST_PARAM, comp.getId());
		
		urlParam.put("bkey", Basket.getInstance(ctx).getSecurityKey());
		String successURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), urlParam)+"&session_id={CHECKOUT_SESSION_ID}";
		urlParam.clear();
		urlParam.put("webaction", TYPE + ".error");		
		String errorURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), urlParam);

		List<LineItem> collect = Basket.getInstance(ctx).getProducts().stream().map(product -> toLineItem(product)).collect(Collectors.toList());

		HashMap<String, String> responseData;
		synchronized (Stripe.class) {
			Stripe.apiKey = getPrivateKey(ctx);

			SessionCreateParams params = SessionCreateParams.builder()
				.addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
				.setMode(SessionCreateParams.Mode.PAYMENT)
				.setSuccessUrl(successURL)
				.setCancelUrl(errorURL)
				.setCustomerEmail(basket.getContactEmail())
				.addAllLineItem(collect).build();

			Session session = Session.create(params);

			responseData = new HashMap<String, String>();
			responseData.put("id", session.getId());
		}
		ctx.getResponse().getWriter().print(gson.toJson(responseData));
		ctx.setStopRendering(true);

		return null;
	}
	
	public static String performSuccessBancontact(ContentContext ctx, RequestService rs) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		PaymentIntent paymentIntent = PaymentIntent.retrieve(rs.getParameter("id"));
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		if (paymentIntent == null || paymentIntent.getAmount() != Math.round(basket.getTotal(ctx, true)*100)) {
			logger.warning("error on bancontact return : "+paymentIntent);
			return i18nAccess.getViewText("ecom.message.error");
		} else {
			basket.setStep(Basket.FINAL_STEP);
			basket.setStatus(Basket.STATUS_VALIDED);
			basket.setTransactionId(paymentIntent.getId());
			basket.setPaymentType("bancontact");
			BasketPersistenceService.getInstance(ctx.getGlobalContext()).storeBasket(basket);

			EcomStatus status = basket.payAll(ctx);
			if (!status.isError()) {				
				AbstractOrderComponent comp = (AbstractOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
				String msg = XHTMLHelper.textToXHTML(comp.getConfirmationEmail(ctx, basket));
				msg = "<p>" + i18nAccess.getViewText("ecom.basket-confirmed") + "</p><p>" + msg + "</p>";
				comp.sendConfirmationEmail(ctx, basket);
				ctx.getRequest().setAttribute("msg", msg);
				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "basket confirmed with stripe : " + ctx.getGlobalContext().getContextKey(), basket.getAdministratorEmail(ctx));
				basket.reset(ctx);
			} else {
				NetHelper.sendMailToAdministrator(ctx.getGlobalContext(), "ERROR: basket NOT confirmed with stripe : " + ctx.getGlobalContext().getContextKey(), basket.getAdministratorEmail(ctx));
			}

			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("ecom.reception-message"), GenericMessage.INFO));
		}
		return null;
	}
}
