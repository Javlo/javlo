package org.javlo.ecom;

import java.util.LinkedList;
import java.util.List;

import org.javlo.ecom.Product.ProductBean;

import junit.framework.TestCase;

public class BasketTest extends TestCase {

	public BasketTest() {
	}

	public BasketTest(String name) {
		super(name);
	}
	
	public void testTotal() throws Exception {
		Basket basket = new Basket();
		basket.setInfo("Basket info");
		basket.setOrganization("Javlo SA");
		basket.setFirstName("Patrick");
		basket.setLastName("Vandermaesen");
		basket.setAddress("23, Rue Java");
		basket.setZip("1000");
		basket.setCity("Bruxelles");
		basket.setCountry("be");
		basket.setContactEmail("info@javlo.be");
		basket.setContactPhone("0123456789");
		basket.setUserReduction(0);
		
		List<ProductBean> products = new LinkedList<ProductBean>();		
		double total = 0;		
		for (int i = 0; i<(int)Math.round(Math.random()*100)+1; i++) {
			ProductBean product = new ProductBean();
			product.setId("ID-ART-"+i);
			product.setName("Article "+i);
			product.setDescription("Short Desc article "+i);
			double price = Math.random()*100;
			int q = (int)Math.round(Math.random()*10)+1;
			total += price*q;			
			product.setPrice(price);
			product.setCurrencyCode("EUR");
			product.setQuantity(q);
			product.setVAT(0.21);
			product.setReduction(0);
			products.add(product);			
		}	
		basket.setProductsBean(products);
		
		assertEquals(total, basket.getTotal(null, true), 0.01);
		assertEquals(total/1.21, basket.getTotal(null, false), 0.01);
		basket.setUserReduction(0.25);
		total = total * 0.75;
		assertEquals(total, basket.getTotal(null, true), 0.01);
		assertEquals(total/1.21, basket.getTotal(null, false), 0.01);		
	}

}
