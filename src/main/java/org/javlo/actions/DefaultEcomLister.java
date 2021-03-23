package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;

public class DefaultEcomLister implements IEcomListner {

	@Override
	public EcomStatus onConfirmBasket(ContentContext ctx, Basket basket) throws ListerException {
		return EcomStatus.VALID;
	}

}
