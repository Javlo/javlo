/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.web2;

import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.fields.Field;
import org.javlo.fields.FieldImage;
import org.javlo.helper.StringHelper;
import org.javlo.utils.StructuredProperties;

/**
 * @author pvandermaesen
 */
public class StaticMapComponent extends DynamicComponent implements IAction {
	
	public static String TYPE = "static-map";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(StaticMapComponent.class.getName());
	
	public StaticMapComponent() {
		properties = new StructuredProperties();
	}
	
	@Override
	protected void init() throws ResourceNotFoundException {	
		
		super.init();
		
		int order=1;
		properties.put("field.map.type", "image");
		properties.put("field.map.order", order++);
		
		properties.put("field.topLeftCoord.type", "text");
		properties.put("field.topLeftCoord.order", order++);
		
		properties.put("field.bottomRightCoord.type", "text");
		properties.put("field.bottomRightCoord", order++);
		
		properties.put("field.points.type", "large-text");
		properties.put("field.points.order", order++);
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		
		for (Field field : getFields(ctx)) {
			if (field.getName().equals("map")) {
				FieldImage image = (FieldImage)field;
				ctx.getRequest().setAttribute("map", image.getURL(ctx));
			} else if (field.getName().equals("points")) {
				Properties prop = new Properties();
				prop.load(new StringReader(field.getValue()));
				ctx.getRequest().setAttribute("points", StringHelper.textToMap(field.getValue()));
			} else {
				ctx.getRequest().setAttribute(field.getName(), field.getValue());
			}
		}
		
	}
	
//	@Override
//	public List<Field> getFields(ContentContext ctx) throws FileNotFoundException, IOException {
//		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
//		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
//		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
//
//		java.util.List<Field> outFields = new LinkedList<Field>();
//
//		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), "map", "map", "image", getId()));
//		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), "top-left-coord", "top-left-coord", "text", getId()));
//		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), "bottom-right-coord", "bottom-right-coord", "text", getId()));		
//		outFields.add(FieldFactory.getField(this, staticConfig, globalContext, i18nAccess, getProperties(), "points", "points", "large-text", getId()));
//
//		Collections.sort(outFields, new FieldOrderComparator());
//		return outFields;
//	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.WEB2_COLOR;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getFontAwesome() {	
		return "location";
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
}