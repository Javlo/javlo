package org.javlo.data.rest;

import java.util.HashMap;
import java.util.Map;

public class RestContainerFactory {
	
	static Map<String, IRestFactory> factories = null;

	public RestContainerFactory() {
	}
	
	protected static Map<String, IRestFactory> getFactories() {
		if (factories == null) {
			factories = new HashMap<String, IRestFactory>();
			IRestFactory fact = new TestRest();
			factories.put(fact.getName(), fact);
			fact = new ContentRest(false, true);
			factories.put(fact.getName(), fact);
			fact = new ContentRest(true, true);
			factories.put(fact.getName(), fact);
			fact = new ContentRest(false, false);
			factories.put(fact.getName(), fact);
			fact = new ContentRest(true, false);
			factories.put(fact.getName(), fact);
			fact = new ComponentRestFactory();
			factories.put(fact.getName(), fact);
			fact = new ThreadRunRest();
			factories.put(fact.getName(), fact);
			fact = new FileRest();
			factories.put(fact.getName(), fact);
			fact = new RestPageHistory();
			factories.put(fact.getName(), fact);
		}
		return factories;
	}
		
	public static IRestFactory getRestFactory(String name) {
		return getFactories().get(name);
	}
}
