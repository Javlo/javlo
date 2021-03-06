package org.javlo.service.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.text.Description;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;

public class SharedContent {

	public static final class SortOnComparator implements Comparator<SharedContent> {

		@Override
		public int compare(SharedContent o1, SharedContent o2) {
			if (o1 == o2) {
				return 0;
			} else {
				if (o1 == null) {
					return -1;
				} else if (o2 == null) {
					return 1;
				}
				if (o2.getSortOn() == o1.getSortOn()) {
					return 0;
				} else if (o1.getSortOn() > o2.getSortOn()) {
					return -1;
				} else {
					return 1;
				}				
			}
		}

	}

	public static final String SHARED_CONTENT_FOLDER = "shared";

	private String title = null;
	private String description = null;
	private String imageURL = null;
	private String photoPageLink = null;
	private String id = null;
	private Collection<String> categories = new HashSet<String>();
	private String linkInfo = null;
	private long sortOn = -1;
	private String editURL = null;
	private boolean editAsModal = false;

	protected List<ComponentBean> content;

	public SharedContent(String id, ComponentBean content) throws Exception {
		init(id, Arrays.asList(new ComponentBean[] { content }));
	}

	public SharedContent(String id, Collection<ComponentBean> content) throws Exception {
		init(id, content);
	}

	private void init(String id, Collection<ComponentBean> content) throws Exception {
		this.id = id;
		if (content != null) {
			this.content = new LinkedList<ComponentBean>();
			for (ComponentBean bean : content) {
				ComponentBean newBean = new ComponentBean(bean);
				newBean.resetArea();
				this.content.add(newBean);
			}
			for (ComponentBean componentBean : this.content) {
				if (componentBean.getType().equals(Title.TYPE)) {
					title = componentBean.getValue();
				} else if (componentBean.getType().equals(Description.TYPE)) {
					description = componentBean.getValue();
				} else if (componentBean.getType().equals(SubTitle.TYPE) && title == null) {
					title = componentBean.getValue();
				}
			}
		}
	}

	/**
	 * load content if remote, do nothing if local.
	 * 
	 * @param ctx
	 */
	public void loadContent(ContentContext ctx) {
	}

	/**
	 * get the content as javlo ComponentBean. Sometime the method need
	 * convertion.
	 * 
	 * @return
	 */
	public List<ComponentBean> getContent() {
		return content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageUrl(String url) {
		imageURL = url;
	}

	public String getId() {
		return id;
	}

	public Collection<String> getCategories() {
		return categories;
	}

	public void setCategories(Collection<String> categories) {
		this.categories = categories;
	}

	public void addCategory(String category) {
		getCategories().add(category);
	}

	/**
	 * get information for create a linked content. A linked content change if
	 * the source is modified.
	 * 
	 * @return null if content not linkable.
	 */
	public String getLinkInfo() {
		return linkInfo;
	}

	public void setLinkInfo(String linkInfo) {
		this.linkInfo = linkInfo;
	}

	public long getSortOn() {
		return sortOn;
	}
	
	/*@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SharedContent)) {
			return super.equals(obj);
		} else {
			return ((SharedContent)obj).getSortOn() == getSortOn();
		}
	}*/

	public void setSortOn(long sortOn) {
		this.sortOn = sortOn;
	}

	public String getEditURL() {
		return editURL;
	}

	public void setEditURL(String editURL) {
		this.editURL = editURL;
	}

	public boolean isEditAsModal() {
		return editAsModal;
	}

	public void setEditAsModal(boolean editAsPopup) {
		this.editAsModal = editAsPopup;
	}

	public String getPhotoPageLink() {
		return photoPageLink;
	}

	public void setPhotoPageLink(String photoPageLink) {
		this.photoPageLink = photoPageLink;
	}

}
