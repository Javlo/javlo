package org.javlo.data.taxonomy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.service.IListItem;
import org.jcodec.common.StringUtils;

public class TaxonomyService {

	private static Logger logger = Logger.getLogger(TaxonomyService.class.getName());

	public static final String KEY = "taxonomy";

	public static final String SESSION_KEY = "session-" + TaxonomyService.class.getName();

	private TaxonomyBean root = new TaxonomyBean("0", "root");

	private String context;

	private Map<String, TaxonomyBean> taxonomyBeanMap = new HashMap<String, TaxonomyBean>();
	private Map<String, TaxonomyBean> taxonomyBeanPathMap = new HashMap<String, TaxonomyBean>();
	private Map<String, TaxonomyDisplayBean> taxonomyDisplayBeanPathMap = new HashMap<String, TaxonomyDisplayBean>();
	private List<Map.Entry<String, String>> options = null;

	public static final TaxonomyService getInstance(ContentContext ctx) {
		return getInstance(ctx, ctx.getRenderMode());
	}

	public static final TaxonomyService getMasterInstance(ContentContext ctx) throws IOException {
		int renderMode = ctx.getRenderMode();
		if (renderMode == ContentContext.PREVIEW_MODE) {
			renderMode = ContentContext.EDIT_MODE;
		}
		if (renderMode == ContentContext.VIEW_MODE && !ctx.getGlobalContext().isPreviewMode()) {
			renderMode = ContentContext.EDIT_MODE;
		}
		GlobalContext globalContext = ctx.getGlobalContext();
		if (!globalContext.isMaster()) {
			globalContext = globalContext.getMasterContext(ctx);
		}
		TaxonomyService outService = (TaxonomyService) globalContext.getAttribute(KEY + renderMode);
		if (outService == null) {
			outService = new TaxonomyService();
			outService.context = "master:" + globalContext.getContextKey();
			globalContext.setAttribute(KEY + renderMode, outService);
		}
		return outService;
	}

