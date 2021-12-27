package org.javlo.ecom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.ecom.ProductComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Product.ProductBean;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.TimeMap;

import com.google.gson.Gson;

public class EcomService {

	private static Map<String, IbanInfo> bicCache = new TimeMap<>(60 * 60 * 24 * 365, 100000);

	private static final String KEY = EcomService.class.getName();
	private static final String COMMAND_FILE = "/ecom/commands.csv";
	private static final String PAYEMENT_PREFIX = "__PAY_SRV_";

	private File commandsFile = null;

	private GlobalContext globalContext;

	public static EcomService getInstance(GlobalContext globalContext, HttpSession session) {

		StaticConfig staticConfig = StaticConfig.getInstance(session);

		EcomService ecomService = (EcomService) session.getAttribute(KEY);
		if (ecomService == null) {
			ecomService = new EcomService();
			ecomService.commandsFile = new File(URLHelper.mergePath(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder()), COMMAND_FILE));
			ecomService.commandsFile.getParentFile().mkdirs();
			session.setAttribute(KEY, ecomService);
		}
		ecomService.globalContext = globalContext;
		return ecomService;
	}

	public void storeBasket(Basket basket) {

		final int LINE_SIZE = 18;

		synchronized (COMMAND_FILE) {
			try {
				String[][] data;
				CSVFactory fact;
				if (!commandsFile.exists() || commandsFile.length() == 0) {
					if (!commandsFile.exists()) {
						commandsFile.createNewFile();
					}
					data = new String[1][];
					data[0] = new String[LINE_SIZE];
					int i = 0;
					data[0][i] = "command id";
					i++;
					data[0][i] = "zone";
					i++;
					data[0][i] = "email";
					i++;
					data[0][i] = "firstname";
					i++;
					data[0][i] = "lastname";
					i++;
					data[0][i] = "phone";
					i++;
					data[0][i] = "organization";
					i++;
					data[0][i] = "vatnumber";
					i++;
					data[0][i] = "address";
					i++;
					data[0][i] = "date";
					i++;
					data[0][i] = "time";
					i++;
					data[0][i] = "product id";
					i++;
					data[0][i] = "title";
					i++;
					data[0][i] = "name";
					i++;
					data[0][i] = "price";
					i++;
					data[0][i] = "quantity";
					i++;
					data[0][i] = "reduction";
					i++;
					data[0][i] = "paypal";
					i++;
					fact = new CSVFactory(commandsFile);
				} else {
					fact = new CSVFactory(commandsFile);
					data = fact.getArray();
				}
				String[][] newData = new String[data.length + basket.getSize() + 1][];
				for (int j = 0; j < data.length; j++) {
					newData[j] = new String[LINE_SIZE];
					for (int j2 = 0; j2 < data[j].length; j2++) {
						newData[j][j2] = data[j][j2];
					}
				}
				List<Product> products = basket.getProducts();
				newData[data.length] = new String[LINE_SIZE];
				for (int j = 0; j < newData[data.length].length; j++) {
					newData[data.length][j] = "";
				}
				Date date = new Date();
				int i = 1;
				for (Product product : products) {
					int j = 0;
					newData[data.length + i] = new String[LINE_SIZE];
					newData[data.length + i][0] = "'" + basket.getId();
					j++;
					newData[data.length + i][j] = basket.getDeliveryZone() == null ? "" : basket.getDeliveryZone();
					j++;
					newData[data.length + i][j] = basket.getContactEmail();
					j++;
					newData[data.length + i][j] = basket.getFirstName();
					j++;
					newData[data.length + i][j] = basket.getLastName();
					j++;
					newData[data.length + i][j] = basket.getContactPhone();
					j++;
					newData[data.length + i][j] = basket.getOrganization();
					j++;
					newData[data.length + i][j] = basket.getVATNumber();
					j++;
					newData[data.length + i][j] = basket.getAddress().replace(System.getProperty("line.separator"), "\\");
					j++;
					newData[data.length + i][j] = StringHelper.renderDate(date);
					j++;
					newData[data.length + i][j] = StringHelper.renderOnlyTime(date);
					j++;
					newData[data.length + i][j] = "'" + product.getId();
					j++;
					newData[data.length + i][j] = product.getShortDescription();
					j++;
					newData[data.length + i][j] = product.getName();
					j++;
					newData[data.length + i][j] = "" + product.getPrice();
					j++;
					newData[data.length + i][j] = "" + product.getQuantity();
					j++;
					newData[data.length + i][j] = "" + product.getReduction();
					j++;
					i++;
				}
				fact = new CSVFactory(newData);
				OutputStream out = new FileOutputStream(commandsFile);
				fact.exportCSV(out);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * return the external service (as paypal, Ogone...) for pay on the site.
	 * 
	 * @return
	 */
	public List<PayementExternalService> getExternalService() {
		List<PayementExternalService> outServices = new LinkedList<PayementExternalService>();
		Map<String, String> payementList = globalContext.getDataWidthKeyPrefix(PAYEMENT_PREFIX);
		for (String payementServiceRAW : payementList.values()) {
			PayementExternalService service = new PayementExternalService(payementServiceRAW);
			outServices.add(service);
		}
		return outServices;
	}

	public PayementExternalService getExternalService(String name) {
		for (PayementExternalService service : getExternalService()) {
			if (service.getName().equals(name)) {
				return service;
			}
		}
		return null;
	}
	
	public List<ProductBean> getProducts(ContentContext ctx) throws Exception {
		List<ProductBean> out = new LinkedList<>();
		List<IContentVisualComponent> comps = ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, ProductComponent.TYPE);
		for(IContentVisualComponent comp : comps) {
			Product product = new Product((ProductComponent)comp, (ProductComponent)comp.getReferenceComponent(ctx));
			MenuElement page = comp.getPage();
			IImageTitle img = page.getImage(ctx);
			if (img != null) {
				product.setImage(ctx, img);
			}
			out.add(product.getBean(ctx));
		}
		return out;
	}
	
	public List<ProductBean> getProductsAllLanguages(ContentContext ctx) throws Exception {
		List<ProductBean> out = new LinkedList<>();
		for (String lg : ctx.getGlobalContext().getContentLanguages()) {
			ContentContext lgCtx = new ContentContext(ctx);
			ctx.setAllLanguage(lg);
			List<IContentVisualComponent> comps = ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(lgCtx, ProductComponent.TYPE);
			for(IContentVisualComponent comp : comps) {
				out.add(new Product((ProductComponent)comp, (ProductComponent)comp.getReferenceComponent(ctx)).getBean(ctx));
			}
		}
		return out;
	}
	
	public ProductBean getProducts(ContentContext ctx, String id) throws Exception {
		List<IContentVisualComponent> comps = ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, ProductComponent.TYPE);
		for(IContentVisualComponent comp : comps) {
			ProductBean bean = new Product((ProductComponent)comp, (ProductComponent)comp.getReferenceComponent(ctx)).getBean(ctx);
			if (bean.getId().equals(id)) {
				return bean;
			}
		}
		return null;
	}
	
	public List<ProductBean> getActiveProducts(ContentContext ctx) throws Exception {
		List<ProductBean> out = new LinkedList<>();
		List<IContentVisualComponent> comps = ContentService.getInstance(ctx.getGlobalContext()).getComponentByType(ctx, ProductComponent.TYPE);
		for (IContentVisualComponent comp : comps) {
			Product product = new Product((ProductComponent)comp, (ProductComponent)comp.getReferenceComponent(ctx));
			MenuElement page = comp.getPage();
			if (page.isActive(ctx)) {
				IImageTitle img = page.getImage(ctx);
				if (img != null) {
					product.setImage(ctx, img);
				}
				out.add(product.getBean(ctx));
			}
		}
		return out;
	}
	

	public ProductBean getProductsOnPage(ContentContext ctx, String pageId) throws Exception {
		ContentService contentService = ContentService.getInstance(ctx.getGlobalContext());
		
		MenuElement page = contentService.getNavigation(ctx).searchChildFromId(pageId);
		if (page != null) {
			List<IContentVisualComponent> comps = page.getContentByType(ctx, ProductComponent.TYPE);
			if (comps.size() > 0) {
				return new Product((ProductComponent)comps.get(0), (ProductComponent)comps.get(0).getReferenceComponent(ctx)).getBean(ctx);
			}
		}
		return null;
	}

	public void storePayementService(PayementExternalService service) {
		if (!service.getName().equals(PAYEMENT_PREFIX + service.getInitialName())) {
			globalContext.removeData(PAYEMENT_PREFIX + service.getInitialName());
		}
		globalContext.setData(PAYEMENT_PREFIX + service.getName(), service.toString());
	}

	public void deletePayementService(String name) {
		globalContext.removeData(PAYEMENT_PREFIX + name);
	}

	public double getDefaultDelivery() {
		return Double.parseDouble(globalContext.getData("ecom.default-delivery", "0"));
	}

	public void setDefaultDelivery(Double price) {
		globalContext.setData("ecom.default-delivery", "" + price);
	}

	/**
	 * extract bic code from online service openiban.com
	 * 
	 * @param ibam
	 * @return null if invalid iban
	 * @throws Exception
	 * @throws MalformedURLException
	 */
	public static IbanInfo getInfoFromIban(String iban) throws MalformedURLException, Exception {
		if (bicCache.get(iban) != null) {
			return bicCache.get(iban);
		} else {
			String url = "https://openiban.com/validate/" + iban + "?getBIC=true";
			String jsonStr = NetHelper.readPageGet(new URL(url));
			IbanInfo info = new Gson().fromJson(jsonStr, IbanInfo.class);
			bicCache.put(iban, info);
			return info;
		}
	}
}

