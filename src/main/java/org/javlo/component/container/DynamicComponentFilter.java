package org.javlo.component.container;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentFactory;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.dynamic.DynamicComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.FieldMultiList;
import org.javlo.fields.IFieldContainer;
import org.javlo.fields.SortContainer;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.DynamicComponentService;
import org.javlo.service.RequestService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Display a search for in view mode.
 * <h4>exposed variables :</h4>
 * <ul>
 * <li>{@link Field} fields : list of search field.</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 */
public class DynamicComponentFilter extends AbstractPropertiesComponent implements IAction {

	public static final String TYPE = "dynamic-component-filter";

	private static final String STYLE_ALL = "default_all";

	private static final String CURRENT_USER_ONLY = "current_user_only";

	private static final String HIDE_FORM = "hide_form";
	
	private static final String HIDE_FORM_USER_ONLY = "hide_form_user_only";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "default_none", STYLE_ALL, HIDE_FORM, CURRENT_USER_ONLY, HIDE_FORM_USER_ONLY };
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		if (!"POST".equals(ctx.getRequest().getMethod())) {
			for (Field field : getSearchField(ctx)) {
				if (!StringHelper.isEmpty(getDefaultValue(field))) {
					field.setValue(getDefaultValue(field));
				}
			}
		}
		ctx.getRequest().setAttribute("fields", getSearchField(ctx));
	}

	protected String getDefaultValue(Field field) {
		return properties.getProperty("default-" + field.getName());
	}

	protected void setDefaultValue(Field field, String value) {
		if (field != null && value != null) {
			properties.setProperty("default-" + field.getName(), value);
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<String> container = service.getAllType(ctx, content.getNavigation(ctx));
		out.println("<div class=\"input-group\">");
		out.println(XHTMLHelper.getInputOneSelect(createKeyWithField("type"), container, getSelectedType(), "form-control"));
		out.println("</div>");
		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer != null) {
			out.println("<div class=\"input-group\">");
			out.println("<label>Sort on : </label>");
			/*
			 * List<String> values = new LinkedList<String>(); values.add("");
			 * for (Field field : fieldContainer.getFields(ctx)) {
			 * values.add(field.getName()); }
			 * out.println(XHTMLHelper.getInputOneSelect(createKeyWithField(
			 * "field"),values , getSelectedField(), "form-control"));
			 */
			out.println("<input type=\"text\" class=\"form-control\" name=\"" + createKeyWithField("field") + "\" value=\"" + StringHelper.neverNull(getSelectedField()) + "\" /></div>");
			out.println("<fieldset><legend>default value</legend>");
			for (Field field : (List<Field>) getSearchField(ctx)) {
				field.setValue(getDefaultValue(field));
				out.println(field.getSearchEditXHTMLCode(ctx));
			}
			out.println("</fieldset>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		prepareView(ctx);

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer == null) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		
		/* special, if no preview mode --> end user can create component --> admin user can manage it */
		boolean componentManager = ctx.isUserWebSiteManager() && !ctx.getGlobalContext().isPreviewMode();
		
		if (!getStyle().equals(HIDE_FORM) && !getStyle().equals(HIDE_FORM_USER_ONLY) || componentManager) {
			out.println("<div class=\"filter-form card panel panel-default\"><form role=\"form\" class=\"generic-form\" id=\"form-filter-" + getId() + "\" name=\"form-filter-" + getId() + "\" action=\"" + URLHelper.createURL(ctx) + "\" method=\"post\">");
			out.println("<div class=\"fields panel-body card-body\"><input type=\"hidden\" name=\"webaction\" value=\"" + getActionGroupName() + ".filter\" />");
			out.println("<input type=\"hidden\" name=\"" + IContentVisualComponent.COMP_ID_REQUEST_PARAM + "\" value=\"" + getId() + "\"><div class=\"row field-row first-row\">");
			final int SIZE = 6;
			int col = 0;
			int size = 0;
			List<Field> fields = (List<Field>) ctx.getRequest().getAttribute("fields");
			for (Field field : fields) {
				int localSize = SIZE;
				if (field.getType().equals(FieldMultiList.TYPE)) {
					if (col>0) {
						out.println("</div><div class=\"row field-row\">");
						col = 0;
					}
					localSize=12;	
				}
				out.println("<div class=\"col-lg-"+localSize+"\">" + field.getSearchEditXHTMLCode(ctx) + "</div>");
				col = col + localSize;
				size = size + 1;
				if (col == 12) {
					if (size < fields.size()) {
						out.println("</div><div class=\"row field-row\">");
						col = 0;
					}					
				}
			}
			out.println("</div><div class=\"action-group form-group text-right\"><input type=\"submit\" class=\"btn btn-primary\" name=\"filter\" value=\"" + i18nAccess.getViewText("global.search") + "\" /></div>");
			out.println("</div></form></div>");
		} else if (ctx.isAsPreviewMode()) {
			out.println("[no - form]");
		}

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		List<IFieldContainer> containers = service.getFieldContainers(ctx, content.getNavigation(ctx), getSelectedType());

		if (!StringHelper.isEmpty(getSelectedField())) {
			Collections.sort(containers, new SortContainer(ctx, getSelectedField()));
		}

		Map<String, Field> fieldsSearch = new HashMap<String, Field>();

		boolean isFilter = getStyle().equals(STYLE_ALL) || getStyle().equals(HIDE_FORM) || getStyle().equals(CURRENT_USER_ONLY) || getStyle().equals(HIDE_FORM_USER_ONLY);
		
		if (componentManager) {
			isFilter = false;
		}
		

		for (Field field : getSearchField(ctx)) {
			fieldsSearch.put(field.getName(), field);
			if (field.getValue() != null && field.getValue().trim().length() > 0) {
				isFilter = true;
			}
		}

		if (isFilter) {
			// out.println("<ul class=\"filter-list\">");
			ctx.getRequest().setAttribute("inList", true);
			Collection<IFieldContainer> toDisplay = new LinkedList<IFieldContainer>();
			boolean onlyUser = CURRENT_USER_ONLY.equals(getStyle()) || HIDE_FORM_USER_ONLY.equals(getStyle());
			for (IFieldContainer container : containers) {
				if (container.isRealContent(ctx)) {					
					List<Field> fields = container.getFields(ctx);
					if (!onlyUser || container.getAuthors().equals(ctx.getCurrentUserId()) || ctx.isUserWebSiteManager()) {
						boolean display = true;
						for (Field field : fields) {
							Field searchField = fieldsSearch.get(field.getName());
							if (searchField != null && searchField.getValue() != null && searchField.getValue().trim().length() > 0) {
								if (!field.search(ctx, searchField.getValue().trim())) {
									display = false;
								}
							}
						}
						if (display) {
							toDisplay.add(container);
						}
					}
				}
			}
			ctx.getRequest().setAttribute("previousSame", false);
			ctx.getRequest().setAttribute("nextSame", true);
			int i = 0;
			for (IFieldContainer container : toDisplay) {
				if (i++ == toDisplay.size() - 1) {
					ctx.getRequest().setAttribute("nextSame", false);
				}
				// out.println("<li class=\"dynamic-component\">");
				out.println(container.getPrefixViewXHTMLCode(ctx));
				out.println(container.getViewListXHTMLCode(ctx));
				out.println(container.getSuffixViewXHTMLCode(ctx));
				// out.println("</li>");
				ctx.getRequest().setAttribute("previousSame", true);
			}
			ctx.getRequest().removeAttribute("inList");
			// out.println("</ul>");
			if (toDisplay.size() == 0) {
				out.println("<div class=\"alert alert-warning\" role=\"alert\">" + i18nAccess.getViewText("global.no-result") + "</div>");
			}
		}
		out.close();
		return new String(outStream.toByteArray());

	}

	private String getSelectedType() {
		return properties.getProperty("type");
	}
	
	private String getParentPage() {
		return properties.getProperty("page");
	}

	private String getSelectedField() {
		return properties.getProperty("field");
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
			if (comp != null) {
				for (Field field : comp.getFields(ctx)) {
					if (field.isSearch()) {
						field.setId(getId());
						fields.add(field);
					}
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
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	public List<String> getFields(ContentContext ctx) throws Exception {
		List<String> outList = new LinkedList<String>();
		outList.add("type");
		outList.add("field");
		return outList;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	public static String performFilter(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		DynamicComponentFilter comp = (DynamicComponentFilter) ComponentHelper.getComponentFromRequest(ctx);
		for (Field field : comp.getSearchField(ctx)) {
			List<String> values = rs.getParameterListValues(field.getInputName(), null);
			if (values != null && values.size() > 0) {
				field.setValues(values);
			} else {
				field.setValue(rs.getParameter(field.getInputName(), ""));
			}
		}
		return null;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String msg = super.performEdit(ctx);
		for (Field field : getSearchField(ctx)) {
			setDefaultValue(field, requestService.getParameter(field.getInputName(), null));
		}
		storeProperties();
		return msg;
	}
	
	@Override
	public String getFontAwesome() {
		return "search";
	}

}
