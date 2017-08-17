package org.javlo.data.taxonomy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.MapEntryComparator;

public class TaxonomyService {
	
	private static Logger logger = Logger.getLogger(TaxonomyService.class.getName());
	
	public static final String KEY = "taxonomy";
	
	private TaxonomyBean root = new TaxonomyBean("0", "root");
	
	private Map<String, TaxonomyBean> taxonomyBeanMap = new HashMap<String, TaxonomyBean>();
	private Map<String, TaxonomyBean> taxonomyBeanPathMap = new HashMap<String, TaxonomyBean>();
	private List<Map.Entry<String, String>> options = null;

	public static final TaxonomyService getInstance(GlobalContext globalContext) {
		TaxonomyService outService = (TaxonomyService)globalContext.getAttribute(KEY);
		if (outService == null) {
			outService = new TaxonomyService();
			//outService.createDebugStructure();
			globalContext.setAttribute(KEY, outService);
		}
		return outService;
	}
	
	private void createDebugStructure() {		
		root.addChildAsFirst(new TaxonomyBean("1", "child 1"));
		root.addChildAsFirst(new TaxonomyBean("2", "child 2"));
		TaxonomyBean child = root.addChildAsFirst(new TaxonomyBean("3", "child 3"));
		child.addChildAsFirst(new TaxonomyBean("3_1", "subchild 1"));
		child.updateLabel("en", "first child");
		child.updateLabel("fr", "second child");
		child.addChildAsFirst(new TaxonomyBean("3_2", "subchild 2"));
	}
	
	public TaxonomyBean getRoot() {
		return root;
	}
	
	public boolean isActive() {
		return root.getChildren().size()>0;
	}
	
	private void recBean(List<TaxonomyBean> list, TaxonomyBean currentBean) {
		list.add(currentBean);
		for (TaxonomyBean bean : currentBean.getChildren()) {
			recBean(list, bean);
		}
	}
	
	public List<TaxonomyBean> getAllBeans() {
		List<TaxonomyBean> outBean = new LinkedList<TaxonomyBean>();
		outBean.add(root);
		for (TaxonomyBean bean : root.getChildren()) {
			recBean(outBean, bean);
		}
		return outBean;		
	}
	
	private boolean recDeleteBean(TaxonomyBean currentBean, String id) {
		Iterator<TaxonomyBean> ite = currentBean.getChildren().iterator();
		boolean deleted = false;
		while (ite.hasNext() && !deleted) {
			TaxonomyBean bean = ite.next();
			if (bean.getId().equals(id)) {
				ite.remove();
			} else {
				deleted = recDeleteBean(bean, id);
			}
		}
		return deleted;
	}
	
	public boolean move(String srcId, String destId, boolean asChild) {		
		TaxonomyBean src = getTaxonomyBeanMap().get(srcId);
		TaxonomyBean target = getTaxonomyBeanMap().get(destId);
		if (!asChild) {			
			target = target.getParent();			
		} else  {
			destId = null;
		}
		if (src != null && target != null) {
			delete(srcId);
			target.addChild(src, destId);
			return true;
		}
		return false;
	}
	
	public boolean delete(String id) {		 
		synchronized(taxonomyBeanMap) {
			boolean del;
			if (root.getId().equals(id)) {
				del = true;
				root = new TaxonomyBean(StringHelper.getRandomId(), "root");
			} else {
				del = recDeleteBean(root, id);
			}
			clearCache();
			return del;			
		}		
	}
	
	private void fillMap(TaxonomyBean currentBean) {
		taxonomyBeanMap.put(currentBean.getId(), currentBean);
		Iterator<TaxonomyBean> ite = currentBean.getChildren().iterator();		
		while (ite.hasNext()) {
			fillMap(ite.next());			
		}		
	}
	
	private void fillMapPath(String path, TaxonomyBean currentBean) {
		if (currentBean.getChildren().size()>0) {
			String newPath = URLHelper.mergePath(path, currentBean.getName());
			taxonomyBeanPathMap.put(newPath, currentBean);	
			for (TaxonomyBean child : currentBean.getChildren()) {
				fillMapPath(newPath, child);
			}
		}		
	}
	
