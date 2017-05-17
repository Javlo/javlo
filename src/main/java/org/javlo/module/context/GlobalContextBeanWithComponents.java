package org.javlo.module.context;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.module.admin.AdminAction.GlobalContextBean;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.template.Template;
import org.javlo.template.TemplateFactory;

public class GlobalContextBeanWithComponents extends GlobalContextBean {

	private List<ComponentRefBean> components = new LinkedList<ComponentRefBean>();
	private List<TemplateRefBean> templates = new LinkedList<TemplateRefBean>();
	private int pages;

	public static final class ComponentRefBean {
		
		public static final class ComponentRefBeanSort implements Comparator<ComponentRefBean> {

			@Override
			public int compare(ComponentRefBean o1, ComponentRefBean o2) {			
				return o2.countRef - o1.countRef;
			}
			
		}
		
		private String component;
		private int countRef = 0;
		private boolean listed = true;
		private boolean unknow = true;

		public ComponentRefBean(String comp, boolean listed, boolean unknow) {
			component = comp;
			this.listed = listed;
			this.unknow = unknow;
		}

		public String getComponent() {
			return component;
		}

		public void setComponent(String component) {
			this.component = component;
		}

		public int getCountRef() {
			return countRef;
		}

		public void setCountRef(int countRef) {
			this.countRef = countRef;
		}

		public boolean isListed() {
			return listed;
		}

		public void setListed(boolean listed) {
			this.listed = listed;
		}

		public boolean isUnknow() {
			return unknow;
		}

		public void setUnknow(boolean unknow) {
			this.unknow = unknow;
		}	
		
	}
	
	public static final class TemplateRefBean {
		
		public static final class ComponentRefBeanSort implements Comparator<TemplateRefBean> {

			@Override
			public int compare(TemplateRefBean o1, TemplateRefBean o2) {			
				return o2.countRef - o1.countRef;
			}
			
		}
		
		private String template;
		private int countRef = 0;
		private boolean listed = true;
		private boolean unknow = true;

		public TemplateRefBean(String comp, boolean listed, boolean unknow) {
			template = comp;
			this.listed = listed;
			this.unknow = unknow;
		}

		public String getTemplate() {
			return template;
		}

		public void setTemplate(String component) {
			this.template = component;
		}

		public int getCountRef() {
			return countRef;
		}

		public void setCountRef(int countRef) {
			this.countRef = countRef;
		}

		public boolean isListed() {
			return listed;
		}

		public void setListed(boolean listed) {
			this.listed = listed;
		}

		public boolean isUnknow() {
			return unknow;
		}

		public void setUnknow(boolean unknow) {
			this.unknow = unknow;
		}	
		
	}

	public GlobalContextBeanWithComponents(ContentContext ctx, HttpSession session) throws Exception {
		super(ctx.getGlobalContext(), session);
		GlobalContext globalContext = ctx.getGlobalContext();
		ContentService content = ContentService.getInstance(globalContext);
		
		MenuElement root = content.getNavigation(ctx);
		Collection<MenuElement> allPages = root.getAllChildrenList();
		
		/** templates **/		
		Map<String, TemplateRefBean> countTempRef = new HashMap<String, TemplateRefBean>();
		for (Template template : TemplateFactory.getAllTemplates(session.getServletContext())) {
			String templateName = template.getName();
			countTempRef.put(templateName, new TemplateRefBean(templateName,true,false));
		}				
		pages = allPages.size();
		for (MenuElement page : allPages) {
			if (page.getTemplateId() != null) {
				TemplateRefBean count = countTempRef.get(page.getTemplateId());
				if (count != null) {
					count.setCountRef(count.getCountRef()+1);
				} else {
					count = new TemplateRefBean(page.getTemplateId(), false, false);
					count.setCountRef(count.getCountRef()+1);
					countTempRef.put(page.getTemplateId(), count);
				}
			}			
		}
		templates = new LinkedList<TemplateRefBean>(countTempRef.values());
		Collections.sort(templates, new TemplateRefBean.ComponentRefBeanSort());
		
		/** components **/		
		Map<String, ComponentRefBean> countRef = new HashMap<String, ComponentRefBean>();
		Map<String, IContentVisualComponent> componentsMap = ComponentFactory.getComponents(ctx.getRequest().getSession().getServletContext());
		for (String compClass : globalContext.getComponents()) {
			if (componentsMap.get(compClass) != null) {
				countRef.put(componentsMap.get(compClass).getType(), new ComponentRefBean(componentsMap.get(compClass).getType(),true,false));
			} else {
				countRef.put(compClass, new ComponentRefBean(compClass,true,true));
			}
		}
		pages = allPages.size();
		for (MenuElement page : allPages) {
			for (ComponentBean contentBean : page.getContent()) {
				ComponentRefBean count = countRef.get(contentBean.getType());
				if (count != null) {
					count.setCountRef(count.getCountRef()+1);
				} else {
					count = new ComponentRefBean(contentBean.getType(), false, false);
					count.setCountRef(count.getCountRef()+1);
					countRef.put(contentBean.getType(), count);
				}
			}
		}
		components = new LinkedList<ComponentRefBean>(countRef.values());
		Collections.sort(components, new ComponentRefBean.ComponentRefBeanSort());
	}
	
	public Collection<ComponentRefBean> getComponents() {
		return components;
	}

	public int getPages() {
		return pages;
	}

	public Collection<TemplateRefBean> getTemplates() {
		return templates;
	}	
}
