package org.javlo.service.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.text.Description;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;

public class SharedContent {
	
	public static final String SHARED_CONTENT_FOLDER = "shared";

	private String title = null;
	private String description = null;
	private String imageURL = null;
	private String id = null;
	private Collection<String> categories = new HashSet<String>();
	private String linkInfo = null;

	protected List<ComponentBean> content;
	
	public SharedContent(String id, ComponentBean content) throws Exception {
		init(id, Arrays.asList(new ComponentBean[] {content}));		
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
				newBean.setArea(null);
				this.content.add(newBean);
			}
			for (ComponentBean componentBean : this.content) {
				if (componentBean.getType().equals(Title.TYPE)) {
					title = componentBean.getValue();
				} else if (componentBean.getType().equals(Description.TYPE)) {
					description = componentBean.getValue();
				}
			}
		}
	}
	
	/**
	 * load content if remote, do nothing if local.
	 * @param ctx
	 */
	public void loadContent(ContentContext ctx) {
	}

	/**
	 * get the content as javlo ComponentBean.  Sometime the method need convertion.
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
	 * get information for create a linked content.
	 * A linked content change if the source is modified.
	 * @return null if content not linkable.
	 */
	public String getLinkInfo() {
		return linkInfo;
	}

	public void setLinkInfo(String linkInfo) {
		this.linkInfo = linkInfo;
	}

}
