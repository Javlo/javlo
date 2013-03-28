package org.javlo.portlet.request;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.service.RequestService;


public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {

	private final RequestService requestService;
	private final String suffix;
	private final Locale locale;
	
	public HttpServletRequestWrapper(ContentContext ctx, String suffix) {
		super(ctx.getRequest());
		
		this.requestService = RequestService.getInstance(ctx.getRequest());
		this.suffix = suffix;
		this.locale = new Locale(ctx.getContentLanguage());
	}

	@Override
	public String getParameter(String name) {
		return requestService.getParameter(name + this.suffix, null);
	}

	@Override
	public Map getParameterMap() {
		Map parametersShort = new HashMap<String,String>();
		Map parametersFull = requestService.getParametersMap();
		for (Object paramObj : parametersFull.keySet()) {
			String paramName = (String) paramObj;
			if (paramName.endsWith(this.suffix)) {
				paramName = paramName.substring(0, paramName.length() - this.suffix.length());
			}
			parametersShort.put(paramName, parametersFull.get(paramObj));
		}
		return parametersShort;
	}

	@Override
	public Enumeration getParameterNames() {
		return Collections.enumeration(getParameterMap().entrySet());
	}

	@Override
	public String[] getParameterValues(String name) {
		return requestService.getParameterValues(name + this.suffix, null);
	}

	@Override
	public Locale getLocale() {
		return 	locale;
	}
}
