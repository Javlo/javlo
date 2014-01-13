package org.javlo.component.image;

import java.io.Serializable;

import org.javlo.context.ContentContext;

public interface IImageTitle extends Serializable {

	public static final String NO_LINK = "#";

	public String getImageDescription(ContentContext ctx);

	public String getResourceURL(ContentContext ctx);

	public String getImageLinkURL(ContentContext ctx);

	public boolean isImageValid(ContentContext ctx);

}
