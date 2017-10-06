package org.javlo.component.container;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.utils.CollectionAsMap;

public class ComponentBasket {
	
	public List<String> components = new LinkedList<String>();
	public Map<String, String> componentsMap = new CollectionAsMap<String>(components); 
	
	public static final String SESSION_KEY = "componentBasket";
	
	public static final ComponentBasket getComponentBasket(ContentContext ctx) {
		ComponentBasket out = (ComponentBasket)ctx.getRequest().getSession().getAttribute(SESSION_KEY);
		if (out == null) {
			out = new ComponentBasket();
			ctx.getRequest().getSession().setAttribute(SESSION_KEY, out);
		}
		return out;
	}
	
	public List<String> getComponents() {
		return components;
	}
	
	public Map<String, String> getComponentsMap() {
		return componentsMap;
	}
	
	public boolean addComponent(String comp) {
		if (comp != null) {
			if (!components.contains(comp)) {
				components.add(comp);
				return true;
			}
		}
		return false;
	}

	public int size() {	 
		return components.size();
	}

	public boolean removeComponent(String comp) {
		return components.remove(comp);
	}	

}
