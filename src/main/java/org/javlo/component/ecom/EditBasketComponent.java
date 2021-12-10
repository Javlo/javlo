/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.ecom;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.javlo.actions.EcomStatus;
import org.javlo.actions.IAction;
import org.javlo.actions.ListerException;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.Product;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.TimeHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;

/**
 * @author pvandermaesen
 */
public class EditBasketComponent extends AbstractPropertiesComponent implements IAction {

	private static final String PROMO_VALUE = "promo-value";
	private static final String PROMO_KEY = "promo-key";
	private static final String SHIPPING_MESSAGE = "shipping-message.i18n";

	static final List<String> FIELDS = Arrays.asList(new String[] { SHIPPING_MESSAGE, PROMO_KEY, PROMO_VALUE });

	@Override
	protected boolean isAllTranslated() {
		return false;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Basket basket = Basket.getInstance(ctx);
		boolean reduction = false;
		for (Product product : basket.getProducts()) {
			if (product.getReduction() > 0) {
				reduction = true;
			}
		}

		if (ctx.getCurrentUser() != null) {
			if (!basket.getTransfertAddressLogin().equals(ctx.getCurrentUser().getLogin())) {
				IUserInfo iui = ctx.getCurrentUser().getUserInfo();
				if (iui instanceof UserInfo) {
					UserInfo ui = (UserInfo) iui;
					/*basket.setFirstName(ui.getFirstName());
					basket.setLastName(ui.getLastName());
					basket.setAddress(ui.getAddress());
					basket.setZip(ui.getPostCode());
					basket.setCity(ui.getCity());
					basket.setCountry(ui.getCountry());
					basket.setContactEmail(ui.getEmail());
					basket.setContactPhone(ui.getMobile());
					basket.setTransfertAddressLogin(ctx.getCurrentUser().getLogin());
					basket.setOrganization(ui.getOrganization());
					basket.setVATNumber(ui.getVat());*/
					
					basket.setCustomerFirstName(ui.getFirstName());
					basket.setCustomerLastName(ui.getLastName());
					basket.setCustomerEmail(ui.getEmail());
					basket.setCustomerPhone(ui.getMobile());
				}
			}
		}

		ctx.getRequest().setAttribute("reduction", reduction);
		if (basket.getUserReduction() > 0) {
			ctx.getRequest().setAttribute("userReduction", '-' + StringHelper.renderDoubleAsPercentage(basket.getUserReduction()));
		}
		ctx.getRequest().setAttribute("deliveryDates", ctx.getGlobalContext().getStaticConfig().getEcomLister().getDeliveryDate(ctx, basket));
		ctx.getRequest().setAttribute("basket", basket);
		ctx.getRequest().setAttribute("totalNoVAT", basket.getTotalString(ctx, false));
		ctx.getRequest().setAttribute("total", basket.getTotalString(ctx, true));
		ctx.getRequest().setAttribute("value", StringHelper.renderDouble(basket.getTotal(ctx, true), 2));
		ctx.getRequest().setAttribute("tax", StringHelper.renderDouble(basket.getTotal(ctx, true) - basket.getTotal(ctx, false), 2));
		ctx.getRequest().setAttribute("promo", !StringHelper.isEmpty(getFieldValue(PROMO_KEY)));
		if (basket.getDeliveryZone() != null) {
			double delivery = basket.getDelivery(ctx, true);
			if (delivery > 0) {
				ctx.getRequest().setAttribute("deliveryStr", Basket.renderPrice(ctx, delivery, basket.getCurrencyCode()));
			}
		}
		if (basket.getStep() >= Basket.FINAL_STEP) {
			ctx.getRequest().setAttribute("reset", "true");
			Basket.setInstance(ctx, null); // display final step and remove
		} else {
			ctx.getRequest().setAttribute("shippingMessage", XHTMLHelper.textToXHTML(getFieldValue(SHIPPING_MESSAGE), ctx.getGlobalContext()));
		}
	}

	public String getType() {
		return "edit-basket";
	}

	@Override
	public String getHexColor() {
		return ECOM_COLOR;
	}
	
	private static String checkPromoCode (ContentContext ctx, boolean nextStep) throws ServiceException, Exception {
		Basket basket = Basket.getInstance(ctx);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		String promoCode = ctx.getRequest().getParameter("promo-code");
		if (promoCode != null && promoCode.trim().length() > 0) {
			EditBasketComponent comp = (EditBasketComponent) ComponentHelper.getComponentFromRequest(ctx);
			if (promoCode.equals(comp.getFieldValue(PROMO_KEY))) {
				basket.setUserReduction(Double.parseDouble(comp.getFieldValue(PROMO_VALUE)) / 100);
				if (nextStep) {
					basket.setStep(Basket.REGISTRATION_STEP);
				}
			} else {
				return i18nAccess.getViewText("ecom.error.promo-code"); 
			}
		} else {
			if (nextStep) {
				basket.setStep(Basket.REGISTRATION_STEP);
			}
		}
		return null;
	}

	public static String performConfirm(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		return checkPromoCode(ctx, true);
	}

