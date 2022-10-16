package org.javlo.data.taxonomy;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.Comparator.MapEntryComparator;
import org.javlo.service.IListItem;
import org.jcodec.common.StringUtils;

import com.google.gson.Gson;

public class TaxonomyService {

	private static Logger logger = Logger.getLogger(TaxonomyService.class.getName());

	public static final String KEY = "taxonomy";

	public static final String SESSION_KEY = "session-" + TaxonomyService.class.getName();

	private TaxonomyBean root = new TaxonomyBean("0", "root");

	private String context;

	private Map<String, TaxonomyBean> taxonomyBeanMapNotResolved = new HashMap<String, TaxonomyBean>();
	private Map<String, TaxonomyBean> taxonomyBeanMapResolved = new HashMap<String, TaxonomyBean>();
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
		TaxonomyBean geo = root.addChildAsFirst(new TaxonomyBean("0", "#geo"));
		geo.addChildAsFirst(new TaxonomyBean("0-1", "be"));
		geo.addChildAsFirst(new TaxonomyBean("0-2", "fr"));
		root.addChildAsFirst(new TaxonomyBean("1", "child 1"));
		root.addChildAsFirst(new TaxonomyBean("2", "child 2"));
		TaxonomyBean child = root.addChildAsFirst(new TaxonomyBean("3", "child 3"));
		child.addChildAsFirst(new TaxonomyBean("3_1", "subchild 3 > 1"));
		child.updateLabel("en", "first child");
		child.updateLabel("fr", "second child");
		child.addChildAsFirst(new TaxonomyBean("3_2", "subchild 3 > 2"));
		TaxonomyBean schild = new TaxonomyBean("3_3", "subchild 3 > 3");
		schild.addChildAsFirst(new TaxonomyBean("3_3_1", ">geo"));
		child.addChildAsFirst(schild);
		schild.addChildAsFirst(new TaxonomyBean("3_3_2", "subsubchild 3 > 3 > 1"));
	}

	public TaxonomyBean getRoot() {
		return root;
	}

	public TaxonomyBean getLinkedRoot() {
		return getTaxonomyBean("0", true);
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

	/**
	 * get all source bean, key of map is target name (start with '>')
	 * 
	 * @return
	 */
	public Map<String, TaxonomyBean> getAllSources() {
		Map<String, TaxonomyBean> out = new HashMap<String, TaxonomyBean>();
		for (TaxonomyBean bean : root.getChildren()) {
			if (bean.isSource()) {
				String targetKey = '>' + bean.getName().substring(1);
				out.put(targetKey, bean);
			}
		}
		return out;
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

	public synchronized boolean delete(String id) {
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

	private Map<String, TaxonomyBean> getTaxonomyMap(boolean resolveLink) {
		if (resolveLink) {
			return taxonomyBeanMapResolved;
		} else {
			return taxonomyBeanMapNotResolved;
		}
	}

	private void fillMap(Map<String, TaxonomyBean> sources, List<TaxonomyBean> tobeDeleted, TaxonomyBean currentBean, boolean resolveLink) {
		if (resolveLink && currentBean.isSource()) {
			tobeDeleted.add(currentBean);
			return;
		}
		Map<String, TaxonomyBean> taxonomyBeanMap = getTaxonomyMap(resolveLink);
		if (resolveLink) {
			TaxonomyBean sourceBean = sources.get(currentBean.getName());
			if (currentBean.isTarget() && sourceBean == null) {
				logger.severe("ref. not found : " + currentBean.getName());
				currentBean.setName(currentBean.getName() + " ERROR REF. NOT FOUND.");
			}
			if (sourceBean != null) {
				TaxonomyBean newBean = sourceBean.duplicateForLink(currentBean.getParent(), currentBean.getId());
				// replace bean in parent
				int pos = 0;
				if (newBean.getChildren().size() > 0) {
					List<TaxonomyBean> parentChildren = new LinkedList<>(currentBean.getParent().getChildren());
					for (TaxonomyBean child : parentChildren) {
						if (child.getId().equals(currentBean.getId())) {
							currentBean.getParent().getChildren().set(pos, newBean.getChildren().get(newBean.getChildren().size() - 1));
							newBean.getChildren().get(newBean.getChildren().size() - 1).setParent(currentBean.getParent());
							for (int i = newBean.getChildren().size() - 2; i >= 0; i--) {
								currentBean.getParent().getChildren().add(pos, newBean.getChildren().get(i));
								newBean.getChildren().get(i).setParent(currentBean.getParent());
							}
						}
						pos++;
					}
				}
				currentBean = newBean;
			}
		}
		taxonomyBeanMap.put(currentBean.getId(), currentBean);
		List<TaxonomyBean> children = new LinkedList<>(currentBean.getChildren());
		Iterator<TaxonomyBean> ite = children.iterator();
		while (ite.hasNext()) {
			fillMap(sources, tobeDeleted, ite.next(), resolveLink);
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

	public TaxonomyBean getTaxonomyBean(String id, boolean resolveLink) {
		return getTaxonomyBeanMap(resolveLink).get(id);
	}

	public Map<String, TaxonomyBean> getTaxonomyBeanMap() {
		return getTaxonomyBeanMap(false);
	}

	public Map<String, TaxonomyBean> getTaxonomyBeanMap(boolean resolveLink) {
		Map<String, TaxonomyBean> taxonomyBeanMap = getTaxonomyMap(resolveLink);
		if (taxonomyBeanMap.size() == 0) {
			synchronized (this) {
				taxonomyBeanMap = getTaxonomyMap(resolveLink);
				if (taxonomyBeanMap.size() == 0) {
					TaxonomyBean root = resolveLink ? this.root.duplicate() : this.root;
					List<TaxonomyBean> toBeDeleted = new LinkedList<>();
					fillMap(getAllSources(), toBeDeleted, root, resolveLink);

					for (TaxonomyBean taxonomyBean : toBeDeleted) {
						taxonomyBean.getParent().removeChild(taxonomyBean.getId());
					}
				}
				options = null;
			}
		}
		return taxonomyBeanMap;
	}

	public synchronized void clearCache() {
		taxonomyBeanMapNotResolved.clear();
		taxonomyBeanMapResolved.clear();
		taxonomyBeanPathMap.clear();
		taxonomyDisplayBeanPathMap.clear();
		options = null;
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
	
	public boolean isAllMatch(ITaxonomyContainer container, ITaxonomyContainer filter, int depth) {
		for (String taxonomy : filter.getTaxonomy()) {
			if (!isMatch(container, new TaxonomyContainerBean(taxonomy), depth)) {
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
	
	public int getDepth(TaxonomyBean bean) {
		if (bean == null) {
			return -1;
		}
		int depth = 0;
		while (bean.getParent() != null) {
			bean = bean.getParent();
			depth++;
		}
		return depth;
	}

	public ITaxonomyContainer addParentsToContainer(ITaxonomyContainer container, int minDepth) {
		if (container.getTaxonomy() == null || container.getTaxonomy().size() == 0) {
			return container;
		} else {
			Set<String> allTaxo = new HashSet<String>();
			for (String taxo : container.getTaxonomy()) {
				TaxonomyBean bean = getTaxonomyBeanMap().get(taxo);
				while (getDepth(bean)>=minDepth) {
					allTaxo.add(bean.getId());
					bean = bean.getParent();
				}
			}
			return new TaxonomyContainerBean(allTaxo);
		}
	}
	
	public boolean isMatch(ITaxonomyContainer container, ITaxonomyContainer filter) {
		return isMatch(container, filter, 1);
	}

	/**
	 * check if a taxonomy group match
	 * 
	 * @param container
	 * @param filter
	 * @param minDepth : min depth of the latest node (0=root, 1=child of root...)
	 * @return
	 */
	public boolean isMatch(ITaxonomyContainer container, ITaxonomyContainer filter, int minDepth) {
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

			//			Set<String> allCont1 = new HashSet<String>(container.getTaxonomy());
//			for (String id : container.getTaxonomy()) {
//				TaxonomyBean bean = getTaxonomyBeanMap().get(id);
//				if (bean != null && bean.getParent() != null) {
//					while (bean.getParent().getParent() != null) {
//						bean = bean.getParent();
//						allCont1.add(bean.getId());
//					}
//				} else {
//					logger.warning("taxonomy bean not found : " + id);
//				}
//			}
			//ITaxonomyContainer filterParents = addParentsToContainer(filter, minDepth);
			return !Collections.disjoint(addParentsToContainer(container, minDepth).getTaxonomy(), filter.getTaxonomy());
		}
	}

	// /**
	// * check if a taxonomy group match
	// *
	// * @param container
	// * @param filter
	// * @return
	// */
	// public boolean isMatchAll(ITaxonomyContainer container, ITaxonomyContainer
	// filter) {
	// if (container == null || filter == null) {
	// return true;
	// }
	// if (container.getTaxonomy() == null || container.getTaxonomy().size() == 0) {
	// if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
	// return true;
	// } else {
	// return false;
	// }
	// }
	// if (filter.getTaxonomy() == null || filter.getTaxonomy().size() == 0) {
	// return false;
	// }
	// for (String taxo : container.getTaxonomy()) {
	// TaxonomyBean bean = getTaxonomyBeanMap().get(taxo);
	// for (String fTaxo : filter.getTaxonomy()) {
	// if (!fTaxo.equals(bean.getName())) {
	// if (!bean.hasParent(fTaxo)) {
	// return false;
	// }
	// }
	// }
	// }
	// return true;
	// }

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

	public String getAsJson() {
		String json = new Gson().toJson(getRoot());
		return json;
	}

	public String getAsJsonLinked() {
		String json = new Gson().toJson(getLinkedRoot());
		return json;
	}

	public String exportAsText() {
		return exportAsText(root, "");
	}

	public void importText(String text) {
		Pattern patternId = Pattern.compile("(.*?)\\|");
		Pattern patternName = Pattern.compile("\\|(.*?)\\[");
		Pattern patternLabels = Pattern.compile("\\[(.*?)\\]");
		BufferedReader reader = new BufferedReader(new StringReader(text));
		String line;
		try {
			line = reader.readLine();
			int lineNumber = 0;
			int latestDepth = 0;
			TaxonomyBean latestNode = null;
			while (line != null) {
				lineNumber++;
				TaxonomyBean bean = new TaxonomyBean();

				int depth = 0;
				while (line.startsWith("-")) {
					line = line.substring(1);
					depth++;
				}

				if (!line.contains("[")) {
					line += "[]";
				}

				Matcher matcher = patternId.matcher(line);
				if (matcher.find()) {
					bean.setId(matcher.group(1));
					if (bean.getId().equals("?")) {
						bean.setId(StringHelper.getRandomId());
					}
				} else {
					logger.severe("error id import taxonomy on line " + lineNumber + " : " + line);
				}

				matcher = patternName.matcher(line);
				if (matcher.find()) {
					bean.setName(matcher.group(1));
				} else {
					logger.severe("error name import taxonomy on line " + lineNumber + " : " + line);
				}

				matcher = patternLabels.matcher(line);
				if (matcher.find()) {
					bean.setLabels(StringHelper.textToMap(matcher.group(1)));
				}

				if (lineNumber == 1) {
					root = bean;
				} else {
					if (depth > latestDepth) {
						latestNode.addChild(bean, null);
					} else if (depth < latestDepth) {
						latestNode.getParent().addChild(bean, null);
					} else {
						latestNode.getParent().addChild(bean, latestNode.getId());
					}
				}

				latestDepth = depth;
				latestNode = bean;

				line = reader.readLine();

			}

			clearCache();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String exportAsText(TaxonomyBean bean, String prefix) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.print(prefix + bean.getId() + '|' + bean.getName());
		if (bean.getLabels().size() > 0) {
			out.print('[');
			try {
				out.print(StringHelper.mapToText(bean.getLabels(), ","));
			} catch (IOException e) {
				e.printStackTrace();
			}
			out.println(']');
		} else {
			out.println("");
		}

		prefix += "-";
		for (TaxonomyBean child : bean.getChildren()) {
			out.print(exportAsText(child, prefix));
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public void reset() {
		root = new TaxonomyBean("0", "root");
		clearCache();
	}

	public static void main(String[] args) {
		TaxonomyService taxo = new TaxonomyService();
		taxo.createDebugStructure();		

		int i = 0;
		for (TaxonomyBean bean : taxo.getRoot().getAllChildren()) {
			System.out.println("> "+(i++)+" = "+bean.getName()+" id="+bean.getId());
		}
		System.out.println("");
		i = 0;
		for (TaxonomyBean bean : taxo.getLinkedRoot().getAllChildren()) {
			System.out.println("> "+(i++)+" = "+bean.getName()+" id="+bean.getId()+" path="+bean.getPath());
		}
		
		System.out.println("");

		TaxonomyBean child = taxo.getRoot().getAllChildren().get(0);
		TaxonomyBean subChild = taxo.getRoot().getAllChildren().get(1);
		TaxonomyBean subChild2 = taxo.getRoot().getAllChildren().get(3);
		TaxonomyBean subSubChild = taxo.getRoot().getAllChildren().get(2);
		TaxonomyBean otherChild = taxo.getRoot().getAllChildren().get(5);
		
		System.out.println("subChild    = "+subChild.getName());
		System.out.println("subChild2   = "+subChild2.getName());
		System.out.println("subSubChild = "+subSubChild.getName());
		System.out.println("otherChild  = "+otherChild.getName());
		System.out.println("");

		System.out.println("true="+taxo.isMatch(new TaxonomyContainerBean(subSubChild.getId()), new TaxonomyContainerBean(subChild.getId())));
		System.out.println("false="+taxo.isMatch(new TaxonomyContainerBean(subChild.getId()), new TaxonomyContainerBean(otherChild.getId())));
		System.out.println("-");
		System.out.println("true="+taxo.isMatch(new TaxonomyContainerBean(subChild.getId()), new TaxonomyContainerBean(subChild.getId(), otherChild.getId())));
		System.out.println("false="+taxo.isAllMatch(new TaxonomyContainerBean(child.getId()), new TaxonomyContainerBean(subChild.getId(), otherChild.getId())));
		System.out.println("-");
		
		
		System.out.println(">>>>>>>>> TaxonomyService.main : child.getId() = "+child.getId()); //TODO: remove debug trace
		System.out.println(">>>>>>>>> TaxonomyService.main : subChild.getParent().getId() = "+subChild.getParent().getId()); //TODO: remove debug trace
		System.out.println(">>>>>>>>> TaxonomyService.main : subChild2.getParent().getId() = "+subChild2.getParent().getId()); //TODO: remove debug trace
		System.out.println("-");
		System.out.println("true = "+taxo.isMatch(new TaxonomyContainerBean(subChild.getId()), new TaxonomyContainerBean(child.getId())));
		System.out.println("true = "+taxo.isMatch(new TaxonomyContainerBean(subChild2.getId()), new TaxonomyContainerBean(child.getId())));
		System.out.println("true = "+taxo.isAllMatch(new TaxonomyContainerBean(subChild.getId(), subChild2.getId()), new TaxonomyContainerBean(child.getId())));
	}

	public static void _main(String[] args) {
		String line = "0|root[]";
		Pattern patternId = Pattern.compile("(.*?)\\|");
		Pattern patternName = Pattern.compile("\\|(.*?)\\[");
		Pattern patternLabels = Pattern.compile("\\[(.*?)\\]");

		Matcher matcher = patternId.matcher(line);
		if (matcher.find()) {
			System.out.println("id:" + matcher.group(1));
		} else {
			logger.severe("error id import taxonomy on line : " + line);
		}

	}

	public static void __main(String[] args) {
		String line = "0|root[]";
		Matcher matcher = Pattern.compile("\\[(.*?)\\]").matcher(line);
		if (matcher.find()) {
			System.out.println(matcher.group(1));
		} else {
			System.out.println("NOT FOUND.");
		}
		matcher = Pattern.compile("(.*?)\\|").matcher(line);
		if (matcher.find()) {
			System.out.println("> " + matcher.group(1));
		} else {
			System.out.println("NOT FOUND.");
		}
		matcher = Pattern.compile("\\|(.*?)\\[").matcher(line);
		if (matcher.find()) {
			System.out.println("> " + matcher.group(1));
		} else {
			System.out.println("NOT FOUND.");
		}
	}

}