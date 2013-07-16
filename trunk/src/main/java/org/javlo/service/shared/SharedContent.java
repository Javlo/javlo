package org.javlo.service.shared;

import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.text.Description;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;

public class SharedContent {
	
	private String title = null;
	private String description = null;
	private String imageURL = null;
	private String id = null;
	
	private final List<ComponentBean> content;
	
	public SharedContent (ContentContext ctx, String id, List<ComponentBean> content) throws Exception {
		this.id = id;
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
			} else if (componentBean.getType().equals(GlobalImage.TYPE)) {								
				try {
					GlobalImage image = new GlobalImage();
					image.init(componentBean, ctx);
					imageURL = image.getPreviewURL(ctx, "shared-preview");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public List<ComponentBean> getContent() {
		return content;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getImageURL() {
		return imageURL;
	}
	
	public String getId() {
		return id;
	}

}
