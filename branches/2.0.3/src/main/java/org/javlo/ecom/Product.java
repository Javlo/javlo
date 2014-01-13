package org.javlo.ecom;

import java.io.Serializable;

import org.javlo.component.ecom.ProductComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class Product {

	public static final class ProductBean implements Serializable {
		private String id;
		private double price;
		private double reduction;
		private double vat;
		private String currencyCode;
		private String name;
		private String description;
		private String imageURL;
		private int quantity = 1;

		public ProductBean() {
		};

		public ProductBean(String id, double price, double reduction, double vat, String currencyCode, String name, String description, String imageURL, int quantity) {
			this.id = id;
			this.price = price;
			this.currencyCode = currencyCode;
			this.name = name;
			this.description = description;
			this.imageURL = imageURL;
			this.quantity = quantity;
			this.reduction = reduction;
			this.vat = vat;
		}
		
		@Override
		public String toString() {
			return getName()+" ("+getId()+")   Quantity:"+getQuantity()+"   Price:"+getPrice()+"   Reduction:"+getReduction()+"   vat:"+getVAT();
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public double getPrice() {
			return price;
		}

		public void setPrice(double price) {
			this.price = price;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getImageURL() {
			return imageURL;
		}

		public void setImageURL(String imageURL) {
			this.imageURL = imageURL;
		}

		public int getQuantity() {
			return quantity;
		}

		public void setQuantity(int quantity) {
			this.quantity = quantity;
		}

		public double getReduction() {
			return reduction;
		}

		public void setReduction(double reduction) {
			this.reduction = reduction;
		}

		public String getCurrencyCode() {
			return currencyCode;
		}

		public void setCurrencyCode(String currencyCode) {
			this.currencyCode = currencyCode;
		}

		public double getVAT() {
			return vat;
		}

		public void setVAT(double vat) {
			this.vat = vat;
		}

	}

	private final ProductComponent comp;

	public Product(ProductComponent comp) {
		this.comp = comp;
	}

	public void reserve(ContentContext ctx) {
		synchronized (comp) {
			comp.setVirtualStock(ctx, comp.getVirtualStock(ctx) - getQuantity());
		}
	}

	public void pay(ContentContext ctx) {
		synchronized (comp) {
			comp.setVirtualStock(ctx, comp.getVirtualStock(ctx) - getQuantity());
			comp.setRealStock(ctx, comp.getRealStock(ctx) - getQuantity());
		}
	}

	private String id = null;
	private String url;
	private String shortDescription;
	private String longDescription;
	private IImageTitle image;
	private String imageURL;
	private int quantity = 1;
	private String fakeName = "no-name";

	public String getName() {
		if (comp == null) {
			return fakeName;
		} else {
			return comp.getName();
		}
	}
	
	public void setFakeName(String name) {
		fakeName = name;
	}

	public String getId() {
		if (id == null) {
			id = StringHelper.getRandomId();
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public double getWeight() {
		return comp.getWeight();
	}

	public double getPrice() {
		if (comp == null) {
			return -1;
		} else {
			return comp.getPrice();
		}
	}

	public double getTotal() {
		double outTotal = getPrice() * getQuantity() * (1 - getReduction());
		return outTotal;
	}

	public double getReduction() {
		if (comp == null) {
			return -1;
		} else {
			return comp.getReduction();
		}
	}

	public double getVAT() {
		return comp.getVAT();
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public IImageTitle getImage() {
		return image;
	}

	public void setImage(ContentContext ctx, IImageTitle image) {
		this.image = new ImageTitleBean(ctx, image);
		try {
			this.imageURL = URLHelper.createTransformURL(ctx, ctx.getCurrentPage(), image.getResourceURL(ctx), "basket");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getImageURL() {
		return imageURL;
	}

	public String getCurrencyCode() {
		return "EUR";
	}

	public String getPriceString() {
		return StringHelper.renderPrice(getPrice(), getCurrencyCode());
	}

	public String getReductionString() {
		return Math.round(getReduction() * 100) + " %";
	}

	public String getTotalString() {
		return StringHelper.renderPrice(getTotal(), getCurrencyCode());
	}

	public static void main(String[] args) {
		Product product = new Product(null);
		System.out.println(ResourceHelper.storeBeanFromXML(product.getBean()));
	}

	public ProductBean getBean() {
		return new ProductBean(getId(), getPrice(), getReduction(), getVAT(), getCurrencyCode(), getName(), getShortDescription(), getImageURL(), getQuantity());
	}

}
