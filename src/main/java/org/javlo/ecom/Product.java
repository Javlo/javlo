package org.javlo.ecom;

import java.io.Serializable;
import java.util.logging.Logger;

import org.javlo.component.ecom.ProductComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.owasp.encoder.Encode;

public class Product {
	
	private static Logger logger = Logger.getLogger(Product.class.getName());

	public static final class ProductBean implements Serializable {
		private String id;
		private String pageId;
		private String pageTitle;
		private String pageDescription;
		private double price;
		private String priceString;
		private double reduction;
		private double vat;
		private double weight;
		private String currencyCode;
		private String name;
		private String label;
		private String description;
		private String imageURL;
		private int quantity = 1;
		private String language;

		public ProductBean() {
		};

		public ProductBean(ContentContext ctx, String id, MenuElement page, String lg, double price, String priceString, double reduction, double vat, String currencyCode, String name, String description, String imageURL, int quantity, double weight) throws Exception {
			this.id = id;
			this.pageId = page.getId();
			if (ctx != null) {
				this.pageTitle = page.getPageTitle(ctx);
				this.pageDescription = page.getDescription(ctx).getText();
			}
			this.price = price;
			this.priceString = priceString;
			this.currencyCode = currencyCode;
			this.name = name;
			this.description = description;
			this.imageURL = imageURL;
			this.quantity = quantity;
			this.reduction = reduction;
			this.vat = vat;
			this.weight = weight;
		}
		
		@Override
		public String toString() {
			return getName()+" ("+getId()+")   Quantity:"+getQuantity()+"   Price:"+getPrice()+"   Reduction:"+getReduction()+"   vat:"+getVAT()+"   label:"+label;
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
		
		public String getEspaceName() {
			return Encode.forHtmlAttribute(name);
		}
		
		public String getAttributeName() {
			return StringHelper.doubleQutotes(name);
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

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public String getPageId() {
			return pageId;
		}

		public void setPageId(String pageId) {
			this.pageId = pageId;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getPriceString() {
			return priceString;
		}

		public void setPriceString(String priceString) {
			this.priceString = priceString;
		}

		public String getLabel() {
			if (label == null) {
				return name;
			}
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getPageTitle() {
			return pageTitle;
		}

		public void setPageTitle(String pageTitle) {
			this.pageTitle = pageTitle;
		}

		public String getPageDescription() {
			return pageDescription;
		}

		public void setPageDescription(String pageDescription) {
			this.pageDescription = pageDescription;
		}

	}

	private final ProductComponent comp;
	private final ProductComponent refComp;

	public Product(ProductComponent comp, ProductComponent refComp) {
		this.comp = comp;
		if (refComp != null) {
			this.refComp = refComp;
		} else {
			logger.severe("refComp not found.");
			this.refComp = comp;
		}
		if (comp != null && comp.getComponentBean() != null) {
			this.lang = comp.getComponentBean().getLanguage();
		}
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
	private double price = 0;
	private String fakeName = "no-name";
	private String lang = "en";

	public String getName() {
		if (comp == null) {
			return fakeName;
		} else {
			return comp.getName();
		}
	}
	
	public String getLabel() {
		if (comp == null) {
			return fakeName;
		} else {
			String label = comp.getLabel();
			if (StringHelper.isEmpty(label)) {
				label = getName();
			}
			return label;
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
		return refComp.getWeight();
	}

	public double getPrice() {
		if (refComp == null) {
			return -1;
		} else {
			if (refComp.getPrice() == 0) {
				return price;
			} else {
				return refComp.getPrice();
			}
		}
	}
	
	public double getReductionPrice() {
		if (comp == null) {
			return -1;
		} else {
			return getPrice() * (1 - getReduction());
		}
	}

	public double getTotal() {
		double outTotal = getPrice() * getQuantity() * (1 - getReduction());
		return outTotal;
	}

	public double getReduction() {
		if (refComp == null) {
			return -1;
		} else {
			return refComp.getReduction();
		}
	}

	public double getVAT() {
		return refComp.getVAT();
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
		return Basket.renderPrice(lang, getPrice(), getCurrencyCode());
	}

	public String getReductionString() {
		return Math.round(getReduction() * 100) + " %";
	}

	public String getTotalString() {
		//return StringHelper.renderPrice(getTotal(), getCurrencyCode());
		return Basket.renderPrice(lang, getTotal(), getCurrencyCode());
	}

	public static void main(String[] args) {
		System.out.println(">> "+StringHelper.doubleQutotes("l'iliade"));
	}

	public ProductBean getBean(ContentContext ctx) throws Exception {
		ProductBean bean = new ProductBean(ctx, getId(), comp.getPage(), comp.getComponentBean().getLanguage(), getPrice(), getPriceString(), getReduction(), getVAT(), getCurrencyCode(), getName(), getShortDescription(), getImageURL(), getQuantity(), getWeight());
		return bean;
	}

	public void setPrice(double price) {
		this.price = price;
	}

}
