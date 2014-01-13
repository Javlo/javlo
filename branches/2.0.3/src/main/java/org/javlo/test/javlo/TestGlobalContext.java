package org.javlo.test.javlo;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.service.PersistenceService;

public class TestGlobalContext extends GlobalContext {
	
	private PersistenceService persistenceService = new TestPersistenceService();
	
	@Override
	public String getContextKey() {
		return "test";
	}
	
	public static GlobalContext getInstance(HttpServletRequest request) {
		GlobalContext outContext = new TestGlobalContext();
		outContext.setApplication(request.getSession().getServletContext());
		outContext.staticConfig = StaticConfig.getInstance((ServletContext)null);		
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
	
	

}
