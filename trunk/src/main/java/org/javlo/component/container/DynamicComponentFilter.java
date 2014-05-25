package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.IFieldContainer;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.DynamicComponentService;
import org.javlo.service.RequestService;

/**
 * Display a search for in view mode. <h4>exposed variables :</h4>
 * <ul>
 * <li>{@link Field} fields : list of search field.</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 */
public class DynamicComponentFilter extends AbstractPropertiesComponent implements IAction {

	public static final String TYPE = "dynamic-component-filter";

	private Boolean realContent = null;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("fields", getSearchField(ctx));
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<String> container = service.getAllType(ctx, content.getNavigation(ctx));

		out.println(XHTMLHelper.getInputOneSelect(createKeyWithField("type"), container, getSelectedType()));

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		realContent = false;

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer == null) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<form class=\"generic-form\" id=\"form-filter-" + getId() + "\" name=\"form-filter-" + getId() + "\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
		out.println("<div class=\"fields\"><input type=\"hidden\" name=\"webaction\" value=\"" + getActionGroupName() + ".filter\" />");
		out.println("<input type=\"hidden\" name=\"" + IContentVisualComponent.COMP_ID_REQUEST_PARAM + "\" value=\"" + getId() + "\">");
		for (Field field : (List<Field>) ctx.getRequest().getAttribute("fields")) {
			out.println(field.getEditXHTMLCode(ctx));
		}
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<div class=\"action\"><input type=\"submit\" name=\"filter\" value=\"" + i18nAccess.getViewText("global.ok") + "\" /></div>");
		out.println("</div></form>");

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		List<IFieldContainer> containers = service.getFieldContainers(ctx, content.getNavigation(ctx), getSelectedType());

		Map<String, Field> fieldsSearch = new HashMap<String, Field>();

		boolean isFilter = false;

		for (Field field : getSearchField(ctx)) {
			fieldsSearch.put(field.getName(), field);
			if (field.getValue() != null && field.getValue().trim().length() > 0) {
				isFilter = true;
			}
		}

		if (isFilter) {
			out.println("<ul class=\"filter-list\">");
			for (IFieldContainer container : containers) {
				boolean display = true;
				List<Field> fields = container.getFields(ctx);
				for (Field field : fields) {
					Field searchField = fieldsSearch.get(field.getName());
					if (searchField != null && searchField.getValue() != null && searchField.getValue().trim().length() > 0) {
						if (!field.getValue().toLowerCase().contains(searchField.getValue().toLowerCase().trim())) {
							display = false;
						}
					}
				}
				if (display) {
					realContent = true;
					out.println("<li class=\"dynamic-component\">");
					out.println(container.getPrefixViewXHTMLCode(ctx));
					out.println(container.getViewListXHTMLCode(ctx));
					out.println(container.getSuffixViewXHTMLCode(ctx));
					out.println("</li>");
				}
			}
		}
		out.println("</ul>");

		out.close();
		return new String(outStream.toByteArray());

	}

	private String getSelectedType() {
		return properties.getProperty("type");
	}

	IFieldContainer getFieldContainer(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<IFieldContainer> containers = service.getFieldContainers(ctx, content.getNavigation(ctx), getSelectedType());
		if (containers.size() > 0) {
			return containers.iterator().next();
		} else {
			return null;
		}
	}

	protected List<Field> getSearchField(ContentContext ctx) throws Exception {
		final String KEY = "search-fields-" + getId();
		List<Field> fields = (List<Field>) ctx.getRequest().getSession().getAttribute(KEY);
		if (fields == null) {
			fields = new LinkedList<Field>();
			DynamicComponent comp = (DynamicComponent) ComponentFactory.getComponentWithType(ctx, getSelectedType());
			for (Field field : comp.getFields(ctx)) {
				if (field.isSearch()) {
					System.out.println("***** DynamicComponentFilter.getSearchField : field name = "+field.getName()); //TODO: remove debug trace
					field.setId(getId());
					fields.add(field);
				} else {
					System.out.println("***** DynamicComponentFilter.getSearchField : NOT field name = "+field.getName()); //TODO: remove debug trace
				}
			}
		}
		return fields;
	}

	@Override
	public String getHeader() {
		return getType();
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {	
		return false;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	public List<String> getFields(ContentContext ctx) throws Exception {
		List<String> outList = new LinkedList<String>();
		outList.add("type");
		return outList;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	public static String performFilter(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		DynamicComponentFilter comp = (DynamicComponentFilter) ComponentHelper.getComponentFromRequest(ctx);
		for (Field field : comp.getSearchField(ctx)) {
			field.setValue(rs.getParameter(field.getInputName(), ""));
		}
		return null;
	}
}
