package org.javlo.ecom;

import org.javlo.component.ecom.ProductComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.image.ImageTitleBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class Product {
	
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
	
	private String id = StringHelper.getRandomId();
	private String url;
	private String shortDescription;
	private String longDescription;
	private IImageTitle image;
	private String imageURL;
	private int quantity = 1;
	
	public String getName() {
		return comp.getName();
	}
	public String getId() {
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
		return comp.getPrice();
	}
	public double getReduction() {
		return comp.getReduction();
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
			this.imageURL = URLHelper.createTransformURL(ctx, ctx.getCurrentPage(), image.getImageURL(ctx), "basket");
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
	
//	@Override
//	public int hashCode() {
//		return getId().hashCode();
//	}
//	@Override
//	public boolean equals(Object obj) {
//		if (obj instanceof Product) {
//			return ((Product) obj).getId().equals(this.getId());
//		}
//		return false;
//	}
}
