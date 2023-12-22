package org.javlo.ecom;

import java.net.URL;

import jakarta.servlet.http.HttpServletRequest;

public interface IOrderEngine {
	
	public String getName();
	
	public URL getOrderURL();
	
	public void performOrder();
	
	public void performSuccess(HttpServletRequest request);

}
