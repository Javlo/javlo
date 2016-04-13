package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.IFieldContainer;
import org.javlo.fields.SortContainer;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.DynamicComponentService;

public class DynamicComponentList extends AbstractPropertiesComponent {

	public static final String TYPE = "dynamic-component-list";

	private static final String CONTAINS = "contains";

	private static final String EQUALS = "equals";

	private static final String MATCH = "match";

	private static final String START = "start";

	static final List<String> filtersType = Arrays.asList(new String[] { EQUALS, CONTAINS, START, MATCH });

	private static final String FILTER_SUFFIX = "-filter";
	private static final String FILTER_TYPE_SUFFIX = "-filter-type";
	
	private Boolean realContent = null;

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getStyleTitle(ContentContext ctx) {
		return "sort on";
	}
	
	@Override
	public String[] getStyleList(ContentContext ctx) {		
		IFieldContainer fieldContainer;
		try {
			fieldContainer = getFieldContainer(ctx);
			if (fieldContainer != null) {
				List<Field> fields = fieldContainer.getFields(ctx);
				String[] outFields = new String[fields.size()+1];
				outFields[0] = "";				
				int i=1;
				for (Field field : fields) {	
					outFields[i] = field.getName();
					i++;
				}
				return outFields;
			} else {
				return super.getStyleList(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
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

		String childrenLabel = i18nAccess.getText("component.filter.children", "search only on children pages.");
		out.println("<div class=\"checkbox\">");
		String checked = "";
		if (isOnlyChildren()) {
			checked = " checked=\"checked\"";
		}
		out.println("<label><input name=\"" + createKeyWithField("children") + "\" type=\"checkbox\"" + checked + " />");
		out.println(childrenLabel + "</label>");
		out.println("</div>");

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer != null) {
			out.println("<fieldset>");
			out.println("<legend>" + i18nAccess.getText("global.filter") + "</legend>");
			List<Field> fields = fieldContainer.getFields(ctx);
			for (Field field : fields) {
				out.println("<div class=\"line\">");
				out.println("<label class=\"large\" for=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\">" + field.getName() + "</label>");
				out.println(XHTMLHelper.getInputOneSelect(createKeyWithField(field.getName() + FILTER_TYPE_SUFFIX), filtersType, properties.getProperty(field.getName() + FILTER_TYPE_SUFFIX)));
				out.println("<input type=\"text\" id=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\" name=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\" value=\"" + properties.getProperty(field.getName() + FILTER_SUFFIX, "") + "\" />");
				out.println("</div>");
			}
		}
		out.println("</fieldset>");

		out.close();
		return new String(outStream.toByteArray());
	}

	boolean fieldMatch(ContentContext ctx, String name, String value) {
		String filterType = properties.getProperty(name + FILTER_TYPE_SUFFIX, CONTAINS);
		String filter = properties.getProperty(name + FILTER_SUFFIX, "");
		
		if (value == null) {
			return filter == null || filter.trim().length() == 0;
		}

		if (ctx.getRequest().getParameter(name) != null) {
			filter = ctx.getRequest().getParameter(name);
		}

		if (filter == null || filter.trim().length() == 0) {
			return true;
		} else {
			if (filterType.equals(EQUALS)) {
				return value.equals(filter);
			} else if (filterType.equals(CONTAINS)) {
				return value.contains(filter);
			} else if (filterType.equals(START)) {
				return value.toLowerCase().startsWith(filter.toLowerCase());
			} else if (filterType.equals(MATCH)) {
				Pattern p = Pattern.compile(filter);
				return p.matcher(value).matches();
			}
			return false;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		
		prepareView(ctx);
		
		realContent = false;

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer == null) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		MenuElement rootPage = content.getNavigation(ctx);
		if (isOnlyChildren()) {
			rootPage = ctx.getCurrentPage();
		}
		List<IFieldContainer> containers = service.getFieldContainers(ctx, rootPage, getSelectedType());
		List<IFieldContainer> visibleContainers = new LinkedList<IFieldContainer>();
		
		for (IFieldContainer container : containers) {
			if (container.isRealContent(ctx)) {
				boolean display = true;
				List<Field> fields = container.getFields(ctx);
				for (Field field : fields) {
					if (!fieldMatch(ctx, field.getName(), field.getValue(new Locale(ctx.getRequestContentLanguage())))) {
						display = false;
					}
				}
				if (display) {
					realContent = true;
					visibleContainers.add(container);
				}
			}
		}
		int index = 0;
		ctx.getRequest().setAttribute("componentSize", visibleContainers.size());
		ctx.getRequest().setAttribute("first", true);
		ctx.getRequest().setAttribute("last", false);
		
		if(!StringHelper.isEmpty(getStyle())) {
			Collections.sort(visibleContainers, new SortContainer(ctx, getStyle()));
		}
		
		for (IFieldContainer container : visibleContainers) {
			index++;
			ctx.getRequest().setAttribute("componentIndex", index);
			if (index == visibleContainers.size()) {
				ctx.getRequest().setAttribute("last", true);
			}
			out.println(container.getViewListXHTMLCode(ctx));
			ctx.getRequest().setAttribute("first", false);			
		}

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

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		List<String> outList = new LinkedList<String>();
		outList.add("type");
		outList.add("children");
		if (getSelectedType() != null) {
			IFieldContainer container = getFieldContainer(ctx);
			if (container != null) {
				List<Field> fields = container.getFields(ctx);
				for (Field field : fields) {
					outList.add(field.getName() + FILTER_SUFFIX);
					outList.add(field.getName() + FILTER_TYPE_SUFFIX);
				}
			}
		}
		return outList;
	}

	@Override
	public String getHeader() {
		return getType();
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		if (realContent == null) {
			try {
				getViewXHTMLCode(ctx);
			} catch (Exception e) {
				realContent = false;
				e.printStackTrace();
			}
		}
		return realContent;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		try {
			IFieldContainer containers = getFieldContainer(ctx);
			if (containers == null) {
				return false;
			}
			List<Field> fields = containers.getFields(ctx);
			for (Field field : fields) {
				if (ctx.getRequest().getParameter(field.getName()) != null) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	private boolean isOnlyChildren() {
		return StringHelper.isTrue(properties.getProperty("children"));
	}

}
