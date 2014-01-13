package org.javlo.ecom;

import java.net.URL;
import java.util.Map;

public interface IOrderEngine {
	
	public String getName();
	
	public Map<String,String> getConfig();
	
	public URL getOrderURL();
	
	public void performOrder();

}
