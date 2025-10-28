package org.javlo.data.taxonomy;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.IListItem;
import org.javlo.service.ListService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TaxonomyDisplayBean {

	private ContentContext ctx;
	private TaxonomyBean bean;
	private boolean displayParentLabel = false;

	public TaxonomyBean getBean() {
		return bean;
	}

	public final static List<TaxonomyDisplayBean> convert (ContentContext ctx, List<TaxonomyBean> inBeans) {
		if (inBeans == null) {
			return null;
		} else if (inBeans.size() == 0) {
			return Collections.EMPTY_LIST;
			
		}
		List<TaxonomyDisplayBean> outBeans = new LinkedList<TaxonomyDisplayBean>();
		for (TaxonomyBean bean : inBeans) {
			outBeans.add(new TaxonomyDisplayBean(ctx, bean));
		}
		return outBeans;
	}
	
	public TaxonomyDisplayBean(ContentContext ctx, TaxonomyBean bean) {		
		this.ctx = ctx;
		this.bean = bean;
	}
	
	public String getPath() {
		return bean.getPath();		
		
	}
	
	public TaxonomyDisplayBean getParent() {
		TaxonomyBean parent = bean.getParent();
		if (parent == null) {
			return null;
		} else {
			return new TaxonomyDisplayBean(ctx, parent);
		}
	}
	
	public List<TaxonomyDisplayBean> getChildren() {
		List<TaxonomyDisplayBean> children = new LinkedList<>();
		for (TaxonomyBean bean : bean.getChildren()) {
			children.add(new TaxonomyDisplayBean(ctx, bean));
		}
		return children;
	}

	public List<TaxonomyDisplayBean> getChildrenSortByLabel() {
		List<TaxonomyDisplayBean> children = new LinkedList<>();
		for (TaxonomyBean bean : bean.getChildren()) {
			children.add(new TaxonomyDisplayBean(ctx, bean));
		}
		Collections.sort(children, (o1, o2) -> o1.getLabel().compareTo(o2.getLabel()));
		return children;
	}
	
	public List<TaxonomyDisplayBean> getAllChildren() {
		List<TaxonomyDisplayBean> children = new LinkedList<>();
		for (TaxonomyBean bean : bean.getAllChildren()) {
			children.add(new TaxonomyDisplayBean(ctx, bean));
		}
		return children;
	}
	
	public String getName() {
		return bean.getName();
	}
	
	public String getId() {
		return bean.getId();
	}
	
	public String getDecoration() {
		return bean.getDecoration();
	}
	
	public String getPathLabel() {
		String parentLabel = "";
		if (getParent() != null && getParent().getParent() != null) {
			if (!getParent().getLabel().equals(".")) {
				parentLabel = getParent().getPathLabel()+" > ";
			}
		}
		String label = bean.getLabels().get(ctx.getRequestContentLanguage());
		if (StringHelper.isEmpty(label)) {
			return parentLabel+getName();
		} else {
			return parentLabel+label;
		}
	}
	
	public String getLabel() {
		String parentLabel = "";
		if (displayParentLabel) {
			parentLabel = getParent().getLabel()+" > ";
		}
		String label = bean.getLabels().get(ctx.getRequestContentLanguage());
		if (StringHelper.isEmpty(label)) {
			return parentLabel+getName();
		} else {
			return parentLabel+label;
		}
	}
	
	public String getLocalLabel() {		
		String label = bean.getLabels().get(ctx.getRequestContentLanguage());
		if (StringHelper.isEmpty(label)) {
			return getName();
		} else {
			return label;
		}
	}

	/**
	 * get the first signifiant parent (not root)
	 * @return
	 */
	public TaxonomyDisplayBean getFirstParent() {
		TaxonomyBean parent = this.bean;
		if (parent.getParent() == null) {
			return this;
		}
		while (parent.getParent().getParent() != null) {
			parent = parent.getParent();
		}
		return new TaxonomyDisplayBean(ctx, parent);
	}
	
	public IListItem getListItem() {
		return new ListService.ListItem(bean.getName(), getLocalLabel());
	}

	public boolean isDisplayParentLabel() {
		return displayParentLabel;
	}

	public void setDisplayParentLabel(boolean displayParentLabel) {
		this.displayParentLabel = displayParentLabel;
	}
	
	public int getDepth() {
		return this.bean.getDepth();
	}

	public String getImage() {
		return this.bean.getImage();
	}
}