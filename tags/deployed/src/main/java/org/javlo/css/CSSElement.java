package org.javlo.css;

import java.util.LinkedList;
import java.util.List;

import org.javlo.helper.XMLManipulationHelper.TagDescription;


public class CSSElement {

	public class Tag {
		private String name;

		private String id;

		private String clazz;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getClazz() {
			return clazz;
		}

		public void setClazz(String clazz) {
			this.clazz = clazz;
		}

		public boolean match(TagDescription tagDescription) {
			boolean match = true;
			
			if (getName() != null) {
				if (!tagDescription.getName().toLowerCase().equals(getName().toLowerCase())) {
					match = false;
				}
			}
			if (getId() != null) {
				if ((tagDescription.getAttributes().get("id") == null)) {
					match = false;
				} else {
					if ((!tagDescription.getAttributes().get("id").toLowerCase().equals(getId()))) {
						match = false;
					}
				}
			}
			
			if (getClazz() != null) {
				if ((tagDescription.getAttributes().get("class") == null)) {
					match = false;
				} else {
					String[] cssClasses = tagDescription.getAttributes().get("class").split(" ");
					match = false;
					for (int i = 0; i < cssClasses.length; i++) {
						if ((cssClasses[i].equals(getClazz()))) {
							match = true;						
						}
					}
				}
			}
			return match;
		}
		@Override
		public String toString() {
			return "name:"+getName()+" class="+getClazz()+" id="+getId();
		}
	}

	private final List<Tag> tags = new LinkedList<Tag>();

	private String style;

	private String tag;

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		String[] tagsArray = tag.split(" ");
		for (String tagStr : tagsArray) {
			Tag newTag = new Tag();
			if (tagStr.contains(".")) {
				if (tagStr.startsWith(".")) {
					newTag.setClazz(tagStr.substring(1));
				} else {
					newTag.setName(tagStr.split("\\.")[0]);
					newTag.setClazz(tagStr.split("\\.")[1]);
				}
			} else if (tagStr.contains("#")) {
				if (tagStr.startsWith("#")) {
					newTag.setId(tagStr.substring(1));
				} else {
					newTag.setName(tagStr.split("#")[0]);
					newTag.setId(tagStr.split("#")[1]);
				}
			} else {
				newTag.setName(tagStr);
			}
			tags.add(newTag);
		}		
		this.tag = tag;
	}

	public List<Tag> getTags() {
		return tags;
	}
}
