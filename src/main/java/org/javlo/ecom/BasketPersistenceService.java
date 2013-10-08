package org.javlo.ecom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;

public class BasketPersistenceService {

	private Map<String, Basket> baskets = null;

	private File dir = null;

	private static final String KEY = BasketPersistenceService.class.getCanonicalName();

	public static BasketPersistenceService getInstance(GlobalContext globalContext) {
		BasketPersistenceService instance = (BasketPersistenceService) globalContext.getAttribute(KEY);
		if (instance == null) {
			instance = new BasketPersistenceService();
			instance.dir = getFolder(globalContext);
			if (!instance.dir.exists()) {
				instance.dir.mkdirs();
			}
			instance.getAllBaskets(); // load baskets
			globalContext.setAttribute(KEY, instance);
		}
		return instance;
	}

	public static File getFolder(GlobalContext globalContext) {
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), "static/ecom/basket"));
	}

	private Basket loadBasket(File file) throws FileNotFoundException {
		return (Basket) ResourceHelper.loadBeanFromXML(file);
	}

	private Map<String, Basket> loadBaskets() {
		Map<String, Basket> baskets = new HashMap<String, Basket>();
		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(".xml")) {
				try {
					Basket basket = loadBasket(file);
					if (basket != null) {
						baskets.put(basket.getId(), basket);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
		return baskets;
	}

	public synchronized Collection<Basket> getAllBaskets() {
		if (baskets == null) {
			baskets = loadBaskets();
		}
		return baskets.values();
	}

	public synchronized void storeBasket(Basket basket) throws IOException {
		File file = new File(URLHelper.mergePath(dir.getAbsolutePath(), basket.getId() + ".xml"));
		if (file.exists()) {
			file.createNewFile();
		}
		ResourceHelper.writeStringToFile(file, ResourceHelper.storeBeanFromXML(basket));
		if (baskets != null) {
			if (baskets.containsKey(basket.getId())) {
				baskets.remove(basket.getId());
			}
			baskets.put(basket.getId(), basket);
		}
	}

	public synchronized Basket getBasket(String id) throws IOException {
		getAllBaskets(); // load basket
		return baskets.get(id);
	}

}
