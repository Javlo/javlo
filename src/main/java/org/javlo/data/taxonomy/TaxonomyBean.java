package org.javlo.data.taxonomy;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.javlo.helper.StringHelper;

public class TaxonomyBean {

	private String id;
	private String name;
	private TaxonomyBean parent;
	private List<TaxonomyBean> children = new LinkedList<TaxonomyBean>();
	private Map<String, String> labels = new HashMap<String, String>();

	public TaxonomyBean() {
	}

	public TaxonomyBean(String id, String name, TaxonomyBean parent) {
		super();
		this.id = id;
		this.name = name;
		this.parent = parent;
	}

	public TaxonomyBean(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public boolean setName(String name) {
		if (this.name == null || !this.name.equals(name)) {
			this.name = name;
			return true;
		} else {
			return false;
		}
	}

	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	public boolean updateLabel(String lang, String label) {
		if (StringHelper.isEmpty(label)) {
			if (labels.get(lang) == null) {
				return false;
			} else {
				labels.remove(lang);
			}
		}
		if (labels.get(lang) == null || !labels.get(lang).equals(label)) {
			labels.put(lang, label);
			return true;
		} else {
			return false;
		}
	}

	public int getLabelsSize() {
		int labelSize = 0;
		for (String label : labels.values()) {
			if (!StringHelper.isEmpty(label)) {
				labelSize++;
			}
		}
		return labelSize;
	}

	public List<TaxonomyBean> getChildren() {
		return children;
	}

	public void setChildren(List<TaxonomyBean> children) {
		this.children = children;
		for (TaxonomyBean child : children) {
			child.setParent(child);
		}
	}

	public TaxonomyBean addChildAsFirst(TaxonomyBean child) {
		children.add(0, child);
		child.setParent(this);
		return child;
	}

	public TaxonomyBean addChildAsLast(TaxonomyBean child) {
		children.add(child);
		child.setParent(this);
		return child;
	}

	public TaxonomyBean addChild(TaxonomyBean newChild, String previousId) {
		int ind = 0;
		if (previousId != null) {
			for (TaxonomyBean child : getChildren()) {
				ind++;
				if (child.getId().equals(previousId)) {
					break;
				}
			}
		}
		children.add(ind, newChild);
		return newChild;
	}

	public boolean hasChild(String id) {
		for (TaxonomyBean bean : children) {
			if (bean.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	public TaxonomyBean getParent() {
		return parent;
	}

	public void setParent(TaxonomyBean parent) {
		this.parent = parent;
	}

	public String getPath() {
		if (getParent() == null) {
			return "";
		} else {
			if (getParent().getParent() == null) {
				return getName();
			} else {
				return getParent().getPath() + " > " + getName();
			}
		}
	}
}
