package org.javlo.ecom;

import java.util.LinkedList;
import java.util.List;

import org.javlo.ecom.Product.ProductBean;

public class TestBasket extends Basket {

	public TestBasket() {
	};

	@Override
	public String getFirstName() {
		return "first name";
	}

	@Override
	public String getLastName() {
		return "last name";
	}

	@Override
	public double getTotalExcludingVAT() {
		return 1;
	}

	@Override
	public double getTotalIncludingVAT() {
		return 1.21;
	}

	@Override
	public String getCity() {
		return "city";
	}

	@Override
	public String getZip() {
		return "1234";
	}

	@Override
	public String getCountry() {
		return "be";
	}

	@Override
	public String getContactEmail() {
		return "test@javlo.org";
	}

	@Override
	public String getContactPhone() {
		return "0123456789";
	}

	@Override
	public String getCurrencyCode() {
		return "EUR";
	}

	@Override
	public List<ProductBean> getProductsBean() {
		List<ProductBean> outProducts = new LinkedList<ProductBean>();
		for (int i = 0; i < 3; i++) {
			ProductBean prd = new ProductBean();
			prd.setPrice(i*2);
			prd.setCurrencyCode("EUR");
			prd.setDescription("article-" + i+" "+prd.getPrice());
			prd.setVAT(0.06);
			outProducts.add(prd);
		}
		return outProducts;
	}
}