	public Map<String, TaxonomyBean> getTaxonomyBeanPathMap() {
		if (taxonomyBeanPathMap.size() == 0) {
			synchronized(taxonomyBeanPathMap) {
				if (taxonomyBeanPathMap.size() == 0) {
					fillMapPath("/", root);
				}
			}
		}
		return taxonomyBeanPathMap;
	}
	
	public Map<String, TaxonomyBean> getTaxonomyBeanMap() {
		if (taxonomyBeanMap.size() == 0) {
			synchronized(taxonomyBeanMap) {
				if (taxonomyBeanMap.size() == 0) {
					fillMap(root);
				}
				options = null;
			}
		}
		return taxonomyBeanMap;
	}

	public void clearCache() {
		synchronized(taxonomyBeanMap) {
			taxonomyBeanMap.clear();
			taxonomyBeanPathMap.clear();
			options = null;
		}
	}
	
	public String getSelectHtml() {
		return getSelectHtml("taxonomy", "form-control chosen-select", null);
	}
	
	public String getSelectHtml(Collection<String> selection) {
		return getSelectHtml("taxonomy", "form-control chosen-select", selection);
	}
	
	public String getSelectHtml(String name, String cssClass, Collection<String> selection) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (options == null) {
			Map<String, TaxonomyBean> beans = getTaxonomyBeanMap();
			options = new LinkedList<Map.Entry<String,String>>();
			for (Map.Entry<String, TaxonomyBean> bean : beans.entrySet()) {
				options.add(new AbstractMap.SimpleEntry<String, String>(bean.getKey(), bean.getValue().getPath()));
			}
			Collections.sort(options, new MapEntryComparator(true));
		}
		out.println("<select id=\""+name+"\" name=\""+name+"\" class=\""+cssClass+"\" multiple>");
		for (Map.Entry<String, String> option : options) {
			String select ="";
			if (selection != null && selection.contains(option.getKey())) {
				select = " selected=\"selected\"";
			}
			out.println("<option value=\""+option.getKey()+"\""+select+">"+option.getValue()+"</option>");
		}
		out.println("</select>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public boolean isMatch(ITaxonomyContainer cont1, ITaxonomyContainer cont2) {
		if (cont1 == null || cont2 == null || cont1.getTaxonomy() == null || cont2.getTaxonomy() == null) {
			return true;
		}
		if (!Collections.disjoint(cont1.getTaxonomy(), cont2.getTaxonomy())) {
			return true;
		} else {			
			Set<String> allCont1 = new HashSet<String>(cont1.getTaxonomy());
			for (String id : cont1.getTaxonomy()) {
				TaxonomyBean bean = getTaxonomyBeanMap().get(id);
				if (bean != null && bean.getParent() != null) {
					while (bean.getParent().getParent() != null) {
						bean = bean.getParent();
						allCont1.add(bean.getId());
					}
				} else {
					logger.warning("taxonomy bean not found : "+id);
				}
			}
			Set<String> allCont2 = new HashSet<String>(cont2.getTaxonomy());
			for (String id : cont2.getTaxonomy()) {
				TaxonomyBean bean = getTaxonomyBeanMap().get(id);
				if (bean != null && bean.getParent() != null) {
					while (bean.getParent().getParent() != null) {
						bean = bean.getParent();
						allCont2.add(bean.getId());
					}
				} else {
					logger.warning("taxonomy bean not found : "+id);
				}
			}
			return !Collections.disjoint(allCont1, allCont2);
		}		
	}
	
	/**
	 * convert list of taxonomybean id to a list of taxonomybean instance.
	 * @param ids
	 * @return
	 */
	public List<TaxonomyBean> convert (Collection<String> ids) {
		if (ids == null) {
			return null;
		} else if (ids.size() == 0) {
			return Collections.EMPTY_LIST;
		}
		List<TaxonomyBean> outBeans = new LinkedList<TaxonomyBean>();
		for (String id : ids) {
			TaxonomyBean bean = getTaxonomyBeanMap().get(id);
			if (bean != null) {
				outBeans.add(bean);
			}
		}
		return outBeans;
	}
}