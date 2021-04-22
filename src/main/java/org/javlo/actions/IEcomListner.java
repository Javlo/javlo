package org.javlo.actions;

import java.util.Date;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Basket;

public interface IEcomListner {
	
	public EcomStatus onConfirmBasket(ContentContext ctx, Basket basket) throws ListerException;
	
	public EcomStatus onConfirmPayment(ContentContext ctx, Basket basket) throws ListerException;
	
	public List<Date> getDeliveryDate(ContentContext ctx, Basket basket);
	
	public EcomStatus onPaymentProcessorEvent(EcomEvent event);

}
