package org.javlo.actions;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;

public interface IEcomListner {
	
	public EcomStatus onConfirmBasket(ContentContext ctx, Basket basket) throws ListerException;

}
