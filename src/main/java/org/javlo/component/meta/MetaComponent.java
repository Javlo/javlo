/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.meta;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.context.ContentContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.fields.Field;
import org.javlo.helper.StringHelper;
import org.javlo.utils.StructuredProperties;

import java.util.logging.Logger;

/**
 * @author pvandermaesen
 */
public class MetaComponent extends DynamicComponent implements IAction {

	public static String TYPE = "meta-component";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(MetaComponent.class.getName());

	public MetaComponent() {
		properties = new StructuredProperties();
	}
	
	@Override
	protected void init() throws ResourceNotFoundException {	
		
		super.init();
		
		int order=10;

		if (StringHelper.isEmpty(getValue())) {

			properties.put("field.title.type", "h1");
			properties.put("field.title.order", "" + order);
			properties.put("field.title.value", "");
			order+=10;

			properties.put("field.description.type", "large-text");
			properties.put("field.description.order", "" + order);
			properties.put("field.description.value", "");
			order+=10;

			properties.put("field.menuLabel.type", "text");
			properties.put("field.menuLabel.order", "" + order);
			properties.put("field.menuLabel.value", "");
			order+=10;

			properties.put("field.pageTitle.type", "text");
			properties.put("field.pageTitle.order", "" + order);
			properties.put("field.pageTitle.value", "");
			order+=10;

			properties.put("field.linkLabel.type", "text");
			properties.put("field.linkLabel.order", "" + order);
			properties.put("field.linkLabel.value", "");
			order+=10;

			storeProperties();
		} else {

			reloadProperties();

			if (!getValue().contains("field.pageTitle")) {
				properties.put("field.pageTitle.type", "text");
				properties.put("field.pageTitle.order", "" + 35);
				properties.put("field.pageTitle.value", "");
				updateOrder();
			}
		}

		/*properties.put("field.pageImage.type", "image");
		properties.put("field.pageImage.order", order++);*/
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (!ctx.isEdition()) {
			return "";
		} else {
			return super.getPrefixViewXHTMLCode(ctx);
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (!ctx.isEdition()) {
			return "";
		} else {
			return super.getSuffixViewXHTMLCode(ctx);
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (!ctx.isEdition()) {
			return "";
		} else {
			String out = "";
			out += "<style>._meta_info-box{border:4px solid var(--javlo-flash-color);border-radius:8px;padding:10px 15px;width:fit-content; max-width: 80%; background-color:#f9f9f9;font-family:sans-serif;margin: 1rem auto;}.info-box h3{margin-top:0;margin-bottom:8px;font-size:1.1em;border-bottom:1px solid #ddd;padding-bottom:4px;}.info-box table{border-collapse:collapse;width:100%;}.info-box td{padding:4px 8px;}.info-box td:first-child{font-weight:bold;color:#555;}\n" +
					"</style>";
			out += "<div class='_meta_info-box'><h3>"+getType()+"&nbsp;:&nbsp;</h3>";
			out += "<table>";
			for (Field f : getFields(ctx)) {
				out += "<tr><th>"+f.getName()+"</th><td>"+f.getValue()+"</td></tr>";
			}
			out += "</table></div>";
			return out;
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
	public String getIcon() {
		return "bi bi-info-circle";
	}

	@Override
	public String getActionGroupName() {
		return TYPE;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_STANDARD;
	}
}