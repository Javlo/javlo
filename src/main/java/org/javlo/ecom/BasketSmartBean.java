package org.javlo.ecom;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Product.ProductBean;
import org.javlo.helper.StringHelper;

public class BasketSmartBean {
	
	private ContentContext ctx;
	private Basket basket;
	
	public static List<BasketSmartBean> getListInstance(ContentContext ctx, Collection<Basket> baskets) {
		List<BasketSmartBean> outList = new LinkedList<BasketSmartBean>();
		for (Basket basket : baskets) {
			outList.add(new BasketSmartBean(ctx, basket));
		}
		return outList;
	}

	public BasketSmartBean(ContentContext ctx, Basket basket) {
		this.ctx = ctx;
		this.basket = basket;
	}
	
	public String getId() {
		return basket.getId();
	}
	
	public String getTotalExcludingVATString() {
		return StringHelper.renderPrice(basket.getTotal(ctx, false), basket.getCurrencyCode());		 
	}
	
	public String getTotalIncludingVATString() {
		return StringHelper.renderPrice(basket.getTotal(ctx, true), basket.getCurrencyCode());		 
	}
	
	public String getDateString() throws FileNotFoundException, IOException {
		return StringHelper.renderShortDate(ctx, basket.getDate());
	}
	
	public String getStatus() {
		return basket.getStatus();
	}
	
	public String getValidationInfo() {
		return basket.getValidationInfo();
	}
	
	public List<ProductBean> getProductsBean() {
		return basket.getProductsBean();
	}
	
	public boolean isDeleted() {
		return basket.isDeleted();
	}
	
	public boolean isReadyToSend() {
		return basket.isReadyToSend();
	}
	 

}
