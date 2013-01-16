package org.javlo.ecom;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.i18n.I18nAccess;

public class DeliveryZone {

	private GlobalContext globalContext;
	private HttpSession session;
	private String name;
	public String getName() {
		return name;
	}
	private DeliveryZone(String name, ContentContext ctx) {
		this.name = name;
		this.globalContext = GlobalContext.getInstance(ctx.getRequest());
		this.session = ctx.getRequest().getSession();
	}
	public String getLabel() {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(globalContext, session);
			return i18nAccess.getViewText(name);
		} catch (Exception e) {
			e.printStackTrace();
			return getName();
		}
	}

	private String url;
	public String getURL() {
		return url;
	}
	public DeliveryZone(String name, String url, ContentContext ctx) {
		this(name, ctx);
		
		this.url = url;
		this.prices = Collections.emptyMap();
	}

	private Map<Integer,Float> prices = new HashMap<Integer,Float>();
	public Map<Integer,Float> getPrices() {
		return prices;
	}
	public DeliveryZone(String name, Map<Integer,Float> prices, ContentContext ctx) {
		this(name, ctx);
		
		this.prices = prices;
		this.url = null;
	}
	
	private String pickupURL;
	public void setPickupURL(String pickupURL) {
		this.pickupURL = pickupURL;
	}
	public String getPickupURL() {
		return pickupURL;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DeliveryZone) {
			return this.name.equals(((DeliveryZone) obj).name);
		}
		return false;
	}
}
