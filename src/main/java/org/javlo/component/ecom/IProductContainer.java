package org.javlo.component.ecom;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Product.ProductBean;

public interface IProductContainer {
	
	public ProductBean getProductBean(ContentContext ctx);

}
