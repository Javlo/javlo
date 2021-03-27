package org.javlo.actions;

import java.util.Date;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;

public class DefaultEcomLister implements IEcomListner {

	@Override
	public EcomStatus onConfirmBasket(ContentContext ctx, Basket basket) throws ListerException {
		return EcomStatus.VALID;
	}

	@Override
	public EcomStatus onConfirmPayment(ContentContext ctx, Basket basket) throws ListerException {
		return EcomStatus.VALID;
	}

	@Override
	public List<Date> getDeliveryDate(ContentContext ctx, Basket basket) {
		return null;
	}

}
