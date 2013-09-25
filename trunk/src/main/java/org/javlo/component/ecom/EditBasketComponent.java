/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.ecom;

import org.javlo.actions.IAction;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.Product;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;


/**
 * @author pvandermaesen
 */
public class EditBasketComponent extends AbstractVisualComponent implements IAction {
	
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
		ctx.getRequest().setAttribute("reduction", reduction);
		ctx.getRequest().setAttribute("basket", basket);
	}

	public String getType() {
		return "edit-basket";
	}

	@Override
	public String getHexColor() {
		return ECOM_COLOR;
	}

	public static String performConfirm(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
		basket.setStep(Basket.REGISTRATION_STEP);
		return null;
	}
	
	public static String performRegistration(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		Basket basket = Basket.getInstance(ctx);
		basket.setStep(Basket.ORDER_STEP);
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "basket";
	}

}
