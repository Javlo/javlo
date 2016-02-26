/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.core;

import org.javlo.context.ContentContext;

/**
 * Need for create a title in the list of component
 *
 * @author pvandermaesen
 */
public class MetaTitle extends AbstractVisualComponent {

    public MetaTitle() {
        super();
    }

    public MetaTitle(String inValue) {
        super();
        setValue(inValue);
        getComponentBean().setModify(false); // reset modif
    }

    @Override
	public String getType() {
        return "special-title";
    }

    /**
     * @see org.javlo.itf.IContentVisualComponent#isVisible(int)
     */
    @Override
	public boolean isVisible(ContentContext ctx) {
        return false;
    }

    @Override
	public String getHexColor() {
		return META_COLOR;
	}
    
    @Override
    public boolean isMetaTitle() {
    	return true;
    }
}
