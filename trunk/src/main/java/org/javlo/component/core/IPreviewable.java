package org.javlo.component.core;

import org.javlo.context.ContentContext;

/**
 * a component with a preview zone, this Interface is use for Ajax response of a update of that zone.
 * @author Patrick Vandermaesen
 *
 */
public interface IPreviewable {
	
	String getPreviewCode (ContentContext ctx, int maxInstance) throws Exception;

}