	public static final TaxonomyService getInstance(ContentContext ctx, int renderMode) {
		if (renderMode == ContentContext.PREVIEW_MODE) {
			renderMode = ContentContext.EDIT_MODE;
		}
		if (renderMode == ContentContext.VIEW_MODE && !ctx.getGlobalContext().isPreviewMode()) {
			renderMode = ContentContext.EDIT_MODE;
		}
		TaxonomyService outService = (TaxonomyService) ctx.getGlobalContext().getAttribute(KEY + renderMode);
		if (outService == null) {
			outService = new TaxonomyService();
			outService.context = "local:" + ctx.getGlobalContext().getContextKey();
			ctx.getGlobalContext().setAttribute(KEY + renderMode, outService);
		}
		ctx.getRequest().setAttribute(KEY, outService);
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
		return root.getChildren().size() > 0;
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
		} else {
			destId = null;
		}
		if (src != null && target != null) {
			TaxonomyBean sameNameBean = target.searchChildByName(src.getName());
			if (sameNameBean == null || sameNameBean.getId().equals(src.getId())) {
				delete(srcId);
				target.addChild(src, destId);
				return true;
			}
		}
		return false;
	}

	public boolean delete(String id) {
		synchronized (taxonomyBeanMap) {
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
		if (currentBean.getChildren().size() > 0) {
			String newPath = URLHelper.mergePath(path, currentBean.getName());
			taxonomyBeanPathMap.put(newPath, currentBean);
			for (TaxonomyBean child : currentBean.getChildren()) {
				fillMapPath(newPath, child);
			}
		}
	}

	private void fillMapPath(String path, TaxonomyDisplayBean currentBean) {
		if (currentBean.getChildren().size() > 0) {
			String newPath = URLHelper.mergePath(path, currentBean.getName());
			taxonomyDisplayBeanPathMap.put(newPath, currentBean);
			for (TaxonomyDisplayBean child : currentBean.getChildren()) {
				fillMapPath(newPath, child);
			}
		}
	}

	public Map<String, TaxonomyBean> getTaxonomyBeanPathMap() {
		if (taxonomyBeanPathMap.size() == 0) {
			synchronized (taxonomyBeanPathMap) {
				if (taxonomyBeanPathMap.size() == 0) {
					fillMapPath("/", root);
				}
			}
		}
		return taxonomyBeanPathMap;
	}

	public Map<String, TaxonomyDisplayBean> getTaxonomyDisplayBeanPathMap(ContentContext ctx) {
		if (taxonomyDisplayBeanPathMap.size() == 0) {
			synchronized (taxonomyDisplayBeanPathMap) {
				if (taxonomyDisplayBeanPathMap.size() == 0) {
					fillMapPath("/", new TaxonomyDisplayBean(ctx, root));
				}
			}
		}
		return taxonomyDisplayBeanPathMap;
	}

	public TaxonomyBean getTaxonomyBean(String id) {
		return getTaxonomyBeanMap().get(id);
	}

	public Map<String, TaxonomyBean> getTaxonomyBeanMap() {
		if (taxonomyBeanMap.size() == 0) {
			synchronized (taxonomyBeanMap) {
				if (taxonomyBeanMap.size() == 0) {
					fillMap(root);
				}
				options = null;
			}
		}
		return taxonomyBeanMap;
	}

	public void clearCache() {
		synchronized (taxonomyBeanMap) {
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
			options = new LinkedList<Map.Entry<String, String>>();
			for (Map.Entry<String, TaxonomyBean> bean : beans.entrySet()) {
				options.add(new AbstractMap.SimpleEntry<String, String>(bean.getKey(), bean.getValue().getPath()));
			}
			Collections.sort(options, new MapEntryComparator(true));
		}
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

	public boolean isAllMatch(ITaxonomyContainer container, ITaxonomyContainer filter) {
		for (String taxonomy : filter.getTaxonomy()) {
			if (!isMatch(container, new TaxonomyContainerBean(taxonomy))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * add all parent of the selection in the filter
	 * 
	 * @param container
	 * @param filter
	 * @return
	 */
	public boolean isMatchWidthParent(ITaxonomyContainer container, ITaxonomyContainer filter) {
		Set<String> newFilter = new HashSet<String>(filter.getTaxonomy());
		for (String id : filter.getTaxonomy()) {
			TaxonomyBean bean = getTaxonomyBeanMap().get(id);
			while (bean != null && bean.getParent() != null) {
				newFilter.add(bean.getParent().getId());
				bean = bean.getParent();
			}
		}
		return !Collections.disjoint(container.getTaxonomy(), newFilter);
	}

	/**
	 * check if a taxonomy group match
	 * 
	 * @param container
	 * @param filter
	 * @return
	 */
	public boolean isMatch(ITaxonomyContainer container, ITaxonomyContainer filter) {
		if (container == null || filter == null) {
			return true;
		}
		if (container.getTaxonomy() == null || container.getTaxonomy().size() == 0) {
			if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
				return true;
			} else {
				return false;
			}
		}
		if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
			return false;
		}
		if (!Collections.disjoint(container.getTaxonomy(), filter.getTaxonomy())) {
			return true;
		} else {
			Set<String> allCont1 = new HashSet<String>(container.getTaxonomy());
			for (String id : container.getTaxonomy()) {
				TaxonomyBean bean = getTaxonomyBeanMap().get(id);
				if (bean != null && bean.getParent() != null) {
					while (bean.getParent().getParent() != null) {
						bean = bean.getParent();
						allCont1.add(bean.getId());
					}
				} else {
					logger.warning("taxonomy bean not found : " + id);
				}
			}
			return !Collections.disjoint(allCont1, filter.getTaxonomy());
		}
	}

	/**
	 * check if a taxonomy group match
	 * 
	 * @param container
	 * @param filter
	 * @return
	 */
	public boolean isMatchAll(ITaxonomyContainer container, ITaxonomyContainer filter) {
		if (container == null || filter == null) {
			return true;
		}
		if (container.getTaxonomy() == null || container.getTaxonomy().size() == 0) {
			if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
				return true;
			} else {
				return false;
			}
		}
		if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
			return false;
		}
		for (String taxo : container.getTaxonomy()) {
			TaxonomyBean bean = getTaxonomyBeanMap().get(taxo);
			for (String fTaxo : filter.getTaxonomy()) {
				if (!fTaxo.equals(bean.getName())) {
					if (!bean.hasParent(fTaxo)) {
						return false;
					}
				}
			}
		}
		return true;
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
			TaxonomyBean bean = getTaxonomyBeanMap().get(id);
			if (bean != null) {
				outBeans.add(bean);
			}
		}
		return outBeans;
	}

	private List<IListItem> getList(ContentContext ctx, String path, boolean displayParentLabel) {
		String[] nodes;
		if (path.contains("/")) {
			nodes = StringUtils.splitS(path, "/");
		} else if (path.contains("-")) {
			nodes = StringUtils.splitS(path, "-");
		} else if (path.contains(".")) {
			nodes = StringUtils.splitS(path, ".");
		} else {
			nodes = StringUtils.splitS(path, ">");
		}
		TaxonomyBean bean = root;
		for (int i = 0; i < nodes.length; i++) {
			bean = bean.searchChildByName(nodes[i].trim());
			if (bean == null) {
				i = nodes.length;
			}
		}
		if (bean == null) {
			bean = getTaxonomyBeanMap().get(path);
		}
		if (bean == null || bean.getChildren().size() == 0) {
			return null;
		} else {
			List<IListItem> outList = new LinkedList<IListItem>();
			for (TaxonomyBean child : bean.getChildren()) {
				TaxonomyDisplayBean displayBean = new TaxonomyDisplayBean(ctx, child);
				displayBean.setDisplayParentLabel(displayParentLabel);
				if (child.getChildren().size() == 0) {
					outList.add(displayBean.getListItem());
				}
				if (child.getChildren().size() > 0) {
					outList.addAll(getList(ctx, child.getPath(), false));
				}
			}
			return outList;
		}
	}

	public static ITaxonomyContainer getSessionFilter(ContentContext ctx) {
		Map<String, String> outSessionFilter = (Map<String, String>) ctx.getRequest().getSession().getAttribute(SESSION_KEY);
		if (outSessionFilter != null && outSessionFilter.size() > 0) {
			return new TaxonomyContainerBean(new HashSet<String>(outSessionFilter.values()));
		} else {
			return TaxonomyContainerBean.EMPTY;
		}
	}

	public static String getSessionFilter(ContentContext ctx, String key) {
		Map<String, String> outSessionFilter = (Map<String, String>) ctx.getRequest().getSession().getAttribute(SESSION_KEY);
		if (outSessionFilter != null) {
			return outSessionFilter.get(key);
		} else {
			return null;
		}
	}

	/**
	 * add a taxonomy filter in the session
	 * 
	 * @param ctx
	 * @param key
	 *            a reference to the component or the filter generator
	 * @param value
	 *            the reference to a taxonomy entry
	 */
	public static void setSessionFilter(ContentContext ctx, String key, String value) {
		Map<String, String> outSessionFilter = (Map<String, String>) ctx.getRequest().getSession().getAttribute(SESSION_KEY);
		if (outSessionFilter == null) {
			outSessionFilter = new HashMap<String, String>();
			ctx.getRequest().getSession().setAttribute(SESSION_KEY, outSessionFilter);
		}
		if (StringHelper.isEmpty(value)) {
			outSessionFilter.remove(key);
		} else {
			outSessionFilter.put(key, value);
		}
	}

	public List<IListItem> getList(ContentContext ctx, String path) {
		return getList(ctx, path, false);
	}

	public void updateId(TaxonomyBean bean, String newId) {
		bean.setId(newId);
		clearCache();
	}

	public String getContext() {
		return context;
	}

}