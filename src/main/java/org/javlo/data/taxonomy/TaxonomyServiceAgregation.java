package org.javlo.data.taxonomy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.service.IListItem;

public class TaxonomyServiceAgregation {

	private static Logger logger = Logger.getLogger(TaxonomyServiceAgregation.class.getName());

	private List<TaxonomyService> services = new LinkedList<TaxonomyService>();
	private TaxonomyBean root = null;

	public TaxonomyServiceAgregation(TaxonomyService... services) {
		for (TaxonomyService taxonomyService : services) {
			if (taxonomyService.isActive()) {
				this.services.add(taxonomyService);
			}
		}
	}

	public boolean isActive() {
		for (TaxonomyService taxonomyService : services) {
			if (taxonomyService.isActive()) {
				return true;
			}
		}
		return false;
	}

	public TaxonomyBean getRoot() {
		if (root == null) {
			root = new TaxonomyBean("0", "root");
			if (isActive()) {
				for (TaxonomyService taxonomyService : services) {
					for (TaxonomyBean bean : taxonomyService.getAllBeans()) {
						if (root.searchChildByName(bean.getName()) == null) {
							root.addChildAsLast(bean);
						}
					}
				}
			}
		}
		return root;
	}

	public TaxonomyBean getBean(String id) {
		for (TaxonomyService taxonomyService : services) {
			TaxonomyBean bean = taxonomyService.getTaxonomyBeanMap().get(id);
			if (bean != null) {
				return bean;
			}
		}
		return null;
	}

	public String getSelectHtml() {
		return getSelectHtml("taxonomy", "form-control chosen-select", null);
	}

	public String getSelectHtml(Collection<String> selection) {
		return getSelectHtml("taxonomy", "form-control chosen-select", selection);
	}

	public String getSelectHtml(String name, String cssClass, Collection<String> selection) {
		Map<String, TaxonomyBean> beans = new HashMap<String, TaxonomyBean>();
		for (TaxonomyService taxonomyService : services) {			
			beans.putAll(taxonomyService.getTaxonomyBeanMap());
		}
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		LinkedList<Map.Entry<String, String>> options = new LinkedList<Map.Entry<String, String>>();
		for (Map.Entry<String, TaxonomyBean> bean : beans.entrySet()) {
			options.add(new AbstractMap.SimpleEntry<String, String>(bean.getKey(), bean.getValue().getPath()));
		}
		Collections.sort(options, new MapEntryComparator(true));
		out.println("<select id=\"" + name + "\" name=\"" + name + "\" class=\"" + cssClass + "\" multiple>");
		for (Map.Entry<String, String> option : options) {
			String select = "";
			if (selection != null && selection.contains(option.getKey())) {
				select = " selected=\"selected\"";
			}
			out.println("<option value=\"" + option.getKey() + "\"" + select + ">" + option.getValue() + "</option>");
		}
		out.println("</select>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	/**
	 * convert list of taxonomybean id to a list of taxonomybean instance.
	 * 
	 * @param ids
	 * @return
	 */
	public List<TaxonomyBean> convert(Collection<String> ids) {
		if (ids == null) {
			return null;
		} else if (ids.size() == 0) {
			return Collections.EMPTY_LIST;
		}
		List<TaxonomyBean> outBeans = new LinkedList<TaxonomyBean>();
		for (String id : ids) {
			TaxonomyBean bean = getBean(id);
			if (bean != null) {
				outBeans.add(bean);
			}
		}
		return outBeans;
	}

	public boolean isMatch(ITaxonomyContainer cont1, ITaxonomyContainer cont2) {
		for (TaxonomyService taxonomyService : services) {
			if (taxonomyService.isMatch(cont1, cont2)) {
				return true;
			}
		}
		return false;
	}
	
	public List<IListItem> getList(ContentContext ctx, String path) {
		for (TaxonomyService taxonomyService : services) {
			List<IListItem> list = taxonomyService.getList(ctx, path);
			if (list != null) {
				return list;
			}
		}
		return null;
	}
}
