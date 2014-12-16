package org.javlo.module.ecom;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.URLHelper;
import org.javlo.utils.Cell;
import org.javlo.utils.XLSTools;

public class DeliveryPrice {

	private Cell[][] priceList;
	private List<String> zones;

	public static DeliveryPrice getInstance(ContentContext ctx, File file) throws Exception {
		DeliveryPrice priceList = (DeliveryPrice) ctx.getGlobalContext().getAttribute(DeliveryPrice.class.getName());
		if (priceList == null) {
			priceList = new DeliveryPrice(ctx, file);
			ctx.getGlobalContext().setAttribute(DeliveryPrice.class.getName(), priceList);
		}
		return priceList;
	}
	
	public static DeliveryPrice getInstance(ContentContext ctx) throws Exception {
		File priceListFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataBaseFolder().getCanonicalPath(), "ecom","delivery.xlsx"));
		if (priceListFile.exists()) {
			return getInstance(ctx,priceListFile);
		} else {
			return null;
		}
		
	}

	public void reset(ContentContext ctx) {
		ctx.getGlobalContext().setAttribute(DeliveryPrice.class.getName(), null);
	}

	private DeliveryPrice(ContentContext ctx, File file) throws Exception {
		priceList = XLSTools.getArray(ctx, file);
	}

	public List<String> getZone() {
		if (zones == null) {
			zones = new LinkedList<String>();
			for (int i = 1; i < priceList[0].length; i++) {
				zones.add(priceList[0][i].getValue().trim());
			}
		}
		return zones;
	}

	public double getPrice(double weight, String zone) {		
		if (zone == null || zone.trim().length() == 0) {
			return 0;
		}
		int y = 1;
		while (y < priceList.length-1 && priceList[y][0].getDoubleValue() < weight) {			
			y++;			
		}		
		int x = 1;
		while (!priceList[0][x].getValue().trim().equals(zone) && x < priceList[0].length) {
			x++;
		}
		if (priceList[y][0].getDoubleValue() < weight) {
			return priceList[y][x].getDoubleValue()*weight/priceList[y][0].getDoubleValue();
		} else {
			return priceList[y][x].getDoubleValue();
		}
	}

	public static void main(String[] args) {
		File test = new File("C:/Users/pvandermaesen/Dropbox/Documents/pro/volpaiole/in/price_list_javlo.xlsx");

		try {
			DeliveryPrice pl = new DeliveryPrice(null, test);
			System.out.println("autria,28 = "+pl.getPrice(112,"AUSTRIA"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
