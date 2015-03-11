/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.ecom;

import java.util.Arrays;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.Product;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserInfo;

/**
 * @author pvandermaesen
 */
public class EditBasketComponent extends AbstractPropertiesComponent implements IAction {
	
	private static final String PROMO_VALUE = "promo-value";
	private static final String PROMO_KEY = "promo-key";
	private static final String SHIPPING_MESSAGE = "shipping-message";
	
	static final List<String> FIELDS = Arrays.asList(new String[] { SHIPPING_MESSAGE, PROMO_KEY, PROMO_VALUE });

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
					basket.setFirstName(ui.getFirstName());
					basket.setLastName(ui.getLastName());
					basket.setAddress(ui.getAddress());
					basket.setZip(ui.getPostCode());
					basket.setCity(ui.getCity());
					basket.setCountry(ui.getCountry());
					basket.setContactEmail(ui.getEmail());
					basket.setContactPhone(ui.getMobile());
					basket.setTransfertAddressLogin(ctx.getCurrentUser().getLogin());
					basket.setOrganization(ui.getOrganization());
					basket.setVATNumber(ui.getVat());
				}
			}
		}

		ctx.getRequest().setAttribute("reduction", reduction);		
		if (basket.getUserReduction() > 0) {
			ctx.getRequest().setAttribute("userReduction",  '-'+StringHelper.renderDoubleAsPercentage(basket.getUserReduction()));
		}
		ctx.getRequest().setAttribute("basket", basket);
		ctx.getRequest().setAttribute("totalNoVAT", basket.getTotalString(ctx, false));
		ctx.getRequest().setAttribute("total", basket.getTotalString(ctx, true));
		ctx.getRequest().setAttribute("promo", !StringHelper.isEmpty(getFieldValue(PROMO_KEY)));
		if (basket.getDeliveryZone() != null) {
			double delivery = basket.getDelivery(ctx, true);
			if (delivery > 0) {
				ctx.getRequest().setAttribute("deliveryStr", Basket.renderPrice(ctx, delivery, basket.getCurrencyCode()));
				if (delivery > 0) {
					ctx.getRequest().setAttribute("deliveryStr", Basket.renderPrice(ctx, delivery, basket.getCurrencyCode()));
				}
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

	public static String performConfirm(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		String promoCode = ctx.getRequest().getParameter("promo-code");		
		if (promoCode != null && promoCode.trim().length() > 0) {
			EditBasketComponent comp = (EditBasketComponent)ComponentHelper.getComponentFromRequest(ctx);
			if (promoCode.equals(comp.getFieldValue(PROMO_KEY))) {				
				basket.setStep(Basket.REGISTRATION_STEP);
				basket.setUserReduction(Double.parseDouble(comp.getFieldValue(PROMO_VALUE))/100);
			} else {
				return "wrong promo code.";
			}			
		} else {			
			basket.setStep(Basket.REGISTRATION_STEP);
		}
		return null;
	}

	public static String performRegistration(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);

		if (rs.getParameter("back", null) != null) {
			if (basket.getStep() > 1) {
				basket.setStep(basket.getStep() - 1);
			}
			return null;
		}

		String firstName = rs.getParameter("firstName", "").trim();
		String lastName = rs.getParameter("lastName", "").trim();
		String email = rs.getParameter("email", "").trim();
		String phone = rs.getParameter("phone", "").trim();
		String country = rs.getParameter("country", "").trim();
		String address = rs.getParameter("address", "").trim();
		String zip = rs.getParameter("zip", "").trim();
		String city = rs.getParameter("city", "").trim();
		boolean noShipping = rs.getParameter("noshipping", null) != null;

		String vta = rs.getParameter("vat", "").trim();
		String company = rs.getParameter("organization", "").trim();

		if (firstName.length() == 0 || lastName.length() == 0 || email.length() == 0 || country.length() == 0 || address.length() == 0 || zip.length() == 0 || city.length() == 0) {
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("global.compulsory-field"), GenericMessage.ERROR));
		} else {
			if (!StringHelper.isMail(email)) {
				messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getViewText("mailing.error.email"), GenericMessage.ERROR));
			} else {
				basket.setFirstName(firstName);
				basket.setLastName(lastName);
				basket.setContactEmail(email);
				basket.setContactPhone(phone);
				basket.setCountry(country);
				basket.setAddress(address);
				basket.setZip(zip);
				basket.setCity(city);
				basket.setStep(Basket.ORDER_STEP);
				basket.setVATNumber(vta);
				basket.setOrganization(company);
				basket.setNoShipping(noShipping);
			}
		}

		return null;
	}

	public static String performReset(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
		basket.reset(ctx);
		return null;
	}

	public static String performBack(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
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
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

}
