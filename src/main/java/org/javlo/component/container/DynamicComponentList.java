package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

	private static final String SUP = "superior";

	private static final String INF = "inferior";

	static final List<String> filtersType = Arrays.asList(new String[] { EQUALS, CONTAINS, START, MATCH, SUP, INF });

	private static final String FILTER_SUFFIX = "-filter";
	private static final String FILTER_TYPE_SUFFIX = "-filter-type";

	private static final String MAX_SIZE_KEY = "__maxsize";

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
				String[] outFields = new String[fields.size()*2 + 1];
				outFields[0] = "";
				int i = 1;
				for (Field field : fields) {
					outFields[i] = "> "+field.getName();
					i++;
				}
				for (Field field : fields) {
					outFields[i] = "< "+field.getName();
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

		out.println("<div class=\"form-group\">" + XHTMLHelper.getInputOneSelect(createKeyWithField("type"), container, getSelectedType(), "form-control") + "</div>");

		out.println("<div class=\"row\">");
		out.println("<div class=\"col-sm-1\"><label for=\"" + getMaxSizeInputName() + "\">max</label></div>");
		out.println("<div class=\"col-sm-3\"><div class=\"form-group\"><input class=\"form-control\" type=\"text\" id=\"" + getMaxSizeInputName() + "\" name=\"" + getMaxSizeInputName() + "\" value=\"" + (getMaxSize()>0?getMaxSize():"") + "\" /></div></div>");
		out.println("</div>");

		String childrenLabel = i18nAccess.getText("component.filter.children", "search only on children pages.");
		out.println("<div class=\"checkbox\">");
		String checked = "";
		if (isOnlyChildren()) {
			checked = " checked=\"checked\"";
		}
		out.println("<label><input class=\"form-group\" name=\"" + createKeyWithField("children") + "\" type=\"checkbox\"" + checked + " />");
		out.println(childrenLabel + "</label>");
		out.println("</div>");

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer != null) {
			out.println("<fieldset>");
			out.println("<legend>" + i18nAccess.getText("global.filter") + "</legend>");
			List<Field> fields = fieldContainer.getFields(ctx);
			for (Field field : fields) {
				out.println("<div class=\"row\">");
				out.println("<div class=\"col-sm-2\"><label class=\"large\" for=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\">" + field.getName() + "</label></div>");
				out.println("<div class=\"col-sm-2\"><div class=\"form-group\">" + XHTMLHelper.getInputOneSelect(createKeyWithField(field.getName() + FILTER_TYPE_SUFFIX), filtersType, properties.getProperty(field.getName() + FILTER_TYPE_SUFFIX), "form-control") + "</div></div>");
				out.println("<div class=\"col-sm-8\"><div class=\"form-group\"><input class=\"form-control\" type=\"text\" id=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\" name=\"" + createKeyWithField(field.getName() + FILTER_SUFFIX) + "\" value=\"" + properties.getProperty(field.getName() + FILTER_SUFFIX, "") + "\" /></div></div>");
				out.println("</div>");
			}
		}
		out.println("</fieldset>");

		out.close();
		return new String(outStream.toByteArray());
	}

	protected String getMaxSizeInputName() {
		return createKeyWithField(MAX_SIZE_KEY);
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
			} else if (filterType.equals(SUP)) {
				if (StringHelper.isDigit(value) && StringHelper.isDigit(filter)) {
					return Integer.parseInt(value) > Integer.parseInt(filter);
				} else {
					Date valueDate = StringHelper.smartParseDate(value);
					Date filterDate = StringHelper.smartParseDate(filter);
					if (valueDate != null && filterDate != null) {
						return valueDate.getTime() > filterDate.getTime();
					} else {
						return true;
					}
				}
			} else if (filterType.equals(INF)) {
				if (StringHelper.isDigit(value) && StringHelper.isDigit(filter)) {
					return Integer.parseInt(value) < Integer.parseInt(filter);
				} else {
					Date valueDate = StringHelper.smartParseDate(value);
					Date filterDate = StringHelper.smartParseDate(filter);
					if (valueDate != null && filterDate != null) {
						return valueDate.getTime() < filterDate.getTime();
					} else {
						return true;
					}
				}
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

		int count = 0;
		int maxSize = getMaxSize();
		for (IFieldContainer container : containers) {
			if (container.isRealContent(ctx)) {
				boolean display = true;
				List<Field> fields = container.getFields(ctx);
				for (Field field : fields) {
					if (!fieldMatch(ctx, field.getName(), field.getReference(ctx).getValue(new Locale(ctx.getRequestContentLanguage())))) {
						display = false;
					}
				}
				if (display) {
					if (maxSize <= 0 || count < maxSize) {
						realContent = true;
						visibleContainers.add(container);
						count++;
					}
				}

			}
		}
		int index = 0;
		ctx.getRequest().setAttribute("componentSize", visibleContainers.size());
		ctx.getRequest().setAttribute("first", true);
		ctx.getRequest().setAttribute("last", false);

		if (!StringHelper.isEmpty(getStyle())) {
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

	private int getMaxSize() {
		String maxSizeStr = StringHelper.neverEmpty(properties.getProperty(MAX_SIZE_KEY), "0");
		if (StringHelper.isDigit(maxSizeStr)) {
			return Integer.parseInt(maxSizeStr);
		}
		return 0;
	}

	IFieldContainer getFieldContainer(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		MenuElement rootPage = content.getNavigation(ctx);
		if (isOnlyChildren()) {
			rootPage = ctx.getCurrentPage();
		}

		List<IFieldContainer> containers = service.getFieldContainers(ctx, rootPage, getSelectedType());
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
		outList.add(MAX_SIZE_KEY);
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
