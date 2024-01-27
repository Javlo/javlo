package org.javlo.component.container;

import org.javlo.context.ContentContext;

public interface ISection extends IContainer {

	public String getSectionId();
	public String getTitle();

	public boolean isOpen(ContentContext ctx);

}
