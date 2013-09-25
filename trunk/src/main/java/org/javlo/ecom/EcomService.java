package org.javlo.ecom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.utils.CSVFactory;
import org.javlo.utils.TimeMap;


public class EcomService {
	
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
				if (!commandsFile.exists()||commandsFile.length() == 0) {
					if (!commandsFile.exists()) {
						commandsFile.createNewFile();
					}
					data = new String[1][];
					data[0] = new String[LINE_SIZE];
					int i=0;
					data[0][i] = "command id";i++;
					data[0][i] = "zone";i++;
					data[0][i] = "email";i++;
					data[0][i] = "firstname";i++;
					data[0][i] = "lastname";i++;
					data[0][i] = "phone";i++;
					data[0][i] = "organization";i++;
					data[0][i] = "vatnumber";i++;
					data[0][i] = "address";i++;
					data[0][i] = "date";i++;
					data[0][i] = "time";i++;
					data[0][i] = "product id";i++;
					data[0][i] = "title";i++;
					data[0][i] = "name";i++;
					data[0][i] = "price";i++;
					data[0][i] = "quantity";i++;
					data[0][i] = "reduction";i++;
					data[0][i] = "paypal";i++;
					fact = new CSVFactory(commandsFile);
				} else {
					fact = new CSVFactory(commandsFile);
					data = fact.getArray();
				}
				String[][] newData = new String[data.length+basket.getSize()+1][];
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
				int i=1;
				for (Product product : products) {
					int j=0;
					newData[data.length+i] = new String[LINE_SIZE];
					newData[data.length+i][0] = "'"+basket.getId();j++;
					newData[data.length+i][j] = basket.getDeliveryZone() == null ? "" : basket.getDeliveryZone().getName(); j++;
					newData[data.length+i][j] = basket.getContactEmail(); j++;
					newData[data.length+i][j] = basket.getFirstName(); j++;
					newData[data.length+i][j] = basket.getLastName(); j++;
					newData[data.length+i][j] = basket.getContactPhone(); j++;
					newData[data.length+i][j] = basket.getOrganization(); j++;
					newData[data.length+i][j] = basket.getVATNumber(); j++;
					newData[data.length+i][j] = basket.getAddress().replace(System.getProperty("line.separator"), "\\"); j++;
					newData[data.length+i][j] = StringHelper.renderDate(date);j++;
					newData[data.length+i][j] = StringHelper.renderOnlyTime(date);j++;
					newData[data.length+i][j] = "'"+product.getId();j++;
					newData[data.length+i][j] = product.getShortDescription();j++;
					newData[data.length+i][j] = product.getName();j++;
					newData[data.length+i][j] = ""+product.getPrice();j++;
					newData[data.length+i][j] = ""+product.getQuantity();j++;
					newData[data.length+i][j] = ""+product.getReduction();j++;										
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
	
	private Map<String, PayementContext> getPayementsMap() {
		final String KEY = "_payement";
		Map<String, PayementContext> allPayements = (Map<String, PayementContext>)globalContext.getAttribute(KEY);
		if (allPayements == null) {
			allPayements = new TimeMap<String, PayementContext>(24*60*60); // payement live 1 day
			globalContext.setAttribute(KEY, allPayements);
		}
		return allPayements;
	}
	
	/**
	 * get the payement context.
	 * @param id the payement id, create a new context if id is null.
	 * @return a new context if id is null.
	 */
	public PayementContext getPayementContext(String id) {
		if (id == null) {
			PayementContext payementContext = new PayementContext();
			getPayementsMap().put(payementContext.getId(), payementContext);
			return payementContext;
		} else {
			return getPayementsMap().get(id);
		}
		
	}
	
	/**
	 * return the external service (as paypal, Ogone...) for pay on the site.
	 * @return
	 */
	public List<PayementExternalService> getExternalService() {
		List<PayementExternalService> outServices = new LinkedList<PayementExternalService>();
		Map<String,String> payementList = globalContext.getDataWidthKeyPrefix(PAYEMENT_PREFIX);
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
	
	public void storePayementService(PayementExternalService service) {
		if (!service.getName().equals(PAYEMENT_PREFIX+service.getInitialName())) {
			globalContext.removeData(PAYEMENT_PREFIX+service.getInitialName());
		}
		globalContext.setData(PAYEMENT_PREFIX+service.getName(), service.toString());
	}
	
	public void deletePayementService(String name) {		
		globalContext.removeData(PAYEMENT_PREFIX+name);
	}

}