	public static String performRegistration(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws SecurityException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ListerException {
		Basket basket = Basket.getInstance(ctx);

		String firstName = rs.getParameter("firstName", "").trim();
		String lastName = rs.getParameter("lastName", "").trim();
		String email = rs.getParameter("email", "").trim();
		String phone = rs.getParameter("phone", "").trim();
		String country = rs.getParameter("country", "").trim();
		String address = rs.getParameter("address", "").trim();
		String number = rs.getParameter("number", "").trim();
		if (!StringHelper.isEmpty(number)) {
			address += ' ' + number;
		}
		String box = rs.getParameter("box", "").trim();
		String zip = rs.getParameter("zip", "").trim();
		String city = rs.getParameter("city", "").trim();
		boolean noShipping = rs.getParameter("noshipping", null) != null;
		
		String vta = rs.getParameter("vat", "").trim();
		String company = rs.getParameter("organization", "").trim();

		basket.setFirstName(firstName);
		basket.setLastName(lastName);
		basket.setContactEmail(email);
		basket.setContactPhone(phone);
		basket.setCountry(country);
		basket.setAddress(address);
		basket.setBox(box);
		basket.setZip(zip);
		basket.setCity(city);
		basket.setVATNumber(vta);
		basket.setOrganization(company);
		basket.setDeliveryInstructions(rs.getParameter("deliveryInstructions"));
		basket.setGiftSender(rs.getParameter("giftSender"));
		basket.setGiftReceiver(rs.getParameter("giftReceiver"));
		basket.setGiftMessage(rs.getParameter("giftMessage"));
		basket.setNoShipping(noShipping);
		
		/** customer **/
		
		String customerEmail = rs.getParameter("customerEmail",null);
		
		basket.setCustomerFirstName(rs.getParameter("customerFirstName"));
		basket.setCustomerLastName(rs.getParameter("customerLastName"));
		basket.setCustomerEmail(customerEmail);
		basket.setCustomerPhone(rs.getParameter("customerPhone"));

		/** billing **/
		basket.setBillingName(rs.getParameter("billingName"));
		basket.setBillingAddress(rs.getParameter("billingAddress"));
		basket.setBillingCity(rs.getParameter("billingCity"));
		basket.setBillingCountry(rs.getParameter("billingCountry"));
		basket.setBillingPostcode(rs.getParameter("billingPostcode"));
		basket.setBillingVat(rs.getParameter("billingVat"));
		
		/** if VAT number defined >> all fields are needed **/
		if (!StringHelper.isEmpty(basket.getBillingVat())) {
			if (StringHelper.isOneEmpty(basket.getBillingName(), basket.getBillingAddress(), basket.getBillingCity(), basket.getBillingPostcode())) {
				return i18nAccess.getViewText("ecom.error.billing-info");
			}
		}
		
		if (rs.getParameter("back", null) != null) {
			if (basket.getStep() > 1) {
				basket.setStep(basket.getStep() - 1);
			}
			return null;
		}

		if (!StringHelper.isEmpty(rs.getParameter("deliveryDate"))) {
			try {
				
				Date deliveryDate = StringHelper.parseInputDate(rs.getParameter("deliveryDate"));
				boolean found = false;
				for (Date possibleDate : ctx.getGlobalContext().getStaticConfig().getEcomLister().getDeliveryDate(ctx, basket)) {
					if (TimeHelper.isEqualForDay(possibleDate, deliveryDate)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return i18nAccess.getViewText("ecom.error.bad-delivery-date");
				}
				basket.setDeliveryDate(StringHelper.parseInputDate(rs.getParameter("deliveryDate")));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else if (rs.getParameter("deliveryDate") != null) {
			return i18nAccess.getViewText("ecom.error.no-delivery-date");
		}
		
		EcomStatus status = ctx.getGlobalContext().getStaticConfig().getEcomLister().onConfirmBasket(ctx, basket);
		if (status.isError()) {
			return status.getMessage();
		}
		
		if (customerEmail != null && !StringHelper.isMail(customerEmail)) {
			return i18nAccess.getViewText("mailing.error.email");
		}

		if (firstName.length() == 0 || lastName.length() == 0 || country.length() == 0 || address.length() == 0 || zip.length() == 0 || city.length() == 0) {
			String msg = i18nAccess.getViewText("global.compulsory-field");
			if (!StringHelper.isEmpty(status.getMessage())) {
				msg = status.getMessage();
			}
			messageRepository.setGlobalMessage(new GenericMessage(msg, GenericMessage.ERROR));
		} else {
			if (!StringHelper.isEmpty(email) && !StringHelper.isMail(email)) {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("mailing.error.email"), GenericMessage.ERROR));
			} else {
				basket.setStep(Basket.ORDER_STEP);
			}
		}

		return null;
	}
	
	public static String performPay(ContentContext ctx, RequestService rs) throws Exception {
		
		boolean accept = StringHelper.isTrue(rs.getParameter("accept"));
		if (!accept) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			return i18nAccess.getViewText("ecom.error.accept");
		}
		
		Basket basket = Basket.getInstance(ctx);
		basket.setStep(Basket.PAY_STEP);
		return null;
	}

	public static String performReset(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
		basket.reset(ctx);
		ctx.getRequest().setAttribute("manualReset", true);
		return null;
	}

	public static String performUpdate(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws ServiceException, Exception {
		Basket basket = Basket.getInstance(ctx);
		String msg = null;
		if (!basket.isLock()) {
			for (Product p : basket.getProducts()) {
				String quantity = rs.getParameter("q-" + p.getId());
				if (StringHelper.isDigit(quantity)) {
					p.setQuantity(Integer.parseInt(quantity));
				}
			}
			msg = checkPromoCode(ctx, false);
		} else {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("ecom.basket-lock"), GenericMessage.ALERT));
		}
		return msg;
	}

	public static String performBack(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
		basket.setLock(false);
		if (basket.getStep() > 1) {
			basket.setStep(basket.getStep() - 1);
		}
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "basket";
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

}
