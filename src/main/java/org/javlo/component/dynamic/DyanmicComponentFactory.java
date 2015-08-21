package org.javlo.component.dynamic;

import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class DyanmicComponentFactory extends AbstractVisualComponent {
	
	protected Properties properties = new Properties();
	
	public static final String TYPE = "dyanmic-component-factory";
	
	private static final String FIELD_ID = "list.id";
	private static final String COMP_TYPE = "component.type";

	public DyanmicComponentFactory() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getType() {
		return 	TYPE;
	}
	
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {	
		super.init(bean, ctx);
		properties.load(stringToStream(getValue()));
	}
	
	@Override
	public String getHexColor() {	
		return CONTAINER_COLOR;
	}
	
	protected String getIdField() {
		return properties.getProperty(FIELD_ID);
	}
	
	protected String getDynamicComponentType() {
		return properties.getProperty(COMP_TYPE);
	}
	
	public void update(ContentContext ctx) {
		if (getDynamicComponentType() != null && getDynamicComponentType().trim().length() > 0) {
			ComponentBean bean = new ComponentBean(StringHelper.getRandomId(), getDynamicComponentType(), "", getComponentBean().getLanguage(), false, ctx.getCurrentEditUser());
			bean.setArea(getArea());
			getPage().addContent(getId(), bean);
		}
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {	
		super.performEdit(ctx);
		properties.clear();
		properties.load(stringToStream(getValue()));
		update(ctx);
	}

}
