package org.javlo.test.javlo;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.service.PersistenceService;

public class TestGlobalContext extends GlobalContext {
	
	private PersistenceService persistenceService = new TestPersistenceService();
	
	public TestGlobalContext() {
		super("Test");
		this.staticConfig = StaticConfig.getInstance((ServletContext)null);
	}

	@Override
	public String getContextKey() {
		return "test";
	}
	
	public static GlobalContext getInstance(HttpServletRequest request) {
		GlobalContext outContext = new TestGlobalContext();
		outContext.setApplication(request.getSession().getServletContext());
		return outContext;
	}	
	
	@Override
	public Object getAttribute(String key) {
		if (key.equals(PersistenceService.getKey(this))) {
			return persistenceService;
		} else {
			return super.getAttribute(key);
		}
	}
	
	public void addInConfig(String key, String value) {
		this.properties.addProperty(key, value);
	}
	
	

}
