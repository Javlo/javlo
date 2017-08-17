package org.javlo.data.taxonomy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class TaxonomyDisplayBean {

	private ContentContext ctx;
	private TaxonomyBean bean;
	
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
		super();
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
	
	public String getName() {
		return bean.getName();
	}
	
	public String getLabel() {
		String label = bean.getLabels().get(ctx.getRequestContentLanguage());
		if (StringHelper.isEmpty(label)) {
			return getName();
		} else {
			return label;
		}
	}
}