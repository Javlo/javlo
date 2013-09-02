package org.javlo.ecom;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.CSVFactory;


public class Basket {
	
	private List<Product> products = new LinkedList<Product>();

	private boolean valid = false;
	private boolean confirm = false;
	
	private String id = StringHelper.getShortRandomId();	
	private String contactEmail="";
	private String contactPhone="";
	
	private int step = 1;	

	public static final String KEY = "basket";
	
	public static class PayementServiceBean {
		private PayementExternalService service;
		private String url;
		
		private PayementServiceBean (PayementExternalService inService, String inURL) {
			this.service = inService;
			this.url = inURL;
		}
		
		public String getName() {
			return service.getName();
		}
		
		public String getURL() {
			if (url == null) {
				return service.getURL();
			} else {
				return url;
			}			
		}
	}
	
	private final List<PayementServiceBean> payementServices = new LinkedList<Basket.PayementServiceBean>();

	public static Basket getInstance(ContentContext ctx) {
		Basket basket = (Basket) ctx.getRequest().getSession().getAttribute(KEY);
		if (basket == null) {
			basket = new Basket();
			for (PayementExternalService service : EcomService.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).getExternalService()) {
				String url = service.getURL();
				if ((url == null || url.trim().length() == 0) && service.getReturnPage() != null && service.getReturnPage().trim().length() > 0) {
					try {
						url = URLHelper.createURLFromPageName(ctx, service.getReturnPage());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				basket.payementServices.add(new PayementServiceBean(service, url));
			}
			ctx.getRequest().getSession().setAttribute(KEY, basket);
		}
		 
		return basket;
	}
	
	public static boolean isInstance(ContentContext ctx) {
		return ctx.getRequest().getSession().getAttribute(KEY) != null;
	}

	public void reserve(ContentContext ctx) {
		for (Product product : products) {
			product.reserve(ctx);
		}
	}
	
	private String paypalTX = "";
	public String getPaypalTX() {
		return paypalTX;
	}
	public void pay(ContentContext ctx, String paypalTX) {
		this.paypalTX = paypalTX;
		for (Product product : products) {
			product.pay(ctx);
		}
	}
	
	public List<Product> getProducts() {
		return Collections.unmodifiableList(products);
	}

	public void addProduct(Product product) {
		setValid(false);
		for (Product item : products) {
			if (item.getName().equals(product.getName()) && item.getPrice() == product.getPrice()) {
				item.setQuantity(item.getQuantity() + product.getQuantity());
				return;
			}
		}
		products.add(product);
	}

	public void removeProduct(String id) {
		Iterator<Product> iter = products.iterator();
		while (iter.hasNext()) {
			if (iter.next().getId().equals(id)) {
				setValid(false);
				iter.remove();
			}
		}
	}
	
	public int getProductCount() {
		int count = 0;
		for (Product product : products) {
			count = count + product.getQuantity();
		}
		return count;
	}

	public double getTotalIncludingVAT() {
		double result = 0;
		for (Product product : products) {
			result = result + (product.getPrice() * (1 - product.getReduction()) * product.getQuantity());
		}
		return result + getDeliveryIncludingVAT();
	}
	
	public String getTotalIncludingVATString() {
		return StringHelper.renderPrice(getTotalIncludingVAT(), getCurrencyCode());
	}

	public double getTotalExcludingVAT() {
		double result = 0;
		for (Product product : products) {
			result = result + (((product.getPrice()) * (1 - product.getReduction()) * product.getQuantity()) / (1 + product.getVAT()));			
		}
		return result + getDeliveryExcludingVAT();
	}
	
	public String getTotalExcludingVATString() {
		return StringHelper.renderPrice(getTotalExcludingVAT(), getCurrencyCode());		
	}

	public double getDeliveryIncludingVAT() {
		double result = 0;
		if (!pickup) {
			if (getDeliveryZone() != null && getDeliveryZone().getPrices() != null && getDeliveryZone().getPrices().size() > 0) {
				
				// pick url should exist here, assert ?
				int number = 0;
				for (Product product : products) {
					number = number + (product.getQuantity() * (int) product.getWeight());
					//result = result + ((product.getPrice() / (1 + product.getVAT())) * (1 - product.getReduction())*product.getQuantity());
				}

				// TODO: ensure increasing order in zones file
				Set<Integer> offsets = new TreeSet<Integer>(getDeliveryZone().getPrices().keySet());
				int up = 0;
				for (int offset : offsets) {
					up = offset;
					if (up >= number) {
						break;
					}
				}
				if (up > 0) {
					int units = number / up;
					if (number > up) {
						units = units + 1;
					}
					result = units * getDeliveryZone().getPrices().get(up);
				}
			}
		}
		return result;
	}

	public double getDeliveryExcludingVAT() {
		return getDeliveryIncludingVAT() / 1.21;
	}

	public int getSize() {
		return products.size();
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public void init(ContentContext ctx) {
		ctx.getRequest().getSession().removeAttribute(KEY);
	}

	public String getId() {
		return id;
	}
	
	public String getStructutedCommunication() {
		return StringHelper.encodeAsStructuredCommunicationMod97(getId());
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getCurrencyCode() {
		String currencyCode = null;
		for (Product product : products) {
			if ((currencyCode != null)&&(!currencyCode.equals(product.getCurrencyCode()))) {
				return null;
			}
			currencyCode = product.getCurrencyCode();
		}
		return currencyCode;
	}

	public boolean isConfirm() {
		return confirm;
	}

	public void setConfirm(boolean confirm) {
		this.confirm = confirm;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public void setContactEmail(String contactEmail) {
		this.contactEmail = contactEmail;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	
	private String firstName = "";
	private String lastName = "";
	private String organization = "";
	private String vatNumber = "";
	private boolean pickup = false;
	private String address = "";
	
public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getVATNumber() {
		return vatNumber;
	}

	public void setVATNumber(String vatNumber) {
		this.vatNumber = vatNumber;
	}

	public boolean isPickup() {
		return pickup;
	}

	public void setPickup(boolean pickup) {
		this.pickup = pickup;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	private DeliveryZone zone;
	public DeliveryZone getDeliveryZone() {
		return zone;
	}
	public void setDeliveryZone(DeliveryZone zone) {
		this.zone= zone;
	}
	private List<DeliveryZone> zones;
	public List<DeliveryZone> getDeliveryZones(ContentContext ctx) {
		if (zones == null) {
			try {
				String productsPath = URLHelper.createStaticTemplateURL(ctx, ctx.getCurrentTemplate(), "ecom_zones.csv");
				InputStream in = ctx.getRequest().getSession().getServletContext().getResourceAsStream(productsPath);

				CSVFactory fact = new CSVFactory(in);
				String[][] csv = fact.getArray();

				zones = new ArrayList<DeliveryZone>();
				int i = 1;
				while (i < csv.length) {
					String zone = csv[i][0];
					String pickupURL = csv[i][4];
					if (!csv[i][1].equals("")) {
						zones.add(new DeliveryZone(zone, csv[i][1], ctx));
						i++;
					} else {
						Map<Integer,Float> prices = new HashMap<Integer, Float>();
						do {
							int offset = Integer.valueOf(csv[i][2]);
							float price = Float.valueOf(csv[i][3]);
							prices.put(offset, price);
							
							i++;
						} while (i < csv.length && csv[i][0].equals(""));
						DeliveryZone newZone = new DeliveryZone(zone, prices, ctx);
						if (pickupURL != null && pickupURL.length() > 0) {
							newZone.setPickupURL(pickupURL);
						}
						zones.add(newZone);
					}
				}
				zones.add(new DeliveryZone("other", "", ctx));
			} catch (Exception e) {
				e.printStackTrace();
				zones = Collections.emptyList();
			}
		}
		return zones;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}
	
	public List<PayementServiceBean> getServices() {
		return payementServices;
	}
	
	public static void main(String[] args) {
		String id = StringHelper.getShortRandomId();
		System.out.println("***** Basket.main : id = "+id); //TODO: remove debug trace
		System.out.println("***** Basket.main : lg = "+id.length()); //TODO: remove debug trace
		id = StringHelper.getShortRandomId();
		System.out.println("***** Basket.main : id = "+id); //TODO: remove debug trace
		System.out.println("***** Basket.main : lg = "+id.length()); //TODO: remove debug trace
		id = StringHelper.getShortRandomId();
		System.out.println("***** Basket.main : id = "+id); //TODO: remove debug trace
		System.out.println("***** Basket.main : lg = "+id.length()); //TODO: remove debug trace
	}
	
	
}
